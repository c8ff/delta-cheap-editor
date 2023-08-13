package dev.seeight.dtceditor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Server {
	public static ServerThread serverThread;

	public static void startServerThread(DeltaCheapEditor editor) {
		if (serverThread != null && serverThread.isAlive()) {
			throw new RuntimeException("Server Thread is already on.");
		}

		serverThread = new ServerThread(new Server());
		serverThread.start();
	}

	public static void shutdownServerThread() throws IOException {
		if (serverThread == null || !serverThread.isAlive()) {
			throw new RuntimeException("Server Thread is already off.");
		}

		serverThread.server.shutdown();
	}

	public static class ServerThread extends Thread {
		public final Server server;

		public ServerThread(Server server) {
			this.server = server;
		}

		@Override
		public void run() {
			try {
				this.server.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private ServerSocket serverSocket;
	private ClientHandler singleClientHandler;

	public void start() {
		try {
			serverSocket = new ServerSocket(4938);
			System.out.println("Server listening on port " + 4938);

			while (true) {
				// Wait for a client to connect
				Socket clientSocket;

				try {
					clientSocket = serverSocket.accept();
				} catch (SocketException e) {
					if (!e.getMessage().equals("Socket closed")) {
						e.printStackTrace();
					}
					break;
				}

				if (singleClientHandler != null && singleClientHandler.isAlive()) {
					clientSocket.getOutputStream().write("disconnect\n".getBytes(StandardCharsets.UTF_8));
					clientSocket.close();
					continue;
				}

				singleClientHandler = new ClientHandler(clientSocket);
				singleClientHandler.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(String msg, String expectedResponse) {
		if (singleClientHandler == null || !singleClientHandler.isAlive()) {
			System.out.println("There aren't any clients.");
			return;
		}

		try {
			ClientHandler thread = singleClientHandler;
			thread.sendMessage(msg);
			String s = thread.waitForMessage(5000L);
			if (!Objects.equals(s, expectedResponse)) {
				System.out.println("Response was '" + s + "' and not the expected '" + expectedResponse + "'.");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() throws IOException {
		sendMessage("disconnect", "OK");
		synchronized (this) {
			serverSocket.close();
		}
	}

	private static class ClientHandler extends Thread {
		private final Socket client;
		private PrintWriter output;

		private final Object lock = new Object();
		private boolean waitingForMessage;
		private String message;

		public ClientHandler(Socket socket) {
			this.client = socket;
		}

		@Override
		public void run() {
			try {
				output = new PrintWriter(client.getOutputStream(), true);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			try (BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));) {
				String l;
				while ((l = input.readLine()) != null) {
					if (l.equals("disconnect")) {
						System.out.println("Disconnecting!");
						output.println("OK");
						break;
					} else {
						synchronized (this) {
							if (waitingForMessage) {
								message = l;
								synchronized (lock) {
									lock.notify();
								}
								waitingForMessage = false;
							}
						}
						System.out.println("Client response: " + l);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			System.out.println("Client closed. Bruh moment.");
		}

		public synchronized void sendMessage(String msg) {
			output.println(msg);
		}

		public String waitForMessage(long timeout) throws InterruptedException {
			waitingForMessage = true;
			message = null;
			synchronized (lock) {
				lock.wait(timeout);
			}
			return message;
		}
	}
}
