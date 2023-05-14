import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutionException;

public class ModBusTCP implements Runnable{ //Server - this part is meant to receive txt from mes

    public int today;
    public ServerSocket serverSocket;
    public Socket clientSocket;
    public ModBusTCP() throws IOException {
        // Set up the UDP socket and packet
        ServerSocket serverSocket;
        today=0;
        Socket clientSocket;
        serverSocket = new ServerSocket(12345); // set up the server socket
        //System.out.println("Waiting client");
        clientSocket = serverSocket.accept(); // wait for a client to connect
        //System.out.println("Client connected");

    }
    public void run() { //antes tinha o nome de public static void ServerTCP()

            //sleep de 60 segundos
            today++;
        }

    }



