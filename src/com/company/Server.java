package com.company;

import java.util.*;
import java.net.*;
import java.io.*;

public class Server {

    private static Socket socket = null;
    private static ServerSocket server = null;
    private static DataInputStream input = null;
    private static DataOutputStream output = null;
    private static Thread clientThread;
    private static ClientThread client;

    public static ArrayList<ClientThread> client_list = new ArrayList<ClientThread>();

    public static int client_id = 0;

    public static void main(String args[]) throws IOException {


        System.out.println("Server has started");
        System.out.println("Waiting for Clients...");
        try {
            server = new ServerSocket(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            socket = null;
            try {
                socket = server.accept();
                System.out.println("New Client has Connected to Server");

                System.out.println("Client : " + socket);

                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());

                client = new ClientThread(socket, input, output, client_id);
                clientThread = new Thread(client);
                client_list.add(client);
                clientThread.start();
                messageAll("!!!!Hello there, a new user entered, with name ===> " + client.getName());
            } catch (IOException e) {
                socket.close();
                for (int i = 0; i < client_list.size(); i++) {
                    ClientThread client_to_close = client_list.get(i);
                    client_to_close.socket.close();
                    client_to_close.output.close();
                    client_to_close.input.close();
                }
                e.printStackTrace();
                System.out.println("There was an unexpected error");
            }
            client_id++;
        }
    }

    public static synchronized void disconnect_from_Server(int user_id) {
        ClientThread client_to_close = client_list.get(user_id);
        try {
            client_to_close.output.writeUTF("Disconnected From SERVER! : " + client_to_close.getName());
            client_to_close.input.close();
            client_to_close.output.close();
            client_to_close.socket.close();
            client_list.remove(user_id);
            for (int i = 0; i < client_list.size(); i++) {
                ClientThread new_id_for_client = client_list.get(i);
                System.out.println("Client THREAD IS : " + new_id_for_client);
                System.out.println("\nClient ID IS : " + new_id_for_client.getName() + "\nhas ID " + i);
                new_id_for_client.setUser_id(i);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("An error occurred while trying to remove user : " + client_to_close.getName() + " from SERVER");
        }

    }

    public static synchronized void messageAll(String s) {
        for (int i = 0; i < client_list.size(); i++) {
            ClientThread client_to_recieve = client_list.get(i);
            try {
                client_to_recieve.output.writeUTF(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

class ClientThread implements Runnable {

    public Socket socket = null;
    public DataInputStream input = null;
    public DataOutputStream output = null;
    private String name;
    private int user_id;

    public ClientThread(Socket socket, DataInputStream input, DataOutputStream output, int user_id) {
        this.socket = socket;
        this.input = input;
        this.output = output;
        try {
            this.name = this.input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error fetching username");
        }
        System.out.println("New Client Thread created !  USERNAME is : " + this.name);
        this.user_id = user_id;
        try {
            this.output.writeUTF("Type any message to chat, if you want to leave chat type 'exit'");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String line = "";
        while (true) {
            try {
                line = input.readUTF();
                if (line.equals("exit")) {
                    System.out.println("Closing connection for " + getName());
                    Server.disconnect_from_Server(user_id);
                    Server.messageAll("============================"+" now "+getName()+" leave chat"+" ===================================");
                    break;
                } else if (!line.equals("exit")) {
                    String newline = getName() + " : " + line;
                    Server.messageAll(newline);
                }
            } catch (IOException i) {
                System.out.println(i);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}