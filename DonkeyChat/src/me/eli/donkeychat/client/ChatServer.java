package me.eli.donkeychat.client;

import java.io.IOException;
import java.net.Socket;

import me.eli.donkeychat.io.IOHandler;
import me.eli.donkeychat.io.Writer;

public class ChatServer implements Writer {
	
	private final Socket socket;
	private final IOHandler io;
	
	public ChatServer(Socket socket) throws IOException {
		this.socket = socket;
		this.io = new IOHandler(socket.getInputStream(), socket.getOutputStream());
	}
	
	@Override
	public void close() throws IOException {
		if(!io.isClosed())
			io.close();
		if(!socket.isClosed())
			socket.close();
	}

	@Override
	public boolean isClosed() {
		return socket.isClosed() && io.isClosed();
	}
	
	@Override
	public Socket getSocket() {
		return socket;
	}
	
	@Override
	public IOHandler getIO() {
		return io;
	}
}
