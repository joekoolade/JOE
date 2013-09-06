// native.c : Native method implementations
//

#include "StdAfx.h"

#define ARRAY_LENGTH_OFFSET -12

void __stdcall debugwwrite(const unsigned short* s, int length)
{
#if defined(_MSC_VER)
    unsigned short* temp = (unsigned short*)malloc((length+1)*sizeof(unsigned short));
    memcpy(temp, s, length*sizeof(unsigned short));
    temp[length] = 0;
    fputws(temp, stdout);
    free(temp);
#else
    // TODO: actually write wide characters
    while (--length >= 0) {
        unsigned short c = *s;
        putchar((char)c);
        ++s;
    }
#endif
    fflush(stdout);
}

void __stdcall debugwwriteln(const unsigned short* s, int length)
{
    debugwwrite(s, length);
    putchar('\n');
}

void __stdcall debugwrite(const char* s, int length)
{
    while (--length >= 0) {
        char c = *s;
        putchar((char)c);
        ++s;
    }
    fflush(stdout);
}

void __stdcall debugwriteln(const char* s, int length)
{
    debugwrite(s, length);
    putchar('\n');
}

void* __stdcall syscalloc(const int size)
{
    void* p = calloc(1, size);
    //printf("Allocated %d bytes at 0x%08x\n", size, p);
    return p;
}

void __stdcall sysfree(void* p)
{
    free(p);
}

#if defined(WIN32)
HINSTANCE __stdcall open_library(char* name)
{
    return LoadLibrary(name);
}

BOOL __stdcall close_library(HINSTANCE lib)
{
    return FreeLibrary(lib);
}

FARPROC __stdcall get_proc_address(HINSTANCE lib, char* name)
{
    return GetProcAddress(lib, name);
}
#else
#include <dlfcn.h>
void* __stdcall open_library(char* name)
{
    return dlopen(name, RTLD_LAZY);
}

int __stdcall close_library(void* lib)
{
    return dlclose(lib);
}

void* __stdcall get_proc_address(void* lib, char* name)
{
    return dlsym(lib, name);
}
#endif

void __stdcall mem_set(void* p, const char c, const int size)
{
    memset(p, c, size);
}

void __stdcall die(const int code)
{
    fflush(stdout);
    fflush(stderr);
    exit(code);
}

#if defined(WIN32)
// Windows time functions
__int64 __stdcall filetimeToJavaTime(const FILETIME* fileTime)
{
    LARGE_INTEGER time;
    time.LowPart = fileTime->dwLowDateTime; time.HighPart = fileTime->dwHighDateTime;
    return (time.QuadPart / 10000L) - 11644473600000LL;
}

void __stdcall javaTimeToFiletime(const __int64 javaTime, FILETIME* fileTime)
{
    LARGE_INTEGER time;
    time.QuadPart = (javaTime + 11644473600000LL) * 10000L;
    fileTime->dwLowDateTime = time.LowPart;
    fileTime->dwHighDateTime = time.HighPart;
}

__int64 __stdcall currentTimeMillis(void)
{
    FILETIME fileTime;
    GetSystemTimeAsFileTime(&fileTime);
    return filetimeToJavaTime(&fileTime);
}
#else
// Generic unix time functions.
__int64 __stdcall currentTimeMillis(void)
{
    struct timeb t;
    ftime(&t);
    return ((__int64)(t.time))*1000 + t.millitm;
}
#endif

void __stdcall mem_cpy(void* to, const void* from, const int size)
{
    memcpy(to, from, size);
}

int __stdcall file_open(const char* s, const int mode, const int smode)
{
#if defined(_MSC_VER)
    return _open(s, mode, smode);
#else
    return open(s, mode, smode);
#endif
}
int __stdcall file_stat(const char* s, struct stat *buf)
{
    return stat(s, buf);
}
int __stdcall file_readbytes(const int fd, char* b, const int len)
{
#if defined(_MSC_VER)
    return _read(fd, b, len);
#else
    return read(fd, b, len);
#endif
}
int __stdcall file_writebyte(const int fd, const int b)
{
#if defined(_MSC_VER)
    return _write(fd, &b, 1);
#else
    return write(fd, &b, 1);
#endif
}
int __stdcall file_writebytes(const int fd, const char* b, const int len)
{
#if defined(_MSC_VER)
    return _write(fd, b, len);
#else
    return write(fd, b, len);
#endif
}
int __stdcall file_sync(const int fd)
{
    return _commit(fd);
}
__int64 __stdcall file_seek(const int fd, const __int64 offset, const int origin)
{
#if defined(_MSC_VER)
    return _lseeki64(fd, offset, origin);
#else
    return lseek(fd, offset, origin);
#endif
}
int __stdcall file_close(const int fd)
{
#if defined(_MSC_VER)
    return _close(fd);
#else
    return close(fd);
#endif
}
#if defined(WIN32)
int __stdcall console_available(void)
{
    HANDLE in = GetStdHandle(STD_INPUT_HANDLE);
    unsigned long count;
    if (!GetNumberOfConsoleInputEvents(in, &count)) return -1;
    else return (int)count;
}
#elif defined(linux)
#include <errno.h>
#include <termios.h>

#define MKFLAG(which) \
static int io_tio_set_flag_##which(int fd, int val, int on, int *old) \
{ struct termios tio; \
    if (tcgetattr(fd,&tio)) return -1; \
    if (old) *old=(tio.which & (val)); \
    if (on) tio.which |= (val); \
    else tio.which &= ~(val); \
    if (tcsetattr(fd,TCSADRAIN,&tio)) return -1; \
    return 0; \
} \
static int io_tio_get_flag_##which(int fd, int bit, int *value) \
{ struct termios tio; \
    if (tcgetattr(fd,&tio)) return -1; \
    *value=(tio.which & (bit)); \
    return 0; \
}
MKFLAG(c_lflag)

static int io_charavail(int fd)
{
    fd_set set;
    struct timeval zeit;
    int ret;
    int tty=1;
    int old_ICANON;
    FD_ZERO(&set);
    FD_SET(fd,&set);
    zeit.tv_sec  = 0;
    zeit.tv_usec = 0;
    if (-1==io_tio_set_flag_c_lflag(fd,ICANON,0,&old_ICANON)) {
        if (errno==EINVAL || errno==ENOTTY) 
            tty=0;
        else {
            perror("ICANON");
            return -1;
        }
    }

    while (1) {
        ret=select(fd+1,&set,0,0,&zeit);
        if (ret>=0) break;
        if (errno==EINTR) continue;
        perror("select");
        break;
    }
    if (tty)
        io_tio_set_flag_c_lflag(fd,ICANON,old_ICANON,NULL);
    return ret;
}
#include <sys/ioctl.h>
int __stdcall console_available(void)
{
    //return io_charavail(0);
    int nbytes;
    ioctl(0, FIONREAD, &nbytes);
    return nbytes;
}
#elif defined(__CYGWIN32__)
int __stdcall console_available(void)
{
    // TODO.
    return 0;
}
#else
#error System type not supported.
#endif
int __stdcall main_argc(void)
{
    return joeq_argc;
}
int __stdcall main_argv_length(const int i)
{
    return (int)strlen(joeq_argv[i]);
}
void __stdcall main_argv(const int i, char* buf)
{
    memcpy(buf, joeq_argv[i], strlen(joeq_argv[i])*sizeof(char));
}
#if defined(WIN32)
int __stdcall fs_getdcwd(const int i, char* buf, const int buflen)
{
    return _getdcwd(i, buf, buflen)?1:0;
}
int __stdcall fs_fullpath(char* buf, const char* s, const int buflen)
{
    return _fullpath(buf, s, buflen)?1:0;
}
int __stdcall fs_getfileattributes(const char* s)
{
    return GetFileAttributes(s);
}
char* __stdcall fs_gettruename(char* s)
{
    WIN32_FIND_DATA fd;
    HANDLE h = FindFirstFile(s, &fd);
    if (h == INVALID_HANDLE_VALUE) return NULL;
    FindClose(h);
    // TODO: oops! cFileName field is char[MAX_PATH], so we are returning local variable!
    return fd.cFileName;
}
int __stdcall fs_access(const char* s, const int mode)
{
    return _access(s, mode);
}
__int64 __stdcall fs_getfiletime(const char* s)
{
    FILETIME fileTime;
    HANDLE file = CreateFile(s, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, 0, NULL);
    int res = GetFileTime(file, NULL, NULL, &fileTime);
    CloseHandle(file);
    if (res == 0) return 0;
    return filetimeToJavaTime(&fileTime);
}
__int64 __stdcall fs_stat_size(const char* s)
{
#if defined(_MSC_VER) || defined(__MINGW32__)
    struct _stati64 buf;
#else
    struct stati64 buf;
#endif
    int res = _stati64(s, &buf);
    if (res != 0) return 0;
    return buf.st_size;
}
#elif defined(linux) || defined(__CYGWIN32__)
int __stdcall fs_getdcwd(const int i, char* buf, const int buflen)
{
    // TODO.
    return 0;
}
int __stdcall fs_fullpath(char* buf, const char* s, const int buflen)
{
    // TODO.
    return 0;
}
int __stdcall fs_getfileattributes(const char* s)
{
    // TODO.
    return 0;
}
char* __stdcall fs_gettruename(char* s)
{
    // TODO.
    return s;
}
int __stdcall fs_access(const char* s, const int mode)
{
    return access(s, mode);
}
__int64 __stdcall fs_getfiletime(const char* s)
{
    struct stat buf;
    int res = stat(s, &buf);
    if (res != 0) return 0;
    return ((__int64)buf.st_mtime) * 1000L;
}
__int64 __stdcall fs_stat_size(const char* s)
{
    struct stat buf;
    int res = stat(s, &buf);
    if (res != 0) return 0;
    return buf.st_size;
}
#else
#error System type not supported.
#endif
int __stdcall fs_remove(const char* s)
{
    return remove(s);
}
#if defined(WIN32)
DIR * __stdcall fs_opendir(const char* s)
{
    int file_attr;
    DIR *dir = (DIR *)malloc(sizeof(DIR));
    if (!dir) return NULL;

    if (s[0] == '\\' && s[1] == 0) {
        char dirname2[4];
        dirname2[0] = _getdrive()+'A'-1;
        dirname2[1] = ':';
        dirname2[1] = '\\';
        dirname2[1] = '\0';
        s = dirname2;
    }

    dir->path = (char *)malloc(strlen(s)+5);
    if (!dir->path) {
        free(dir); return NULL;
    }
    strcpy(dir->path, s);

    file_attr = GetFileAttributes(dir->path);
    if ((file_attr == -1) || ((file_attr & FILE_ATTRIBUTE_DIRECTORY) == 0)) {
        free(dir->path); free(dir); return NULL;
    }

    if (dir->path[1] == ':' && (dir->path[2] == 0 || (dir->path[2] == '\\' && dir->path[3] == 0))) {
        strcat(dir->path, "*.*");
    } else {
        strcat(dir->path, "\\*.*");
    }

    dir->handle = FindFirstFile(dir->path, &dir->find_data);
    if (dir->handle == INVALID_HANDLE_VALUE) {
        if (GetLastError() != ERROR_FILE_NOT_FOUND) {
            free(dir->path); free(dir); return NULL;
        }
    }
    return dir;
}
struct dirent * __stdcall fs_readdir(DIR *dir)
{
    if (dir->handle == INVALID_HANDLE_VALUE) return NULL;
    strcpy(dir->dirent.d_name, dir->find_data.cFileName);
    if (!FindNextFile(dir->handle, &dir->find_data)) {
        if (GetLastError() == ERROR_INVALID_HANDLE) return NULL;
        FindClose(dir->handle);
        dir->handle = INVALID_HANDLE_VALUE;
    }
    return &dir->dirent;
}
int __stdcall fs_closedir(DIR *dir)
{
    if (dir->handle != INVALID_HANDLE_VALUE) {
        if (!FindClose(dir->handle)) return -1;
        dir->handle = INVALID_HANDLE_VALUE;
    }
    free(dir->path); free(dir);
    return 0;
}
int __stdcall fs_setfiletime(const char* s, const __int64 time)
{
    FILETIME fileTime;
    HANDLE file;
    int res;
    javaTimeToFiletime(time, &fileTime);
    file = CreateFile(s, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, 0, NULL);
    res = SetFileTime(file, NULL, NULL, &fileTime);
    CloseHandle(file);
    return res;
}
int __stdcall fs_getlogicaldrives(void)
{
    return GetLogicalDrives();
}
#elif defined(linux) || defined(__CYGWIN32__)
DIR * __stdcall fs_opendir(const char* s)
{
    return opendir(s);
}
struct dirent * __stdcall fs_readdir(DIR *dir)
{
    return readdir(dir);
}
int __stdcall fs_closedir(DIR *dir)
{
    return closedir(dir);
}
int __stdcall fs_setfiletime(const char* s, const __int64 time)
{
    // TODO.
    return 0;
}
int __stdcall fs_getlogicaldrives(void)
{
    // TODO
    return 0;
}
#else
#error System type not supported.
#endif
int __stdcall fs_mkdir(const char* s)
{
    return _mkdir(s);
}
int __stdcall fs_rename(const char* s, const char* s1)
{
    return rename(s, s1);
}
int __stdcall fs_chmod(const char* s, const int mode)
{
#if defined(_MSC_VER)
    return _chmod(s, mode);
#else
    return chmod(s, mode);
#endif
}
void __stdcall yield(void)
{
    Sleep(0);
}
void __stdcall msleep(int ms)
{
    Sleep(ms);
}
#if defined(WIN32)
HANDLE __stdcall get_current_thread_handle(void)
{
    HANDLE currentProcess = GetCurrentProcess();
    HANDLE targetHandle;
    DuplicateHandle(currentProcess, GetCurrentThread(), currentProcess, &targetHandle, 0, TRUE, DUPLICATE_SAME_ACCESS);
    return targetHandle;
}
HANDLE __stdcall create_thread(LPTHREAD_START_ROUTINE start, LPVOID arg)
{
    DWORD tid;
    return CreateThread(NULL, 0, start, arg, CREATE_SUSPENDED, &tid);
}
HANDLE __stdcall init_thread(void)
{
    // nothing to do here.
    return get_current_thread_handle();
}
int __stdcall set_thread_priority(const HANDLE handle, const int level)
{
    return SetThreadPriority(handle, level);
}
int __stdcall resume_thread(const HANDLE handle)
{
    return ResumeThread(handle);
}
int __stdcall suspend_thread(const HANDLE handle)
{
    return SuspendThread(handle);
}

void* __stdcall allocate_stack(const int size)
{
    LPVOID lpvAddr;
    DWORD dwPageSize;
    DWORD dwFinalSize;
    DWORD oldProtect;
    SYSTEM_INFO sSysInfo;
    GetSystemInfo(&sSysInfo);     // populate the system information structure

    dwPageSize = sSysInfo.dwPageSize;
    dwFinalSize = ((size / dwPageSize) + 2) * dwPageSize;
    lpvAddr = VirtualAlloc(NULL, dwFinalSize, MEM_RESERVE | MEM_COMMIT, PAGE_READWRITE);
    if (lpvAddr == NULL) {
        fprintf(stderr, "PANIC! Cannot allocate stack!\n");
        lpvAddr = calloc(sizeof(char), size);
        if (lpvAddr != NULL) {
            lpvAddr = (void*)((DWORD_PTR)lpvAddr+size);
            fprintf(stderr, "Stack allocated from normal memory (no stack overflow checking)\n");
        }
        return lpvAddr;
    }
    if (!VirtualProtect(lpvAddr, dwPageSize, PAGE_GUARD | PAGE_READWRITE, &oldProtect)) {
        fprintf(stderr, "PANIC! Cannot create stack guard page!\n");
    }
    //printf("Allocated stack of size %d starting at 0x%08x\n", size, (int)lpvAddr+dwFinalSize);
    return (void*)((DWORD_PTR)lpvAddr+dwFinalSize);
}

int __stdcall get_thread_context(HANDLE t, CONTEXT* c)
{
    int result = GetThreadContext(t, c);
    if (!result) {
        DWORD err = GetLastError();
        fprintf(stderr, "PANIC! Cannot get context of thread %p: %lu.\n", t, err);
    }
    return result;
}

int __stdcall set_thread_context(HANDLE t, CONTEXT* c)
{
    int result = SetThreadContext(t, c);
    if (!result) {
        DWORD err = GetLastError();
        fprintf(stderr, "PANIC! Cannot set context of thread %p: %lu.\n", t, err);
    }
    return result;
}

CRITICAL_SECTION semaphore_init;
CRITICAL_SECTION* p_semaphore_init;

void initSemaphoreLock(void)
{
    InitializeCriticalSection(&semaphore_init);
    p_semaphore_init = &semaphore_init;
}

HANDLE __stdcall init_semaphore(void)
{
    HANDLE sema;
    EnterCriticalSection(p_semaphore_init);
    sema = CreateSemaphore(0, 0, 1, 0);
    LeaveCriticalSection(p_semaphore_init);
    return sema;
}

int __stdcall wait_for_single_object(HANDLE handle, int time)
{
    return WaitForSingleObject(handle, time);
}

int __stdcall release_semaphore(HANDLE semaphore, int a)
{
    return ReleaseSemaphore(semaphore, a, NULL);
}

void __stdcall timer_tick(LPVOID arg, DWORD lo, DWORD hi)
{
    NativeThread* native_thread;
    // get current Java thread
    Thread* java_thread;
#if defined(_MSC_VER)
    __asm {
        // get thread block
        mov EDX, FS:14h
        mov java_thread, EDX
    }
#else
    __asm__ __volatile__ (
        "movl %%fs:20, %0"
        :"=r"(java_thread)
        :
    );
#endif
    // check if thread switch is ok
    if (java_thread->thread_switch_enabled != 0) {
        return;
    }

    native_thread = java_thread->native_thread;

    // disable thread switch.
    ++java_thread->thread_switch_enabled;

    // call the threadSwitch method.
    threadSwitch(native_thread);
}

void __stdcall set_interval_timer(int type, int ms)
{
    HANDLE hTimer;
    LARGE_INTEGER liDueTime;

    liDueTime.QuadPart=-10000*ms;

    // Create a waitable timer.
    hTimer = CreateWaitableTimer(NULL, TRUE, "WaitableTimer");
    if (!hTimer)
    {
        printf("CreateWaitableTimer failed (%lu)\n", GetLastError());
        return;
    }

    // Set a timer to wait.
    if (!SetWaitableTimer(hTimer, &liDueTime, ms, timer_tick, NULL, 0))
    {
        printf("SetWaitableTimer failed (%lu)\n", GetLastError());
        return;
    }
}

#elif defined(linux) || defined(__CYGWIN32__)
#if defined(USE_CLONE)
int __stdcall suspend_thread(const int pid)
#else
int __stdcall suspend_thread(const pthread_t pid)
#endif
{
    //printf("Suspending thread %d.\n", pid);
#if defined(USE_CLONE)
    kill(pid, SIGSTOP);
#else
    pthread_kill(pid, SIGSTOP);
#endif
    return 0;
}

int thread_start_trampoline(void *t)
{
#if defined(USE_CLONE)
    const int pid = getpid();
#else
    const pthread_t pid = pthread_self();
#endif
    //printf("Thread %d started, suspending self.\n", pid);
    void* arg = *((void**)t);
    int (*start)(void *) = (int (*)(void*)) ((void**)t)[1];
    // write our pid in temp[0]
    *((int*)t) = getpid();
    // write '0' in temp[1], signaling parent that we have finished
    // initialization.
    ((void**)t)[1] = 0;
    // wait until parent receives notification and notifies us back.
    while (!((void**)t)[1]) {
      sched_yield();
      //Sleep(1);
    }
    // free temp array
    free(t);

#if 0
    int foo;
    sigset_t set; sigemptyset(&set); sigaddset(&set, SIGCONT); sigaddset(&set, SIGKILL);
    sigwait(&set, &foo);
    if (foo == SIGKILL) {
        printf("Thread %d killed while waiting.\n", pid);
        return 0;
    }
    //printf("Thread %d resumed (signal=%d)\n", pid, foo);
#endif
    //printf("Thread %d calling function at 0x%08x, arg 0x%08x.\n", pid, start, arg);
    return start(arg);
}

#if defined(USE_CLONE)
int __stdcall create_thread(int (*start)(void *), void* arg)
#else
pthread_t __stdcall create_thread(int (*start)(void *), void* arg)
#endif
{
#if defined(USE_CLONE)
    unsigned long int pid;
#else
    pthread_t pid;
#endif
    void** temp = (void**)malloc(sizeof(start) + sizeof(arg));
    temp[0] = arg;
    temp[1] = (void*)start;
#if defined(USE_CLONE)
    {
        void* child_stack = calloc(1, 65536);
        pid = clone(thread_start_trampoline, child_stack, CLONE_VM | CLONE_FS | CLONE_FILES | CLONE_SIGHAND, temp);
    }
#else
    pthread_create(&pid, 0, (void* (*)(void *))thread_start_trampoline, temp);
#endif
    //printf("Created thread %d.\n", pid);

    // wait for child to start.
    while (temp[1]) {
      sched_yield();
      //Sleep(1);
    }

    // child has started, suspend it.
#if defined(USE_CLONE)
    kill(pid, SIGSTOP);
#else
    pthread_kill(pid, SIGSTOP);
#endif

    // mark child to continue, once it is resumed.
    temp[1] = (void*)start;
    //printf("Child %d marked to continue.\n", pid);

    return pid;
}

#if defined(linux)
/* We don't want to include the kernel header.  So duplicate the
   information.  */

/* Structure passed on `modify_ldt' call.  */
struct modify_ldt_ldt_s
{
    unsigned int entry_number;
    unsigned long int base_addr;
    unsigned int limit;
    unsigned int seg_32bit:1;
    unsigned int contents:2;
    unsigned int read_exec_only:1;
    unsigned int limit_in_pages:1;
    unsigned int seg_not_present:1;
    unsigned int useable:1;
    unsigned int empty:25;
};

_syscall3( int, modify_ldt, int, func, void *, ptr, unsigned long, bytecount )

/* Initialize the thread-unique value.  */
#define INIT_THREAD_SELF(descr, descrsize, nr) \
{                                                                             \
  struct modify_ldt_ldt_s ldt_entry =                                         \
    { nr, (unsigned long int) descr, descrsize, 1, 0, 0, 0, 0, 1, 0 };        \
  if (modify_ldt (1, &ldt_entry, sizeof (ldt_entry)) != 0)                    \
    abort ();                                                                 \
  __asm__ __volatile__ ("movw %w0, %%fs" : : "q" (nr * 8 + 7));               \
}
#elif defined(__CYGWIN32__)
#define INIT_THREAD_SELF(descr, descrsize, nr)  // todo
#endif

static int current_id = 16;

int __stdcall init_thread()
{
    int my_id;
    void* descr = calloc(1, 1024);
    __asm__ __volatile__ (
        "nop\n"\
        "uphere:\n\t"\
        "movl %2, %%eax\n\t"\
        "movl %%eax, %%ebx\n\t"\
        "inc %%ebx\n\t"\
        "lock cmpxchgl %%ebx, %1\n\t"\
        "jne uphere\n\t"\
        "movl %%ebx, %0\n\t"
        :"=r"(my_id), "=m"(current_id)
        :"m"(current_id)
        :"%eax"
        );
    INIT_THREAD_SELF(descr, 1*1024, my_id);
    //printf("Thread %d finished initialization, pid=%d, id=%d.\n", pthread_self(), getpid(), my_id);
    return getpid();
}
#if defined(USE_CLONE)
int __stdcall set_thread_priority(const int pid, const int level)
#else
int __stdcall set_thread_priority(const pthread_t pid, const int level)
#endif
{
#if defined(USE_CLONE)
  return setpriority(PRIO_PROCESS, pid, level);
#else
  struct sched_param param;
  int a = sched_get_priority_min(SCHED_OTHER);
  int b = sched_get_priority_max(SCHED_OTHER);
  memset(&param, 0, sizeof(param));
  param.sched_priority = (int) ((level+15) / 30. * (b - a) + a);
  return pthread_setschedparam(pid, SCHED_OTHER, &param);
#endif
}
#if defined(USE_CLONE)
int __stdcall resume_thread(const int pid)
#else
int __stdcall resume_thread(const pthread_t pid)
#endif
{
    //printf("Resuming thread %d.\n", pid);
    //ptrace( PTRACE_CONT, pid, (caddr_t)1, SIGSTOP );
#if defined(USE_CLONE)
    kill(pid, SIGCONT);
#else
    pthread_kill(pid, SIGCONT);
#endif
    return 0;
}

void* __stdcall allocate_stack(const int size)
{
    // TODO
    void* p = calloc(sizeof(char), size);
    //printf("Allocating stack at 0x%08x of size %d.\n", p, size);
    if (p != NULL) p = (char*)p+size;
    return p;
}

#if defined(linux)
static inline int get_debug_reg( int pid, int num, DWORD *data )
{
    int res = ptrace( PTRACE_PEEKUSER, pid, DR_OFFSET(num), 0 );
    if ((res == -1) && errno)
    {
        // TODO
        return -1;
    }
    *data = res;
    return 0;
}

int __stdcall get_thread_context(const int pid, CONTEXT* context)
{
    int status;
    int result;
    int flags;
    //printf("Getting thread context for pid %d.\n", pid);
    if (ptrace(PTRACE_ATTACH, pid, 0, 0) == -1) {
        printf("Attempt to attach to pid %d failed! %s errno %d\n", pid, strerror(errno), errno);
        return 0;
    }
    waitpid(pid, &status, WUNTRACED);
    if (WIFSTOPPED(status)) {
        //printf("Child is stopped, signal=%d.\n", WSTOPSIG(status));
    }

    result = 0;
    flags = context->ContextFlags;
    if (flags & CONTEXT_FULL) {
        struct kernel_user_regs_struct regs;
        if (ptrace( PTRACE_GETREGS, pid, 0, &regs ) == -1) goto error;
        if (flags & CONTEXT_INTEGER) {
            context->Eax = regs.eax;
            context->Ebx = regs.ebx;
            context->Ecx = regs.ecx;
            context->Edx = regs.edx;
            context->Esi = regs.esi;
            context->Edi = regs.edi;
        }
        if (flags & CONTEXT_CONTROL) {
            context->Ebp    = regs.ebp;
            context->Esp    = regs.esp;
            context->Eip    = regs.eip;
            context->SegCs  = regs.cs;
            context->SegSs  = regs.ss;
            context->EFlags = regs.eflags;
        }
        if (flags & CONTEXT_SEGMENTS) {
            context->SegDs = regs.ds;
            context->SegEs = regs.es;
            context->SegFs = regs.fs;
            context->SegGs = regs.gs;
        }
    }
    if (flags & CONTEXT_DEBUG_REGISTERS) {
        if (get_debug_reg( pid, 0, &context->Dr0 ) == -1) goto error;
        if (get_debug_reg( pid, 1, &context->Dr1 ) == -1) goto error;
        if (get_debug_reg( pid, 2, &context->Dr2 ) == -1) goto error;
        if (get_debug_reg( pid, 3, &context->Dr3 ) == -1) goto error;
        if (get_debug_reg( pid, 6, &context->Dr6 ) == -1) goto error;
        if (get_debug_reg( pid, 7, &context->Dr7 ) == -1) goto error;
    }
    if (flags & CONTEXT_FLOATING_POINT) {
        /* we can use context->FloatSave directly as it is using the */
        /* correct structure (the same as fsave/frstor) */
        if (ptrace( PTRACE_GETFPREGS, pid, 0, &context->FloatSave ) == -1) goto error;
        context->FloatSave.Cr0NpxState = 0;  /* FIXME */
    }
    result = 1;
    goto cleanup;
error:
    // TODO: error condition
    printf("Error occurred while getting context of thread %d: %s errno %d.\n", pid, strerror(errno), errno);

cleanup:
    if (ptrace(PTRACE_DETACH, pid, 0, SIGSTOP) == -1) {
        printf("Attempt to detach from pid %d failed! %s errno %d\n", pid, strerror(errno), errno);
        return 0;
    }
    return result;
}

int __stdcall set_thread_context(int pid, CONTEXT* context)
{
    int status;
    int result;
    int flags;
    //printf("Setting thread context for pid %d, ip=0x%08x, sp=0x%08x\n", pid, context->Eip, context->Esp);
    if (ptrace(PTRACE_ATTACH, pid, 0, 0) == -1) {
        printf("Attempt to attach to pid %d failed! %s errno %d\n", pid, strerror(errno), errno);
        return 0;
    }
    waitpid(pid, &status, WUNTRACED);
    if (WIFSTOPPED(status)) {
        //printf("Child is stopped, signal=%d.\n", WSTOPSIG(status));
    }

    result = 0;
    flags = context->ContextFlags;
    if (flags & CONTEXT_FULL) {
        struct kernel_user_regs_struct regs;

        /* need to preserve some registers (at a minimum orig_eax must always be preserved) */
        if (ptrace( PTRACE_GETREGS, pid, 0, &regs ) == -1) goto error;

        if (flags & CONTEXT_INTEGER) {
            regs.eax = context->Eax;
            regs.ebx = context->Ebx;
            regs.ecx = context->Ecx;
            regs.edx = context->Edx;
            regs.esi = context->Esi;
            regs.edi = context->Edi;
        }
        if (flags & CONTEXT_CONTROL) {
            regs.ebp = context->Ebp;
            regs.esp = context->Esp;
            regs.eip = context->Eip;
            regs.cs  = context->SegCs;
            regs.ss  = context->SegSs;
            regs.eflags = context->EFlags;
        }
        if (flags & CONTEXT_SEGMENTS) {
            regs.ds = context->SegDs;
            regs.es = context->SegEs;
            regs.fs = context->SegFs;
            regs.gs = context->SegGs;
        }
        if (ptrace( PTRACE_SETREGS, pid, 0, &regs ) == -1) goto error;
    }
    if (flags & CONTEXT_DEBUG_REGISTERS) {
        if (ptrace( PTRACE_POKEUSER, pid, DR_OFFSET(0), context->Dr0 ) == -1) goto error;
        if (ptrace( PTRACE_POKEUSER, pid, DR_OFFSET(1), context->Dr1 ) == -1) goto error;
        if (ptrace( PTRACE_POKEUSER, pid, DR_OFFSET(2), context->Dr2 ) == -1) goto error;
        if (ptrace( PTRACE_POKEUSER, pid, DR_OFFSET(3), context->Dr3 ) == -1) goto error;
        if (ptrace( PTRACE_POKEUSER, pid, DR_OFFSET(6), context->Dr6 ) == -1) goto error;
        if (ptrace( PTRACE_POKEUSER, pid, DR_OFFSET(7), context->Dr7 ) == -1) goto error;
    }
    if (flags & CONTEXT_FLOATING_POINT) {
        /* we can use context->FloatSave directly as it is using the */
        /* correct structure (the same as fsave/frstor) */
        if (ptrace( PTRACE_SETFPREGS, pid, 0, &context->FloatSave ) == -1) goto error;
    }
    result = 1;
    goto cleanup;
error:
    // TODO: error condition
    printf("Error occurred while setting context of thread %d: %s errno %d.\n", pid, strerror(errno), errno);

cleanup:
    if (ptrace(PTRACE_DETACH, pid, 0, SIGSTOP) == -1) {
        printf("Attempt to detach from pid %d failed! %s errno %d\n", pid, strerror(errno), errno);
        return 0;
    }
    return result;
}
#elif defined(__CYGWIN32__)
#define HANDLE int
int __stdcall get_thread_context(HANDLE t, CONTEXT* c)
{
    // TODO.
    return 0;
}

int __stdcall set_thread_context(HANDLE t, CONTEXT* c)
{
    // TODO.
    return 0;
}
#endif

#if defined(USE_CLONE)
int __stdcall get_current_thread_handle(void)
#else
pthread_t __stdcall get_current_thread_handle(void)
#endif
{
#if defined(USE_CLONE)
    return getpid();
#else
    return pthread_self();
#endif
}

sem_t semaphore_init;
sem_t *p_semaphore_init;

void initSemaphoreLock(void)
{
    sem_init(&semaphore_init, 0, 1);
    p_semaphore_init = &semaphore_init;
}

int __stdcall init_semaphore(void)
{
    sem_t* n_sem = (sem_t*)malloc(sizeof(sem_t));
    sem_wait(p_semaphore_init);
    sem_init(n_sem, 0, 0);
    sem_post(p_semaphore_init);
    //printf("Initialized new semaphore %x.\n", (int)n_sem);
    return (int)n_sem;
}

#if !defined(timersub)
# define timersub(a, b, result)                                               \
  do {                                                                        \
    (result)->tv_sec = (a)->tv_sec - (b)->tv_sec;                             \
    (result)->tv_usec = (a)->tv_usec - (b)->tv_usec;                          \
    if ((result)->tv_usec < 0) {                                              \
      --(result)->tv_sec;                                                     \
      (result)->tv_usec += 1000000;                                           \
    }                                                                         \
  } while (0)
#endif

#define WAIT_TIMEOUT 0x00000102
int __stdcall wait_for_single_object(int handle, int time)
{
    struct timeval tv1;
    //printf("Thread %d: Waiting on semaphore %x for %d ms.\n", pthread_self(), (int)handle, time);
    if (sem_trywait((sem_t*)handle) == 0) return 0;
    //printf("Thread %d: Waiting on semaphore %x initially failed, looping.\n", pthread_self(), (int)handle);
    fflush(stdout);
    gettimeofday(&tv1, 0);
    for (;;) {
        struct timeval tv2;
        long diff;
        sched_yield();
        if (sem_trywait((sem_t*)handle) == 0) {
            //printf("Thread %d: Waiting on semaphore %x succeeded.\n", pthread_self(), (int)handle);
            fflush(stdout);
            return 0;
        }
        gettimeofday(&tv2, 0);
        timersub(&tv2, &tv1, &tv2);
        diff = tv2.tv_sec*1000000+tv2.tv_usec*1000;
        //printf("Thread %d: Waiting on semaphore %x failed again, time passed %d us.\n", pthread_self(), (int)handle, diff);
        fflush(stdout);
        if (diff > time*1000) return WAIT_TIMEOUT;
    }
}

int __stdcall release_semaphore(int semaphore, int a)
{
    int v = 0;
    //printf("Thread %d: Releasing semaphore %x %d times.\n", pthread_self(), (int)semaphore, a);
    while (--a >= 0)
        v = sem_post((sem_t*)semaphore);
    return v;
}

void __stdcall set_interval_timer(int type, int ms)
{
    struct itimerval v;
    //printf("Thread %d: Setting interval timer type %d, %d ms.\n", pthread_self(), (int)type, ms);
    v.it_interval.tv_sec = v.it_value.tv_sec = ms / 1000;
    v.it_interval.tv_usec = v.it_value.tv_usec = (ms % 1000) * 1000;
    setitimer(type, &v, 0);
}
#else
#error System type not supported.
#endif

void __stdcall set_current_context(Thread* jthread, const CONTEXT* context)
{
#if defined(WIN32)
    //printf("Thread %d: switching to jthread 0x%08x, ip=0x%08x, sp=0x%08x\n", GetCurrentThreadId(), jthread, context->Eip, context->Esp);
#else
#if defined(USE_CLONE)
    int pid = getpid();
#else
    pthread_t pid = pthread_self();
#endif
    //printf("Thread %d: switching to jthread 0x%08x, ip=0x%08x, sp=0x%08x\n", pid, jthread, context->Eip, context->Esp);
#endif

#if defined(_MSC_VER)
    __asm {
        // set thread block
        mov EDX, jthread
        mov FS:14h, EDX
        // load context into ECX
        mov ECX, context
        // set stack pointer
        mov ESP, [ECX+196]
        // set fp regs
        frstor [ECX+28]
        // push return address
        push [ECX+184]
        // change stack pointer to include return address
        mov [ECX+196], ESP
        // push all GPRs
        push [ECX+176] // eax
        push [ECX+172] // ecx
        push [ECX+168] // edx
        push [ECX+164] // ebx
        push [ECX+196] // esp
        push [ECX+180] // ebp
        push [ECX+160] // esi
        push [ECX+156] // edi
        // push eflags
        push [ECX+192]
        // reenable interrupts
        dec dword ptr [EDX+04h]

        // from this point on, the thread can be preempted again.
        // but all GPRs and EIP are on the thread's stack, so it is safe.

        // restore eflags
        popfd
        // restore all GPRs
        popad
        // return to eip
        ret
    }
#else
    __asm volatile (
        "movl %0, %%edx\n\t"\
        "movl %%edx, %%fs:20\n\t"\
        "movl %1, %%ecx\n\t"\
        "movl 196(%%ecx), %%esp\n\t"\
        "frstor 28(%%ecx)\n\t"\
        "pushl 184(%%ecx)\n\t"\
        "movl %%esp, 196(%%ecx)\n\t"\
        "pushl 176(%%ecx)\n\t"\
        "pushl 172(%%ecx)\n\t"\
        "pushl 168(%%ecx)\n\t"\
        "pushl 164(%%ecx)\n\t"\
        "pushl 196(%%ecx)\n\t"\
        "pushl 180(%%ecx)\n\t"\
        "pushl 160(%%ecx)\n\t"\
        "pushl 156(%%ecx)\n\t"\
        "pushl 192(%%ecx)\n\t"\
        "decl 4(%%edx)\n\t"\
        "popf\n\t"\
        "popa\n\t"\
        "ret\n\t"
        :
        :"r"(jthread), "r"(context)
        );
#endif
}
