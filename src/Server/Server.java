package Server;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.awt.image.BufferedImage;
import javax.imageio.*;

public class Server {

    ServerSocket serverSocket; // Establish the server socket
    ArrayList<ClientHandler> clientArray = new ArrayList<ClientHandler>();
    Socket clientSocket; // Receiver for the clientSocket that connects to our server
    ArrayList<ChatRoom> rooms = new ArrayList<ChatRoom>();

    public class ChatRoom {
        ArrayList<ClientHandler> clients;
        String name;

        public ChatRoom(ClientHandler init, String name) {
            this.name = name;
            clients = new ArrayList<ClientHandler>();
            clients.add(init);
        }

        public boolean hasName(String name) {
            if (name.equals(this.name))
                return true;
            return false;
        }

        public boolean hasUser(ClientHandler client) {
            for (ClientHandler clientsInChat : clients) {
                if (client.name.equals(clientsInChat.name))
                    return true;
            }
            return false;
        }

        /**
         * This function returns a boolean denoting whether the room is now empty
         * 
         * @param client is a valid ClientHandler object
         * @return boolean denoting whether room is empty
         */
        public boolean add(ClientHandler client) {
            clients.add(client);
            return true;
        }

        public boolean remove(ClientHandler client) {
            clients.remove(client);
            return clients.size() == 0 ? true : false;
        }

        public void emit(String sender, String message) {
            // This function takes in a sender and a string message and emits it to all
            // members that belong to this chat
            for (ClientHandler client : clients) {
                client.textToClient.println("[" + sender + "]: " + message);
            }
        }

    }

    // ClientHandler extends thread for multi-threading, just like ReceiverThread
    public class ClientHandler extends Thread {
        Socket socket; // Receiver for the clientSocket that connects to our server
        BufferedReader fromClient; // Used to read messages sent from the client
        OutputStream imageToClient; // Used to output the image byteArray as an output stream over to the client
        PrintWriter textToClient; // Used to output text messages to the client :)
        ClientHandler client;
        String name;
        String clientState = "LOBBY";
        ChatRoom clientCurrRoom;

        public ClientHandler(Socket socket, ArrayList<ClientHandler> clients) {
            this.socket = socket;
        }

        public void run() {
            try {
                fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                textToClient = new PrintWriter(socket.getOutputStream(), true);
                this.name = fromClient.readLine();
                while (true) {
                    // Client can be in one of 2 states
                    if (this.clientState.equals("LOBBY")) {
                        textToClient.println(
                                """
                                        Hello!

                                        Welcome to the Proof of Concept for the WhatsApp Dos Message Transfer Protocol (WADMTPs)
                                        You can use any of the following commands:

                                        /leave
                                        Allows you to leave the chatroom you are currently in

                                        /join <roomName>
                                        Allows you to join the room with name <roomName> if the room is currently available. Otherwise creates a room with that name

                                        /list
                                        Lists all of the chat rooms currently available on the server by name

                                        /exit
                                        End the program overall

                                            """);

                        // Await for response from client response will most likely be a command if not
                        // a command, handle appropriately
                        while (this.clientState.equals("LOBBY")) {
                            String message = fromClient.readLine();
                            if (message.startsWith("/")) {
                                String[] messageComponents = message.split(" ");
                                if (messageComponents[0].equals("/list")) {
                                    String listOfRooms = "The following is a list of active chat rooms: \n\n";
                                    for (ChatRoom room : rooms) {
                                        listOfRooms += room.name + "\n";
                                    }

                                    textToClient.println(listOfRooms + "\n");

                                } else if (messageComponents[0].equals("/leave")) {
                                    System.out.println("Client has disconnected from clientHandler for IP: "
                                            + socket.getInetAddress());

                                } else if (messageComponents[0].equals("/join")) {
                                    if (messageComponents.length < 2) {
                                        textToClient.println(
                                                "Invalid use of join command: Please provide the name of the room you wish to join or create");
                                    } else {
                                        if (roomExists(messageComponents[1])) {
                                            ChatRoom roomToJoin = getRoom(messageComponents[1]);
                                            // Add room to client curr room
                                            this.clientCurrRoom = roomToJoin;
                                            this.clientCurrRoom.add(this);
                                            this.clientState = "ROOM";

                                        } else {
                                            ChatRoom roomToJoin = new ChatRoom(this, messageComponents[1]);
                                            this.clientCurrRoom = roomToJoin;
                                            rooms.add(roomToJoin);
                                            this.clientState = "ROOM";
                                        }
                                    }
                                }

                            } else {
                                textToClient.println(
                                        """
                                                Sorry, it appears that you have typed something that is not a command. Please try again using one of the following commands:

                                                /leave
                                                Allows you to leave the chatroom you are currently in

                                                /join <roomName>
                                                Allows you to join the room with name <roomName> if the room is currently available

                                                /list
                                                Lists all of the chat rooms currently available on the server by name

                                                /exit
                                                End the program overall
                                                """);

                                // Go back to the loop, weh
                            }
                        }
                    } else if (this.clientState.equals("ROOM")) {
                        while (this.clientState.equals("ROOM")) {
                            String message = fromClient.readLine();
                            if (message.equals("/leave")) {
                                // Handle the room leaving process which involves
                                // Notifying the other chat memebers the person has left
                                // Removing this clientHandler from the room
                                // removing the room from the clientHandler
                                // Reverting back to LOBBY State

                                ChatRoom roomLeft = this.clientCurrRoom;
                                this.clientCurrRoom = null;
                                this.clientState = "LOBBY";
                                if (roomLeft.remove(this)) {
                                    rooms.remove(roomLeft);
                                } else {
                                    roomLeft.emit("Server", "Client with IP: " + socket.getInetAddress().toString()
                                            + " and name: " + this.name + " has left the chatroom");
                                }
                                break;
                            } else {
                                this.clientCurrRoom.emit(this.name, message);
                            }
                        }
                    }

                    // System.out.println("Received message: " + clientMessage);
                    // emitToClients(clientMessage);
                }

            } catch (Exception e) {
                System.out.println("Exception occured in clientHandler for IP: " + socket.getInetAddress());
                System.out.println(e.getMessage());
                if (this.clientCurrRoom != null) {
                    this.clientCurrRoom.emit("Server", "Client with IP: " + socket.getInetAddress().toString()
                            + " with name: " + this.name + " has disconnected");
                    if (this.clientCurrRoom.remove(this)) {
                        rooms.remove(this.clientCurrRoom);
                    }

                }

                // I LOVE NESTED TRY CATCH EXCEPTIONS
                try {
                } catch (Exception exception) {
                    System.out.println("Client Disconnect Message was not able to be sent to clients.");
                }
            }
        }

        public boolean roomExists(String name) {
            for (ChatRoom room : rooms) {
                if (room.hasName(name)) {
                    return true;
                }
            }
            return false;
        }

        public ChatRoom getRoom(String name) {
            for (ChatRoom room : rooms) {
                if (room.hasName(name))
                    return room;
            }
            return null;
        }

    }

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server Initialized: Ready for Clients to Connect");

        // For this setup, I'll be using a multi-threading approach
        // that generates an arbitrary amount of client socket "handlers"
        // wwhenever a new client connection is attempted

        while (true) {
            clientSocket = serverSocket.accept();
            System.out.println(
                    "Client has successfully connected from IP: " + clientSocket.getRemoteSocketAddress().toString());
            ClientHandler newClient = new ClientHandler(clientSocket, clientArray);
            clientArray.add(newClient);
            newClient.start();

        }
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(3000);
        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
    }

}
