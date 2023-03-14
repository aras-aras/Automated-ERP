import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
public class Main {
    public static void main(String[] args) throws IOException { //Server
        /*while(true) {
            System.out.println("want to end things :( ?(s/n)");

            Scanner scanner = new Scanner(System.in);
            String inputString = scanner. nextLine();
            XMLudp2 receiver = new XMLudp2();

            receiver.receive();
            receiver.unload();

            if(inputString.equals("s"))
            {
                receiver.adios();
                break;
            }

        }*/
        ModBusTCP server = new ModBusTCP();
        server.ServerTCP();



    }
}