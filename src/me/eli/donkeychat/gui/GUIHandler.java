package me.eli.donkeychat.gui;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class GUIHandler implements Closeable {
	
	private PrintWriter out;
	private BufferedReader in;
	
	public GUIHandler(JTextAreaInputStream in, JTextAreaOutputStream out) throws IOException {
		if (in == null || out == null)
			throw new IllegalArgumentException("Arguments cannot be null");
		this.out = new PrintWriter(out, true);
		this.in = new BufferedReader(new InputStreamReader(in));
	}
	
	public void println(String str) {
		out.println(str);
	}
	
	public String readLine() throws IOException {
		return in.readLine();
	}
	
	public PrintWriter getOut() {
		return out;
	}
	
	public BufferedReader getIn() {
		return in;
	}
	
	@Override
	public void close() throws IOException {
		in.close();
		in = null;
		out.close();
		out = null;
	}
	
	public boolean isClosed() {
		return in == null && out == null;
	}
}
