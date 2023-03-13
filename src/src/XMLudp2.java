import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

    public class XMLudp2 {
        public static void main(String[] args) throws Exception {
            // Set up the UDP socket and packet
            DatagramSocket socket = new DatagramSocket(12345); // Use the same port number as the sender
            byte[] buffer = new byte[1024]; // Choose an appropriate buffer size
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // Receive the packet
            socket.receive(packet);

            // Write the contents of the packet to a file
            FileOutputStream fos = new FileOutputStream("received_file.xml");
            fos.write(packet.getData(), 0, packet.getLength());

            // Clean up
            fos.close();
            socket.close();
        }
    }


