import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class File_treatment {
    LinkedList<String> cars = new LinkedList<String>();
    File fil;
    public int Read_File()
    {

        //get the document builder

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();

            //get document

            Document document = builder.parse(new File("received_file.xml"));

            //normalize the xml structure

            document.getDocumentElement().normalize();

            //Get all the element by the tag name

            NodeList ClientList =document.getElementsByTagName("Client");
            for(int i=0;i<ClientList.getLength();i++)
            {
                Node Client = ClientList.item(i);
                if(Client.getNodeType()== Node.ELEMENT_NODE)
                {
                    Order ord= new Order();
                    Element ClientElement = (Element) Client;
                    ord.Client_name=ClientElement.getAttribute("NameId");
                    NodeList Clientdetails = Client.getChildNodes();
                    for(int j=0; j < Clientdetails.getLength(); j++)
                    {
                        Node detail= Clientdetails.item(j);
                        if(detail.getNodeType()== Node.ELEMENT_NODE)
                        {
                           Element detailElement = (Element) detail;
                           ord.Order_num=detailElement.getAttribute("Number");
                           ord.Work_Piece=detailElement.getAttribute("WorkPiece");
                           ord.Quantity=detailElement.getAttribute("Quantity");
                           ord.DueDate=detailElement.getAttribute("DueDate");
                           ord.Late_Pen=detailElement.getAttribute("LatePen");
                           ord.Early_Pen=detailElement.getAttribute("EarlyPen");
                           // chamar função de meter as coisas na lista.
                        }
                    }
                }
            }
        }
        catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
        return 1;
    }
    public void Create_List()
    {

    }
    public void put_in_list()
    {

    }
}

