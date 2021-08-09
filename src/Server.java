import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server {

    //stop the client connection
    private boolean stopRequested;
    //port number for socket
    public static final int PORT = 7777;
    //List of all connected chat clients
    public List<ChatConnection> listChatConnection = new ArrayList<ChatConnection>();


    public Server()
    {
        stopRequested = false;
    }

    //starts the server
    public void startServer()
    {
        //start the server or receive a error upon start up
        stopRequested = false;
        ServerSocket serverSocket = null;
        try
        {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started at "
                    + InetAddress.getLocalHost() + " on port " + PORT);
        }
        catch (IOException e)
        {
            System.err.println("Server can't listen on port: " + e);
            System.exit(-1);
        }

        //while stopRequested in false continue in the loop keeping the server running
        try
        {
            while (!stopRequested) {
                Socket socket = serverSocket.accept(); //->can set a time out parameter here in desired
                System.out.println("Connection made with " + socket.getInetAddress());
                // start a chat with this connection
                ChatConnection chatConnection = new ChatConnection(socket);
                //add the new connection to the list
                listChatConnection.add(chatConnection);
                //start new thread for the connection
                Thread thread = new Thread(chatConnection);
                thread.start();
            }
            //when stopRequest is true closing server
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Can't accept client connection: " + e);
        }
        System.out.println("Server finishing");
    }

    //method to broadcast all chat client messages to other clients
    //called from the chatConnection object below
    public void broadcastMessage(String message){
        for (ChatConnection chat:listChatConnection) {
            chat.sendMessage(message);
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // inner class that represents a single chat client across a socket
    private class ChatConnection implements Runnable
    {
        //value to end connection which must be typed by client and recognized by by the server
        private String value = "QUIT";
        private Socket socket; // socket for client/server communication
        PrintWriter pw; // output stream to client
        private Boolean closeChat = false; //close chat boolean


        public ChatConnection(Socket socket)
        {
            try{
                this.socket = socket;
                pw = new PrintWriter(socket.getOutputStream(), true); //-> true set autoFlush to occur

            }catch (IOException e){
                System.out.println(e);
            }

        }


        public void run()
        {
            BufferedReader br; // input stream from client
            try
            {
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //System.out.println("Running run method server");
                pw.println("You are connected to the chat group! Type QUIT to exit.");

                do
                {
                    String clientRequest = br.readLine(); //-> if encountering block no response could be occurring here
                    System.out.println(clientRequest);

                    if (!clientRequest.equals(value)) {
                        broadcastMessage(clientRequest);
                    }
                    else {
                        closeChat = true;
                    }

                }
                while (!closeChat);
                pw.close();
                br.close();
                System.out.println("Closing connection with " + socket.getInetAddress());
                socket.close();
            }
            catch (IOException e)
            {  System.err.println("Server error: " + e);
            }
        }

        //method to sendMessages/write to all clients from the input
        //called from the Broadcast message method
        public void sendMessage(String message){

            pw.println(message);
        }
    }
}