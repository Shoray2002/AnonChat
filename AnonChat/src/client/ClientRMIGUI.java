package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.rmi.RemoteException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.Border;

import com.talanlabs.avatargenerator.*;
import com.talanlabs.avatargenerator.layers.others.RandomColorPaintLayer;


import java.util.Map;
import java.util.List;
import java.security.NoSuchAlgorithmException;

public class ClientRMIGUI extends JFrame implements ActionListener {
	private JPanel textPanel, inputPanel;
	 JTextField textField;
	private String name, message;
	private Font meiryoFont = new Font("Meiryo", Font.PLAIN, 14);
	private Border blankBorder = BorderFactory.createEmptyBorder(10, 10, 20, 10);// top,r,b,l
	private ChatClient chatClient;
	private JList<String> list;
	private DefaultListModel<String> listModel;

	protected JTextArea textArea, userArea;
	protected JFrame frame;
	protected JButton privateMsgButton, startButton, sendButton;
	protected JPanel clientPanel, userPanel;

	public static void main(String args[]) {
		// set the look and feel to 'Nimbus'
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
		}
		new ClientRMIGUI();
	}

	/**
	 * GUI Constructor
	 */
	public ClientRMIGUI() {

		frame = new JFrame("AnonChat");
		ImageIcon image = new ImageIcon("AnonChat\\src\\assets\\icon2.png");
		frame.setIconImage(image.getImage());
		frame.setBackground(new Color(44, 47, 51));
		frame.setMinimumSize(new java.awt.Dimension(800, 500));
		// frame.setUndecorated(true);
		
		/*
		 * intercept close method, inform server we are leaving then let the system
		 * exit.
		 */
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {

				if (chatClient != null) {
					try {
						// sendMessageMap(chatClient.encryptMessage("Bye all, I am leaving"));
						sendMessageMap(chatClient.encryptLeaveMessage());
						chatClient.serverIF.leaveChat(name);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				System.exit(0);
			}
		});

		Container c = getContentPane();
		JPanel outerPanel = new JPanel(new BorderLayout());
		outerPanel.setBackground(new Color(255, 255, 255));
		outerPanel.add(getTextPanel(), BorderLayout.CENTER);
		outerPanel.add(getInputPanel(), BorderLayout.SOUTH);

		c.setLayout(new BorderLayout());
		c.add(outerPanel, BorderLayout.CENTER);
		c.add(getUsersPanel(), BorderLayout.EAST);

		frame.add(c);
		frame.pack();
		// frame.setAlwaysOnTop(true);
		frame.setLocation(150, 150);
		textArea.requestFocus();

	
	}

	/**
	 * Method to set up the JPanel to display the chat text
	 */
	public JPanel getTextPanel() {
		String welcome = "Welcome! Enter your name and press Start to begin \n";
		textArea = new JTextArea(welcome, 5, 34);
		textArea.setMargin(new Insets(10, 10, 10, 10));
		textArea.setFont(meiryoFont);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBorder(null);
		textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
		textPanel.add(scrollPane);
		textPanel.setFont(meiryoFont);
		textPanel.setBorder(blankBorder);
		textPanel.setBackground(new Color(114, 137, 218));
		return textPanel;
	}

	/**
	 * Method to build the panel with input field
	 */
	public JPanel getInputPanel() {
		inputPanel = new JPanel(new GridLayout(1, 1, 5, 5));
		inputPanel.setBorder(blankBorder);
		textField = new JTextField();
		textField.setFont(meiryoFont);
		JScrollPane scrollPanel = new JScrollPane(textField);
		inputPanel.add(scrollPanel);
		inputPanel.setBackground(new Color(114, 137, 218));
		return inputPanel;
	}

	/**
	 * Method to build the panel displaying currently connected users with a call to
	 * the button panel building method
	 */
	public JPanel getUsersPanel() {

		userPanel = new JPanel(new BorderLayout());
		String userStr = "<html><FONT COLOR=#E9B018> Online Users </FONT></html>";
		JLabel title = new JLabel(userStr);
		title.setForeground(new Color(35, 39, 42));
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setVerticalAlignment(JLabel.CENTER);
		title.setFont(new Font("Meiryo", Font.BOLD, 25));
		userPanel.add(title, BorderLayout.NORTH);

		String[] noClientsYet = { "No other users" };
		setClientPanel(noClientsYet);

		clientPanel.setFont(meiryoFont);
		clientPanel.setBackground(new Color(114, 137, 218));
		userPanel.add(makeButtonPanel(), BorderLayout.SOUTH);
		userPanel.setBorder(blankBorder);
		userPanel.setBackground(new Color(114, 137, 218));
		return userPanel;
	}

	/**
	 * Populate current user panel with a selectable list of currently connected
	 * users
	 */
	public void setClientPanel(String[] currClients) {
		clientPanel = new JPanel(new BorderLayout());
		listModel = new DefaultListModel<String>();

		for (String s : currClients) {
			listModel.addElement(s);
		}
		if (currClients.length > 2) {
			privateMsgButton.setEnabled(true);
		}

		// Create the list and put it in a scroll pane.
		list = new JList<String>(listModel);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setSelectionBackground(new Color(114, 137, 218));
		list.setSelectionForeground(Color.BLACK);
		list.setVisibleRowCount(8);
		list.setFont(meiryoFont);
		list.setBackground(new Color(114, 100, 255));
		list.setBorder(blankBorder);
		JScrollPane listScrollPane = new JScrollPane(list);
		listScrollPane.setBorder(null);
		clientPanel.add(listScrollPane, BorderLayout.CENTER);
		userPanel.add(clientPanel, BorderLayout.CENTER);
	}

	/**
	 * Make the buttons and add the listener
	 */
	public JPanel makeButtonPanel() {
		sendButton = new JButton("<html><FONT COLOR=#404EED><STRONG>SEND</STRONG></FONT></html>");
		sendButton.addActionListener(this);
		sendButton.setEnabled(false);
		sendButton.setFont(meiryoFont);
		sendButton.setBackground(new Color(87, 242, 135));

		privateMsgButton = new JButton("<html><FONT COLOR=#FFFFFF><STRONG>SEND</STRONG></FONT></html>");

		if (list.getSelectedValue() == null) {
			privateMsgButton.setEnabled(false);
		}
		privateMsgButton.addActionListener(this);
		privateMsgButton.setBackground(new Color(0, 0, 0));
		startButton = new JButton("<html><FONT COLOR=#404EED><STRONG>START</STRONG></FONT></html>");
		startButton.addActionListener(this);
		startButton.setBackground(new Color(87, 242, 135));
		JPanel buttonPanel = new JPanel(new GridLayout(5, 1));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(privateMsgButton);
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(startButton);
		buttonPanel.add(sendButton);
		buttonPanel.setBackground(new Color(114, 137, 218));
		return buttonPanel;
	}

	/**
	 * Action handling on the buttons
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		try {
			// get connected to chat service
			if (e.getSource() == startButton) {
				name = textField.getText();
				// Boolean isValid = true;
				// String[] users = new String[50];
				// for (int i = 0; i < list.getModel().getSize(); i++) {
				// users[i] = list.getModel().getElementAt(i);
				// System.out.println(users[i]);
				// }
				// // check if name is valid
				// for (int i = 0; i < users.length; i++) {
				// if (users[i] != null) {
				// if (users[i].equals(name)) {
				// isValid = false;
				// JOptionPane.showMessageDialog(null, "Name already exists");
				// break;
				// }
				// }
				// }

				if (name.length() != 0) {
					frame.setTitle(name + "'s console ");
					textField.setText("");
					textArea.append("username : " + name + " connecting to chat...\n");
					// frame.setIconImage(image);
					getConnected(name);
					if (!chatClient.connectionProblem) {
						startButton.setEnabled(false);
						sendButton.setEnabled(true);
					}
					File file = new File("AnonChat\\src\\assets\\avatar.png");
					long randomLong = Math.round(Math.random() * 12345654321L);
					Color randomColor = new Color((int) (Math.random() * 0x1000000));
					Avatar avatar = GitHubAvatar.newAvatarBuilder().layers(new RandomColorPaintLayer())
							.color(randomColor).size(80, 80).build();
					avatar.createAsPngToFile(randomLong, file);
					frame.setIconImage(new ImageIcon(file.getAbsolutePath()).getImage());
					file.delete();
					// creates an image and stores in the cache and then deletes it
				} else {
					JOptionPane.showMessageDialog(frame, "Enter your name to Start");
				}
			}

			// get text and clear textField
			if (e.getSource() == sendButton) {
				String text = textField.getText();
				if (text.length() != 0) {
					textField.setText("");
					sendMessageMap(chatClient.encryptMessage(text));
				} else {
					JOptionPane.showMessageDialog(frame, "Enter Message to Send");
				}

			}

			// send a private message, to selected users
			if (e.getSource() == privateMsgButton) {
				if (list.getSelectedValue() + "'s console" == frame.getTitle()) {
					JOptionPane.showMessageDialog(frame, "Select a user to send a private message");
				} else {
					message = textField.getText();
					List<String> usernameList = list.getSelectedValuesList();
					// if the list has current user, remove it
					if (usernameList.contains(name)) {
						usernameList.remove(name);
						JOptionPane.showMessageDialog(frame, "You can't send a private message to yourself");
					}
					textField.setText("");
					for (int i = 0; i < usernameList.size(); i++) {
						String username = usernameList.get(i);
						sendMessageMap(chatClient.encryptPrivateMessage(username, message));
					}

				}

			}

		} catch (

		Exception e2) {
			e2.printStackTrace();
		}

	}

	// --------------------------------------------------------------------

	/**
	 * Send a Base64 encoded RSA encrypted message
	 */

	private void sendMessageMap(Map<String, String> messageMap) throws Exception {
		chatClient.serverIF.displayMessageFromMap(messageMap);
	}

	/**
	 * Make the connection to the chat server
	 */
	private void getConnected(String userName) throws RemoteException, NoSuchAlgorithmException {
		// remove whitespace and non word characters to avoid malformed url
		String cleanedUserName = userName.replaceAll("\\s+", "_");
		cleanedUserName = userName.replaceAll("\\W+", "_");
		try {
			chatClient = new ChatClient(this, cleanedUserName);
			chatClient.startClient();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}
