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
                String message= null;
                try {
                    today=data.today_day(con);
                    message = create_str(today);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
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
                Thread.sleep(59200);
                System.out.println("new today:");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            today++;
            System.out.println(today);



        }
    }

    public void process_str(String received) throws SQLException {
         Account acc= new Account();
         acc.process_str(received);
    }

    public String create_str(int today) throws SQLException {
        String message = null;
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
        String deliver=data.get_deliver(con,today);
        message=result+"."+deliver;
        System.out.println(message);
        return message;
    }

}



