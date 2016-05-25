package me.eli.donkeychat.server;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
import me.eli.donkeychat.io.packet.Packet;
import me.eli.donkeychat.io.packet.PingRequest;
import me.eli.donkeychat.io.packet.PingResponse;
import me.eli.donkeychat.io.packet.PublicKey;
import me.eli.donkeychat.io.packet.Result;

public class ChatServer implements Consumer<Socket>, StringReadListener, ObjectReadListener {
	
	private final ServerSocket socket;
	private final ClientAcceptor acceptor;
	private final List<Client> clients = new ArrayList<>();
	private final Map<Client, PingRequest> pings = new HashMap<>();
	private final Thread pingThread;
	private final StreamReadHandler readHandler;
	private final GUIReadHandler guiReadHandler;
	private final GUI g;
	// Diffie-Hellman exchange variables
	private final BigInteger privateKey; // randomly generated
	private final BigInteger publicKey; // public = generator^private % prime
	
	public ChatServer(int port) throws IOException {
		println("[startup] Opening GUI");
		this.g = new GUI();
		println("[startup] Creating encryption keys");
		Random rand = new Random();
		this.privateKey = new BigInteger(2048, rand);
		this.publicKey = Chatroom.GENERATOR.modPow(privateKey, Chatroom.PRIME);
		println("[startup] Opening socket on port " + port);
		this.socket = new ServerSocket(port);
		println("[startup] Creating handlers");
		this.acceptor = new ClientAcceptor(socket, this);
		this.pingThread = new Thread(new Pinger());
		this.pingThread.start();
		this.readHandler = new StreamReadHandler(this);
		this.guiReadHandler = new GUIReadHandler(this);
		guiReadHandler.readFrom(g.getHandler().getIn());
		println("Successfully started server on port " + socket.getLocalPort());
	}
	
	public void kick(Client c, String reason) {
		disconnect(c, reason, true);
	}
	
	public void disconnect(Client c, String reason) {
		disconnect(c, reason, false);
	}
	
	public void disconnect(Client c, String reason, boolean kick) {
		if (!clients.contains(c))
			return;
		try {
			c.getIO().write(new Disconnect(reason, kick));
		} catch(IOException e) {}
		try {
			if (!c.isClosed())
				c.close();
		} catch(IOException e) {}
		clients.remove(c);
		broadcast("[" + (kick ? "kick" : "quit") + "] " + c.getNickname() + ": " + reason);
		updateClientLists();
	}
	
	public void broadcast(String message) {
		println(message);
		broadcastPacket(new Message(message));
	}
	
	public void broadcastPacket(Packet packet) {
		for(Client c : getClients()) {
			try {
				c.getIO().write(packet);
			} catch(IOException e) {}
		}
	}
	
	public void shutdown() throws IOException {
		g.dispose();
		for(Client c : getClients())
			kick(c, "Server shutting down");
		readHandler.stop();
		guiReadHandler.stop();
		pingThread.interrupt();
		acceptor.stop();
		socket.close();
		System.exit(0);
	}
	
	public Client[] getClients() {
		return clients.toArray(new Client[clients.size()]);
	}
	
	public Client[] getClients(Client... except) {
		return clients.stream().filter(c -> {
			for(Client cl : except) {
				if (c.equals(cl))
					return false;
			}
			return true;
		}).collect(Collectors.toList()).toArray(new Client[clients.size()]);
	}
	
	public String[] getClientNames() {
		return clients.stream().map(c -> c.getNickname()).sorted().collect(Collectors.toList()).toArray(new String[clients.size()]);
	}
	
	public Client getClient(String nickname) {
		for(Client c : getClients()) {
			if (c.getNickname().equalsIgnoreCase(nickname))
				return c;
		}
		return null;
	}
	
	public void updateClientLists() {
		String[] clients = getClientNames();
		g.setClients(clients);
		broadcastPacket(new ClientList(clients));
	}
	
	public void println(String message) {
		System.out.println(message);
		if (g != null)
			g.getHandler().println(message);
	}
	
	@Override
	public void accept(Socket s) {
		Client c = null;
		try {
			c = new Client(s);
			Object read = c.getIO().read();
			if (!(read instanceof PublicKey)) {
				c.getIO().write(new Disconnect("Invalid packet: Expected PublicKey"));
				c.close();
				return;
			}
			BigInteger publicKey = (BigInteger) ((PublicKey) read).getKey();
			c.getIO().write(new PublicKey(this.publicKey));
			c.getIO().setSharedKey(publicKey.modPow(privateKey, Chatroom.PRIME));
			read = null;
			String n = null;
			do {
				if (read != null)
					c.getIO().write(new Result(Results.INVALID_NICKNAME));
				read = c.getIO().read();
				if (read == null)
					break;
				if (!(read instanceof Nickname))
					continue;
				n = ((Nickname) read).getNickname();
				if (n == null)
					break;
				n = n.split(" ")[0].trim();
			} while(!(read instanceof Nickname) || !isValidNickname(n));
			if (n != null)
				c.setNickname(n);
			else
				c.setNickname("Guest" + new Random().nextInt(10000));
			c.getIO().write(new Result(Results.VALID_NICKNAME));
			c.getIO().write(new Nickname(c.getNickname()));
			println("> [" + c.getNickname() + "] is joining from " + s.getLocalAddress().getHostAddress() + ":" + s.getPort());
			broadcast("[join] " + c.getNickname());
			clients.add(c);
			readHandler.readFrom(c);
			updateClientLists();
		} catch(IOException e) {
			e.printStackTrace();
			try {
				if (c != null)
					c.close();
			} catch(IOException e2) {}
		}
	}
	
	public boolean isValidNickname(String nickname) {
		nickname = nickname.trim();
		boolean valid = !nickname.contains("<") && !nickname.contains(">");
		for(Client cl : getClients()) {
			if (cl.getNickname().equalsIgnoreCase(nickname))
				return false;
		}
		return valid;
	}
	
	@Override
	public void onRead(String str) throws IOException {
		if (str.trim().isEmpty())
			return;
		if (str.startsWith("/")) {
			str = str.substring(1);
			if (str.equalsIgnoreCase("help"))
				println("Available commands:\n/help\t\tDisplay this message\n/exit\t\tShutdown the server\n/nick <client> <nick>\tChange a client's nickname\n/kick <client> [message]\tKick a client");
			else if (str.equalsIgnoreCase("exit"))
				shutdown();
			else if (str.toLowerCase().startsWith("nick") && str.replace("nick", "").trim().split(" ").length > 1) {
				String n = str.split(" ")[2];
				if (n.length() > 16)
					n = n.substring(0, 16);
				if (isValidNickname(n)) {
					Client c = getClient(str.split(" ")[1]);
					if (c == null)
						println("That client is not online");
					String old = c.getNickname();
					c.setNickname(n);
					broadcast("[nick] " + old + " is now known as " + n);
					updateClientLists();
				} else
					println("That is not a valid nickname");
			} else if (str.toLowerCase().startsWith("kick") && !str.replace("kick", "").trim().isEmpty()) {
				Client c = getClient(str.split(" ")[1]);
				if (c == null)
					println("That client is not online");
				if (str.split(" ").length > 2)
					kick(c, str.replace("kick", "").replace(str.split(" ")[1], "").trim());
				else
					kick(c, "Kicked by server");
			} else
				println("Invalid command! Type /help for a list of commands");
		} else
			broadcast("<Server>: " + str);
	}
	
	@Override
	public void onRead(Writer w, Object obj) throws IOException {
		if (!(w instanceof Client))
			return;
		Client c = (Client) w;
		if (obj instanceof PingResponse && pings.containsKey(w) && pings.get(w).getKey() == ((PingResponse) obj).getKey())
			pings.remove(w);
		else if (obj instanceof Disconnect)
			disconnect(c, "Disconnected");
		else if (obj instanceof Message)
			broadcast(c.getNickname() + ": " + ((Message) obj).getMessage());
		else if (obj instanceof Nickname) {
			String n = ((Nickname) obj).getNickname().trim();
			if (isValidNickname(n)) {
				String old = c.getNickname();
				c.setNickname(n);
				c.getIO().write(new Result(Results.VALID_NICKNAME));
				broadcast("[nick] " + old + " is now known as " + n);
				updateClientLists();
			} else
				c.getIO().write(new Result(Results.INVALID_NICKNAME));
		}
	}
	
	public class Pinger implements Runnable {
		
		public void run() {
			while(true) {
				for(Client c : pings.keySet())
					disconnect(c, "Timed out");
				updateClientLists();
				for(Client c : getClients()) {
					PingRequest request = new PingRequest();
					try {
						c.getIO().write(request);
					} catch(IOException e) {
						// thrown if client quits before its first ping and some other stuff
						disconnect(c, "Disconnected");
						continue;
					}
					pings.put(c, request);
				}
				try {
					// wait 10 seconds before pinging again
					Thread.sleep(10000);
				} catch(InterruptedException e) {}
			}
		}
	}
}
