import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Scanner;



//This is ERP


public class Main {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        /*XMLudp2 receiver = new XMLudp2();
        OrderList ord= new OrderList();
        File_treatment treatment= new File_treatment(receiver, ord);
        ModBusTCP server = new ModBusTCP();


            Thread thread1 = new Thread(receiver);
            thread1.start();

            Thread thread2 = new Thread(treatment);
            thread2.start();

            Thread thread3 = new Thread(server);
            thread3.start();*/
        //Versao 2

        XMLudp2 receiver = new XMLudp2();
        OrderList ord= new OrderList();
        File_treatment treatment= new File_treatment(receiver, ord);
        ModBusTCP server = new ModBusTCP();
        int today= server.today;


            Thread thread1 = new Thread(receiver);
            thread1.start();

            Thread thread2 = new Thread(treatment);
            thread2.start();

            Thread thread3 = new Thread(server);
            thread3.start();

        //server.ServerTCP(); não usar
    }
}