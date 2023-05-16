import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Scanner;




//This is ERP


public class Main {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {


        XMLudp2 receiver = new XMLudp2();
        System.out.println("maça");
        TCP server = new TCP();
        System.out.println("maça1");
        Managment man = new Managment();
        System.out.println("maça2");
        int today= server.today;


            Thread thread1 = new Thread(receiver);
            thread1.start();

            Thread thread2 = new Thread(man);
            thread2.start();

            Thread thread3 = new Thread(server);
            thread3.start();
    }


}