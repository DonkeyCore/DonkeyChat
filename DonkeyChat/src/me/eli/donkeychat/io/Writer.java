package me.eli.donkeychat.io;

import java.io.Closeable;
import java.net.Socket;

public interface Writer extends Closeable {
	
	public boolean isClosed();
	
	public Socket getSocket();
	
	public IOHandler getIO();
	
}
