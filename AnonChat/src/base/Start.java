package base;

import Sha512.Sha512;
import javax.swing.*;

import SVG.SVGImage;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Random;

public class Start extends JPanel {
    String key, name;
    public String saltStrgen() {
        this.setSize(new Dimension(250, 150));
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890*/-$%&";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 10) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }

    public void display(String saltStr) throws IOException {
        JFrame frame = new JFrame("AnonChat");
        frame.setVisible(false);
        //frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(0, 1, 20, 25));
        frame.setSize(300, 450);
        JPanel panel_main = new JPanel();
        JPanel panel_get_code = new JPanel();
        JPanel panel_set_code = new JPanel();
        JButton button_get_code = new JButton("Get Code");
        JButton button_set_code = new JButton("Enter Code");
        JButton submit1 = new JButton("Enter");
        JButton submit2 = new JButton("Enter");
        JButton back_to_main = new JButton("Back");
        JLabel get_code_label = new JLabel("AnonChat Code");
        JLabel set_code_label = new JLabel("Enter AnonChat Code");
        JTextField tf1 = new JTextField();
        JLabel l2 = new JLabel("Enter Name");
        JTextField tf2 = new JTextField();
        tf2.setFont(new Font("Arial", Font.BOLD, 20));
        tf2.setHorizontalAlignment(JTextField.CENTER);
        tf2.setSize(200, 50);
        tf2.setText("");
        tf2.setEditable(true);
        JTextField tf3 = new JTextField();
        tf1.setFont(new Font("Arial", Font.BOLD, 20));
        tf1.setHorizontalAlignment(JTextField.CENTER);
        l2.setFont(new Font("Arial", Font.BOLD, 20));
        l2.setHorizontalAlignment(JLabel.CENTER);
        l2.setSize(200, 50);
        tf3.setFont(new Font("Arial", Font.BOLD, 20));
        tf3.setHorizontalAlignment(JTextField.CENTER);
        tf3.setSize(200, 50);
        tf3.setText("");
        tf3.setEditable(true);

        SVGImage svgImage1 = new SVGImage();
        svgImage1.setSvgImage("base/assets/1.svg",100,100);
        svgImage1.setHorizontalAlignment(SVGImage.CENTER);
        svgImage1.setVerticalAlignment(SVGImage.CENTER);
        panel_main.add(svgImage1);



        // panel main
        panel_main.setLayout(new GridLayout(0, 1, 20, 25));
        panel_main.add(button_get_code);
        panel_main.add(button_set_code);
        frame.add(panel_main);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        // action handlers
        button_get_code.addActionListener((ActionEvent e) -> {
            frame.remove(panel_main);
            panel_get_code.setLayout(new GridLayout(0, 1, 20, 25));
            back_to_main.setSize(200, 50);
            panel_get_code.add(back_to_main);
            get_code_label.setFont(new Font("Arial", Font.BOLD, 20));
            get_code_label.setHorizontalAlignment(JLabel.CENTER);
            get_code_label.setSize(200, 50);
            panel_get_code.add(get_code_label);
            tf1.setEditable(false);
            tf1.setText(saltStr);
            panel_get_code.add(tf1);
            panel_get_code.add(l2);
            panel_get_code.add(tf3);
            submit1.setFont(new Font("Arial", Font.BOLD, 20));
            submit1.setHorizontalAlignment(JButton.CENTER);
            submit1.setSize(200, 50);
            submit1.setText("Enter");
            submit1.setVisible(true);
            panel_get_code.add(submit1);
            frame.add(panel_get_code);
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);
            frame.repaint();
        });
        button_set_code.addActionListener((ActionEvent e) -> {
            frame.remove(panel_main);
            panel_set_code.setLayout(new GridLayout(0, 1, 20, 25));
            back_to_main.setSize(200, 50);
            panel_set_code.add(back_to_main);
            set_code_label.setFont(new Font("Arial", Font.BOLD, 20));
            set_code_label.setHorizontalAlignment(JLabel.CENTER);
            set_code_label.setSize(200, 50);
            panel_set_code.add(set_code_label);
            tf2.setEditable(true);
            tf2.setText("");
            panel_set_code.add(tf2);
            panel_set_code.add(l2);
            panel_set_code.add(tf3);
            submit2.setFont(new Font("Arial", Font.BOLD, 20));
            submit2.setHorizontalAlignment(JButton.CENTER);
            submit2.setText("Enter");
            submit2.setVisible(true);
            panel_set_code.add(submit2);
            frame.add(panel_set_code);
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);
            frame.repaint();
        });
        submit1.addActionListener((ActionEvent e) -> {
            
            if (!tf3.getText().equals("")) {
                name = tf3.getText();
            } else {
                name = "Anon";
            }
            try {
                key = Sha512.hashText(tf1.getText());
                System.out.println(key);
                Client.main(new String[]{key, name});
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            // frame.remove(panel_get_code);
            // frame.add(panel_main);
            // frame.repaint();
            frame.dispose();
        });
        submit2.addActionListener((ActionEvent e) -> {
            try {
                System.out.println(Sha512.hashText(tf2.getText()));
                if (key.equals(Sha512.hashText(tf2.getText()))) {
                    System.out.println("Success");
                } else {
                    System.out.println("Failed");
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            if (!tf3.getText().equals("")) {
                name = tf3.getText();
            } else {
                name = "Anon";
            }
            frame.remove(panel_set_code);
            frame.add(panel_main);
            frame.repaint();
        });
        back_to_main.addActionListener((ActionEvent e) -> {
            frame.remove(panel_get_code);
            frame.remove(panel_set_code);
            frame.add(panel_main);
            frame.repaint();
        });
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Start s = new Start();
                try {
                    s.display(s.saltStrgen());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}