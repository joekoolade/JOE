;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;; Isolate specific types ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

%JavaCommonClass = type { [1 x %JavaObject*], i32,
                          %JavaClass**, i16, %UTF8*, %JavaClass*, i8*, %VT* }

%ClassBytes = type { i32, i8* }

%JavaClass = type { %JavaCommonClass, i32, i32, [1 x %TaskClassMirror],
                    %JavaField*, i16, %JavaField*, i16, %JavaMethod*, i16,
                    %JavaMethod*, i16, i8*, %ClassBytes*, %JavaConstantPool*, %Attribut*,
                    i16, %JavaClass**, i16, %JavaClass*, i16, i8, i8, i32, i32 }
