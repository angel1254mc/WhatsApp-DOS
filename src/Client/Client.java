package Client;

import java.util.Scanner;
import java.util.UUID;
import java.io.*;
import java.net.*;

// Importing jFrame utilities

public class Client {
    
    Socket clientSocket;
    BufferedReader fromServer;
    PrintWriter textToServer;
    OutputStream imageToServer;
    String username;
    Scanner scanner;
    boolean sessionEnd;
    ReceiverThread receiverThread;

    public class ReceiverThread extends Thread {
        private BufferedReader fromServer;
        public ReceiverThread (BufferedReader fromServer) {
            this.fromServer = fromServer;
        }
    
        /** This function runs upon instantiating the ReceiverThread object and using the .start() function from base class Thread*/
        public void run() {
            // Exception should be non-halting. Caught by client and addressed
            try {
                String messageSpecs;
                while (true) {
                    messageSpecs = fromServer.readLine();
                    System.out.println(messageSpecs);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
    
            }
        }
    
    }
    

    public Client(){
        this.scanner = new Scanner(System.in);
    }

    public void connect(String IP, int port) throws IOException {
        
        // Connect just like in the past assignments
        clientSocket = new Socket(IP, port);

        fromServer =  new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        textToServer = new PrintWriter(clientSocket.getOutputStream(), true); // Autoflush output Stream please

        System.out.println("Successfully allocated Writers and Readers, and Connected to Server");

    }

    public void startListening() {
        System.out.println("Please provide a name to use for the Whatsapp Dos");
        textToServer.println(this.scanner.nextLine());

        while (true) {
            String message = this.scanner.nextLine();
            textToServer.println(message);
            // If message is exit, that means the user wants to leave the program
            if (message.equals("/exit"))
                System.exit(0);
        }
    }
    public void startThread(BufferedReader fromServer) {
        receiverThread = new ReceiverThread(fromServer);
        receiverThread.start();
        startListening();
    }

    public static void main(String[] args) {
        try {
            Client client = new Client();
            if (args.length > 0)
                client.connect(args[0], 3000);
            else
                client.connect("localhost", 3000);
            client.startThread(client.fromServer);
        } catch (Exception e) {
            System.out.println("An Exception Occured! Error description below: ");
            System.out.println(e.getMessage());
        }
    }
}