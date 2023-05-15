import java.io.IOException;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class File_treatment {

    OrderList ordens;

    static ArrayList<Order> list;


    public File_treatment( OrderList ord){
        list = ord.orders;
        ordens = ord;
    }

    public void treat(String serverData) { //Thread 2
        //isto faz o parser da string;
        while (true) {

                System.out.println("test3 (th2)");
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                Document document = null;
                try {
                    document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(serverData)));
                } catch (ParserConfigurationException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (SAXException e) {
                    throw new RuntimeException(e);
                }
                document.getDocumentElement().normalize();
                NodeList ClientList = document.getElementsByTagName("Client");
                NodeList orderList = document.getElementsByTagName("Order");
                for (int i = 0; i < ClientList.getLength(); i++) {
                    Element clientElement = (Element) ClientList.item(i);
                    Node Client = ClientList.item(i);
                    for (int j = 0; j < orderList.getLength(); j++) {
                        Order ord = new Order();
                        ord.Client_name = clientElement.getAttribute("NameId");
                        Element orderElement = (Element) orderList.item(j);
                        ord.Order_num = orderElement.getAttribute("Number");
                        ord.Work_Piece = orderElement.getAttribute("WorkPiece");
                        ord.Quantity = orderElement.getAttribute("Quantity");
                        ord.DueDate = orderElement.getAttribute("DueDate");
                        ord.Late_Pen = orderElement.getAttribute("LatePen");
                        ord.Early_Pen = orderElement.getAttribute("EarlyPen");
                        list.add(ord);
                        DataBase data=new DataBase();
                        Connection lig=data.create_connection();
                        try {
                            data.new_order(lig, ord.Client_name, ord.Order_num, ord.Work_Piece, ord.Quantity, ord.DueDate, ord.Late_Pen, ord.Early_Pen);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        list.clear();

}
                System.out.println("--------------------------------------");
                ordens.print_list(list);

            }
        }
    }
}

