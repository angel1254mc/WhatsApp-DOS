package Server;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.awt.image.BufferedImage;
import javax.imageio.*;

public class Server {

    ServerSocket serverSocket; // Establish the server socket
    Socket clientSocket; // Receiver for the clientSocket that connects to our server
    BufferedReader fromClient; // Used to read messages sent from the client
    OutputStream imageToClient; // Used to output the image byteArray as an output stream over to the client
    PrintWriter textToClient; // Used to output text messages to the client :)
    ByteArrayOutputStream imageStream;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server Initialized: Ready for Clients to Connect");
    
        // Wait for the client to connect
        clientSocket = serverSocket.accept();
        System.out.println("Client has successfully connected from IP: " + clientSocket.getRemoteSocketAddress().toString());
    
        fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));;
        textToClient =  new PrintWriter(clientSocket.getOutputStream(), true);

        // Wait for the client to be ready to accept images
    }
    
    public static void main(String[] args) {
        try {
            Server server = new Server(3000);
        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
    }

}
