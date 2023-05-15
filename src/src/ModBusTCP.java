import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ModBusTCP implements Runnable { //Server - this part is meant to receive txt from mes

    public int today;
    public Socket clientSocket;
    private static final int SERVER_PORT = 8080;
    private static final String SERVER_IP = "localhost";
    Socket socket;

    public ModBusTCP() throws IOException {
        // Set up the UDP socket and packet
        today = 0;
        socket = new Socket(SERVER_IP, SERVER_PORT);

    }

    public void run() {

        while (true) {
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                // Connect to the server

                // Create input and output streams
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Send a message to the server
                out.println("Hello, server!");

                // Read the response from the server
                String response = in.readLine();
                System.out.println("Received from server: " + response);

                // Close the connection

                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //sleep de 60 segundos
            try {
                Thread.sleep(59200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            today++;

        }


    }
}



