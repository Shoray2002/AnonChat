package client;

import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.JOptionPane;

import server.ChatServerIF;

import java.util.Base64;
import java.util.Map;
import java.util.HashMap;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;

public class ChatClient extends UnicastRemoteObject implements ChatClientIF {

	// private static final long serialVersionUID = 7468891722773409712L;
	ClientRMIGUI chatGUI;
	private String hostName = "localhost";
	private String serviceName = "GroupChatService";
	private String clientServiceName;
	private String name;
	protected ChatServerIF serverIF;
	protected boolean connectionProblem = false;
	private PrivateKey privateKey;
	public PublicKey publicKey;
	public Map<String, PublicKey> chatterPublicKeyMap;

	/**
	 * class constructor
	 */
	public ChatClient(ClientRMIGUI aChatGUI, String userName) throws RemoteException, NoSuchAlgorithmException {
		super();
		this.chatGUI = aChatGUI;
		this.name = userName;
		this.clientServiceName = "ClientListenService_" + userName;
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(1024);
		KeyPair pair = generator.generateKeyPair();
		this.privateKey = pair.getPrivate();
		this.publicKey = pair.getPublic();
	}

	/**
	 * Register our own listening service/interface lookup the server RMI interface,
	 * then send our details
	 * 
	 * @throws RemoteException
	 */
	public void startClient() throws RemoteException {
		String[] details = { name, hostName, clientServiceName };

		try {
			Naming.rebind("rmi://" + hostName + "/" + clientServiceName, this);
			serverIF = (ChatServerIF) Naming.lookup("rmi://" + hostName + "/" + serviceName);
		} catch (ConnectException e) {
			JOptionPane.showMessageDialog(chatGUI.frame, "The server seems to be unavailable\nPlease try later",
					"Connection problem", JOptionPane.ERROR_MESSAGE);
			connectionProblem = true;
			e.printStackTrace();
		} catch (NotBoundException | MalformedURLException me) {
			connectionProblem = true;
			me.printStackTrace();
		}
		if (!connectionProblem) {
			registerWithServer(details);
		}
		System.out.println("Client Listen RMI Server is running...\n");
	}

	/**
	 * pass our username, hostname and RMI service name to the server to register
	 * out interest in joining the chat
	 * 
	 * @param details
	 */
	public void registerWithServer(String[] details) {
		try {
			serverIF.passIDentity(this.ref);
			serverIF.registerListener(details);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Receive a string from the chat server this is the clients RMI method, which
	 * will be used by the server to send messages to us
	 */
	@Override
	public void messageFromServer(String message) throws RemoteException {
		System.out.println(message);
		chatGUI.textArea.append(message);
		//scroll to bottom
		chatGUI.textArea.setCaretPosition(chatGUI.textArea.getDocument().getLength());
	}

	/**
	 * A method to update the display of users currently connected to the server
	 */
	@Override
	public void updateUserList(String[] currentUsers) throws RemoteException {

		if (currentUsers.length < 3) {
			chatGUI.privateMsgButton.setEnabled(false);
		}
		chatGUI.userPanel.remove(chatGUI.clientPanel);
		chatGUI.setClientPanel(currentUsers);
		chatGUI.clientPanel.repaint();
		chatGUI.clientPanel.revalidate();
	}

	/**
	 * returns the public key
	 */
	@Override
	public PublicKey getPublicKey() throws RemoteException, NoSuchAlgorithmException {

		return this.publicKey;
	}

	/**
	 * A method to decrypt the Base64 encoded RSA encrypted message and show it in
	 * the GUI
	 */
	@Override
	public void decryptAndSend(String encodedMessage) throws Exception {
		Cipher decryptCipher = Cipher.getInstance("RSA");
		decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
		String decryptedMessage = new String(decryptCipher.doFinal(Base64.getDecoder().decode(encodedMessage)),
				StandardCharsets.UTF_8);
		chatGUI.textArea.append(decryptedMessage);
	}

	/**
	 * returns the name
	 */
	@Override
	public String getName() throws RemoteException {
		return this.name;
	}

	/**
	 * A method to update username to public key map when a new user joins the chat
	 * room
	 */
	@Override
	public void updatePublicKeyMap(Map<String, PublicKey> publicKeyMap) throws RemoteException {
		this.chatterPublicKeyMap = publicKeyMap;
	}

	/**
	 * A method to encrypt the message with public key of all clients in the chat
	 * room and return username, encrypted message map
	 */
	@Override
	public Map<String, String> encryptMessage(String message) throws Exception {
		Map<String, String> messageMap = new HashMap<String, String>();
		message = "[" + this.name + "] : " + message + "\n";
		for (Map.Entry<String, PublicKey> entry : chatterPublicKeyMap.entrySet()) {
			Cipher encryptCipher = Cipher.getInstance("RSA");
			encryptCipher.init(Cipher.ENCRYPT_MODE, entry.getValue());
			String encodedMessage = Base64.getEncoder()
					.encodeToString(encryptCipher.doFinal(message.getBytes(StandardCharsets.UTF_8)));
			messageMap.put(entry.getKey(), encodedMessage);
		}
		return messageMap;
	}

	/**
	 * A method to encrypt the message with public key of only selected clients (for
	 * Private Messaging) and return username, encrypted message map
	 */
	@Override
	public Map<String, String> encryptPrivateMessage(String username, String message) throws Exception {
		Map<String, String> messageMap = new HashMap<String, String>();
		String receiverMessage = "[PM from " + this.name + "] : " + message + "\n";
		String senderMessage = "[PM to " + username + "] : " + message + "\n";
		for (Map.Entry<String, PublicKey> entry : chatterPublicKeyMap.entrySet()) {
			if (username.equals(entry.getKey())) {
				Cipher encryptCipher = Cipher.getInstance("RSA");
				encryptCipher.init(Cipher.ENCRYPT_MODE, entry.getValue());
				String encodedMessage = Base64.getEncoder()
						.encodeToString(encryptCipher.doFinal(receiverMessage.getBytes(StandardCharsets.UTF_8)));
				messageMap.put(username, encodedMessage);
			} else if (this.name.equals(entry.getKey())) {
				Cipher encryptCipher = Cipher.getInstance("RSA");
				encryptCipher.init(Cipher.ENCRYPT_MODE, entry.getValue());
				String encodedMessage = Base64.getEncoder()
						.encodeToString(encryptCipher.doFinal(senderMessage.getBytes(StandardCharsets.UTF_8)));
				messageMap.put(this.name, encodedMessage);
			}
		}
		return messageMap;
	}

	/**
	 * A method to encrypt leave message with public keys of all users in chat room
	 * and return the username to encrypted message map
	 */
	@Override
	public Map<String, String> encryptLeaveMessage() throws Exception {
		Map<String, String> messageMap = new HashMap<String, String>();
		String message = this.name + " has left the chat.\n";
		for (Map.Entry<String, PublicKey> entry : chatterPublicKeyMap.entrySet()) {
			Cipher encryptCipher = Cipher.getInstance("RSA");
			encryptCipher.init(Cipher.ENCRYPT_MODE, entry.getValue());
			String encodedMessage = Base64.getEncoder()
					.encodeToString(encryptCipher.doFinal(message.getBytes(StandardCharsets.UTF_8)));
			messageMap.put(entry.getKey(), encodedMessage);
		}
		return messageMap;
	}
}
