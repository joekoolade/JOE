#include <stdio.h>
#include <fcntl.h>
#if defined(WIN32)
#include <windows.h>
#else
#include <unistd.h>
#include <sys/mman.h>
#endif

#if defined(WIN32)
static HANDLE FileHandle;
static HANDLE MappingHandle;
static LPVOID BaseAddress;

void open_file(char* filename)
{
    FileHandle = CreateFile(filename,
                        GENERIC_READ | GENERIC_WRITE,
                        FILE_SHARE_READ,
                        NULL,
                        OPEN_EXISTING,
                        0,
                        NULL
                        );
    if (FileHandle == INVALID_HANDLE_VALUE) {
        printf("Cannot open file %s, error code %lu.", filename, GetLastError());
        exit(-1);
    }
}

unsigned char* my_mmap(int start, int length)
{
    int end, actual_start, actual_length;
    SYSTEM_INFO SystemInfo;
    int page_size;

    MappingHandle = CreateFileMapping(
                        FileHandle,
                        NULL,
                        PAGE_READWRITE,
                        0,
                        0,
                        NULL
                        );
    if (MappingHandle == NULL) {
        printf("CreateFileMapping failed, error code %lu.", GetLastError());
        exit(-1);
    }
    
    GetSystemInfo(&SystemInfo);
    page_size = SystemInfo.dwPageSize;

    end = start + length;
    actual_start = start / page_size * page_size;
    actual_length = (end - actual_start + page_size - 1) / page_size * page_size;

    BaseAddress = MapViewOfFile(
                        MappingHandle,
                        FILE_MAP_READ | FILE_MAP_WRITE,
                        0,
                        actual_start,
                        actual_length
                        );
    if (BaseAddress == NULL) {
        printf("MapViewOfFile failed, error code %lu.", GetLastError());
        exit(-1);
    }
    return ((unsigned char*)BaseAddress) + start - actual_start;
}

void my_unmmap()
{
    CloseHandle( MappingHandle );
    UnmapViewOfFile( BaseAddress );
}

void close_file()
{
    CloseHandle( FileHandle );
}
#else
static int fd;
void* buffer;
static int actual_length;
void open_file(char* filename)
{
    fd = open(filename, O_RDWR);
    if (!fd) {
        printf("Cannot open file %s.\n", filename);
        exit(-1);
    }
}

unsigned char* my_mmap(int start, int length)
{
    size_t page_size = getpagesize();
    int actual_start, end;

    end = start + length;
    actual_start = start / page_size * page_size;
    actual_length = (end - actual_start + page_size - 1) / page_size * page_size;

    buffer = (unsigned char*)mmap(0, actual_length, PROT_READ | PROT_WRITE, MAP_SHARED, fd, actual_start);
    if (buffer == MAP_FAILED) {
        printf("Cannot memory map at offset %d size %d.\n", actual_start, actual_length);
        exit(-1);
    }
    return ((unsigned char*)buffer)+start-actual_start;
}

void my_unmmap()
{
    munmap(buffer, actual_length);
}

void close_file()
{
    close(fd);
}

#endif

void patchELF(unsigned int *p)
{
    unsigned int e_phoff;
    unsigned short i, e_phnum;

    e_phoff = *(p+7);
    e_phnum = *(unsigned short *)(p+11);
    printf("Program header table at offset %d, %d entries.\n", e_phoff, e_phnum);

    my_unmmap();

    p = (unsigned int *)my_mmap(e_phoff, 32*e_phnum);
    for (i=0; i<e_phnum; ++i) {
        printf("Program header %d type %x flags %x\n", i, p[0], p[6]);
        if (p[0] == 1) { /* PT_LOAD */
            if (p[6] == 5) { /* PF_R | PF_X */
                printf("Adding writeable flag.\n");
                p[6] = 7;
            }
        }
        p += 8;
    }

    my_unmmap();
    printf("Patched successfully.\n");
}

void patchPE(unsigned int *p, unsigned short n_sections)
{
    unsigned short i;
    for (i=0; i<n_sections; ++i) {
        printf("Section %d name %.8s flags %x\n", i, p, p[9]);
        if ((p[9] & 0x00000020) != 0 &&
            (p[9] & 0x20000000) != 0 &&
            (p[9] & 0x40000000) != 0 &&
            (p[9] & 0x80000000) == 0) {
            printf("Adding writeable flag.\n");
            p[9] |= 0x80000000;
        }
        p += 10;
    }

    my_unmmap();
    printf("Patched successfully.\n");
}

int main(int argc, char** argv)
{
    unsigned char *b;
    int pe_offset;

    if (argc < 2) {
        printf("Usage: %s <executable file>\n", argv[0]);
        return 0;
    }

    open_file(argv[1]);

    b = my_mmap(0, 0x40);
    pe_offset = *((unsigned int *)(b+0x3c));
    if (b[0] == 0x7f && b[1] == 'E' && b[2] == 'L' && b[3] == 'F') {
        printf("Executable is an ELF file.\n");
        patchELF((unsigned int *)b);
    } else if (pe_offset >= 0 && (pe_offset & 0x7) == 0) {
        my_unmmap();
        b = my_mmap(pe_offset, 22);
        if (b[0] == 'P' && b[1] == 'E' && b[2] == 0 && b[3] == 0) {
            unsigned short n_sections = *((unsigned short *)(b+6));
            unsigned short opt_header_size = *((unsigned short *)(b+20));
            printf("Executable is an PE file with %d sections.\n", n_sections);
            my_unmmap();
            b = my_mmap(pe_offset+24+opt_header_size, n_sections*40);
            patchPE((unsigned int *)b, n_sections);
        } else {
            printf("Unknown executable format (bad PE header).\n");
        }
    } else {
        printf("Unknown executable format.\n");
    }

    close_file();
    return 0;
}
