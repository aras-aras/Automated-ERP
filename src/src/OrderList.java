import java.util.ArrayList;

public class OrderList {

    static ArrayList<Order> orders;

    public OrderList()
    {
         orders = new ArrayList<Order>();
    }
    public void print_list(ArrayList list)
    {
        for(int i=0;i<orders.size();i++)
        {
            System.out.println(orders.get(i).Client_name);
        }

    }

}
