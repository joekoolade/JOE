%ThreadAllocator = type { i8*, i8*, i8*, i8*, i8*, i8*, i8* }

;;; Field 0: the thread
;;; field 1: allocator
;;; field 2: MutatorContext
;;; field 3: realRoutine
;;; field 4: CollectionAttempts
%MutatorThread = type { %Thread, %ThreadAllocator, i8*, i8*, i32 }
