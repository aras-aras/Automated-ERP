import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCP implements Runnable { //Server - this part is meant to receive txt from mes

    public int today;
    public Socket clientSocket;
    private static final int SERVER_PORT = 8181;
    private static final String SERVER_IP = "localhost";
    Socket socket;

    public TCP() throws IOException {
        // Set up the UDP socket and packet
        today = 0;
        socket = new Socket(SERVER_IP, SERVER_PORT);
        System.out.println("Connected");

 //ola olha, n consigo compilar pq ele n esta a conseguir ligar ao tcp pq eu n tenho o mes a correr,
        //por isso vou cagar para o tcp para ja
        // mal acabemos isto vamos, tenho ideias
        //temos de ajudar o gabi, ele está completamente perdido no mes, okok
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
                String message= create_str(today);
                // Send a message to the server
                out.println("Today variable: " + message);

                // Read the response from the server
                String response = in.readLine();
                System.out.println("Received from server: " + response);
                //process_str(response);
                // Close the connection

                //socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //sleep de 60 segundos
            try {
                System.out.println("test4");
                Thread.sleep(59200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            today++;

        }


    }
    public void process_str(String received)
    {

         Account acc= new Account();
         acc.perceber_a_string(received);


    }
    public String create_str(int today)
    {
        String message = "";
        DataBase data=new DataBase();
        Connection con=data.create_connection();
        try {
            List<String[]> rows = data.information(con, String.valueOf(today));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


            /* procuras quais linhas é que tem o dia igual a today
             * Ajuda necessaria e urgente do francisco*/

        return message;
    }

}



