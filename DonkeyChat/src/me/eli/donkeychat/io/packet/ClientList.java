package me.eli.donkeychat.io.packet;

public class ClientList implements Packet {
	
	private static final long serialVersionUID = -1712321637443159903L;
	private final String[] clients;
	
	public ClientList(String... clients) {
		this.clients = clients;
	}
	
	public String[] getClients() {
		return clients;
	}
	
}
