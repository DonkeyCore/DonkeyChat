package me.eli.donkeychat.gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.Closeable;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

public class GUI extends JFrame implements WindowListener {
	
	private static final long serialVersionUID = 9136030901992245274L;
	public static final String NAME = "Chat";
	public static final int WIDTH = 600;
	public static final int HEIGHT = 600;
	private final JTextAreaInputStream in;
	private final JTextAreaOutputStream out;
	private final JTextArea clientList;
	private final GUIHandler handler;
	private boolean disposed = false;
	
	public GUI() throws IOException {
		setName(NAME);
		setTitle(NAME);
		setSize(WIDTH, HEIGHT);
		setLocationRelativeTo(null);
		addWindowListener(this);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Throwable t) {
			t.printStackTrace();
		}
		JSplitPane main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JSplitPane chat = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JTextArea area = new JTextArea(5, 0);
		area.setEditable(false);
		this.out = new JTextAreaOutputStream(area);
		chat.setTopComponent(new JScrollPane(area));
		JTextArea input = new JTextArea(2, 0);
		this.in = new JTextAreaInputStream(input);
		chat.setBottomComponent(input);
		main.setLeftComponent(chat);
		this.handler = new GUIHandler(in, out);
		this.clientList = new JTextArea(0, 16);
		clientList.setEditable(false);
		main.setRightComponent(new JScrollPane(clientList));
		chat.setResizeWeight(1);
		main.setResizeWeight(1);
		add(main);
		setVisible(true);
	}
	
	public GUIHandler getHandler() {
		return handler;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		disposed = true;
	}
	
	public JTextAreaInputStream getIn() {
		if (disposed)
			throw new IllegalStateException();
		return in;
	}
	
	public JTextAreaOutputStream getOut() {
		if (disposed)
			throw new IllegalStateException();
		return out;
	}
	
	public final void setClients(final String[] clients) {
		clientList.setText("");
		for(String c : clients)
			clientList.setText(clientList.getText() + c + "\n");
	}
	
	private Closeable c = null;
	
	public void setOnClose(Closeable c) {
		this.c = c;
	}
	
	@Override
	public void windowActivated(WindowEvent e) {}
	
	@Override
	public void windowClosed(WindowEvent e) {}
	
	@Override
	public void windowClosing(WindowEvent e) {
		if (c != null) {
			try {
				c.close();
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	@Override
	public void windowDeactivated(WindowEvent e) {}
	
	@Override
	public void windowDeiconified(WindowEvent e) {}
	
	@Override
	public void windowIconified(WindowEvent e) {}
	
	@Override
	public void windowOpened(WindowEvent e) {}
}
