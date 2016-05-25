package me.eli.donkeychat;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;

import me.eli.donkeychat.gui.StartScreen;

public class Chatroom {

	// Prime number and generator are from RFC 5114 Section 2.3
	public static final BigInteger GENERATOR = new BigInteger("8041367327046189302693984665026706374844608289874374425728797669509435881459140662650215832833471328470334064628508692231999401840332046192569287351991689963279656892562484773278584208040987631569628520464069532361274047374444344996651832979378318849943741662110395995778429270819222431610927356005913836932462099770076239554042855287138026806960470277326229482818003962004453764400995790974042663675692120758726145869061236443893509136147942414445551848162391468541444355707785697825741856849161233887307017428371823608125699892904960841221593344499088996021883972185241854777608212592397013510086894908468466292313");
	public static final BigInteger PRIME = new BigInteger("17125458317614137930196041979257577826408832324037508573393292981642667139747621778802438775238728592968344613589379932348475613503476932163166973813218698343816463289144185362912602522540494983090531497232965829536524507269848825658311420299335922295709743267508322525966773950394919257576842038771632742044142471053509850123605883815857162666917775193496157372656195558305727009891276006514000409365877218171388319923896309377791762590614311849642961380224851940460421710449368927252974870395873936387909672274883295377481008150475878590270591798350563488168080923804611822387520198054002990623911454389104774092183");
	
	public static void main(String... args) throws IOException {
		new StartScreen();
		
		/*args = new String[]{isServerRunning(4242) ? "localhost" : "-s"};
		Flags f;
		try {
			f = parseFlags(args);
		} catch(IllegalArgumentException e) {
			System.out.println(e.getMessage());
			return;
		}
		if (f.server)
			new ChatServer(f.port);
		else
			new Client(f.ip, f.port);*/
	}
	
	public static boolean isServerRunning(int port) {
		try {
			new ServerSocket(port).close();
		} catch(IOException t) {
			return true;
		}
		return false;
	}
	
	public static Flags parseFlags(String... args) throws IllegalArgumentException {
		if (args.length < 1)
			return new Flags();
		boolean server = false;
		boolean portComingUp = false;
		String ip = null;
		int port = 4242;
		for(String a : args) {
			if (a.length() == 0)
				continue;
			else if (portComingUp) {
				try {
					port = Integer.parseInt(a);
					if (port < 0 || port > 65535)
						throw new NumberFormatException("" + port);
				} catch(NumberFormatException e) {
					throw new IllegalArgumentException("Not a valid port: " + a);
				}
			} else if (a.equals("-s") || a.equals("--server"))
				server = true;
			else if (a.equals("-p") || a.equalsIgnoreCase("--port"))
				portComingUp = true;
			else if (ip == null)
				ip = a;
			else {
				String illegal = a;
				if (a.startsWith("--"))
					illegal = a.substring(2);
				else if (a.startsWith("-"))
					illegal = a.substring(1);
				throw new IllegalArgumentException("Unknown flag: " + illegal);
			}
		}
		return new Flags(server, ip, port);
	}
	
	protected static final class Flags {
		
		public final boolean server;
		public final String ip;
		public final int port;
		
		public Flags() {
			this(false, null, 4242);
		}
		
		public Flags(boolean server, String ip, int port) {
			if (server && ip != null)
				throw new IllegalArgumentException("IPs aren't used with servers");
			this.server = server;
			this.ip = ip;
			this.port = port;
		}
	}
}
