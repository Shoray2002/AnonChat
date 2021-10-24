package base;

import javax.swing.*;

import com.talanlabs.avatargenerator.*;
import com.talanlabs.avatargenerator.layers.masks.RoundRectMaskLayer;
import com.talanlabs.avatargenerator.layers.others.RandomColorPaintLayer;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class Client extends JPanel {
    BufferedWriter writer;
    BufferedReader reader;
    private String key;
    private String name;

    Client(String key, String name) {
        this.key = key;
        this.name = name;
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

        Avatar avatar = GitHubAvatar.newAvatarBuilder().layers(new RandomColorPaintLayer(), new RoundRectMaskLayer())
                .margin(20).padding(20).color(randomColor).build();

        File file = new File("AnonChat\\src\\base\\assets\\test.png");
        avatar.createAsPngToFile(randomLong, file);
        avatar.getHeight();
        System.out.println(avatar.getHeight());

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
        JButton sendMessage = new JButton("Send Message");
        sendMessage.setBackground(new Color(255, 204, 25));
        JTextArea chatBox = new JTextArea();
        chatBox.setEditable(false);
        chatBox.setFont(new Font("Serif", Font.PLAIN, 18));
        chatBox.setLineWrap(true);
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

        // f1.setMinimumSize(new Dimension(230, 500));
        // JPanel p1 = new JPanel();
        // JPanel p2 = new JPanel();
        // p1.setBackground(new Color(7, 94, 84));
        // p1.setBounds(0, 0, f1.getWidth(), 70);
        // p2.setBounds(0, 70, f1.getWidth(), f1.getHeight() - 70);
        // p1.setSize(f1.getWidth(), 70);
        // p2.setSize(f1.getWidth(), f1.getHeight() - 70);
        // f1.add(p1);
        // f1.add(p2);

        sendMessage.addActionListener((ActionEvent e) -> {
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
                    // writer.write("\r\n");
                    writer.flush();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                messageBox.setText("");
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
                        chatBox.append("<" + name + ">:  " + messageBox.getText() + "\n");
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
            }
        });
        // delete file if the window is closed
        File file = new File("AnonChat\\src\\base\\assets\\test.png");
        file.deleteOnExit();
    }
}