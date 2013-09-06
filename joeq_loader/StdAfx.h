// All include files for the joeq virtual machine.
// Be careful!  joeq compiles on many compilers/OS's, so be sure that your
// changes here do not break the build for other compilers/OS's.

// Includes that are standard for all compilers.
#include <stdio.h>          // printf, etc...
#include <fcntl.h>
#include <sys/stat.h>
#include <stddef.h>
#include <stdlib.h>

// Includes for WIN32 systems.
#if defined(WIN32)

#define WIN32_LEAN_AND_MEAN     // Exclude rarely-used stuff from Windows headers
#define _WIN32_WINNT 0x0400     // Include SetWaitableTimer

#include <windows.h>
#include <io.h>
//#if !defined(__CYGWIN32__)    // cygwin doesn't have this file
#include <direct.h>     // _getdcwd, _getdrive, _mkdir
//#endif


#if defined(__MINGW32__)
//#undef __STRICT_ANSI__
//#undef RC_INVOKED
#include <excpt.h>
#endif


#if defined(__BORLANDC__)
#include <dos.h>
#endif

#else

#if !defined(__stdcall)
#define __stdcall __attribute__((stdcall))
#endif
#define __int64 int64_t
#define _commit fsync
#define _mkdir(s) mkdir((s),0777)
#define Sleep(ms) usleep(1000*(ms))

#include <wchar.h>
#include <sys/timeb.h>
#include <string.h>
#include <unistd.h>
#include <dirent.h>

#endif

#if !defined(_umask)
#define _umask umask
#endif

#if !defined(DWORD_PTR)
#define DWORD_PTR int
#endif

#if defined(linux)
#include <linux/unistd.h>
#include "context.h"
#include <sys/ptrace.h>
#include <pthread.h>
#include <signal.h>
#include <sys/user.h>
#include <sys/wait.h>
#include <sys/time.h>
#include <semaphore.h>
#endif

#if defined(__CYGWIN32__)
#include <pthread.h>
#include <signal.h>
#include <sys/wait.h>
#include <sys/time.h>
#include <sys/types.h>
#include <semaphore.h>
#include "context.h"
#endif

#include "native.h"
#include "handler.h"

typedef struct _Thread {
    CONTEXT* registers;
    int thread_switch_enabled;
    struct _NativeThread* native_thread;
} Thread;

typedef struct _NativeThread {
    int thread_handle;
    Thread* currentThread;
    int pid;
} NativeThread;

typedef struct _Utf8 {
    char* data;
    int hash;
    void* cache;
} Utf8;

typedef struct _jq_Type {
    Utf8* desc;
    void* class_object;
    void* display;
    int offset;
    void* s_s_array;
    int s_s_array_length;
} jq_Type;

typedef struct _jq_Reference jq_Reference;

typedef struct _VTable {
    jq_Reference* type;
} VTable;

struct _jq_Reference {
    jq_Type type;
    VTable* vtable;
    int state;
    void* class_loader;
};

void __stdcall trap_handler(int);
void __stdcall debug_trap_handler(int);
void __stdcall ctrl_break_handler();
void __stdcall threadSwitch(void*);
