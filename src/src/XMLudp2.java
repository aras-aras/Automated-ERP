import java.io.*;
import java.net.*;

public class XMLudp2 {
    public File xmlFile;
    public FileOutputStream fos;
    public byte[] xmlBytes;
    public DatagramSocket socket;
    public int port = 9999; // Choose a suitable port number
    public DatagramPacket packet;
    public InetAddress address;

    public XMLudp2() throws SocketException, UnknownHostException
    {
           // Set up the UDP socket and packet
           socket = new DatagramSocket(port);
           address = InetAddress.getLocalHost();


    }
    public void receive() throws IOException
    {
        // Receive the packet
        xmlBytes = new byte[1024];
        packet = new DatagramPacket(xmlBytes, xmlBytes.length);
        socket.receive(packet);
    }
    public void unload() throws IOException {

        // Write the contents of the packet to a file
        fos = new FileOutputStream("received_file.xml");
        fos.write(packet.getData(), 0, packet.getLength());
    }
    public void adios() throws IOException {
            fos.close();
            socket.close();
    }
    }


