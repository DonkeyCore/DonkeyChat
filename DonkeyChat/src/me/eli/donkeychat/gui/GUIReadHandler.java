package me.eli.donkeychat.gui;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import me.eli.donkeychat.io.StringReadListener;

public class GUIReadHandler {
	
	private final List<Thread> threads = new ArrayList<>();
	private final List<Future<String>> readers = new ArrayList<>();
	private boolean running = true;
	private final ExecutorService executor = Executors.newFixedThreadPool(10);
	private final StringReadListener onRead;
	
	public GUIReadHandler(StringReadListener onRead) {
		this.onRead = onRead;
	}
	
	public void stop() {
		running = false;
		for(Future<String> f : readers) {
			if (f != null)
				f.cancel(true);
		}
		for(Thread t : threads) {
			if (t != null)
				t.interrupt();
		}
	}
	
	public void readFrom(BufferedReader r) {
		Thread t = new Thread(() -> {
			try {
				do {
					Future<String> f = executor.submit(() -> r.readLine());
					readers.add(f);
					onRead.onRead(f.get());
					readers.remove(f);
				} while(running);
			} catch(Exception e) {}
		});threads.add(t);t.start();
}}
