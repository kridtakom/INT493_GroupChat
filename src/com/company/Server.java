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
            // open server in port 8080
            server = new ServerSocket(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            socket = null;
            try {
                socket = server.accept();
                System.out.println("New client has connected to server");
                System.out.println("Client : " + socket);

                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());

                //creat new client, creat thread for a new client and add to a client to client list
                client = new ClientThread(socket, input, output, client_id);
                clientThread = new Thread(client);
                client_list.add(client);
                clientThread.start();
                messageAll("============================== New client join chat room " + client.getName() + " ============================================", -1);
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

    // this func. was called to disconnect client that type 'exit'
    public static synchronized void disconnect_from_Server(int user_id) {
        ClientThread client_to_close = client_list.get(user_id);
        try {
            client_to_close.output.writeUTF("================================= Disconnected From SERVER! : " + client_to_close.getName() + "\nYou can close console ===============================================");
            client_to_close.input.close();
            client_to_close.output.close();
            client_to_close.socket.close();
            client_list.remove(user_id);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("An error occurred while trying to remove user : " + client_to_close.getName() + " from SERVER");
        }

    }

    // func. broadcasts to all client
    // this func. will get all client and sent message that get from a client to all client
    public static synchronized void messageAll(String s, int user_id) {
        for (int i = 0; i < client_list.size(); i++) {
            ClientThread client_to_recieve = client_list.get(i);
            try {
                if (client_to_recieve.getUserId() != user_id || user_id == -1) {
                    client_to_recieve.output.writeUTF(s);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

// class for manage and create thread for client that able to server can parallel work
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
        System.out.println("New Client Thread created : " + this.name);
        this.user_id = user_id;
        try {
            this.output.writeUTF("Type any message to chat, if you want to leave chat type 'exit'");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public int getUserId() {
        return user_id;
    }

    // this func. manage message from client and broadcast
    @Override
    public void run() {
        String line = "";
        while (true) {
            try {
                line = input.readUTF();
                if (line.equals("exit")) {
                    System.out.println("Closing connection for " + getName());
                    Server.disconnect_from_Server(user_id);
                    Server.messageAll("============================" + " now " + getName() + " leave chat" + " ===================================", -1);
                    break;
                } else if (!line.equals("exit")) {
                    String newline = getName() + " : " + line;
                    Server.messageAll(newline,user_id);
                }
            } catch (IOException i) {
                System.out.println(i);
            }
        }
    }
}