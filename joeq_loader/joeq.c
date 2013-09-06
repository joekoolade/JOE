// joeq.c : Defines the entry point for the console application.
//

#include "StdAfx.h"

int joeq_argc;
char **joeq_argv;

extern void __stdcall entry(void);
extern void installSignalHandler(void);
extern void initSemaphoreLock(void);

extern void* joeq_code_startaddress;
extern void* joeq_code_endaddress;
extern void* joeq_data_startaddress;
extern void* joeq_data_endaddress;

extern int trace_exceptions;
extern int trap_on_exceptions;

int main(int argc, char* argv[])
{
    // clear umask
    _umask(0);

    // check for C loader flags.
    while (argc > 1) {
        if (!strcmp(argv[1], "--trap_on_exceptions")) {
            trap_on_exceptions = 1;
            trace_exceptions = 1;
            ++argv; --argc;
        } else if (!strcmp(argv[1], "--trace_exceptions")) {
            trace_exceptions = 1;
            ++argv; --argc;
        } else
            break;
    }

    // initialize argc and argv
    joeq_argc = argc-1;
    joeq_argv = argv+1;

#if defined(WIN32)
    // install hardware exception handler.
    // NOTE that this must be on the stack and have a lower address than any previous handler.
    // Therefore, it must be done in main().
    {
        HandlerRegistrationRecord er, *erp = &er;
        er.previous = NULL;
        er.handler = hardwareExceptionHandler;
#if defined(_MSC_VER)
        _asm mov eax,[erp]
        _asm mov fs:[0],eax // point first word of thread control block to exception handler registration chain
#else
        __asm__ __volatile__ (
            "movl %0, %%eax\n\t"\
            "movl %%eax, %%fs:0\n\t"
            :
            :"r"(erp)
            :"%eax"
            );
#endif
    }
#endif

    installSignalHandler();
    initSemaphoreLock();

    printf("Code segment at 0x%p-0x%p, Data segment at 0x%p-0x%p\n",
        &joeq_code_startaddress, &joeq_code_endaddress,
        &joeq_data_startaddress, &joeq_data_endaddress);
    printf("branching to entrypoint at location 0x%p\n", entry);
    fflush(stdout);

#if defined(_MSC_VER)
    __asm {
        // set it up so FP = 0, so we know the stack top.
        push EBP
        xor EBP, EBP

        // jump into joeq
        call entry

        // restore FP, so chkesp doesn't complain
        pop EBP
    }
#elif defined(WIN32)
    __asm__ __volatile__ (
        "pushl %%ebp\n\t"\
        "xor %%ebp, %%ebp\n\t"\
        "call _entry@0\n\t"\
        "popl %%ebp\n\t"
            :
            :
            :"%eax","%edx","%ecx","%ebx","%edi","%esi"
            );
#else
    __asm__ __volatile__ (
        "pushl %%ebp\n\t"\
        "xor %%ebp, %%ebp\n\t"\
        "call entry\n\t"\
        "popl %%ebp\n\t"
            :
            :
            :"%eax","%edx","%ecx","%ebx","%edi","%esi"
            );
#endif

    // only reachable if jq.boot returns normally.
    return 0;
}
