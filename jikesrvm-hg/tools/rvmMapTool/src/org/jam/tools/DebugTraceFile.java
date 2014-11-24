package org.jam.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class DebugTraceFile {

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

	private String dumpFileName;
	private String debuggedFileName="trace.log";
	private RvmMap symbolMapFile;
	
	public DebugTraceFile() throws FileNotFoundException
	{
        symbolMapFile = new RvmMap("RVM.map");
	}
	
	void processDumpFile() throws IOException {
		BufferedReader dumpFile = new BufferedReader(new FileReader(dumpFileName));
		BufferedWriter tracedFile = new BufferedWriter(new FileWriter(debuggedFileName));
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
						System.out.println(symbol.getDetails());
					}
					else
					{
						System.out.println(symbol.getDetails() + " + 0x" + Long.toHexString((address - symbol.getContent())));
					}
				}
			}
		}
	}
	private void processFile() {
    }

	void processArguments(String[] args) {
		if(args.length == 0) {
			dumpFileName = "stderr.txt";
		} else {
			dumpFileName = args[0];
		}
    }
}
