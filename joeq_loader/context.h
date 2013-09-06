/* The Win32 register context */

/* CONTEXT is the CPU-dependent context; it should be used        */
/* wherever a platform-specific context is needed (e.g. exception */
/* handling, Win32 register functions). */

/* CONTEXT86 is the i386-specific context; it should be used     */
/* wherever only a 386 context makes sense (e.g. DOS interrupts, */
/* Win16 register functions), so that this code can be compiled  */
/* on all platforms. */

#define SIZE_OF_80387_REGISTERS      80

typedef unsigned long   DWORD;
typedef unsigned char   BYTE;

typedef struct _FLOATING_SAVE_AREA
{
    DWORD   ControlWord;
    DWORD   StatusWord;

    DWORD   TagWord;
    DWORD   ErrorOffset;
    DWORD   ErrorSelector;
    DWORD   DataOffset;
    DWORD   DataSelector;
    BYTE    RegisterArea[SIZE_OF_80387_REGISTERS];
    DWORD   Cr0NpxState;
} FLOATING_SAVE_AREA, *PFLOATING_SAVE_AREA;

#define MAXIMUM_SUPPORTED_EXTENSION     512

typedef struct _CONTEXT86
{
    DWORD   ContextFlags;

    /* These are selected by CONTEXT_DEBUG_REGISTERS */
    DWORD   Dr0;
    DWORD   Dr1;
    DWORD   Dr2;
    DWORD   Dr3;
    DWORD   Dr6;
    DWORD   Dr7;

    /* These are selected by CONTEXT_FLOATING_POINT */
    FLOATING_SAVE_AREA FloatSave;

    /* These are selected by CONTEXT_SEGMENTS */
    DWORD   SegGs;
    DWORD   SegFs;
    DWORD   SegEs;
    DWORD   SegDs;

    /* These are selected by CONTEXT_INTEGER */
    DWORD   Edi;
    DWORD   Esi;
    DWORD   Ebx;
    DWORD   Edx;
    DWORD   Ecx;
    DWORD   Eax;

    /* These are selected by CONTEXT_CONTROL */
    DWORD   Ebp;
    DWORD   Eip;
    DWORD   SegCs;
    DWORD   EFlags;
    DWORD   Esp;
    DWORD   SegSs;

    BYTE    ExtendedRegisters[MAXIMUM_SUPPORTED_EXTENSION];
} CONTEXT86;

#define CONTEXT_X86       0x00010000
#define CONTEXT_i386      CONTEXT_X86
#define CONTEXT_i486      CONTEXT_X86

#define CONTEXT86_CONTROL   (CONTEXT_i386 | 0x0001) /* SS:SP, CS:IP, FLAGS, BP */
#define CONTEXT86_INTEGER   (CONTEXT_i386 | 0x0002) /* AX, BX, CX, DX, SI, DI */
#define CONTEXT86_SEGMENTS  (CONTEXT_i386 | 0x0004) /* DS, ES, FS, GS */
#define CONTEXT86_FLOATING_POINT  (CONTEXT_i386 | 0x0008L) /* 387 state */
#define CONTEXT86_DEBUG_REGISTERS (CONTEXT_i386 | 0x0010L) /* DB 0-3,6,7 */
#define CONTEXT86_FULL (CONTEXT86_CONTROL | CONTEXT86_INTEGER | CONTEXT86_SEGMENTS)

/* i386 context definitions */
#ifdef __i386__

#define CONTEXT_CONTROL         CONTEXT86_CONTROL
#define CONTEXT_INTEGER         CONTEXT86_INTEGER
#define CONTEXT_SEGMENTS        CONTEXT86_SEGMENTS
#define CONTEXT_FLOATING_POINT  CONTEXT86_FLOATING_POINT
#define CONTEXT_DEBUG_REGISTERS CONTEXT86_DEBUG_REGISTERS
#define CONTEXT_FULL            CONTEXT86_FULL

typedef CONTEXT86 CONTEXT;

#endif  /* __i386__ */

#define DR_OFFSET(dr) ((int)((((struct user *)0)->u_debugreg) + (dr)))

struct kernel_user_regs_struct
{
    long ebx, ecx, edx, esi, edi, ebp, eax;
    unsigned short ds, __ds, es, __es;
    unsigned short fs, __fs, gs, __gs;
    long orig_eax, eip;
    unsigned short cs, __cs;
    long eflags, esp;
    unsigned short ss, __ss;
};
