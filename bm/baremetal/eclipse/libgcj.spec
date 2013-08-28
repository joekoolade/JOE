#
# This spec file is read by gcj when linking.
# It is used to specify the standard libraries we need in order
# to link with libgcj.
#
%rename lib liborig
*lib:
-lbm -lm -lstub %(libgcc)

# *jc1: -fno-use-divide-subroutine

# *lib: --start-group -lbm -lm --end-group
# *jc1: -fhash-synchronization -fno-use-divide-subroutine  -fuse-boehm-gc -fnon-call-exceptions -fkeep-inline-functions
