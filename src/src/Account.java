import java.sql.Connection;
import java.sql.SQLException;

public class Account {
    int cost; // custo por supplier
    int Price;
    int depreciation0; // estimativa do depreciation cost
    public int costSup(int N, String L, String raw)
    {
        if (L.equals('C')==true) {
            if(raw.equals("P1")==true)
            {
                Price=55;
            }
            else
            {
                Price=18;
            }
        }
        if (L.equals('B')==true) {
            if(raw.equals("P1")==true)
            {
                Price=45;
            }
            else
            {
                Price=15;
            }

        }
        if (L.equals('A')==true) {
            if(raw.equals("P1")==true)
            {
                Price=30;
            }
            else
            {
                Price=10;
            }
        }

        cost=Price*N;

        return cost;
    }
    public int Depre(String L, String raw, int dispatchd, int arrivald )
    {
        if (L.equals('C')==true) {
            if(raw.equals("P1")==true)
            {
                Price=55;
            }
            else
            {
                Price=18;
            }
        }
        if (L.equals('B')==true) {
            if(raw.equals("P1")==true)
            {
                Price=45;
            }
            else
            {
                Price=15;
            }
        }
        if (L.equals('A')==true) {
            if(raw.equals("P1")==true)
            {
                Price=30;
            }
            else
            {
                Price=10;
            }
        }

        depreciation0= Price*(dispatchd-arrivald)/100;
        return depreciation0;
    }

    public void process_str(String coisa_grande) throws SQLException {
        /*Basicamente aqui estou a dividir a string recebida do mes
        * String do mes: ord,PC1,AD1,DD1,PC2,AD2,DD2...deliver_date*/
        DataBase data=new DataBase();
        Connection con=data.create_connection();
        int order_num=0, n=1;
        int total_unit_cost=0;
        String[] items = coisa_grande.split(",");
        int[] numbers = new int[items.length];
        for (int i = 0; i < coisa_grande.length(); i++) {
            numbers[i] = Integer.parseInt(items[i]);
        }
        order_num=numbers[0]; // basicamente a primeira posição é o numero d ordem
        while(numbers.length-1 > n)
        {
            total_unit_cost=total_unit_cost+unit_cost(numbers[0],numbers[n],numbers[n+1], numbers[n+2]);
            n=n+3;
        }
        String[] dat=data.order_info(con, String.valueOf(numbers[0]));
        int duedate= Integer.parseInt(dat[4]);
        int Penalty= Integer.parseInt(dat[3]);
        total_unit_cost=total_unit_cost-((numbers[numbers.length-1])*Penalty);
        data.account_order(con,dat[0],total_unit_cost);

    }

    public int unit_cost(int ord_num, int PC, int DD, int AD)
    {
        /*Aqui preciso que vas buscar quantas peças estas a fazer com este order number, Pc é o total production
        * time e vem do mes, DD é o disptch day, ou seja o dia em q acaba de ser produzida,
        * e o AD é o dia em que a peça chega a maquina*/
        int  number=0, unit_cost=0;
        int RC=0;//custo do raw material de cada supplier
                /* Ok basicamente tambem vou precisas de uma coluna em que guardes qual o supplier e o preço que cada
                * ordem usou, vais ter de guardar no management ou assim e depois aqui vais buscar
                * porque precisamos de saber isso para o custo total*/
        int DC= RC*(DD-AD)/100;
        unit_cost=RC+PC+DC;
        return unit_cost;
    }
}