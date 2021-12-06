package org.jam.tests;

public class OnesChecksum {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		short[] buffer = new short[4];
		
		buffer[0] = (short) 0xffff;
		buffer[1] = (short) 0xffff;
		buffer[2] = (short) 0xffff;
		buffer[3] = (short) 0xffff;
		
		int csum = 0;
		int index = buffer.length-1;
		for(; index >= 0; index--)
		{
			csum += (buffer[index] & 0xFFFF);
		}
		System.out.println("0 csum : " + Integer.toHexString(csum));
		csum = (csum>>16) + (csum & 0xFFFF);
		System.out.println("1 csum : " + Integer.toHexString(csum));
		
	}

}
