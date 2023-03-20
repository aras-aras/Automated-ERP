import java.io.IOException;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class File_treatment {

    public void Read_File(String xmlData, ArrayList list) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xmlData)));
        document.getDocumentElement().normalize();
        NodeList ClientList = document.getElementsByTagName("Client");

        NodeList orderList = document.getElementsByTagName("Order");


        for (int i = 0; i < ClientList.getLength(); i++) {
            Order ord= new Order();
            Element clientElement =(Element) ClientList.item(i);
            Node Client = ClientList.item(i);
            ord.Client_name=clientElement.getAttribute("NameId");

            Element orderElement =(Element) orderList.item(i);
            ord.Order_num=orderElement.getAttribute("Number");
            ord.Work_Piece=orderElement.getAttribute("WorkPiece");
            ord.Quantity=orderElement.getAttribute("Quantity");
            ord.DueDate=orderElement.getAttribute("DueDate");
            ord.Late_Pen=orderElement.getAttribute("LatePen");
            ord.Early_Pen=orderElement.getAttribute("EarlyPen");
            list.add(ord);
        }


    }
}

