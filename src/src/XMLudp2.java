import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class XMLudp2 {
    public File xmlFile;
    public FileOutputStream fos;
    public byte[] xmlBytes;
    public DatagramSocket socket;
    public int port = 9999; // Choose a suitable port number
    public DatagramPacket packet;
    public InetAddress address;

    public String xmlData;

    public XMLudp2() throws SocketException, UnknownHostException, FileNotFoundException {
           // Set up the UDP socket and packet
           socket = new DatagramSocket(port);
           address = InetAddress.getLocalHost();
           fos = new FileOutputStream("received_file.xml");


    }
    public void receive() throws IOException
    {
        // Receive the packet
        xmlBytes = new byte[2043];
        packet = new DatagramPacket(xmlBytes, xmlBytes.length);
        System.out.println("?");
        socket.receive(packet) ;

    }
    public String unload() throws IOException, ParserConfigurationException, SAXException {

        // Write the contents of the packet to a string
        xmlData = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
        //System.out.println(xmlData);
        return xmlData;

    }
    public void adios() throws IOException {
            //fos.close();
            socket.close();
    }
    }


