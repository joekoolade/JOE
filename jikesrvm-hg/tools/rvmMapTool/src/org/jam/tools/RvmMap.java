/**
 * 
 */
package org.jam.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * @author jkulig
 * 
 */
public class RvmMap {
	RvmMapFileScanner mapFile;
	ArrayList<RvmSymbol> codeTable;
	LinkedHashMap<Long, RvmSymbol> tibTable;
	File traceFile;

	// Statistics
	static int codeSymbols = 0;
	static int fieldSymbols = 0;
	static int literalFieldSymbols = 0;
	static int literalSymbols = 0;
	static int tibSymbols = 0;
	static int unknownSymbols = 0;
	private String logFile;
	
	class RvmMapFileScanner {
		final String mapFileName = "RVM.map";
		File file;
		Scanner fileScanner;
		int line=0;
		
		public RvmMapFileScanner() throws FileNotFoundException {
			file = new File(mapFileName);
			fileScanner = new Scanner(file);
		}

		public RvmMapFileScanner(String mapFileName)
		        throws FileNotFoundException {
			file = new File(mapFileName);
			fileScanner = new Scanner(file);
		}

		public RvmSymbol scanLine() throws Exception {
			while (true) {
				// Line number we are processing
				line++;
				// Looking for a slot
				if (!fileScanner.hasNextInt()) {
					fileScanner.nextLine();
					continue;
				}
				int slot = fileScanner.nextInt();

				// looking for offset
				if (!fileScanner.hasNext()) {
					fileScanner.nextLine();
					continue;
				}
				int offset = scanOffset();
				
				// look for category
				if(!fileScanner.hasNext()) {
					fileScanner.nextLine();
					continue;
				}
				String category = fileScanner.next();

				// look for content
				if (!fileScanner.hasNext()) {
					fileScanner.nextLine();
					continue;
				}
				long content = scanContent();

				// Get the details
				String details = scanRestOfLine();

				RvmSymbol symbol = new RvmSymbol(slot, offset, category,
				        content, details);
				fileScanner.nextLine();
				return symbol;
			}
		}

		private int scanOffset() {
	        String offsetString = fileScanner.next();
	        BigInteger offset = new BigInteger(offsetString.substring(2), 16);
	        return offset.intValue();
        }

		private long scanContent() {
			String contentString = fileScanner.next();
	        BigInteger content = new BigInteger(contentString.substring(2), 16);
	        return content.longValue();
		}
		
		/**
		 * Returns the rest of the line
		 * 
		 * @return rest of the line
		 */
		private String scanRestOfLine() {
			Pattern currentDelimiterPattern = fileScanner.delimiter();
			fileScanner.useDelimiter("\\n");
			String restOfLine = fileScanner.next();
			fileScanner.useDelimiter(currentDelimiterPattern);
			return restOfLine;
		}
		
		public int getLine() {
			return line;
		}
	}

	public RvmMap() throws FileNotFoundException {
		mapFile = new RvmMapFileScanner();
		codeTable = new ArrayList<RvmSymbol>();
		tibTable = new LinkedHashMap<Long, RvmSymbol>();
		createTables();
		sortCodeTableSymbols();
	}

	public RvmMap(String mapFileName) throws FileNotFoundException {
		mapFile = new RvmMapFileScanner(mapFileName);
		mapFile = new RvmMapFileScanner();
		codeTable = new ArrayList<RvmSymbol>();
		tibTable = new LinkedHashMap<Long, RvmSymbol>();
		createTables();
		sortCodeTableSymbols();
	}

	/**
	 * Creates a symbol table of code and tib symbols
	 */
	private void createTables() {
		RvmSymbol symbol = null;
		while (true) {
			try {
				symbol = mapFile.scanLine();
			} catch (NoSuchElementException e) {
				System.out.println("End of symbol file reached!");
				printStatistics();
				return;
			} catch (Exception e) {
				e.printStackTrace();
				printStatistics();
				return;
			}
			if (symbol.isCodeSymbol()) {
				codeTable.add(symbol);
			}
			if(symbol.isTibSymbol()) {
				tibTable.put(symbol.getContent(), symbol);
			}
		}
	}

	public RvmSymbol findSymbol(long address) {
		int min=0, max, midpoint;
		max = codeTable.size()-1;
		
		// check min and max
		if(address < codeTable.get(0).content || address > codeTable.get(max).content)
			return RvmSymbol.unknownSymbol;
		
		// Do binary search
		while(min < max) {
			midpoint = (min + max)/2;
			RvmSymbol symbol = codeTable.get(midpoint);
			if(symbol.getContent() == address)
				return symbol;
			if(symbol.getContent() < address) {
				RvmSymbol nextSymbol = codeTable.get(midpoint+1);
				if(nextSymbol.getContent() == address)
					return nextSymbol;
				if(nextSymbol.getContent() > address)
					return symbol;
				// Calculate a new midpoint
				min = midpoint+2;
			}
			else {
				RvmSymbol previousSymbol = codeTable.get(midpoint-1);
				if(previousSymbol.getContent() == address)
					return previousSymbol;
				if(previousSymbol.getContent() < address)
					return previousSymbol;
				// set new minimum
				max = midpoint-2;
			}
		}
		return RvmSymbol.unknownSymbol;
	}
	private void sortCodeTableSymbols() {
		Collections.sort(codeTable);
		removeDuplicates();
	}
	
	private void removeDuplicates() {
		// TODO Auto-generated method stub
		
	}

	public void printStatistics() {
		System.out.println("Symbol Statistics\nCode: " + codeSymbols
		        + "\nTibs: " + tibSymbols + "\nFields: " + fieldSymbols
		        + "\nLiterals: " + literalSymbols + "\nLiteral/Field: "
		        + literalFieldSymbols + "\nUnknowns: " + unknownSymbols + "\nLines processed: " + mapFile.getLine());
	}

	public static void main(String[] args) {
		try {
	        RvmMap symbolMapFile = new RvmMap();
        } catch (FileNotFoundException e) {
	        System.out.print(e.getMessage());
	        e.printStackTrace();
        }
	}

}
