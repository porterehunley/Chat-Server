import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

final class ChatServer {
    private static int uniqueId = 0;
    // Data structure to hold all of the connected clients
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;			// port the server is hosted on
    private List<TicTacToeGame> Games = new ArrayList<>();
    private static final Object object = new Object();
    /**
     * ChatServer constructor
     * @param port - the port the server is being hosted on
     */
    private ChatServer(int port) {
        this.port = port;
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while(true) {
                Socket socket = serverSocket.accept(); //checks for room

                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *	Sample code to use as a reference for Tic Tac Toe
     *
     * directMessage - sends a message to a specific username, if connected
     * @param  - the string to be sent
     * @param  - the user the message will be sent to
     */
    private void directMessage(String message, String username) {
        synchronized (object) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String time = sdf.format(new Date());
            String formattedMessage = time + " "+  message + "\n";

            for (ClientThread clientThread : clients) {
                if (clientThread.username.equalsIgnoreCase(username)) {
                    clientThread.writeMessage(formattedMessage);
                }
            }
        }
    }


    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        ChatServer server = new ChatServer(1500);

        server.start();
    }


    /*
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     */
    private final class ClientThread implements Runnable {
        Socket socket;                  // The socket the client is connected to
        ObjectInputStream sInput;       // Input stream to the server from the client
        ObjectOutputStream sOutput;     // Output stream to the client from the server
        String username;                // Username of the connected client
        ChatMessage cm;                 // Helper variable to manage messages
        int id;

        /*
         * socket - the socket the client is connected to
         * id - id of the connection
         */
        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            // Read the username sent to you by client
            try {
                boolean cont = true;
                for(ClientThread clientThread: clients){
                    if(clientThread.username.equals(username) && clientThread.id != id){
                        cont = false;
                    }
                }
                broadcast(username + " has logged in");
                if(cont) {
                    do {
                        cm = (ChatMessage) sInput.readObject();
                        if (cm.getType() == ChatMessage.MESSAGE) {
                            broadcast(username + ": " + cm.getMsgCliSvr());
                        } else if (cm.getType() == ChatMessage.LOGOUT) {
                            String logmsg = username + " has logged out";
                            broadcast(logmsg);
                            remove(id);
                            close();
                        } else if (cm.getType() == ChatMessage.DM) {
                            boolean exists = false;
                            for (ClientThread clientThread : clients) {
                                if (clientThread.username.equals(cm.getDmGc())) {
                                    directMessage(username + " -> " + cm.getDmGc() + ":" + cm.getMsgCliSvr(), cm.getDmGc());
                                    directMessage(username + " -> " + cm.getDmGc() + ":" + cm.getMsgCliSvr(), username);
                                    System.out.println(username + " -> " + cm.getDmGc() + ":" + cm.getMsgCliSvr());
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                writeMessage("Username is not logged on \n");
                            }
                        } else if (cm.getType() == ChatMessage.LIST) {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                                String time = sdf.format(new Date());
                                String listmessage = time + " list of users connected right now: ";
                                sOutput.writeObject(listmessage + listUsers(username));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (cm.getType() == ChatMessage.TICTACTOE) {
                            try {
                                int space = Integer.parseInt(cm.getMsgCliSvr());
                                boolean processed = processTurn(username, cm.getDmGc(), space);
                                if (!processed) {
                                    directMessage("Invalid Space!", username);
                                } else {
                                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                                    String time = sdf.format(new Date());
                                    directMessage("piece placed against " + cm.getDmGc(), username);
                                    System.out.println(time + " " + username + " placed piece against " + cm.getDmGc());
                                }
                            } catch (NumberFormatException e) {
                                boolean start = startGame(username, cm.getDmGc());
                                if (!start) {
                                    directMessage("Other user not found", username);
                                }

                            }
                        }
                    } while (cm.getType() != ChatMessage.LOGOUT);
                }else {
                    writeMessage("Someone with username already exists");
                    remove(id);
                    close();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }



            // Send message back to the client


        }

        private boolean writeMessage(String msg){
            if (this.socket == null){
                return false;
            }else {
                try {
                    sOutput.writeObject(msg);
                }catch (IOException e){
                    e.printStackTrace();
                    return false;
                }
                return true;

            }

        }
        private void close(){
            try {
                socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    private void broadcast(String message){
        synchronized (object) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
            Date now = new Date();
            for (ClientThread clientThread : clients) {
                clientThread.writeMessage( simpleDateFormat.format(now)+ " " + message + '\n');
            }
            System.out.println(simpleDateFormat.format(now)+ " " + message);
        }
    }
    private void remove(int id) {
        synchronized (object) {
            for (int a = 0; a < clients.size(); a++) {
                if (clients.get(a).id == id) {
                    clients.remove(a);
                    break;
                }
            }
        }
    }
    private String listUsers(String userName){
        synchronized (object) {
            String list = "";
            for (ClientThread clientThread : clients) {
                if(!clientThread.username.equals(userName)) {
                    list = list + clientThread.username + ", ";
                }
            }
            list = list.substring(0, list.length() - 2);
            list = list + "\n";
            return list;
        }
    }
    private synchronized boolean startGame(String hostUser, String otherUser){
            boolean exists = false;
            for(ClientThread clientThread: clients){
                if(clientThread.username.equals(otherUser) && !hostUser.equals(otherUser)){
                    exists = true;
                }
            }
            if(Games.size() != 0 && exists) {
                for (TicTacToeGame ticTacToeGame : Games) {
                    if (ticTacToeGame.getHostPlayer().equals(hostUser) && ticTacToeGame.getOtherPlayer().equals(otherUser)){
                        char state = ticTacToeGame.getGameState();
                        if(state == '-') {
                            directMessage(ticTacToeGame.toString(), hostUser);
                            return true;
                        }else{
                            endGame(ticTacToeGame.getHostPlayer(), ticTacToeGame.getOtherPlayer(), state);
                            return true;
                        }
                    }else if(ticTacToeGame.getHostPlayer().equals(otherUser) && ticTacToeGame.getOtherPlayer().equals(hostUser)){
                        char state = ticTacToeGame.getGameState();
                        if(state == '-') {
                            directMessage(ticTacToeGame.toString(), hostUser);
                            return true;
                        }else{
                            endGame(ticTacToeGame.getHostPlayer(), ticTacToeGame.getOtherPlayer(), state);
                            return true;
                        }
                    }
                }
                Games.add(new TicTacToeGame(otherUser,hostUser));
                directMessage("Game Started between " + hostUser + " and " + otherUser, otherUser);
                directMessage("Game Started between " + hostUser + " and " + otherUser,hostUser);
                System.out.println("Game Started between " + hostUser + " and " + otherUser);
                return true;
            }else if(Games.size() == 0 && exists){
                Games.add(new TicTacToeGame(otherUser,hostUser));
                directMessage("Game Started between " + hostUser + " and " + otherUser, otherUser);
                directMessage("Game Started between " + hostUser + " and " + otherUser,hostUser);
                System.out.println("Game Started between " + hostUser + " and " + otherUser);
                return true;
            }else {
                return false;
            }

    }
    private synchronized void endGame(String hostUser, String otherUser, char Outcome){
            if(Games.size() == 0){

            }else {
                for (TicTacToeGame ticTacToeGame : Games) {
                    if (ticTacToeGame.getHostPlayer().equals(hostUser) && ticTacToeGame.getOtherPlayer().equals(otherUser)) {
                        Games.remove(ticTacToeGame);
                        if (Outcome == 'X') {
                            directMessage("You've won agaisnt " + otherUser, hostUser);
                            directMessage("you've lost against " + hostUser, otherUser);
                        } else if (Outcome == 'O') {
                            directMessage("You've won agaisnt " + hostUser, otherUser);
                            directMessage("you've lost against " + otherUser, hostUser);
                        } else {
                            directMessage("You've tied against " + otherUser, hostUser);
                            directMessage("You've tied against " + hostUser, otherUser);
                        }

                    }else if(ticTacToeGame.getHostPlayer().equals(otherUser) && ticTacToeGame.getOtherPlayer().equals(hostUser)){
                        Games.remove(ticTacToeGame);
                        if (Outcome == 'X') {
                            directMessage("You've won agaisnt " + otherUser, hostUser);
                            directMessage("you've lost against " + hostUser, otherUser);
                        } else if (Outcome == 'O') {
                            directMessage("You've won agaisnt " + hostUser, otherUser);
                            directMessage("you've lost against " + otherUser, hostUser);
                        } else {
                            directMessage("You've tied against " + otherUser, hostUser);
                            directMessage("You've tied against " + hostUser, otherUser);
                        }
                    }
                }
            }


    }
    private synchronized boolean processTurn(String userOne, String userTwo, int space) {
        char temp = ' ';
        if (Games.size() == 0) {
            return false;
        } else {
            for (TicTacToeGame ticTacToeGame : Games) {
                if (ticTacToeGame.getHostPlayer().equals(userOne) && ticTacToeGame.getOtherPlayer().equals(userTwo)) {
                    temp = ticTacToeGame.getGameState();
                    if (temp == '-') {
                        if (ticTacToeGame.takeTurn(space, userOne) == -1) {
                            directMessage("Invalid Space!", userOne);
                            return false;
                        } else {
                            temp = ticTacToeGame.getGameState();
                            if (temp != '-'){
                                endGame(userOne,userTwo, temp);
                                directMessage(ticTacToeGame.toString(), userOne);
                            }else {
                                directMessage(userOne + " has taken a turn!", userTwo);
                                directMessage(ticTacToeGame.toString(), userOne);
                                return true;
                            }
                        }
                    } else { //if the game is over

                        endGame(userOne, userTwo, temp);
                        return false;

                    }
                }else if(ticTacToeGame.getHostPlayer().equals(userTwo) && ticTacToeGame.getOtherPlayer().equals(userOne)){
                    temp = ticTacToeGame.getGameState();
                    if (temp == '-') {
                        if (ticTacToeGame.takeTurn(space, userOne) == -1) {
                            directMessage("Invalid Space!", userOne);
                            return false;
                        } else {
                            temp = ticTacToeGame.getGameState();
                            if (temp != '-'){
                                endGame(userOne,userTwo, temp);
                                directMessage(ticTacToeGame.toString(), userOne);
                            }else {
                                directMessage(userOne + " has taken a turn!", userTwo);
                                directMessage(ticTacToeGame.toString(), userOne);
                                return true;
                            }
                        }
                    } else { //if the game is over

                        endGame(userTwo, userOne, temp);
                        return false;

                    }
                }
                else {
                    return false;
                }
            }
            return false;
        }
    }
}