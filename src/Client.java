import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Runnable{

    //change host name to IP address of server for computer to computer running
    public static final String HOST_NAME = "localhost";
    public static final int HOST_PORT = 7777; // host port number

    public Socket socket = null;
    //isConnected keeps both threads running till connection ends
    public Boolean isConnected = true;


    public Client() {
    }

    public static void main(String[] args)
    {
        //create new client
        Client client = new Client();
        //create socket for client
        client.socketOpen();
        //create 2nd thread
        Thread secondThread = new Thread(client);
        secondThread.start();
        //start client
        client.startClient();

    }


    //runs 2nd thread
    //2nd thread responsible for getting server responses and printing them to the client
    public void run(){
        //System.out.println("run client1");
        BufferedReader br; // input stream from server

        try{
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            do {
                try {
                    String serverResponse = br.readLine();
                    System.out.println(serverResponse);
                } catch (IOException e) {
                    //empty
                    //once connection is false the catch will be trigger and loop will exit
                    //due to isConnected being false
                }

            }while (isConnected);

            //close bufferedreader
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //method to create client socket
    public void socketOpen(){
        try
        {
            socket = new Socket(HOST_NAME, HOST_PORT);
        }
        catch (IOException e)
        {  System.err.println("Client could not make connection: " + e);
            System.exit(-1);
        }

    }


    //main thread
    // thread responsible for listening for user input and sending to server
    public void startClient(){

        Scanner keyboardInput = new Scanner(System.in);
        PrintWriter pw; // output stream to server

        try
        {  // create an autoflush output stream for the socket
            pw = new PrintWriter(socket.getOutputStream(), true);
            // create a buffered input stream for this socket

            do
            {
                // get user input and sent it to server
                String input = keyboardInput.nextLine();
                pw.println(input);

                //isConnected becomes false here if user types "QUIT"
                if (input.equals("QUIT")){
                    //System.out.println("QUIT");
                    isConnected = false;
                }
            }
            while (isConnected);
            //close printwriter and socket
            pw.close();
            //br.close();
            socket.close();
        }
        catch (IOException e)
        {  System.err.println("Client error with game: " + e);
        }

    }


}