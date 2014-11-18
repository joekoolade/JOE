package org.jam.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class DebugTraceFile {
	
	public static void main(String args[])
	{
		try {
	        RvmMap symbolMapFile = new RvmMap("RVM.map.0");
	        DebugTraceFile traceFile = new DebugTraceFile();
	        
	        traceFile.processArguments(args);
	        traceFile.processFile();
	        traceFile.openTraceFile();
        } catch (FileNotFoundException e) {
	        System.out.print(e.getMessage());
	        e.printStackTrace();
        }
	}

	private File traceFile;
	private String traceFileName;
	
	void openTraceFile() {
		traceFile = new File(traceFileName);
	}

	void processTraceFile() throws FileNotFoundException {
		Scanner scanFile = new Scanner(traceFile);
		
	}
	private void processFile() {
    }

	void processArguments(String[] args) {
		if(args.length == 0) {
			traceFileName = "stderr.txt";
		} else {
			traceFileName = args[0];
		}
    }
}
