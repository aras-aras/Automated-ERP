import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


public class TCP implements Runnable { //Server - this part is meant to receive txt from mes

    public int today;
    public Socket clientSocket;

    private static final int SERVER_PORT = 8181;
    private static final String SERVER_IP = "localhost";
    Socket socket;

    DataBase data;

    public TCP() throws IOException {
        // Set up the UDP socket and packet
        today = 0;
        socket = new Socket(SERVER_IP, SERVER_PORT);
        System.out.println("Connected");
        data = new DataBase();


    }

    public void run() {

        while (true) {
            Connection con = data.create_connection();
            try {

                data.day_counter(con,today);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

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

    public String create_str(int today) {
        String message = "";
        DataBase data = new DataBase();
        Connection con = data.create_connection();
        List<String[]> rows;
        try {
            rows = data.information(con, String.valueOf(today));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        StringBuilder result = new StringBuilder();
        for (String[] array : rows) {
            StringBuilder concatenated = new StringBuilder();
            for (String element : array) {
                concatenated.append(element);
            }
            result.append(concatenated);
        }

       //falta acrescentar
        012,109,202
        // entar a parte da string que tm o transporte daquele dia
        //vamos terv de ir ao calendario e verificar que ordem é para entregar no dia de hoje,
        //but, o calendario n tem essa informação
        //temos de meter
        //função que va buscar
        System.out.println(message);

        return message;
    }

}



