package me.ByteEdit.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class CustomBufferedReader extends BufferedReader {
	
	public int currentLine = 0;
	
	public CustomBufferedReader(Reader in) {
		super(in);
	}
	
	@Override
	public String readLine() throws IOException {
		currentLine++;
		return super.readLine();
	}
}
