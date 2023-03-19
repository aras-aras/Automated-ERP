import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;


//This is ERP


public class Main {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        XMLudp2 receiver = new XMLudp2();
        OrderList ord= new OrderList();

        while(true) {

            receiver.receive();
            String xmlData;
            xmlData=receiver.unload();//tratar o ficheiro

            File_treatment treatment= new File_treatment();
            treatment.Read_File(xmlData, ord.orders);
            ord.print_list(ord.orders);

            System.out.println("want to end things :( ?(s/n)");

            Scanner scanner = new Scanner(System.in);
            String inputString = scanner. nextLine();
            if(inputString.equals("s"))
            {
                receiver.adios();
                break;
            }

        }
        //ModBusTCP server = new ModBusTCP();
        //server.ServerTCP();




    }
}