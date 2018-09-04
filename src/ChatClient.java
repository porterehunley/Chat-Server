

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;


    private final String server;
    private final String username;
    private final int port;
    private Thread t;

    /* ChatClient constructor
     * @param server - the ip address of the server as a string
     * @param port - the port number the server is hosted on
     * @param username - the username of the user connecting
     */
    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    private ChatClient(String username, int port){
        this.server = "localhost";
        this.port = port;
        this.username = username;
    }
    private ChatClient(String username){
        this.server = "localhost";
        this.port = 15000;
        this.username = username;
    }

    /**
     * Attempts to establish a connection with the server
     * @return boolean - false if any errors occur in startup, true if successful
     */
    private boolean start() {
        // Create a socket
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // Attempt to create output stream
        try {
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // Attempt to create input stream
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // Create client thread to listen from the server for incoming messages
        Runnable r = new ListenFromServer();
        t = new Thread(r);
        t.start();

        // After starting, send the clients username to the server.
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scanner scanner = new Scanner(System.in);
        String input ="";
        while (!input.equalsIgnoreCase("/logout")) {
            input = scanner.nextLine();
            if(input.equalsIgnoreCase("/logout")){
                ChatMessage chatMessage = new ChatMessage(1,"","");
                this.sendMessage(chatMessage);
                try {
                    this.socket.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }else if(input.substring(0,4).equalsIgnoreCase("/msg")){
                try {
                    String username = input.substring(input.indexOf(" ") + 1, input.indexOf(" ", input.indexOf(" ") + 1));
                    String message = input.substring(input.indexOf(" ", input.indexOf(" ") + 1), input.length());
                    ChatMessage chatMessage = new ChatMessage(2,message,username);
                    this.sendMessage(chatMessage);
                }catch (StringIndexOutOfBoundsException e){
                    System.out.println("Please specify a recipiant and a message to send them");
                }

            }else if(input.equalsIgnoreCase("/list")){
                ChatMessage chatMessage = new ChatMessage(3,"","");
                this.sendMessage(chatMessage);
            }else if(input.contains("/ttt")){
                if(input.length()>6){
                    try{
                        int space = Integer.parseInt(input.substring(input.lastIndexOf(" ")+1, input.length()));
                        String username = input.substring(input.indexOf(" ")+1,input.lastIndexOf(" "));
                        ChatMessage chatMessage = new ChatMessage(ChatMessage.TICTACTOE, String.valueOf(space), username);
                        this.sendMessage(chatMessage);
                    }catch (NumberFormatException e){
                        String username = input.substring(input.indexOf(" ")+1);
                        ChatMessage chatMessage = new ChatMessage(ChatMessage.TICTACTOE, "",username);
                        this.sendMessage(chatMessage);
                    }


                }else{
                    System.out.println("Please specify a user");
                    try{
                        int space = Integer.parseInt(input.substring(input.lastIndexOf(" "), input.length()));
                        String username = input.substring(input.indexOf(" ")+1,input.lastIndexOf(" "));
                        ChatMessage chatMessage = new ChatMessage(ChatMessage.TICTACTOE, String.valueOf(space), username);
                        this.sendMessage(chatMessage);
                    }catch (NumberFormatException e){
                        String username = input.substring(6);
                        ChatMessage chatMessage = new ChatMessage(ChatMessage.TICTACTOE, "",username);
                        this.sendMessage(chatMessage);
                    }
                }
            }else{
                ChatMessage chatMessage = new ChatMessage(0,input,"");
                this.sendMessage(chatMessage);
            }
            try {
                this.t.sleep((long) 250);
            }catch (InterruptedException e){
                e.printStackTrace();
            }


        }
        try {
            this.sInput.close();
            this.sOutput.close();
        }catch (IOException e){
            e.printStackTrace();
        }




        return true;
    }


    /*
     * Sends a string to the server
     * @param msg - the message to be sent
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) {
        // Get proper arguments and override defaults

        // Create your client and start it
        ChatClient client1 = new ChatClient("localhost", 1500, args[0]);
        if(!client1.start()){
            System.out.println("Client could not be started");
        }







        // Send an empty message to the server

    }


    /*
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     */
    private final class ListenFromServer implements Runnable {
        public void run() {
            while(!socket.isClosed()){
            try {
                String msg = (String) sInput.readObject();
                System.out.print(">"+ msg);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("\nYou've lost connection to the server");
                System.exit(0);
            }
            }
        }
    }
}