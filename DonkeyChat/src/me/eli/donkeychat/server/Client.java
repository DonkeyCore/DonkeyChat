package me.eli.donkeychat.server;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;

import me.eli.donkeychat.io.IOHandler;
import me.eli.donkeychat.io.Writer;

public class Client implements Writer {
	
	private final Socket socket;
	private final IOHandler io;
	private String nickname = "Guest" + new Random().nextInt(10000);
	
	public Client(Socket socket) throws IOException {
		this.socket = socket;
		this.io = new IOHandler(socket.getInputStream(), socket.getOutputStream());
	}
	
	public String getNickname() {
		return nickname;
	}
	
	public String setNickname(String nickname) {
		String old = this.nickname;
		this.nickname = nickname;
		return old;
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
