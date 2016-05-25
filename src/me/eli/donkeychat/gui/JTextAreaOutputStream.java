package me.eli.donkeychat.gui;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;

public class JTextAreaOutputStream extends OutputStream {
	
	private final JTextArea out;
	
	public JTextAreaOutputStream(final JTextArea out) {
		this.out = out;
	}
	
	@Override
	public void write(int b) throws IOException {
		out.setText(out.getText() + ((char) b));
	}
	
	public void clear() {
		out.setText("");
	}
	
}
