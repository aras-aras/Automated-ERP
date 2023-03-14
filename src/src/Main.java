import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        while(1) {
            XMLudp2 receiver = new XMLudp2();
            //Sara's version
            String path = "xml_file.xml";
            // Gabi's Version
            // Maia's version
            receiver.receive();
            receiver.unload();
        }



    }
}