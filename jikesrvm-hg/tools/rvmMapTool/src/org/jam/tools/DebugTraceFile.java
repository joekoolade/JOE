package org.jam.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DebugTraceFile {
	private String symbolMapFileName;
	private String dumpFileName;
	private String debuggedFileName="trace.log";
	private RvmMap symbolMapFile;
	
	public static void main(String args[])
	{
		try {
	        DebugTraceFile traceFile = new DebugTraceFile();
	        traceFile.processArguments(args);
	        traceFile.processDumpFile();
	    } catch (FileNotFoundException e) {
	        System.out.print(e.getMessage());
	        e.printStackTrace();
        } catch (IOException e) {
        	System.out.print(e.getMessage());
			e.printStackTrace();
		}
	}

	void processDumpFile() throws IOException {
		BufferedReader dumpFile = new BufferedReader(new FileReader(dumpFileName));
		BufferedWriter tracedFile = new BufferedWriter(new FileWriter(debuggedFileName));
        symbolMapFile = new RvmMap(symbolMapFileName);
		String line;
		
		while(dumpFile.ready()) {
			line = dumpFile.readLine();
			if(line.equals("----------------")) {
				String inToken = dumpFile.readLine();
				if(inToken.equals("IN: ")) {
					String asmLine = dumpFile.readLine();
					Long address = Long.valueOf(asmLine.substring(2,10), 16);
					RvmSymbol symbol = symbolMapFile.findSymbol(address);
					if(symbol.isUnknown())
					{
						continue;
					}
					if(symbol.getContent() == address)
					{
						System.out.println("0x" + Long.toHexString(address) + ": " +symbol.getClassName()+"."+symbol.getMethodName()+"()");
					}
					else
					{
						System.out.println("0x" + Long.toHexString(address) + ": [0x" + Long.toHexString(symbol.getContent())+"]" + symbol.getClassName()+"."+symbol.getMethodName()+"() + 0x" + Long.toHexString((address - symbol.getContent())));
					}
				}
			}
		}
	}
	private void processFile() {
    }

	void processArguments(String[] args) {
		if(args.length == 0) {
			dumpFileName = "qemu.log";
		} else {
			dumpFileName = args[0];
		}
		
		if(args.length == 2)
		{
			symbolMapFileName = args[1];
		}
		else
		{
			symbolMapFileName = "RVM.map";
		}
    }
}
