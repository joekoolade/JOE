/**
 * 
 */
package org.jam.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.HashMap;
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
	TreeMap<Long, RvmSymbol> codeSymbolTable;

	// Statistics
	static int codeSymbols = 0;
	static int fieldSymbols = 0;
	static int literalFieldSymbols = 0;
	static int literalSymbols = 0;
	static int tibSymbols = 0;
	static int unknownSymbols = 0;
	
	enum SymbolCategory {
		literal, code, field, tib, literal_field, unknown,
	}

	class RvmSymbol {
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
		codeSymbolTable = new TreeMap<Long, RvmMap.RvmSymbol>();
		createMaps();
	}

	public RvmMap(String mapFileName) throws FileNotFoundException {
		mapFile = new RvmMapFileScanner(mapFileName);
		createMaps();
	}

	/**
	 * Creates a symbol table of code and tib symbols
	 */
	private void createMaps() {
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
			if (symbol.isCodeSymbol() || symbol.isTibSymbol()) {
				codeSymbolTable.put(symbol.getContent(), symbol);
				System.out.println(symbol);
			}
		}
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
