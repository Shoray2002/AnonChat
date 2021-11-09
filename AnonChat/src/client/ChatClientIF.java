package client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public interface ChatClientIF extends Remote{

	public void messageFromServer(String message) throws RemoteException;

	public void updateUserList(String[] currentUsers) throws RemoteException;

	public PublicKey getPublicKey() throws RemoteException,NoSuchAlgorithmException;

	public String getName() throws RemoteException;

	public void updatePublicKeyMap(Map <String,PublicKey> publicKeyMap) throws RemoteException;
	
	public void decryptAndSend(String encodedMessage) throws Exception;

	public Map<String,String> encryptMessage(String message) throws Exception;

	public Map<String,String> encryptPrivateMessage(String username, String message) throws Exception;

	public Map<String,String> encryptLeaveMessage() throws Exception;
}
