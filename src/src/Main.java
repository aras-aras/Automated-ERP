import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        XMLudp sender= new XMLudp();
        sender.xmlFile = new File("C:\\Users\\sarin\\IdeaProjects\\fabrica\\src");
        sender.load();
        sender.send();



    }
}