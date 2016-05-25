package me.eli.donkeychat.gui;

import java.awt.GridLayout;
import java.io.IOException;
import java.net.SocketException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import me.eli.donkeychat.Chatroom;
import me.eli.donkeychat.client.Client;
import me.eli.donkeychat.server.ChatServer;

public class StartScreen extends JFrame {
	
	private static final long serialVersionUID = 6484826960417406372L;
	
	public static final int WIDTH = 400;
	public static final int HEIGHT = 200;
	
	public StartScreen() {
		setName("DonkeyChat v0.1");
		setTitle("DonkeyChat v0.1");
		setSize(WIDTH, HEIGHT);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Throwable t) {
			t.printStackTrace();
		}
		JPanel p = new JPanel();
		p.setBorder(new EmptyBorder(10, 10, 10, 10));
		p.setLayout(new GridLayout(3, 0, 10, 10));
		JButton server = new JButton("Host Chatroom");
		server.addActionListener(event -> {
			boolean invalid = false;
			String msg = "Please enter a port for the server. (Default = 4242)";
			while(true) {
				String in = JOptionPane.showInputDialog(this, msg, "Host Chatroom", JOptionPane.PLAIN_MESSAGE);
				if(in == null)
					break;
				try {
					int port = 4242;
					if(!in.trim().isEmpty())
						port = Integer.parseInt(in);
					if(Chatroom.isServerRunning(port))
						throw new IllegalStateException();
					try {
						dispose();
						new ChatServer(port);
						break;
					} catch(IOException e) {
						e.printStackTrace();
					}
					break;
				} catch(IllegalArgumentException | IllegalStateException e) {
					if(!invalid)
						msg = "Invalid port or port is busy. " + msg;
					invalid = true;
				}
			}
		});
		JButton client = new JButton("Join Chatroom");
		client.addActionListener(event -> {
			boolean invalid = false;
			String msg = "Please enter a hostname and port to connect. (Default = localhost:4242)";
			while(true) {
				String s = JOptionPane.showInputDialog(this, msg, "Join Chatroom", JOptionPane.PLAIN_MESSAGE);
				if(s == null)
					break;
				try {
					String host = "localhost";
					String portString;
					if(s.contains(":")) {
						host = s.split(":")[0];
						portString = s.split(":")[1];
					} else
						portString = s;
					final String hostFinal = host;
					int _port = 4242;
					if(!portString.trim().isEmpty())
						_port = Integer.parseInt(portString);
					final int port = _port;
					new Thread(() -> {
						try {
							dispose();
							new Client(hostFinal, port);
						} catch(SocketException e) {
							JOptionPane.showMessageDialog(this, "The specified server isn't online.", "Could not connect", JOptionPane.ERROR_MESSAGE);
							new StartScreen();
						} catch(IOException e) {
							e.printStackTrace();
						}
					}).start();
					break;
				} catch(Exception e) {
					if(!invalid)
						msg = "Invalid hostname/port combination. " + msg;
					invalid = true;
				}
			}
		});
		JButton about = new JButton("About");
		about.addActionListener(event-> {
			JOptionPane.showMessageDialog(this, "A very simple chat client that I created to allow for connections to be made through the\ninternet so that people can chat with each other. It has an API (somewhat) that still\nneeds work. Not anything serious or for commercial use.\n\nCopyright © Eli Blaney 2016.", "About DonkeyChat v0.1", JOptionPane.INFORMATION_MESSAGE);
		});
		p.add(server);
		p.add(client);
		p.add(about);
		add(p);
		setVisible(true);
	}
	
}
