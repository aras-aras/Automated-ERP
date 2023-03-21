import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;

public class XMLudp {
        public File xmlFile;
        public FileInputStream fis;
        public byte[] xmlBytes;
        public DatagramSocket socket;
        public int port = 9999; // Choose a suitable port number
        public DatagramPacket packet;

        public InetAddress address;

        public XMLudp() throws SocketException, UnknownHostException {
            // Set up the UDP socket and packet
            socket = new DatagramSocket();
            address = InetAddress.getLocalHost();

        }
            // Load the XML file into a byte array
        public void load(String file) throws IOException {

            xmlFile = new File(file);
            FileInputStream fis = new FileInputStream(xmlFile);
            xmlBytes = new byte[(int) xmlFile.length()];
            fis.read(xmlBytes);
            packet = new DatagramPacket(xmlBytes, xmlBytes.length, address, port);
        }

        public void send() throws IOException {
            // Send the packet
            socket.send(packet);
        }
        public void adios() throws IOException {
            // Clean up
            fis.close();
            socket.close();
        }
    }

