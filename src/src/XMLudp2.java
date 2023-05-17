import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class XMLudp2 implements Runnable {
    public byte[] xmlBytes;
    public DatagramSocket socket;
    public int port = 12345; // Choose a suitable port number
    public DatagramPacket packet;
    public InetAddress address;

    public String xmlData;
    public File_treatment treatment;
    public XMLudp2() throws SocketException, UnknownHostException, FileNotFoundException {
           // Set up the UDP socket and packet

           socket = new DatagramSocket(port);
           address = InetAddress.getLocalHost();
           OrderList ord = new OrderList();
           treatment= new File_treatment(ord);
    }

    public void run() { //Thread 1 - não tem delay porque faz logo unload
        // Receive the packet
        while(true) {
            xmlBytes = new byte[2043];
            packet = new DatagramPacket(xmlBytes, xmlBytes.length);
            System.out.println("?");
            try {
                socket.receive(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Packet received (th1)");
            try {
                xmlData=unload();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
            try {
                treatment.treat(xmlData);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public String unload() throws IOException, ParserConfigurationException, SAXException {

        // Write the contents of the packet to a string
        System.out.println("???");
        xmlData = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
        return xmlData;
    }
    public void adios() throws IOException {
            //fos.close();
            socket.close();
    }
    }


