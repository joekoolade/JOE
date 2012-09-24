//===------------- llcj.cpp - Java ahead of time compiler -----------------===//
//
//                           The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "llvm/Support/ManagedStatic.h"
#include "llvm/Support/Path.h"
#include "llvm/Support/Program.h"
#include "llvm/Support/Signals.h"

#include "LinkPaths.h"

#include <cstdio>
#include <cstdlib>
#include <cstring>

using namespace llvm;

int main(int argc, char **argv) {
  llvm_shutdown_obj X;  // Call llvm_shutdown() on exit.

  bool SaveTemps = false;
  char* opt = 0;
  
  const char** vmjcArgv = new const char*[argc + 5];
  int vmjcArgc = 1;
  const char** gccArgv = new const char*[argc + 32];
  int gccArgc = 1;
 
  bool runGCC = true;
  char* className = 0;
  bool shared = false;
  bool withJIT = false;

  for (int i = 1; i < argc; ++i) {
    if (!strcmp(argv[i], "-shared")) {
      gccArgv[gccArgc++] = argv[i];
      shared = true;
    } else if (!strcmp(argv[i], "-with-jit") ||
               !strcmp(argv[i], "--with-jit")) {
      withJIT = true;
      vmjcArgv[vmjcArgc++] = argv[i];
    } else if (!strcmp(argv[i], "-O1") || !strcmp(argv[i], "-O2") ||
               !strcmp(argv[i], "-O3")) {
      opt = argv[i];
      vmjcArgv[vmjcArgc++] = (const char*)"-std-compile-opts";
    } else if (argv[i][0] == '-' && argv[i][1] == 'S') {
      runGCC = false;
    } else if (argv[i][0] == '-' && argv[i][1] == 'c') {
      gccArgv[gccArgc++] = argv[i];
    } else if (argv[i][0] == '-' && argv[i][1] == 'l') {
      gccArgv[gccArgc++] = argv[i];
    } else if (argv[i][0] == '-' && argv[i][1] == 'L') {
      gccArgv[gccArgc++] = argv[i];
    } else if (argv[i][0] == '-' && argv[i][1] == 'W') {
      gccArgv[gccArgc++] = argv[i];
    } else if (argv[i][0] == '-' && argv[i][1] == 'g') {
      gccArgv[gccArgc++] = argv[i];
    } else if (argv[i][0] == '-' && argv[i][1] == 'p' && argv[i][2] == 'g') {
      gccArgv[gccArgc++] = argv[i];
    } else if (argv[i][0] == '-' && argv[i][1] == 'o') {
      gccArgv[gccArgc++] = argv[i++];
      gccArgv[gccArgc++] = argv[i];
    } else if (argv[i][0] != '-') {
      char* name = argv[i];
      int len = strlen(name);
      if (len > 4 && (!strcmp(&name[len - 4], ".jar") || 
                      !strcmp(&name[len - 4], ".zip"))) {
        vmjcArgv[vmjcArgc++] = name;
        char* slash = strrchr(name, '/');
        if (slash) {
          name = slash;
          len = strlen(name);
        }
        className = strdup(name);
        className[len - 4] = 0;
      } else if (len > 6 && !strcmp(&name[len - 6], ".class")) {
        vmjcArgv[vmjcArgc++] = name;
        char* slash = strrchr(name, '/');
        if (slash) {
          name = slash;
          len = strlen(name);
        }
        className = strdup(name);
        className[len - 6] = 0;
      } else {
        gccArgv[gccArgc++] = name;
      }
    } else if (!strcmp(argv[i], "--help")) {
      fprintf(stderr, "Usage: llcj [options] file ...\n"
                      "The Java to native compiler. Run vmjc --help for more "
                      "information on the real AOT compiler.\n");
      delete gccArgv;
      delete vmjcArgv;
      if (className) free(className);
      return 0;
    } else {
      vmjcArgv[vmjcArgc++] = argv[i];
    }
  } 

  vmjcArgv[vmjcArgc] = 0;
  gccArgv[gccArgc] = 0;

  std::string errMsg;
 
  const sys::Path& tempDir = SaveTemps
      ? sys::Path(sys::Path::GetCurrentDirectory())
      : sys::Path(sys::Path::GetTemporaryDirectory());

  sys::Path Out = tempDir;
  int res = 0;
  sys::Path Prog;
  
  if (!className) {
    fprintf(stderr, "No Java file specified.... Abort\n");
    goto cleanup;
  }
  
  Prog = sys::Program::FindProgramByName("vmjc");

  if (Prog.isEmpty()) {
    fprintf(stderr, "Can't find vmjc.... Abort\n");
    goto cleanup;
  }
  
  Out.appendComponent(className);
  Out.appendSuffix("bc");
  
  vmjcArgv[0] = Prog.c_str();
  vmjcArgv[vmjcArgc++] = "-f";
  vmjcArgv[vmjcArgc++] = "-o";
  vmjcArgv[vmjcArgc++] = Out.c_str();

  res = sys::Program::ExecuteAndWait(Prog, vmjcArgv);

  if (!res && opt) {
    sys::Path OptOut = tempDir;
    OptOut.appendComponent("llvmopt");
    OptOut.appendSuffix("bc");
    
    sys::Path Prog = sys::Program::FindProgramByName("opt");
  
    if (Prog.isEmpty()) {
      fprintf(stderr, "Can't find opt.... Abort\n");
      goto cleanup;
    }
    
    const char* optArgv[7];
    optArgv[0] = Prog.c_str();
    optArgv[1] = Out.c_str();
    optArgv[2] = "-f";
    optArgv[3] = "-o";
    optArgv[4] = OptOut.c_str();
    if (opt) {
      optArgv[5] = opt;
      optArgv[6] = 0;
    } else {
      optArgv[5] = 0;
    }
  
    res = sys::Program::ExecuteAndWait(Prog, optArgv);
    Out = OptOut;
  }

  if (!res) {
    sys::Path LlcOut;
    
    if (runGCC)
      LlcOut= tempDir;
    else
      LlcOut = sys::Path(sys::Path::GetCurrentDirectory());

    LlcOut.appendComponent(className);
    LlcOut.appendSuffix("s");
   
    sys::Path Prog = sys::Program::FindProgramByName("llc");
  
    if (Prog.isEmpty()) {
      fprintf(stderr, "Can't find llc.... Abort\n");
      goto cleanup;
    }
    
    const char* llcArgv[8];
    int i = 0;
    llcArgv[i++] = Prog.c_str();
    llcArgv[i++] = Out.c_str();
    if (shared) llcArgv[i++] = "-relocation-model=pic";
    llcArgv[i++] = "-disable-fp-elim";
    llcArgv[i++] = "-f";
    llcArgv[i++] = "-o";
    llcArgv[i++] = LlcOut.c_str();
    llcArgv[i++] = 0;
  
    res = sys::Program::ExecuteAndWait(Prog, llcArgv);
    Out = LlcOut;
  }

  if (!res && runGCC) {
    sys::Path Prog = sys::Program::FindProgramByName("g++");
  
    if (Prog.isEmpty()) {
      fprintf(stderr, "Can't find gcc.... Abort\n");
      goto cleanup;
    }

    gccArgv[0] = Prog.c_str();
    gccArgv[gccArgc++] = Out.c_str();
    gccArgv[gccArgc++] = LLVMLibs;
    gccArgv[gccArgc++] = VMKITLibs1;
    gccArgv[gccArgc++] = VMKITLibs2;
    gccArgv[gccArgc++] = VMKITLibs3;
    gccArgv[gccArgc++] = "-pthread";
    gccArgv[gccArgc++] = "-lm";
    gccArgv[gccArgc++] = "-ldl";
    gccArgv[gccArgc++] = "-lz";
    gccArgv[gccArgc++] = "-lJ3";
    gccArgv[gccArgc++] = "-lClasspath";
    gccArgv[gccArgc++] = "-lJ3";
    gccArgv[gccArgc++] = "-lClasspath";
    if (withJIT) {
      gccArgv[gccArgc++] = "-lJ3Compiler";
    }
    gccArgv[gccArgc++] = "-lAllocator";
    gccArgv[gccArgc++] = "-lCommonThread";
    gccArgv[gccArgc++] = "-lMvm";
    gccArgv[gccArgc++] = "-lMvmCompiler";
    gccArgv[gccArgc++] = "-lGCMmap2";
    gccArgv[gccArgc++] = "-lvmjc";
    gccArgv[gccArgc++] = "-lLLVMSupport";
    gccArgv[gccArgc++] = "-lLLVMSystem";
#if !defined(__MACH__)
    gccArgv[gccArgc++] = "-rdynamic";
#endif
    gccArgv[gccArgc++] = 0;

    res = sys::Program::ExecuteAndWait(Prog, gccArgv);
    
  }

cleanup:
  if (!SaveTemps) 
    tempDir.eraseFromDisk(true);
  
  delete gccArgv;
  delete vmjcArgv;
  free(className);

  return 0;
}

