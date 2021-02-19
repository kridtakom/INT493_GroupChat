package com.company;

import java.util.*;
import java.net.*;
import java.io.*;

public class Client {

    private static Socket socket = null;
    private static DataInputStream input = null;
    private static DataOutputStream output = null;

    private static Write_Messages write_messages;
    private static Read_Messages read_messages;
    private static Thread write_message_thread;
    private static Thread read_message_thread;

    public static boolean alive = true;

    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);
        InetAddress ip = null;
        try {
            ip = InetAddress.getByName("localhost"); // Localhost
            System.out.print("Enter your name : ");
            String name = scanner.nextLine();
            socket = new Socket(ip, 8080);
            try {
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                output.writeUTF(name);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error sending username to server");
            }

            write_messages = new Write_Messages(output);
            read_messages = new Read_Messages(input);

            write_message_thread = new Thread(write_messages);
            read_message_thread = new Thread(read_messages);

            write_message_thread.start();
            read_message_thread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isAlive() {
        return alive;
    }

    public static void setAlive(boolean alive) {
        Client.alive = alive;
    }
}

class Write_Messages implements Runnable {

    private static DataOutputStream output = null;

    public Write_Messages (DataOutputStream output) {
        this.output = output;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line_to_send = scanner.nextLine();
            if (line_to_send.equals("exit")) {
                Client.setAlive(false);
            }
            try {
                output.writeUTF(line_to_send);
            } catch (IOException e) {
                System.out.println("An error occurred while trying to send '" + line_to_send + "' to the SERVER");
                e.printStackTrace();
            }
        }
    }
}

class Read_Messages implements Runnable {
    private static DataInputStream input = null;

    public Read_Messages(DataInputStream input) {
        this.input = input;
    }

    @Override
    public void run() {

        while (true) {
            if (!Client.isAlive()) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            } else {
                try {
                    String line_to_read = input.readUTF();
                    System.out.println(line_to_read);
                } catch (IOException e) {
                    System.out.println("An error occurred while trying to read from the SERVER");
                    e.printStackTrace();
                }
            }
        }
    }
}
