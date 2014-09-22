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
	
	enum SymbolCategory {
		literal, code, field, tib, literal_field, unknown,
	}

	class RvmSymbol implements Comparable {
		int slot;
		int offset;
		SymbolCategory category;
		long content;
		String details;

		public RvmSymbol(int slot, int offset, String category, long content,
		        String details) throws Exception {
			this.slot = slot;
			this.offset = offset;
			this.content = content;
			this.details = details;

			category = category.replace('/', '_');
			if (category.equals(SymbolCategory.literal.toString())) {
				this.category = SymbolCategory.literal;
				literalSymbols++;
			} else if (category.equals(SymbolCategory.code.toString())) {
				this.category = SymbolCategory.code;
				codeSymbols++;
			} else if (category.equals(SymbolCategory.field.toString())) {
				this.category = SymbolCategory.field;
				fieldSymbols++;
			} else if (category.equals(SymbolCategory.tib.toString())) {
				this.category = SymbolCategory.tib;
				tibSymbols++;
			} else if (category.equals(SymbolCategory.literal_field.toString())) {
				this.category = SymbolCategory.literal_field;
				literalFieldSymbols++;
			} else if (category.equals(SymbolCategory.unknown.toString())) {
				if(content != 0) {
					this.category = SymbolCategory.tib;
					tibSymbols++;
				} else {
					this.category = SymbolCategory.unknown;
					unknownSymbols++;
				}
			}
			else {
				throw new Exception("Invalid category: " + category + " this: " + this);
			}
		}

		@Override
        public int compareTo(Object arg0) {
			RvmSymbol o = (RvmSymbol)arg0;
			if(content > o.content) return 1;
			else if(content < o.content) return -1;
			else return 0;
		}
		public boolean isCodeSymbol() {
			return category == SymbolCategory.code;
		}

		public boolean isTibSymbol() {
			return category == SymbolCategory.tib;
		}

		public int getSlot() {
			return slot;
		}

		public void setSlot(int slot) {
			this.slot = slot;
		}

		public int getOffset() {
			return offset;
		}

		public void setOffset(int offset) {
			this.offset = offset;
		}

		public SymbolCategory getCategory() {
			return category;
		}

		public void setCategory(SymbolCategory category) {
			this.category = category;
		}

		public long getContent() {
			return content;
		}

		public void setContent(long content) {
			this.content = content;
		}

		public String getDetails() {
			return details;
		}

		public void setDetails(String details) {
			this.details = details;
		}
		
		public String toString() {
			return slot + " " + Integer.toHexString(offset) + " " + category + " " + Long.toHexString(content) + " " + details;
		}

	}

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
		codeTable = new ArrayList<RvmMap.RvmSymbol>();
		tibTable = new LinkedHashMap<Long, RvmMap.RvmSymbol>();
		createTables();
		sortCodeTableSymbols();
	}

	public RvmMap(String mapFileName) throws FileNotFoundException {
		mapFile = new RvmMapFileScanner(mapFileName);
		createTables();
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
		
		// Do binary search
		while(min < max) {
			midpoint = (min + max)/2;
			RvmSymbol symbol = codeTable.get(midpoint);
			if(symbol.getContent() == address)
				return symbol;
			if(symbol.getContent() < address) {
				RvmSymbol previousSymbol = codeTable.get(midpoint-1);
				if(previousSymbol.getContent() == address)
					return previousSymbol;
				if(previousSymbol.getContent() < address)
					return symbol;
				// Calculate a new midpoint
				max = midpoint-2;
			}
			else {
				RvmSymbol nextSymbol = codeTable.get(midpoint+1);
				if(nextSymbol.getContent() == address)
					return nextSymbol;
				if(nextSymbol.getContent() > address)
					return symbol;
				// set new minmu
				min = midpoint+1;
			}
		}
		return null;
	}
	private void sortCodeTableSymbols() {
		Collections.sort(codeTable);
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
	        processArguments(args);
	        symbolMapFile.processFile();
	        symbolMapFile.openTraceFile();
        } catch (FileNotFoundException e) {
	        System.out.print(e.getMessage());
	        e.printStackTrace();
        }
	}

	void openTraceFile() {
		traceFile = new File("stderr.txt");
		
	}

	private void processFile() {
	    // TODO Auto-generated method stub
	    
    }

	private static void processArguments(String[] args) {
		if(args.length == 0) {
			logFile = "stderr.txt";
		} else {
			logFile = args[0];
		}
    }
}
