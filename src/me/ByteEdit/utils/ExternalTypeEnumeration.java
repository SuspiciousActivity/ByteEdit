package me.ByteEdit.utils;

public class ExternalTypeEnumeration {
	
	private String descriptor;
	private int index;
	
	public ExternalTypeEnumeration(String paramString) {
		setDescriptor(paramString);
	}
	
	ExternalTypeEnumeration() {}
	
	void setDescriptor(String paramString) {
		this.descriptor = paramString;
		reset();
	}
	
	public void reset() {
		this.index = (this.descriptor.indexOf('(') + 1);
		if (this.index < 1) {
			throw new IllegalArgumentException("Missing opening parenthesis in descriptor [" + this.descriptor + "]");
		}
	}
	
	public boolean hasMoreTypes() {
		return this.index < this.descriptor.length() - 1;
	}
	
	public String nextType() {
		int i = this.index;
		this.index = this.descriptor.indexOf(',', i);
		if (this.index < 0) {
			this.index = this.descriptor.indexOf(')', i);
			if (this.index < 0) {
				throw new IllegalArgumentException("Missing closing parenthesis in descriptor [" + this.descriptor + "]");
			}
		}
		return this.descriptor.substring(i, this.index++).trim();
	}
	
	public String methodName() {
		return this.descriptor.substring(0, this.descriptor.indexOf('(')).trim();
	}
}
