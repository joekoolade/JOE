//===-- JavaJITClasspath.h - Classpath-specific JavaJIT Methods -----------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//


void JavaJIT::lowerArraycopy(std::vector<Value*>& args) {
  Function* meth = TheCompiler->getMethod(upcalls->VMSystemArraycopy, NULL);

  Value* ptr_src = args[0];
  Value* int32_start = args[1];
  Value* ptr_dst = args[2];
  Value* int32_start2 = args[3];
  Value* int32_length = args[4];

  JITVerifyNull(ptr_src);
  JITVerifyNull(ptr_dst);

  BasicBlock* label_entry = currentBlock;
  BasicBlock* label_bb = createBasicBlock("bb");
  BasicBlock* label_bb2 = createBasicBlock("bb2");
  BasicBlock* label_bb4 = createBasicBlock("bb4");
  BasicBlock* label_bb5 = createBasicBlock("bb5");
  BasicBlock* label_bb12_preheader = createBasicBlock("bb12.preheader");
  BasicBlock* label_bb7 = createBasicBlock("bb7");
  BasicBlock* label_bb11 = createBasicBlock("bb11");
  BasicBlock* label_memmove = createBasicBlock("memmove");
  BasicBlock* label_backward = createBasicBlock("backward");
  BasicBlock* label_return = createBasicBlock("return");
  
  BasicBlock* log_label_entry = createBasicBlock("log_entry");
  BasicBlock* log_label_bb = createBasicBlock("log_bb");
    
  // Block entry (label_entry)
  CallInst* ptr_16 = CallInst::Create(intrinsics->GetVTFunction, ptr_src, "",
                                      label_entry);
  CallInst* ptr_17 = CallInst::Create(intrinsics->GetVTFunction, ptr_dst, "",
                                      label_entry);
  
  ICmpInst* int1_18 = new ICmpInst(*label_entry, ICmpInst::ICMP_EQ, ptr_16,
                                   ptr_17, "");
  BranchInst::Create(label_bb, label_bb2, int1_18, label_entry);
    
  // Block bb (label_bb)
  currentBlock = label_bb;
  CallInst* ptr_20 = CallInst::Create(intrinsics->GetClassFunction, ptr_src, "",
                                      label_bb);
  std::vector<Value*> ptr_21_indices;
  ptr_21_indices.push_back(intrinsics->constantZero);
  ptr_21_indices.push_back(intrinsics->OffsetAccessInCommonClassConstant);
  Instruction* ptr_21 =
    GetElementPtrInst::Create(ptr_20, ptr_21_indices, "", label_bb);
  LoadInst* int32_22 = new LoadInst(ptr_21, "", false, label_bb);
  Value* cmp = BinaryOperator::CreateAnd(int32_22, intrinsics->IsArrayConstant, "",
                                         label_bb);
  Value* zero = ConstantInt::get(Type::getInt16Ty(*llvmContext), 0);
  ICmpInst* int1_23 = new ICmpInst(*label_bb, ICmpInst::ICMP_NE, cmp, zero, "");
  BranchInst::Create(label_bb4, label_bb2, int1_23, label_bb);
   

  // Block bb2 (label_bb2)
  currentBlock = label_bb2;
  invoke(meth, args, "", label_bb2);
  BranchInst::Create(label_return, currentBlock);
    
    
  // Block bb4 (label_bb4)
  currentBlock = label_bb4;
  BinaryOperator* int32_27 = BinaryOperator::Create(Instruction::Add,
                                                    int32_length, int32_start,
                                                    "", label_bb4);
  Value* int32_28 = arraySize(ptr_src);
    
  ICmpInst* int1_29 = new ICmpInst(*label_bb4, ICmpInst::ICMP_ULE, int32_27,
                                   int32_28, "");
  BranchInst::Create(label_bb5, label_bb7, int1_29, label_bb4);
    
  // Block bb5 (label_bb5)
  currentBlock = label_bb5;
  BinaryOperator* int32_31 = BinaryOperator::Create(Instruction::Add,
                                                    int32_length, int32_start2,
                                                    "", label_bb5);
  Value* int32_32 = arraySize(ptr_dst);
    
  ICmpInst* int1_33 = new ICmpInst(*label_bb5, ICmpInst::ICMP_ULE, int32_31,
                                   int32_32, "");
  BranchInst::Create(label_bb12_preheader, label_bb7, int1_33, label_bb5);
    
  // Block bb12.preheader (label_bb12_preheader)
  currentBlock = label_bb12_preheader;
  ICmpInst* int1_35 = new ICmpInst(*label_bb12_preheader, ICmpInst::ICMP_UGT,
                                   int32_length, intrinsics->constantZero, "");
  BranchInst::Create(log_label_entry, label_return, int1_35, label_bb12_preheader);
    
  // Block bb7 (label_bb7)
  currentBlock = label_bb7;
  Value* VTArgs[1] = { Constant::getNullValue(intrinsics->VTType) };
  throwRuntimeException(intrinsics->ArrayStoreExceptionFunction, VTArgs, 1);
   

  
  // Block entry (label_entry)
  currentBlock = log_label_entry;
  Value* ptr_10_indices[2] = { intrinsics->constantZero,
                               intrinsics->OffsetBaseClassInArrayClassConstant };
  Instruction* temp = new BitCastInst(ptr_20, intrinsics->JavaClassArrayType, "",
                                      log_label_entry);
  Instruction* ptr_10 = GetElementPtrInst::Create(temp, ptr_10_indices, "",
                                                  log_label_entry);

  LoadInst* ptr_11 = new LoadInst(ptr_10, "", false, log_label_entry);
    
  Value* ptr_12_indices[2] = { intrinsics->constantZero,
                               intrinsics->OffsetAccessInCommonClassConstant };
  Instruction* ptr_12 = GetElementPtrInst::Create(ptr_11, ptr_12_indices, "",
                                                  log_label_entry);
  LoadInst* int16_13 = new LoadInst(ptr_12, "", false, log_label_entry);

  BinaryOperator* int32_15 = BinaryOperator::Create(Instruction::And, int16_13,
                                                    intrinsics->IsPrimitiveConstant,
                                                    "", log_label_entry);
  ICmpInst* int1_16 = new ICmpInst(*log_label_entry, ICmpInst::ICMP_EQ,
                                   int32_15, zero, "");
  BranchInst::Create(label_bb2, log_label_bb, int1_16, log_label_entry);
    
  // Block bb (log_label_bb)
  currentBlock = log_label_bb;
  Value* ptr_11_indices[2] = { intrinsics->constantZero,
                               intrinsics->OffsetLogSizeInPrimitiveClassConstant };
  temp = new BitCastInst(ptr_11, intrinsics->JavaClassPrimitiveType, "",
                         log_label_bb);
  GetElementPtrInst* ptr_18 = GetElementPtrInst::Create(temp, ptr_11_indices,
                                                        "", log_label_bb);
  LoadInst* int32_20 = new LoadInst(ptr_18, "", false, log_label_bb);
   
  int32_start = BinaryOperator::CreateShl(int32_start, int32_20, "",
                                          log_label_bb);
  int32_start2 = BinaryOperator::CreateShl(int32_start2, int32_20, "",
                                           log_label_bb);
  int32_length = BinaryOperator::CreateShl(int32_length, int32_20, "",
                                           log_label_bb);

  ptr_src = new BitCastInst(ptr_src, intrinsics->JavaArrayUInt8Type, "",
                            log_label_bb);
  
  ptr_dst = new BitCastInst(ptr_dst, intrinsics->JavaArrayUInt8Type, "",
                            log_label_bb);
  
  Value* indexes[3] = { intrinsics->constantZero,
                        intrinsics->JavaArrayElementsOffsetConstant,
                        int32_start };
  Instruction* ptr_42 = GetElementPtrInst::Create(ptr_src, indexes,
                                                  "", log_label_bb);
  
  indexes[2] = int32_start2;
  Instruction* ptr_44 = GetElementPtrInst::Create(ptr_dst, indexes,
                                                  "", log_label_bb);
 
  BranchInst::Create(label_memmove, log_label_bb);

  // Block memmove
  currentBlock = label_memmove;
  Value* src_int = new PtrToIntInst(ptr_42, intrinsics->pointerSizeType, "",
                                    currentBlock);
  
  Value* dst_int = new PtrToIntInst(ptr_44, intrinsics->pointerSizeType, "",
                                    currentBlock);

  cmp = new ICmpInst(*currentBlock, ICmpInst::ICMP_ULT, dst_int, src_int, "");

  Value* increment = SelectInst::Create(cmp, intrinsics->constantOne,
                                        intrinsics->constantMinusOne, "",
                                        currentBlock);
  BranchInst::Create(label_bb11, label_backward, cmp, currentBlock);

  PHINode* phi_dst_ptr = PHINode::Create(ptr_44->getType(), 3, "", label_bb11);
  PHINode* phi_src_ptr = PHINode::Create(ptr_44->getType(), 3, "", label_bb11);
  phi_dst_ptr->addIncoming(ptr_44, currentBlock);
  phi_src_ptr->addIncoming(ptr_42, currentBlock);
 
  // Block backward
  currentBlock = label_backward;

  ptr_42 = GetElementPtrInst::Create(ptr_42, int32_length, "",
                                     currentBlock);
  
  ptr_44 = GetElementPtrInst::Create(ptr_44, int32_length, "",
                                     currentBlock);
  
  ptr_42 = GetElementPtrInst::Create(ptr_42, intrinsics->constantMinusOne, "",
                                     currentBlock);
  
  ptr_44 = GetElementPtrInst::Create(ptr_44, intrinsics->constantMinusOne, "",
                                     currentBlock);
  
  phi_dst_ptr->addIncoming(ptr_44, currentBlock);
  phi_src_ptr->addIncoming(ptr_42, currentBlock);

  BranchInst::Create(label_bb11, currentBlock);
  
  // Block bb11 (label_bb11)
  currentBlock = label_bb11;
  Argument* fwdref_39 = new Argument(Type::getInt32Ty(*llvmContext));
  PHINode* int32_i_016 = PHINode::Create(Type::getInt32Ty(*llvmContext), 2,
                                         "i.016", label_bb11);
  int32_i_016->addIncoming(fwdref_39, label_bb11);
  int32_i_016->addIncoming(intrinsics->constantZero, log_label_bb);
   
  LoadInst* ptr_43 = new LoadInst(phi_src_ptr, "", false, label_bb11);
  new StoreInst(ptr_43, phi_dst_ptr, false, label_bb11);


  ptr_42 = GetElementPtrInst::Create(phi_src_ptr, increment, "",
                                     label_bb11);
  
  ptr_44 = GetElementPtrInst::Create(phi_dst_ptr, increment, "",
                                     label_bb11);
  phi_dst_ptr->addIncoming(ptr_44, label_bb11);
  phi_src_ptr->addIncoming(ptr_42, label_bb11);

  BinaryOperator* int32_indvar_next =
    BinaryOperator::Create(Instruction::Add, int32_i_016, intrinsics->constantOne,
                           "indvar.next", label_bb11);
  ICmpInst* int1_exitcond = new ICmpInst(*label_bb11, ICmpInst::ICMP_EQ,
                                         int32_indvar_next, int32_length,
                                         "exitcond");
  BranchInst::Create(label_return, label_bb11, int1_exitcond, label_bb11);

  // Resolve Forward References
  fwdref_39->replaceAllUsesWith(int32_indvar_next); delete fwdref_39;

  currentBlock = label_return;
}
