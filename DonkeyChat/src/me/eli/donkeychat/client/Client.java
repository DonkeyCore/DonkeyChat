package me.eli.donkeychat.client;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import me.eli.donkeychat.Chatroom;
import me.eli.donkeychat.gui.GUI;
import me.eli.donkeychat.gui.GUIReadHandler;
import me.eli.donkeychat.io.ObjectReadListener;
import me.eli.donkeychat.io.Results;
import me.eli.donkeychat.io.StreamReadHandler;
import me.eli.donkeychat.io.StringReadListener;
import me.eli.donkeychat.io.Writer;
import me.eli.donkeychat.io.packet.ClientList;
import me.eli.donkeychat.io.packet.Disconnect;
import me.eli.donkeychat.io.packet.Message;
import me.eli.donkeychat.io.packet.Nickname;
import me.eli.donkeychat.io.packet.PingRequest;
import me.eli.donkeychat.io.packet.PingResponse;
import me.eli.donkeychat.io.packet.PublicKey;
import me.eli.donkeychat.io.packet.Result;

public class Client implements StringReadListener, ObjectReadListener {
	
	private final ChatServer server;
	private final Socket socket;
	private final StreamReadHandler readHandler;
	private final GUIReadHandler guiReadHandler;
	private final GUI g;
	private final BigInteger privateKey;
	private final BigInteger publicKey;
	private final BigInteger serverKey;
	private final BigInteger sharedKey;
	
	public Client(String ip, int port) throws UnknownHostException, IOException {
		println("[startup] Opening GUI");
		this.g = new GUI();
		println("[startup] Creating encryption keys");
		Random rand = new Random();
		this.privateKey = new BigInteger(2048, rand);
		this.publicKey = Chatroom.GENERATOR.modPow(privateKey, Chatroom.PRIME);
		println("[startup] Connecting to server");
		this.socket = new Socket(ip, port);
		this.server = new ChatServer(socket);
		println("[startup] Authenticating");
		server.getIO().write(new PublicKey(publicKey));
		this.serverKey = ((BigInteger) ((PublicKey) server.getIO().read()).getKey());
		this.sharedKey = serverKey.modPow(privateKey, Chatroom.PRIME);
		server.getIO().setSharedKey(sharedKey);
		Object read = null;
		do {
			server.getIO().write(new Nickname(null));
			read = server.getIO().read();
		} while(read instanceof Result && ((Result) read).getResult() == Results.INVALID_NICKNAME);
		read = server.getIO().read();
		Nickname nickname = (Nickname) read;
		this.readHandler = new StreamReadHandler(this);
		readHandler.readFrom(server);
		this.guiReadHandler = new GUIReadHandler(this);
		guiReadHandler.readFrom(g.getHandler().getIn());
		println("Connected to " + ip + ":" + port + " as " + nickname.getNickname());
	}
	
	public void println(String message) {
		System.out.println(message);
		if (g != null)
			g.getHandler().println(message);
	}
	
	public void close() throws IOException {
		readHandler.stop();
		guiReadHandler.stop();
		if (!server.getIO().isClosed())
			server.getIO().close();
		if (!socket.isClosed())
			socket.close();
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	@Override
	public void onRead(String str) throws IOException {
		if (str.trim().isEmpty())
			return;
		if (str.startsWith("/")) {
			str = str.substring(1);
			if (str.equalsIgnoreCase("help"))
				println("Available commands:\n/help\tDisplay this message\n/exit\tDisconnect\n/nick <nick>\tChange your nickname");
			else if (str.equalsIgnoreCase("exit")) {
				server.getIO().write(new Disconnect("Quit"));
				g.dispose();
				close();
				System.exit(0);
			} else if (str.toLowerCase().startsWith("nick") && !str.replace("nick", "").trim().isEmpty()) {
				String n = str.replace("nick", "").trim();
				if (n.length() > 16)
					n = n.substring(0, 16);
				server.getIO().write(new Nickname(n));
			} else
				println("Invalid command! Type /help for a list of commands");
		} else
			server.getIO().write(new Message(str));
	}
	
	@Override
	public void onRead(Writer w, Object obj) throws IOException {
		if (!(w instanceof ChatServer))
			return;
		if (obj instanceof PingRequest)
			w.getIO().write(new PingResponse((PingRequest) obj));
		else if (obj instanceof ClientList)
			g.setClients(((ClientList) obj).getClients());
		else if (obj instanceof Message)
			println(((Message) obj).getMessage());
		else if (obj instanceof Result) {
			int result = ((Result) obj).getResult();
			if (result == Results.INVALID_NICKNAME)
				println("That is not a valid nickname");
		} else if (obj instanceof Disconnect) {
			Disconnect d = (Disconnect) obj;
			println("[" + (d.isKick() ? "kicked" : "disconnected") + "] " + d.getReason());
			println("\n> Disconnected");
		}
	}
}
