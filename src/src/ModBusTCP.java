import java.net.*;
import java.io.*;

public class ModBusTCP { //Server
    public static void ServerTCP() {
        try {
            ServerSocket serverSocket = new ServerSocket(12345); // set up the server socket
            Socket clientSocket = serverSocket.accept(); // wait for a client to connect
            System.out.println("Client connected");

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) { // read input from client
                System.out.println("Received message: " + inputLine);
                out.println(inputLine); // send response back to client
            }

            // clean up
            out.close();
            in.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

}
