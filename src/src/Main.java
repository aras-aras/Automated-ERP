import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Scanner;


//This is ERP


public class Main {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        XMLudp2 receiver = new XMLudp2();
        OrderList ord= new OrderList();
        File_treatment treatment= new File_treatment(receiver, ord);


            Thread thread1 = new Thread(receiver);
            thread1.start();

            Thread thread2 = new Thread(treatment);
            thread2.start();

        //ModBusTCP server = new ModBusTCP();
        //server.ServerTCP();
    }
}