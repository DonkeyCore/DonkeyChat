package me.eli.donkeychat.io;

import java.io.IOException;

public interface ObjectReadListener {
	
	public void onRead(Writer src, Object obj) throws IOException;
	
}
