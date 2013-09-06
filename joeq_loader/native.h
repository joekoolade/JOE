
void __stdcall debugmsg(const char* s);
void* __stdcall syscalloc(const int size);
void __stdcall die(const int code);
__int64 __stdcall currentTimeMillis(void);
void __stdcall mem_cpy(void* to, const void* from, const int size);
int __stdcall file_open(const char* s, const int mode, const int smode);
int __stdcall file_stat(const char* s, struct stat *buf);
int __stdcall file_readbytes(const int fd, char* b, const int len);
int __stdcall file_writebyte(const int fd, const int b);
int __stdcall file_writebytes(const int fd, const char* b, const int len);
int __stdcall file_sync(const int fd);
__int64 __stdcall file_seek(const int fd, const __int64 offset, const int origin);
int __stdcall file_close(const int fd);
int __stdcall console_available(void);
int __stdcall main_argc(void);
int __stdcall main_argv_length(const int i);
void __stdcall main_argv(const int i, char* buf);
extern int joeq_argc;
extern char** joeq_argv;
#if defined(WIN32)
struct dirent {
    long d_ino;              /* inode number */
    unsigned long d_off;     /* offset to this dirent */
    unsigned short d_reclen; /* length of this d_name */
    unsigned char d_type;    /* type of file */
    char d_name[MAX_PATH];   /* file name (null-terminated) */
};
typedef struct {
    struct dirent dirent;
    char *path;
    HANDLE handle;
    WIN32_FIND_DATA find_data;
} DIR;
DIR * __stdcall fs_opendir(const char* s);
struct dirent * __stdcall fs_readdir(DIR *dir);
int __stdcall fs_closedir(DIR *dir);
#endif
int __stdcall fs_getdcwd(const int i, char* buf, const int buflen);
int __stdcall fs_fullpath(char* buf, const char* s, const int buflen);
int __stdcall fs_getfileattributes(const char* s);
char* __stdcall fs_gettruename(char* s);
int __stdcall fs_access(const char* s, const int mode);
__int64 __stdcall fs_getfiletime(const char* s);
__int64 __stdcall fs_stat_size(const char* s);
int __stdcall fs_remove(const char* s);
int __stdcall fs_mkdir(const char* s);
int __stdcall fs_rename(const char* s, const char* s1);
int __stdcall fs_chmod(const char* s, const int mode);
int __stdcall fs_setfiletime(const char* s, const __int64 time);
int __stdcall fs_getlogicaldrives(void);

