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
	        RvmMap symbolMapFile = new RvmMap("RVM.map.0");
	        DebugTraceFile traceFile = new DebugTraceFile();
	        
	        traceFile.processArguments(args);
	        traceFile.processFile();
	    } catch (FileNotFoundException e) {
	        System.out.print(e.getMessage());
	        e.printStackTrace();
        }
	}

	private String dumpFileName;
	private String debuggedFileName="trace.log";
	
	void processDumpFile() throws IOException {
		BufferedReader dumpFile = new BufferedReader(new FileReader(dumpFileName));
		BufferedWriter tracedFile = new BufferedWriter(new FileWriter(debuggedFileName));
		String line;
		
		while(dumpFile.ready()) {
			line = dumpFile.readLine();
			if(line.equals("----------------")) {
				String inToken = dumpFile.readLine();
				if(inToken.equals("IN:")) {
					String asmLine = dumpFile.readLine();
					
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
