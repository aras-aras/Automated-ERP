import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;


//This is ERP


public class Main {
    public static void main(String[] args) throws IOException {
        XMLudp2 receiver = new XMLudp2();

        while(true) {

            receiver.receive();
            receiver.unload();
            //tratar oficheiro:
            File_treatment treatment= new File_treatment();

            System.out.println("want to end things :( ?(s/n)");

            Scanner scanner = new Scanner(System.in);
            String inputString = scanner. nextLine();
            if(inputString.equals("s"))
            {
                receiver.adios();
                break;
            }

        }
        ModBusTCP server = new ModBusTCP();
        server.ServerTCP();



    }
}