;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;; Printing functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

declare void @printFloat(float)
declare void @printDouble(double)
declare void @printLong(i64)
declare void @printInt(i32)
declare void @printObject(i8*)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;; Math ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

declare double @llvm.sqrt.f64(double) nounwind
declare double @llvm.sin.f64(double) nounwind
declare double @llvm.cos.f64(double) nounwind
declare double @tan(double)
declare double @asin(double)
declare double @acos(double)
declare double @atan(double)
declare double @exp(double)
declare double @log(double)
declare double @ceil(double)
declare double @floor(double)
declare double @cbrt(double)
declare double @cosh(double)
declare double @expm1(double)
declare double @log10(double)
declare double @log1p(double)
declare double @sinh(double)
declare double @tanh(double)
declare double @fabs(double)
declare double @rint(double)
declare double @hypot(double, double)
declare double @pow(double, double)
declare double @atan2(double, double)
declare float  @fabsf(float)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;; Memory ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

declare void @llvm.memcpy.i32(i8 *, i8 *, i32, i32) nounwind
declare void @llvm.memset.i32(i8 *, i8, i32, i32) nounwind
declare i8*  @llvm.frameaddress(i32) nounwind readnone

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;; Atomic ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

declare i8  @llvm.atomic.cmp.swap.i8.p0i8(i8*, i8, i8) nounwind
declare i16 @llvm.atomic.cmp.swap.i16.p0i16(i16*, i16, i16) nounwind
declare i32 @llvm.atomic.cmp.swap.i32.p0i32(i32*, i32, i32) nounwind
declare i64 @llvm.atomic.cmp.swap.i64.p0i64(i64*, i64, i64) nounwind


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; GC ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

declare void @unconditionalSafePoint() nounwind
declare void @conditionalSafePoint() nounwind
declare void @llvm.gcroot(i8**, i8*)
declare i8* @gcmalloc(i32, i8*)
declare i8* @gcmallocUnresolved(i32, i8*)
declare void @addFinalizationCandidate(i8*)
declare void @arrayWriteBarrier(i8*, i8**, i8*)
declare void @fieldWriteBarrier(i8*, i8**, i8*)
declare void @nonHeapWriteBarrier(i8**, i8*)



declare i32 @_setjmp(i8*) nounwind
declare void @registerSetjmp(i8*) nounwind
declare void @unregisterSetjmp(i8*) nounwind
