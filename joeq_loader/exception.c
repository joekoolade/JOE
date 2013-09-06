
#include "StdAfx.h"

int trace_exceptions = 0;
int trap_on_exceptions = 0;

#if defined(WIN32)

void dump_context_record(CONTEXT *c)
{
	printf("Register state at hardware exception:\n");
	printf("EAX: %08x  EDX: %08x  ECX: %08x  EBX: %08x\n", c->Eax, c->Edx, c->Ecx, c->Ebx);
	printf("EDI: %08x  ESI: %08x  EBP: %08x  ESP: %08x\n", c->Edi, c->Esi, c->Ebp, c->Esp);
}

EXCEPTION_DISPOSITION hardwareExceptionHandler(EXCEPTION_RECORD *exceptionRecord,
                                               void *establisherFrame,
                                               CONTEXT *contextRecord,
                                               void *dispatcherContext)
{
    DWORD_PTR eip = contextRecord->Eip;
    int ex_code = exceptionRecord->ExceptionCode;
    int java_ex_code;
    DWORD* esp;
    switch (ex_code) {
        case EXCEPTION_ACCESS_VIOLATION: // null pointer exception
            // int 5 seems to create an access violation, for some reason.
            if (*((int*)(eip-2)) == 0x05cd0272) java_ex_code = 1;
            else java_ex_code = 0;
            break;
        case EXCEPTION_ARRAY_BOUNDS_EXCEEDED: // array bounds exception
            java_ex_code = 1;
            break;
        case EXCEPTION_INT_DIVIDE_BY_ZERO: // divide by zero exception
            java_ex_code = 2;
            break;
        case EXCEPTION_STACK_OVERFLOW:
            java_ex_code = 3;
            break;
        default:
            java_ex_code = -1;
            break;
    }

    //dump_context_record(contextRecord);

    // push arguments
    esp = (DWORD*)contextRecord->Esp;
    if (trace_exceptions) {
        printf("Hardware exception occurred at eip=0x%p, code=%d, java_code=%d\n", (void*)eip, ex_code, java_ex_code);
    }
    
    *--esp = (DWORD)java_ex_code;
    *--esp = (DWORD)eip;
    contextRecord->Esp = (DWORD)esp;
    if (trap_on_exceptions) {
        // resume execution at internal debugger
        contextRecord->Eip = (DWORD)debug_trap_handler;
    } else {
        // resume execution at java trap handler
        contextRecord->Eip = (DWORD)trap_handler;
    }
    return ExceptionContinueExecution;
}

BOOL WINAPI windows_break_handler(DWORD dwCtrlType)
{
    if (dwCtrlType == CTRL_BREAK_EVENT) {
        ctrl_break_handler();
        return 1;
    }
    return 0;
}

void installSignalHandler(void)
{
    // exception handler is done in main()

    // install ctrl-break handler
    SetConsoleCtrlHandler(windows_break_handler, TRUE);
}

#elif defined(linux)

void hardwareExceptionHandler(int signo, siginfo_t *si, void *context)
{
    struct sigcontext *sc;
    void *eip;
    int ex_code;
    int java_ex_code;
    int *esp;

    // magic to get sigcontext!
    sc = (struct sigcontext *)((char *)context+5*4);
    eip = (void *)sc->eip;
    ex_code = signo;
    switch (ex_code) {
  case SIGSEGV: // null pointer exception
      // int 5 seems to create an access violation, for some reason.
      // on linux, eip points AFTER the instruction, rather than before.
      if (*((int*)(((int)eip)-4)) == 0x05cd0272) java_ex_code = 1;
      else java_ex_code = 0;
      break;
  case SIGFPE: // divide by zero exception
      java_ex_code = 2;
      break;
  case SIGTRAP: // stack overflow
      java_ex_code = 3;
      break;
  default:
      java_ex_code = -1;
      break;
    }

    // push arguments
    esp = (int *)sc->esp;
    *--esp = java_ex_code;
    *--esp = (int)eip;
    sc->esp = (int)esp;

    if (trap_on_exceptions) {
        // resume execution at internal debugger
        sc->eip = (int)debug_trap_handler;
    } else {
        // resume execution at java trap handler
        sc->eip = (int)trap_handler;
    }
    return;
}

void copyFromSigcontext(CONTEXT* c, struct sigcontext* sc)
{
    c->Eax = sc->eax;
    c->Ebx = sc->ebx;
    c->Ecx = sc->ecx;
    c->Edx = sc->edx;
    c->Esi = sc->esi;
    c->Edi = sc->edi;
    c->Ebp = sc->ebp;
    c->Esp = sc->esp;
    c->Eip = sc->eip;
    c->SegCs = sc->cs;
    c->SegSs = sc->ss;
    c->EFlags = sc->eflags;
    memcpy(&c->FloatSave, sc->fpstate, sizeof(FLOATING_SAVE_AREA));
    //sc->fpstate->magic = 0xffff; // regular FPU data only
}

void softwareSignalHandler(int signo, siginfo_t *si, void *context)
{
    Thread* java_thread;
    NativeThread* native_thread;
    struct sigcontext *sc;
    int *esp;
    //printf("PID %d received tick.\n", getpid());

    // get current Java thread
    __asm ("movl %%fs:20, %0":"=r"(java_thread));
    // check if thread switch is ok
    if (java_thread->thread_switch_enabled != 0) {
        //printf("Java thread 0x%08x: thread switch not enabled (%d)\n", java_thread, java_thread->thread_switch_enabled);
        return;
    }

    native_thread = java_thread->native_thread;

    // magic to get sigcontext!
    sc = (struct sigcontext *)((char *)context+5*4);

    //printf("Java thread 0x%08x: thread switch enabled (%d) eip=0x%08x esp=0x%08x\n", java_thread, java_thread->thread_switch_enabled, sc->eip, sc->esp);

    // simulate a call to the threadSwitch method.
    esp = (int *)sc->esp;
    *--esp = (int)native_thread;
    *--esp = (int)sc->eip;
    sc->esp = (int)esp;
    sc->eip = (int)threadSwitch;

    copyFromSigcontext(java_thread->registers, sc);

    // disable thread switch.
    ++java_thread->thread_switch_enabled;

    //printf("Java thread 0x%08x: calling threadSwitch...\n");

    return;
}

void installSignalHandler(void)
{
    // install a stack for the hardware trap handler
    stack_t stack;
    struct sigaction action;
    memset(&stack, 0, sizeof stack);
    stack.ss_sp = malloc(SIGSTKSZ);
    stack.ss_size = SIGSTKSZ;
    //printf("Installing hardware trap signal handler stack.\n");
    if (sigaltstack(&stack, 0)) {
        // TODO: error.
        // Note: use of "errno" seems broken on RedHat 9.
        //printf("Error installing hardware trap signal handler stack %d.\n", errno);
        printf("Error installing hardware trap signal handler stack.\n");
        return;
    }

    // install hardware trap signal handler
    memset(&action, 0, sizeof action);
    action.sa_sigaction = &hardwareExceptionHandler;

    // mask all signals from reaching the signal handler while the signal
    // handler is running
    //printf("Filling hardware trap signal handler set.\n");
    if (sigfillset(&(action.sa_mask))) {
        // TODO: error.
        // Note: use of "errno" seems broken on RedHat 9.
        //printf("Error filling hardware trap signal handler set %d.\n", errno);
        printf("Error filling hardware trap signal handler set.\n");
        return;
    }
#if 0
    // ignore the SIGSTOP/SIGCONT signals; they are used to stop and restart
    // native threads.
    if (sigdelset(&(action.sa_mask), SIGSTOP)) {
        // TODO: error.
        // Note: use of "errno" seems broken on RedHat 9.
        //printf("Error deleting from hardware trap signal handler set %d.\n", errno);
        printf("Error deleting from hardware trap signal handler set.\n");
        return;
    }
    if (sigdelset(&(action.sa_mask), SIGCONT)) {
        // TODO: error.
        // Note: use of "errno" seems broken on RedHat 9.
        //printf("Error deleting from hardware trap signal handler set %d.\n", errno);
        printf("Error deleting from hardware trap signal handler set.\n");
        return;
    }
#endif
    action.sa_flags = SA_SIGINFO | SA_ONSTACK | SA_RESTART;
    //printf("Setting hardware trap signal handler.\n");
    if (sigaction(SIGSEGV, &action, 0)) {
        // TODO: error.
        // Note: use of "errno" seems broken on RedHat 9.
        //printf("Error setting hardware trap signal handler %d.\n", errno);
        printf("Error setting hardware trap signal handler.\n");
        return;
    }
    //printf("Setting hardware trap signal handler.\n");
    if (sigaction(SIGFPE, &action, 0)) {
        // TODO: error.
        // Note: use of "errno" seems broken on RedHat 9.
        //printf("Error setting hardware trap signal handler %d.\n", errno);
        printf("Error setting hardware trap signal handler.\n");
        return;
    }
    //printf("Setting hardware trap signal handler.\n");
    if (sigaction(SIGTRAP, &action, 0)) {
        // TODO: error.
        // Note: use of "errno" seems broken on RedHat 9.
        //printf("Error setting hardware trap signal handler %d.\n", errno);
        printf("Error setting hardware trap signal handler.\n");
        return;
    }

    // install software signal handler
    action.sa_sigaction = &softwareSignalHandler;
    //printf("Setting software signal handler.\n");
    if (sigaction(SIGVTALRM, &action, 0)) {
        // TODO: error.
        // Note: use of "errno" seems broken on RedHat 9.
        //printf("Error setting software signal handler %d.\n", errno);
        printf("Error setting software signal handler.\n");
        return;
    }

}

#elif defined(__CYGWIN32__)
void installSignalHandler(void)
{
    // TODO.
}
#endif
