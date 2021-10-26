package base;

import java.net.*;
import java.io.*;
import java.util.*;

import Sha512.Sha512;

public class Server implements Runnable {

    Socket skt;
    static String name;
    public static String key;
    public Set<String> users = new HashSet<String>();

    public Server(Socket socket, String key, String name) {
        try {
            this.skt = socket;
            Server.name = name;
            Server.key = Sha512.hashText(key);

        } catch (Exception e) {
        }
    }

    public void run() {
        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(skt.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(skt.getOutputStream()));
            while (true) {
                // send the key
                writer.write(key);
                writer.newLine();
                writer.flush();
                // get the message
                String message = reader.readLine();
                System.out.println(message);
                if (message.equals("exit")) {
                    users.add(name);
                    break;
                }
            }
            
            while (true) {
                System.out.println("start messaging ");
                String data = reader.readLine().trim();
                System.out.println("Received " + data);
                for (int i = 0; i < users.size(); i++) {
                    try {
                        BufferedWriter bw = (BufferedWriter) users.toArray()[i];
                        if (users.toArray()[i] != name) {
                            bw.write(data + "\n");
                            bw.flush();
                        }
                    } catch (Exception e) {
                    }
                }

            }
        } catch (Exception e) {
        }

    }

    public static void main(String[] args) throws Exception {
        ServerSocket s = new ServerSocket(2003);
        while (true) {
            Socket socket = s.accept();
            Server server = new Server(socket, args[0], args[1]);
            // unused thread
            Thread t = new Thread(server);
            if (!t.isAlive()) {
                t.start();
            }
            // Thread thread = new Thread(server);

            // thread.start();
        }
    }
}