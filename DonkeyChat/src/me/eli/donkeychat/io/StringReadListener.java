package me.eli.donkeychat.io;

import java.io.IOException;

public interface StringReadListener {
	
	public void onRead(String str) throws IOException;
	
}
