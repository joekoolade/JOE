package org.jikesrvm.compilers.opt.lir2mir.ia32; 
interface BURS_Definitions  {
	byte stm_NT  	= 1;
	byte r_NT  	= 2;
	byte czr_NT  	= 3;
	byte cz_NT  	= 4;
	byte szpr_NT  	= 5;
	byte szp_NT  	= 6;
	byte riv_NT  	= 7;
	byte rlv_NT  	= 8;
	byte any_NT  	= 9;
	byte sload8_NT  	= 10;
	byte uload8_NT  	= 11;
	byte load8_NT  	= 12;
	byte sload16_NT  	= 13;
	byte uload16_NT  	= 14;
	byte load16_NT  	= 15;
	byte load32_NT  	= 16;
	byte load16_32_NT  	= 17;
	byte load8_16_32_NT  	= 18;
	byte load64_NT  	= 19;
	byte address1scaledreg_NT  	= 20;
	byte address1reg_NT  	= 21;
	byte address_NT  	= 22;
	byte boolcmp_NT  	= 23;
	byte bittest_NT  	= 24;
	byte float_load_NT  	= 25;
	byte double_load_NT  	= 26;

}
