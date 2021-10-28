package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
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
import javax.swing.plaf.ColorUIResource;

import com.talanlabs.avatargenerator.*;
import com.talanlabs.avatargenerator.layers.others.RandomColorPaintLayer;
import java.awt.event.*;

public class ClientRMIGUI extends JFrame implements ActionListener {

	// private static final long serialVersionUID = 1L;
	private JPanel textPanel, inputPanel;
	private JTextField textField;
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
		/*
		 * intercept close method, inform server we are leaving then let the system
		 * exit.
		 */
		frame.setBackground(new Color(44, 47, 51));
		frame.setMinimumSize(new java.awt.Dimension(800, 500));
		// frame.setUndecorated(true);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {

				if (chatClient != null) {
					try {
						sendMessage("Bye all, I am leaving");
						chatClient.serverIF.leaveChat(name);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				System.exit(0);
			}
		});
		// -----------------------------------------
		// remove window buttons and border frame
		// to force user to exit on a button
		// - one way to control the exit behaviour
		// frame.setUndecorated(true);
		// frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);

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

		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setVisible(true);
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
		textPanel.setFont(new Font("Meiryo", Font.PLAIN, 14));
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
		String userStr = "Online Users";
		// set userStr in the center of the label
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
	 * 
	 * @param currClients
	 */
	public void setClientPanel(String[] currClients) {
		clientPanel = new JPanel(new BorderLayout());
		listModel = new DefaultListModel<String>();

		for (String s : currClients) {
			listModel.addElement(s);
		}
		if (currClients.length > 1) {
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
	 * 
	 * @return
	 */
	public JPanel makeButtonPanel() {
		sendButton = new JButton("Send");
		sendButton.addActionListener(this);
		sendButton.setEnabled(false);
		sendButton.setFont(meiryoFont);
		sendButton.setBackground(new Color(58, 237, 151));

		privateMsgButton = new JButton("Send PM");

		if (list.getSelectedValue() == null) {
			privateMsgButton.setEnabled(false);
		}
		privateMsgButton.addActionListener(this);
		privateMsgButton.setBackground(new Color(58, 237, 151));
		startButton = new JButton("Start");
		startButton.addActionListener(this);
		startButton.setBackground(new Color(58, 237, 151));
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
					// delete the file
					file.delete();
				} else {
					JOptionPane.showMessageDialog(frame, "Enter your name to Start");
				}
			}

			// get text and clear textField
			if (e.getSource() == sendButton) {
				message = textField.getText();
				textField.setText("");
				sendMessage(message);
				System.out.println("Sending message : " + message);
			}

			// send a private message, to selected users
			if (e.getSource() == privateMsgButton) {
				int[] privateList = list.getSelectedIndices();
				System.out.println("selected balues " + list.getSelectedValue());
				if (list.getSelectedValue() + "'s console" == frame.getTitle()) {
					JOptionPane.showMessageDialog(frame, "Select a user to send a private message");
				} else {
					for (int i = 0; i < privateList.length; i++) {
						System.out.println("Selected index :" + privateList[i]);
					}
					message = textField.getText();
					textField.setText("");
					sendPrivate(privateList);

				}

			}

		} catch (RemoteException remoteExc) {
			remoteExc.printStackTrace();
		}

	}// end actionPerformed

	// --------------------------------------------------------------------

	/**
	 * Send a message, to be relayed to all chatters
	 */
	private void sendMessage(String chatMessage) throws RemoteException {
		chatClient.serverIF.updateChat(name, chatMessage);
	}

	/**
	 * Send a message, to be relayed, only to selected chatters
	 */
	private void sendPrivate(int[] privateList) throws RemoteException {
		String privateMessage = "[PM from " + name + "] :" + message + "\n";
		chatClient.serverIF.sendPM(privateList, privateMessage);
	}

	/**
	 * Make the connection to the chat server
	 */
	private void getConnected(String userName) throws RemoteException {
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