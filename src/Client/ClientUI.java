package Client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;
import java.nio.file.*;

import javax.sound.midi.Receiver;
// Importing jFrame utilities
import javax.swing.*;

public class ClientUI {
    
    Socket clientSocket;
    BufferedReader fromServer;
    PrintWriter textToServer;
    OutputStream imageToServer;
    String username;
    ChatUI ui;
    
    boolean sessionEnd;
    ReceiverThread receiverThread;

    public class ReceiverThread extends Thread {
        private BufferedReader fromServer;
        private ChatUI ui;
        public ReceiverThread (BufferedReader fromServer, ChatUI ui) {
            this.fromServer = fromServer;
            this.ui = ui;
        }
    
        /** This function runs upon instantiating the ReceiverThread object and using the .start() function from base class Thread*/
        public void run() {
            // Exception should be non-halting. Caught by client and addressed
            try {
                String messageSpecs;
                while (true) {
                    messageSpecs = fromServer.readLine();
                    if (messageSpecs == null) break;
                    // Split the message into it's individual components
                    System.out.println("YES");
                    String[] directivesArray = messageSpecs.split("\0");
                    ui.appendMessage(directivesArray[0], directivesArray[1], directivesArray[2], "Don't worry bout it");
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
    
            }
        }
    
    }
    

    public class ChatUI extends JFrame implements ActionListener {
        private JPanel chatBox;
        private JTextField messageInput;
        private JButton sendButton;
        private JScrollPane scrollPane;
        /**
         * Thank you to StackOverflow user "user288467" for literally being the goat and working on your own chat app
         */
        public class ChatMessage {
            private String messageType;
            private Object messageContentObj;
            private String sender;
            JTextPane messageContent;
            
            public ChatMessage(String messageType, Object messageContentObj, String sender) {
                this.messageType = messageType;
                this.messageContentObj = messageContentObj;
                this.sender = sender;
    
                if (messageType.equals("String")) {
    
                    messageContent = new JTextPane();

                    messageContent.setBorder(new EmptyBorder(7, 7, 7, 7)); 
                    messageContent.setOpaque(false);
                    messageContent.setEditable(false);
                    StyledDocument messageStyles = messageContent.getStyledDocument();
                    Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
                    Style regular = (messageStyles.addStyle("regular", def));
    
                    Style textBody = (messageStyles.addStyle("body", regular));
                    StyleConstants.setFontFamily(textBody,"Inter");
    
                    Style foot = messageContent.addStyle("sender", textBody);
                    StyleConstants.setFontSize(foot, 8);
                    try {
                        messageStyles.insertString(0, ((String)this.messageContentObj + "\n"), textBody);
                        messageStyles.insertString(messageStyles.getLength(), this.sender, foot);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
    
                    // What is going ONNNNNNN
                }
                if (messageType.equals("Server")) {
                    messageContent = new JTextPane();
                    messageContent.setBorder(new EmptyBorder(7, 7, 7, 7)); 
                    messageContent.setOpaque(false);
                    messageContent.setEditable(false);
                    StyledDocument messageStyles = messageContent.getStyledDocument();
                    Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
                    Style regular = (messageStyles.addStyle("regular", def));
    
                    Style textBody = (messageStyles.addStyle("body", regular));
                    StyleConstants.setFontFamily(textBody,"Inter");
    
                    Style foot = messageContent.addStyle("sender", textBody);
                    StyleConstants.setFontSize(foot, 8);
                    try {
                        messageStyles.insertString(0, ((String)this.messageContentObj + "\n"), textBody);
                        messageStyles.insertString(messageStyles.getLength(), this.sender, foot);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
    
            public JTextPane getMessageComponent() {
                return messageContent;
            }
        }
        public ChatUI() {
            setTitle("Chat Client");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(400, 400);
    
            // sing JPanel as this allows us to be more creative with our message display
            // BoxLayout seems to be the functional equivalent to display: block in HTML
            chatBox = new JPanel();
            chatBox.setLayout(new BoxLayout(chatBox, BoxLayout.Y_AXIS));
            scrollPane = new JScrollPane(chatBox);
    
            // Create the message input field and send buttons to add to bottom panel
            messageInput = new JTextField(25);
            sendButton = new JButton("Send");
            sendButton.addActionListener(this);
    
            // Create bottom panel with configurable child alignments
            JPanel chatControls = new JPanel(new BorderLayout());
            chatControls.add(messageInput, BorderLayout.CENTER);
            chatControls.add(sendButton, BorderLayout.EAST);
    
            // Add alignments to the parent JFrame
            setLayout(new BorderLayout());
            add(scrollPane, BorderLayout.CENTER);
            add(chatControls, BorderLayout.SOUTH);
        }
        
        public void appendMessage(String messageType, Object message_content, String sender, String time) {
            if (messageType.equals("String")) {
                System.out.println("added to chtbox");
               JPanel row = new JPanel(new BorderLayout(10, 10));
               if (!sender.equals(username))
               row.add((new ChatMessage(messageType, message_content, sender)).getMessageComponent(), BorderLayout.WEST);
               else
                row.add((new ChatMessage(messageType, message_content, sender)).getMessageComponent(), BorderLayout.EAST);
               chatBox.add(row);
               chatBox.revalidate();
               chatBox.repaint();
            }
            if (messageType.equals("Server")) {
                System.out.println("Message from Server");
                JPanel row = new JPanel(new BorderLayout(10, 10));
                row.add((new ChatMessage(messageType, message_content, sender)).getMessageComponent(), BorderLayout.CENTER);
                chatBox.add(row);
                chatBox.revalidate();
                chatBox.repaint();
            }
        }
    
        @Override
        public void actionPerformed(ActionEvent e) {

            if (e.getSource() == sendButton) {
                textToServer.println("String\0" + messageInput.getText().trim() + "\0" + username +"\0today");
            }
        }

    } 

    public ClientUI(){
        ui = new ChatUI();
    }

    public void connect(String IP, int port, String username) throws IOException {
        
        this.username =  username;
        UUID unique_user_identifier = UUID.randomUUID();
        this.username = unique_user_identifier + this.username;
        // Connect just like in the past assignments
        clientSocket = new Socket(IP, port);

        fromServer =  new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        textToServer = new PrintWriter(clientSocket.getOutputStream(), true); // Autoflush output Stream please

        System.out.println("Successfully allocated Writers and Readers, and Connected to Server");


    }
    public void startThread(BufferedReader fromServer) {
        receiverThread = new ReceiverThread(fromServer, ui);
        receiverThread.start();
    }

    public void instantiateUI() {
        SwingUtilities.invokeLater(() -> {
            ui.setVisible(true);
        });
    }
    public static void main(String[] args) {
        try {
            ClientUI client = new ClientUI();
            if (args.length > 0)
                client.connect(args[0], Integer.parseInt(args[1]), args[2]);
            else
                client.connect("localhost", 3000, "Angel");
            client.startThread(client.fromServer);
            client.instantiateUI();
        } catch (Exception e) {
            System.out.println("An Exception Occured! Error description below: ");
            System.out.println(e.getMessage());
        }
    }
}