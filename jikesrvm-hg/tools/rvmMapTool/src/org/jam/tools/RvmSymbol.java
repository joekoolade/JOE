package org.jam.tools;

import javassist.bytecode.stackmap.TypeData.ClassName;

class RvmSymbol implements Comparable {
	enum SymbolCategory {
		literal, code, field, tib, literal_field, unknown,
	}

	int slot;
	int offset;
	SymbolCategory category;
	long content;
	String details;
	String clsName;
	String methodName;
	String argName;
	String fieldName;
	static final public RvmSymbol unknownSymbol = new RvmSymbol();
	
	/*
	 * Creates the unknown symbol
	 */
	private RvmSymbol()
	{
		slot = -1;
		offset = -1;
		category = SymbolCategory.unknown;
		content = -1;
		details = null;
	}
	
	public RvmSymbol(int slot, int offset, String category, long content,
	        String details) throws Exception {
		this.slot = slot;
		this.offset = offset;
		this.content = content;
		this.details = details.trim();

		category = category.replace('/', '_');
		if (category.equals(SymbolCategory.literal.toString())) {
			this.category = SymbolCategory.literal;
			RvmMap.literalSymbols++;
		} else if (category.equals(SymbolCategory.code.toString())) {
			this.category = SymbolCategory.code;
			getCodeFields();
			RvmMap.codeSymbols++;
		} else if (category.equals(SymbolCategory.field.toString())) {
			this.category = SymbolCategory.field;
			RvmMap.fieldSymbols++;
		} else if (category.equals(SymbolCategory.tib.toString())) {
			this.category = SymbolCategory.tib;
			RvmMap.tibSymbols++;
		} else if (category.equals(SymbolCategory.literal_field.toString())) {
			this.category = SymbolCategory.literal_field;
			RvmMap.literalFieldSymbols++;
		} else if (category.equals(SymbolCategory.unknown.toString())) {
			if(content != 0) {
				this.category = SymbolCategory.tib;
				RvmMap.tibSymbols++;
			} else {
				this.category = SymbolCategory.unknown;
				RvmMap.unknownSymbols++;
			}
		}
		else {
			throw new Exception("Invalid category: " + category + " this: " + this);
		}
	}

	private void getCodeFields() {
		// Get the class name
		int nameStart = details.indexOf(',')+3;
		int nameEnd = details.indexOf(';', nameStart);
		clsName = details.substring(nameStart, nameEnd).replace('/', '.');
		// get the method name
		nameStart = nameEnd+4;
		nameEnd = details.indexOf('(', nameStart)-1;
		methodName = details.substring(nameStart, nameEnd);
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

	public boolean isUnknown()
	{
		return this==unknownSymbol;
	}
	
	public String getClassName()
	{
		return clsName;
	}
	
	public String getMethodName()
	{
		return methodName;
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