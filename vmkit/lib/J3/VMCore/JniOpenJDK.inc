// OpenJDK-specific JNI implementation

// We don't support DirectBuffer calls yet. TODO: Implement these!
jobject NewDirectByteBuffer(JNIEnv *env, void *address, jlong capacity) {
  NYI();
  abort();
  return NULL;
}

void *GetDirectBufferAddress(JNIEnv *env, jobject _buf) {
  NYI();
  abort();
  return NULL;
}
