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
    // ClientHandler extends thread for multi-threading, just like ReceiverThread
    public class ClientHandler extends Thread {
        Socket socket; // Receiver for the clientSocket that connects to our server
        BufferedReader fromClient; // Used to read messages sent from the client
        OutputStream imageToClient; // Used to output the image byteArray as an output stream over to the client
        PrintWriter textToClient; // Used to output text messages to the client :)
        ArrayList<ClientHandler> clients; // This is a reference to an array containing all clients connected to the session. Useful for dispersing messages
        ClientHandler client;
        public ClientHandler(Socket socket, ArrayList<ClientHandler> clients) {

            this.socket = socket;
            this.clients = clients;
        }

        public void run() {
            try {
                fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                textToClient = new PrintWriter(socket.getOutputStream());

                String clientMessage;
                while((clientMessage = fromClient.readLine()) != null) {
                    System.out.println("Received message: " + clientMessage);
                    emitToClients(clientMessage);
                }

            } catch (Exception e) {
                System.out.println("Exception occured in clientHandler for IP: " + socket.getInetAddress());
                System.out.println(e.getMessage());
                clients.remove(this);
                // I LOVE NESTED TRY CATCH EXCEPTIONS
                try {
                    emitToClients("Server\0Client with IP: " + socket.getInetAddress().toString() + " has disconnected.\0server\0" + LocalDateTime.now().toString());
                } catch(Exception exception) {
                    System.out.println("Client Disconnect Message was not able to be sent to clients.");
                }
            }
        }

        public void emitToClients(String message) throws IOException {
            for (ClientHandler clientHandler : clients) {
                System.out.println("Sent Message to client: " + clientHandler.socket.getInetAddress().toString());
                PrintWriter clientWriter = new PrintWriter(clientHandler.socket.getOutputStream(), true);
                clientWriter.println(message);
            }
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
            System.out.println("Client has successfully connected from IP: " + clientSocket.getRemoteSocketAddress().toString());
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
