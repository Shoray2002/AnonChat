package server;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.lang.ProcessHandle.Info;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteRef;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.Vector;
import client.ChatClientIF;

import java.security.PublicKey;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;
import java.nio.charset.StandardCharsets;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.Border;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.BorderLayout;

public class ChatServer extends UnicastRemoteObject implements ChatServerIF {

	// Frame frame=new Frame('server');

	String line = "---------------------------------------------\n";
	private Vector<Chatter> chatters;
	private Map<String, PublicKey> publicKeyMap = new HashMap<String, PublicKey>();
	private JFrame frame;
	private JPanel mainPanel;
	private JButton stop;
	private JLabel status;

	// Constructor
	public ChatServer() throws RemoteException, MalformedURLException {
		super();
		chatters = new Vector<Chatter>(10, 1);
		frame = new JFrame("AnonChat Server");
		URL url = new URL("https://i.ibb.co/m58BsMS/imageedit-9-3783505595.png");
		ImageIcon icon = new ImageIcon(url);
		frame.setIconImage(icon.getImage());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(250, 200);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setLayout(new BorderLayout());
		stop = new JButton("<html><FONT COLOR=#404EED><STRONG>Stop Server</STRONG></FONT></html>");
		stop.setBackground(new Color(87, 242, 135));
		stop.setFont(new Font("Meiryo", Font.BOLD, 30));
		status = new JLabel("<html><FONT COLOR=#EEB81F> Server Is Running </FONT></html>");
		status.setFont(new Font("Meiryo", Font.BOLD, 25));
		status.setHorizontalAlignment(JLabel.CENTER);
		status.setVerticalAlignment(JLabel.CENTER);
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(status, BorderLayout.NORTH);
		mainPanel.add(stop, BorderLayout.SOUTH);
		frame.add(mainPanel, BorderLayout.CENTER);
		frame.setVisible(true);
		// handle stop button
		stop.addActionListener(e -> {
			System.exit(0);
		});

	}

	// -----------------------------------------------------------
	/**
	 * LOCAL METHODS
	 */
	public static void main(String[] args) {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
		}

		startRMIRegistry();
		String hostName = "localhost";
		String serviceName = "GroupChatService";

		if (args.length == 2) {
			hostName = args[0];
			serviceName = args[1];
		}

		try {
			ChatServerIF hello = new ChatServer();
			Naming.rebind("rmi://" + hostName + "/" + serviceName, hello);
			System.out.println("Group Chat RMI Server is running...");
		} catch (Exception e) {
			System.out.println("Server had problems starting");
		}
	}

	public static void startRMIRegistry() {
		try {
			java.rmi.registry.LocateRegistry.createRegistry(1099);
			System.out.println("RMI Server ready");

		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	// -----------------------------------------------------------
	/*
	 * REMOTE METHODS
	 */

	/**
	 * Return a message to client
	 */
	public String sayHello(String ClientName) throws RemoteException {
		System.out.println(ClientName + " sent a message");
		return "Hello " + ClientName + " from group chat server";
	}

	/**
	 * Send a string to all connected clients
	 */
	public void updateChat(String name, String nextPost) throws RemoteException {
		String message = name + " : " + nextPost + "\n";
		sendToAll(message);
	}

	/**
	 * Receive a new client remote reference
	 */
	@Override
	public void passIDentity(RemoteRef ref) throws RemoteException {
		// System.out.println("\n" + ref.remoteToString() + "\n");
		try {
			System.out.println(line + ref.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}// end passIDentity

	/**
	 * Receive a new client and display details to the console send on to register
	 * method
	 */
	@Override
	public void registerListener(String[] details) throws RemoteException {
		System.out.println(new Date(System.currentTimeMillis()));
		System.out.println(details[0] + " has joined the chat session");
		System.out.println(details[0] + "'s hostname : " + details[1]);
		System.out.println(details[0] + "'sRMI service : " + details[2]);
		registerChatter(details);
	}

	/**
	 * register the clients interface and store it in a reference for future
	 * messages to be sent to, ie other members messages of the chat session. send a
	 * test message for confirmation / test connection
	 */
	private void registerChatter(String[] details) {
		try {
			ChatClientIF nextClient = (ChatClientIF) Naming.lookup("rmi://" + details[1] + "/" + details[2]);
			chatters.addElement(new Chatter(details[0], nextClient));
			nextClient.messageFromServer("[Server] : Hello " + details[0] + " you are now free to chat.\n");
			sendToAll("[Server] : " + details[0] + " has joined the group.\n");
			addPublicKeys();
			updateClientPublicKeys();
			updateUserList();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update all clients by remotely invoking their updateUserList RMI method
	 */
	private void updateUserList() {
		String[] currentUsers = getUserList();
		for (Chatter c : chatters) {
			try {
				c.getClient().updateUserList(currentUsers);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * generate a String array of current users
	 */
	private String[] getUserList() {
		String[] allUsers = new String[chatters.size()];
		for (int i = 0; i < allUsers.length; i++) {
			allUsers[i] = chatters.elementAt(i).getName();
		}
		return allUsers;
	}

	/**
	 * Send a message to all users
	 */
	public void sendToAll(String newMessage) {
		for (Chatter c : chatters) {
			try {
				c.getClient().messageFromServer(newMessage);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	public void displayMessageFromMap(Map<String, String> messageMap) throws RemoteException {
		for (Chatter c : chatters) {
			try {
				String username = c.getClient().getName();
				for (Map.Entry<String, String> entry : messageMap.entrySet()) {
					if (username.equals(entry.getKey())) {
						String encodedMessage = entry.getValue();
						c.getClient().decryptAndSend(encodedMessage);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void addPublicKeys() throws Exception {
		for (Chatter c : chatters) {
			try {
				String chatterName = c.getClient().getName();
				if (publicKeyMap.get(chatterName) == null) {
					PublicKey publicKey = c.getClient().getPublicKey();
					publicKeyMap.put(chatterName, publicKey);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void updateClientPublicKeys() throws Exception {
		for (Chatter c : chatters) {
			try {
				c.getClient().updatePublicKeyMap(publicKeyMap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * remove a client from the list, notify everyone
	 */
	@Override
	public void leaveChat(String userName) throws RemoteException {

		for (Chatter c : chatters) {
			if (c.getName().equals(userName)) {
				System.out.println(line + userName + " left the chat session");
				System.out.println(new Date(System.currentTimeMillis()));
				chatters.remove(c);
				break;
			}
		}
		if (!chatters.isEmpty()) {
			updateUserList();
		}
	}

	/**
	 * A method to send a private message to selected clients The integer array
	 * holds the indexes (from the chatters vector) of the clients to send the
	 * message to
	 */
	@Override
	public void sendPM(int[] privateGroup, String privateMessage) throws RemoteException {
		Chatter pc;
		for (int i : privateGroup) {
			pc = chatters.elementAt(i);
			pc.getClient().messageFromServer(privateMessage);
		}
	}
}
