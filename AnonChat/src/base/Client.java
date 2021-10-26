package base;

import javax.swing.*;

import com.talanlabs.avatargenerator.*;
// import com.talanlabs.avatargenerator.layers.masks.RoundRectMaskLayer;
import com.talanlabs.avatargenerator.layers.others.RandomColorPaintLayer;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class Client extends JPanel {
    BufferedWriter writer;
    static BufferedReader reader;
    static private String key;
    static private String name;
    static JTextArea chatBox = new JTextArea();

    Client(String key, String name) {
        Client.key = key;
        Client.name = name;
    }

    public void display(String key, String name) throws IOException {
        JFrame f1 = new JFrame(name);
        f1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f1.setLayout(new BorderLayout(50, 50));
        f1.setSize(460, 740);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.setBackground(new Color(7, 94, 84));
        topPanel.setPreferredSize(new Dimension(f1.getWidth(), 100));

        long randomLong = Math.round(Math.random() * 12345654321L);
        // generate a random color
        Color randomColor = new Color((int) (Math.random() * 0x1000000));

        Avatar avatar = GitHubAvatar.newAvatarBuilder().layers(new RandomColorPaintLayer()).color(randomColor)
                .size(80, 80).build();
        // , new RoundRectMaskLayer()
        File file = new File("AnonChat\\src\\base\\assets\\test.png");
        avatar.createAsPngToFile(randomLong, file);
        f1.setIconImage(Toolkit.getDefaultToolkit().getImage(file.getAbsolutePath()));

        // SVGImage svgImage1 = new SVGImage();
        // svgImage1.setSvgImage("base/assets/test.png", 100, 100);
        // svgImage1.setHorizontalAlignment(SVGImage.CENTER);
        // svgImage1.setVerticalAlignment(SVGImage.CENTER);
        ImageIcon imageIcon = new ImageIcon("AnonChat\\src\\base\\assets\\test.png");
        JLabel label = new JLabel(imageIcon);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);
        label.setPreferredSize(new Dimension(100, 100));
        label.setMaximumSize(new Dimension(100, 100));
        label.setMinimumSize(new Dimension(100, 100));
        label.setSize(100, 100);
        label.setBackground(new Color(7, 94, 84));
        label.setOpaque(true);
        label.setBorder(BorderFactory.createLineBorder(Color.black));
        topPanel.add(label, BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        southPanel.setBackground(new Color(7, 94, 84));
        southPanel.setLayout(new BorderLayout());
        southPanel.setPreferredSize(new Dimension(f1.getWidth(), f1.getHeight() / 10));

        JTextField messageBox = new JTextField();
        messageBox.setOpaque(false);
        messageBox.requestFocusInWindow();
        messageBox.setPreferredSize(new Dimension(f1.getWidth() - 70, f1.getHeight() / 10 - 70));
        messageBox.setSelectedTextColor(Color.red);
        // enable text wrapping
        chatBox.setLineWrap(true);
        JButton sendMessage = new JButton("Send Message");
        sendMessage.setBackground(new Color(255, 204, 25));
        chatBox.setEditable(false);
        chatBox.setFont(new Font("Serif", Font.PLAIN, 18));
        chatBox.setLineWrap(true);
        chatBox.setWrapStyleWord(true);
        chatBox.setBackground(Color.GRAY);
        mainPanel.add(new JScrollPane(chatBox), BorderLayout.CENTER);

        southPanel.add(messageBox, BorderLayout.CENTER);
        southPanel.add(sendMessage, BorderLayout.EAST);
        mainPanel.add(BorderLayout.NORTH, topPanel);
        mainPanel.add(BorderLayout.SOUTH, southPanel);
        f1.add(mainPanel);
        f1.setLocationRelativeTo(null);
        f1.setVisible(true);
        try {
            Socket socketClient = new Socket("localhost", 2003);
            writer = new BufferedWriter(new OutputStreamWriter(socketClient.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
        } catch (Exception e) {
        }

        f1.setMinimumSize(new Dimension(230, 500));

        sendMessage.addActionListener((ActionEvent e) -> {
            if (messageBox.getText().length() < 1) {
                // do nothing
            } else if (messageBox.getText().equals(".clear")) {
                chatBox.setText("Cleared all messages\n");
                messageBox.setText("");
            } else {
                String str = "<" + name + ">:  " + messageBox.getText() + "\n";
                try {
                    writer.write(str);
                    writer.flush();
                    messageBox.setText("");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            messageBox.requestFocusInWindow();
        });

        // if enter is pressed
        messageBox.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (messageBox.getText().length() < 1) {
                        // do nothing
                    } else if (messageBox.getText().equals(".clear")) {
                        chatBox.setText("Cleared all messages\n");
                        messageBox.setText("");
                    } else {
                        String str = "<" + name + ">:  " + messageBox.getText() + "\n";
                        chatBox.append(str);
                        try {
                            writer.write(str);
                            writer.flush();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        messageBox.setText("");
                    }
                    messageBox.requestFocusInWindow();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(KeyEvent e) {
                // TODO Auto-generated method stub

            }
        });

    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Client client_new = new Client(args[0], args[1]);
                try {
                    client_new.display(args[0], args[1]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Thread t = new Thread(() -> {
                    try {
                        while (true) {
                            String str = reader.readLine();
                            System.out.println(str);
                            if (str.equals("")) {
                            } else {
                                chatBox.append(str + "\n");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                t.start();

            }
        });
        // delete file if the window is closed
        File file = new File("AnonChat\\src\\base\\assets\\test.png");
        file.deleteOnExit();
    }
}