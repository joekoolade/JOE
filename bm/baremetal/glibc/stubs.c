int errno=0;
int stderr = 0;
typedef int size_t;

/* 
   program termination
 */
void
abort() {
}

void
free(void *ptr) {
}

void*
malloc(size_t size) {
  return 0;
}

size_t
fwrite(void* ptr, size_t size, size_t elements, void* stream) {
  return 0;
}

void
dl_iterate_phdr(void *ptr0, void *ptr1) {
}

int
fputs(char* s, void* file) {
  return 0;
}

int
__assert_fail(int b) {
  return 0;
}

void
__libc_start_main() {
}

void
__libc_csu_fini() {
}

void
__libc_csu_init() {
}
