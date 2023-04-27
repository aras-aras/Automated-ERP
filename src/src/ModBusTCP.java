import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutionException;

public class ModBusTCP implements Runnable{ //Server - this part is meant to receive txt from mes

    public ServerSocket serverSocket;
    public Socket clientSocket;
    public ModBusTCP() throws IOException {
        // Set up the UDP socket and packet
        ServerSocket serverSocket;
        Socket clientSocket;
        serverSocket = new ServerSocket(12345); // set up the server socket
        //System.out.println("Waiting client");
        clientSocket = serverSocket.accept(); // wait for a client to connect
        //System.out.println("Client connected");

    }
    public void run() { //antes tinha o nome de public static void ServerTCP()
        while (true) {
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {

                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) { // read input from client
                    System.out.println("Received message: " + inputLine);
                    out.println(inputLine); // send response back to client
                }

                // clean up
                //out.close();
                //in.close();
                //clientSocket.close();
                //serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
    }
    /*public void adios() throws IOException {
        //out.close();
        //in.close();
        //clientSocket.close();
        //serverSocket.close();
    }*/

    }

}
