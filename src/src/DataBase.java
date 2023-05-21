import javax.swing.plaf.nimbus.State;
import javax.xml.crypto.Data;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DataBase {
        public Connection create_connection(){
            try {
                Connection con = null;
                Class.forName("org.postgresql.Driver");
                con = DriverManager.getConnection("jdbc:postgresql://db.fe.up.pt:5432/up201905243","up201905243", "mestres_em_infi23");
                if(con!=null){
                    return con;
                }
                else{
                    System.out.println("Connection failed");
                }
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return null;
        }
    public void new_order(Connection con, String client_name, String order_num, String work_piece, String quantity, String duedate, String late_pen, String early_pen, String status) throws SQLException{
            Statement stmt=con.createStatement();
            String sql="insert into infi.order(order_number, client_name, nr_pieces, type_piece, duedate, late_penalty, early_penalty, processed) values ('"+order_num+"', '"+client_name+"', '"+quantity+"', '"+work_piece+"', '"+duedate+"', '"+late_pen+"', '"+early_pen+"', '"+status+"')";
            stmt.executeUpdate(sql);


        //Statement stmt=con.createStatement();
        //String sql="create table if not exists testesi.table2(col1 real)";
        //String sql1="insert into testesi.table2(col1) values (1.1)";
        //stmt.executeUpdate(sql);
        //stmt.executeUpdate(sql1);
    }
    public String[] order_not_processed(Connection con) throws SQLException{
            Statement stmt=con.createStatement();
            String sql="select order_number, client_name, nr_pieces, type_piece, duedate, late_penalty, early_penalty from infi.order where processed='"+0+"'";
            ResultSet re=stmt.executeQuery(sql);
            while(re.next()){
                String[] atr= new String[7];
                atr[0]=re.getString("order_number");
                atr[1]=re.getString("client_name");
                atr[2]=re.getString("nr_pieces");
                atr[3]=re.getString("type_piece");
                atr[4]=re.getString("duedate");
                atr[5]=re.getString("late_penalty");
                atr[6]=re.getString("early_penalty");
                    return atr;
            }
            return null;
    }
    public void processed_status(Connection con, String order_num) throws SQLException{
        Statement stmt=con.createStatement();
        String sql="update infi.order set processed='"+1+"' where order_number='"+order_num+"'";
        stmt.executeUpdate(sql);
    }

    public void cancelling_order(Connection con, String order_num) throws  SQLException{
            Statement stmt=con.createStatement();
            String sql="update infi.order set canceled='"+1+"' where order_number='"+order_num+"'";
            stmt.executeUpdate(sql);
    }
    public void account_order(Connection con, String order_num, int cost) throws  SQLException{
        Statement stmt=con.createStatement();
        String cost0= String.valueOf(cost);
        String sql="update infi.order set total_unit_cost='"+cost0+"' where order_number='"+order_num+"'";
        stmt.executeUpdate(sql);
    }

    //public void updating_price(Connection con, String price)
    public int already_exists(Connection con,String id) throws SQLException {
        Statement stmt=con.createStatement();
        String sql="select today from infi.today where id='"+id+"'";
        ResultSet re=stmt.executeQuery(sql);
        while(re.next()){
            String[] str= new String[1];
            str[0]=re.getString("order_number");
            if(str[0].equals(id)==true)
            {
                return 1;
            }
        }
        return 0;
    }
    public String[] order_info(Connection con, String order) throws SQLException {
        Statement stmt=con.createStatement();
        String sql="select order_number, client_name, nr_pieces, type_piece, duedate, late_penalty, early_penalty from infi.order where order_number='"+0+"'";
        ResultSet re=stmt.executeQuery(sql);

        while(re.next()){
            String[] atr= new String[4];
            atr[0]=re.getString("order_number");
            atr[1]=re.getString("nr_pieces");
            atr[2]=re.getString("type_piece");
            atr[3]=re.getString("late_penalty");
            atr[4]=re.getString("duedate");
            return atr;
        }
        return null;
    }

    /////////////////////////////////                 WAREHOUSE                   //////////////////////////////////////
   /* public void book_pieces(Connection con, String quantity, String type) throws SQLException{
            Statement stmt=con.createStatement();
            String sql="update infi.warehouse ";
    }*/
    public int[] check_pieces(Connection con, String p, String day) throws SQLException{
            Statement stmt=con.createStatement();
            p=p.toLowerCase();
            System.out.println("today: "+ day);
            System.out.println("p:"+p);
            String sql="select "+p+"_existing, "+p+"_reserved from infi.warehouse where day='"+day+"'";
            ResultSet re=stmt.executeQuery(sql);
            int[] arr=new int[2];
            while(re.next()){
                arr[0]=re.getInt(""+p+"_existing");
                arr[1]=re.getInt(""+p+"_reserved");
                return arr;
            }
            arr[0]=0;
            arr[1]=0;
            return arr;
    }
    public void reserving_pieces(Connection con, String p, int day, int new_reserved) throws SQLException{
            Statement stmt=con.createStatement();
            p=p.toLowerCase();
            String sql="update infi.warehouse set "+p+"_reserved='"+new_reserved+"' where day='"+day+"'";
        stmt.executeUpdate(sql);
    }

    public void arriving_new_pieces(Connection con, String piece, int day, int quantity, String order) throws SQLException{
            Statement stmt=con.createStatement();
            String sql="update infi.warehouse set "+piece+"_existing='"+quantity+"' where day='"+day+"'";
            stmt.executeUpdate(sql);
            int i=check_day(con, day);
            if(i==0)//está livre
            {
                sql="update infi.scheduling set arriving='"+order+"' where day='"+day+"'";
                stmt.executeUpdate(sql);
            }
            else if(i==-1){//dia n existe, inserir dia
                sql="insert into infi.scheduling(day, producing_1, delivering, arriving, producing_2) values('"+day+"', '"+0+"', '"+0+"', '"+order+"', '"+0+"')";
                stmt.executeUpdate(sql);
            }
            else if(i!=-1 && i!=0){// o dia já existe e está ocupado, que alteração faço no dia?
                sql="select arriving from infi.scheduling where day='"+day+"'";
                ResultSet re=stmt.executeQuery(sql);
                String ord = null;
                while(re.next()) {
                    ord = re.getString("arriving");
                }
                order= ord +","+ order;
                sql="update infi.scheduling set arriving='"+order+"' where day='"+day+"'";
                stmt.executeUpdate(sql);
            }
    }

    public void just_arrived(Connection con, String piece, int day, int quantity, String order) throws  SQLException{
        Statement stmt=con.createStatement();
        piece.toLowerCase();
        String d=String.valueOf(day);
        String q=String.valueOf(quantity);
        String sql="insert into infi.warehouse(day, "+piece+"_existing, "+piece+"_reserved) values ('"+d+"', '"+q+"', '"+0+"')";
        stmt.executeUpdate(sql);
        int i=check_day(con, day);
        if(i==0)//está livre
        {
            sql="update infi.scheduling set arriving='"+order+"' where day='"+day+"'";
            stmt.executeUpdate(sql);
        }
        else if(i==-1){//dia n existe, inserir dia
            sql="insert into infi.scheduling(day, producing_1, delivering, arriving, producing_2) values('"+day+"', '"+0+"', '"+0+"', '"+order+"', '"+0+"')";
            stmt.executeUpdate(sql);
        }
        else if(i!=-1 && i!=0){// o dia já existe e está ocupado, que alteração faço no dia?
            sql="select arriving from infi.scheduling where day='"+day+"'";
            ResultSet re=stmt.executeQuery(sql);
            String ord = null;
            while(re.next()) {
                ord = re.getString("arriving");
            }
            order= ord +","+ order;
            sql="update infi.scheduling set arriving='"+order+"' where day='"+day+"'";
            stmt.executeUpdate(sql);
        }

    }


//na função a seguir, basicamente estamos a eliminar as peças do armazem quand for o dia de entrega
    public void leaving(Connection con, int day, String order) throws SQLException{
        Statement stmt=con.createStatement();
        //tem de ser o mes a eliminar a peça porque nos nao controlamos essas peças só o raw material
        int i=check_day(con, day);
        String sql;

        if(i==0)//está livre
        {
            sql="update infi.scheduling set delivering='"+order+"' where day='"+day+"'";
            stmt.executeUpdate(sql);
        }
        else if(i==-1){//dia n existe, inserir dia
            sql="insert into infi.scheduling(day, producing_1, delivering, arriving, producing_2) values('"+day+"', '"+order+"', '"+0+"', '"+0+"', '"+0+"')";
            stmt.executeUpdate(sql);
        }
        else if(i!=-1 && i!=0){// o dia já existe e está ocupado, que alteração faço no dia?
            sql="select arriving from infi.scheduling where day='"+day+"'";
            ResultSet re=stmt.executeQuery(sql);
            String ord = null;
            while(re.next()) {
                ord = re.getString("arriving");
            }
            order= ord +","+ order;
            sql="update infi.scheduling set arriving='"+order+"' where day='"+day+"'";
            stmt.executeUpdate(sql);
        }
    }
    public String get_deliver(Connection con, int day) throws SQLException {
        Statement stmt=con.createStatement();
        String sql;
        String del = null;
        sql="select delivering from infi.scheduling where day='"+day+"'";
        ResultSet re=stmt.executeQuery(sql);
        String ord = null;
        while(re.next()) {
            ord = re.getString("arriving");
        }
        String[] arr=order_info(con, ord);
        if(arr==null)
        {
            return "0";
        }
        del= ord +","+arr[1]+","+arr[2];
        return del;

    }





    //////////////////////////////                  TRANSFORMATIONS                      ////////////////////////////////////////////////////////

    public String[] transformation(Connection con, String pi, String pf) throws SQLException{
            Statement stmt=con.createStatement();
            String sql="select time, tool from infi.transformations where p_initial='"+pi+"' and p_final='"+pf+"'";
            ResultSet re=stmt.executeQuery(sql);
            while(re.next()){
                String[] str= new String[2];
                str[0]=re.getString("time");
                str[1]=re.getString("tool");
                return str;
            }
            return null;
    }

    ///////////////////////////                     PIECES TRANSFORMATIONS              //////////////////////////

    public void piece(Connection con, String order, String type1, String deliver) throws SQLException{
            Statement stmt=con.createStatement();
            String sql="insert into infi.pieces_trans(\"order\", deliver) values ('"+order+"', '"+deliver+"') limit 1";
            stmt.executeUpdate(sql);
        }

    public void piece_update(Connection con, String order, String machine1, String tool1, String work_time1, String type_out1,
                             String machine2, String tool2, String work_time2, String type_out2, String deliver,
                              String day) throws SQLException{
            Statement stmt=con.createStatement();
            String sql="update infi.pieces_trans set machine1='"+machine1+"', tool1='"+tool1+"', work_time1='"+work_time1+"'," +
                    " type_out1='"+type_out1+"', machine2='"+machine2+"', tool2='"+tool2+"', " +
                    "work_time2='"+work_time2+"', type_out2='"+type_out2+"',deliver='"+deliver+"' ,day='"+day+"'" +
                    " where order='"+order+"' order by machine1 asc" +
                    "Limit 1";
            stmt.executeUpdate(sql);
        }

    public List<String[]> information(Connection con, String day) throws SQLException{
        Statement stmt=con.createStatement();
        String sql="select * from infi.pieces_trans where day='"+day+"'";
        ResultSet re=stmt.executeQuery(sql);
        List<String[]> rows = new ArrayList<>();
        while(re.next()){
            String[] str= new String[21];
            str[0]=re.getString("order");
            str[1]=re.getString("type_t");
            str[2]=re.getString("machine1");
            str[3]=re.getString("tool1");
            str[4]=re.getString("work_time1");
            str[5]=re.getString("type_out1");
            str[6]=re.getString("machine2");
            str[7]=re.getString("tool2");
            str[8]=re.getString("work_time2");
            str[9]=re.getString("type_out2");
            str[10]=re.getString("day");
            str[11]=re.getString("id");
            rows.add(str);
        }
        return rows;
    }

//////////////////////////////                      detailed transformations            /////////////////////////

    public String[] info(Connection con, String p_initial, String p_final) throws SQLException{
            Statement stmt=con.createStatement();
            String sql="select * from infi.detailed_transf where p_initial='"+p_initial+"' and p_final='"+p_final+"'";
            ResultSet re=stmt.executeQuery(sql);
            while(re.next()){
                String[] str= new String[18];
                str[0]=re.getString("p_initial");
                str[1]=re.getString("machine1");
                str[2]=re.getString("tool1");
                str[3]=re.getString("work_time1");
                str[4]=re.getString("type_out1");
                str[5]=re.getString("machine2");
                str[6]=re.getString("tool2");
                str[7]=re.getString("work_time2");
                str[8]=re.getString("type_out2");
                str[9]=re.getString("machine3");
                str[10]=re.getString("tool3");
                str[11]=re.getString("work_time3");
                str[12]=re.getString("type_out3");
                str[13]=re.getString("machine4");
                str[14]=re.getString("tool4");
                str[15]=re.getString("work_time4");
                str[16]=re.getString("type_out4");
                str[17]=re.getString("p_final");
                return str;
            }
            return null;
        }




/////////////////////////////////                   SUPPLIERS                       ////////////////////////////////////

    public void sup(Connection con, String day, String sup, String quantity) throws SQLException{
            Statement stmt=con.createStatement();
            String sql="insert into infi.suppliers(day,"+sup+") values ('"+day+"', '"+quantity+"')";
            stmt.executeUpdate(sql);
    }
//aqui depois das peças chegarem do forncedor, falta retirar da bd as ordens

//////////////////////////////            DAY COUNTER                        ////////////////////////////////////////


    public void day_counter( Connection con, int value) throws SQLException {
        Statement stmt=con.createStatement();
        int id=1;
        String sql="update infi.today set today='"+value+"' where id='"+id+"'";
        stmt.executeUpdate(sql);
    }
    public int today_day(Connection con) throws SQLException {
        int today=0;
        int id=1;
        Statement stmt=con.createStatement();
        String sql="select today from infi.today where id='"+id+"'";
        ResultSet re=stmt.executeQuery(sql);
        while(re.next()){
            String[] str= new String[1];
            str[0]=re.getString("today");

            return Integer.parseInt(str[0]);
        }
        return Integer.parseInt(null);
    }




    ////////////////////////////////            SUB_TRANSFORMATIONS                 ///////////////////////////
    public String sub_trans(Connection con, String p_init, String p_final) throws  SQLException{
        Statement stmt=con.createStatement();
        String sql="select p_intermediate from infi.sub_transformations where p_initial='"+p_init+"' and p_final='"+p_final+"'";
        ResultSet re= stmt.executeQuery(sql);
        while(re.next()){
            String str;
            str=re.getString("p_intermediate");
            return str;
        }
        return null;
    }

    /////////////////////////////////               SCHEDULING              ////////////////////////////////////////

    public int[] calendar(Connection con,int today, int[]aux, String piece, String order) throws SQLException {
        //aqui vamos assumir que a linha de cada peça ja está criada, é apenas necessario
        int day;
        //aux[0]- numero total de dias em que as peças estao e produção
        //aux[1]- a data em que começam a ser feitas
        //aux[2]- duedate
        //0_._._._.4.4.1.1.d
        //aux[1] = duedate - today - aux[0]-1;
        int[] days = new int[aux[0]];
        int x=1;
        if(piece.equals("P6")==true)
        {
            for(int n=today;n<aux[0]+today;n++) //estamos a verificar se cada dia está livre
            {

                if (verify(con,2,(int)(aux[2]+today-1-x)) == 1) {//é menos dois porque é o dia antes da entrega
                    System.out.println("day is free");
                    days[x-1]=aux[2]+today-1-x;
                    int i=update_line(con, 2, (int)(aux[2]+today-1-x),order);
                }
                else if (verify(con,2,(int)(aux[2]+today-1-x)) == 0)
                {
                    System.out.println("day is ocupied");
                    day=recursive(con,aux[2]+today-1-x,2,order);
                    aux[2]+=day;
                    days[x-1]=today+aux[2]-x-1;
                }
                else if(verify(con,2,(int)(aux[2]+today-1-x)) == 2){
                    System.out.println("day doesnt exist");
                    int i=insert_day(con, 2, (int)(aux[2]+today-1-x), order);
                    days[x-1]=today+aux[2]-x-1;
                }
                x++;
                }
            }
        else if(piece.equals("P8")==true )
        {
            x=0;
            //verificar producing 1 e 2
            for(int n=today;n<aux[0]/2+today;n++) //estamos a verificar se cada dia está livre
            {

                if (verify(con,2,(int)(aux[1])+x) == 1) {// _._.P.P.P.P._.D
                    System.out.println("day is free");

                    int i=update_line(con, 2, (int)(aux[1]+x), order);
                    days[x]=today+aux[1]+x;
                }
                else if (verify(con,2,(int)(aux[1]+x)) == 0)
                {
                    System.out.println("day is ocupied");
                    day=recursive(con,aux[1]+x+1,2, order);
                    //marcar o aux[2]+today-2+day
                    aux[2]+=day;
                    days[x]=today+aux[2]-aux[0]+x-1;

                }
                else if(verify(con,2,(int)(aux[1])) == 2){
                    System.out.println("day doesnt exist");
                    int i=insert_day(con, 2, (int)(aux[1]+x), order);
                    days[x]=today+aux[1]+x;

                }
                x++;
            }
            x=0;
            for(int n=today;n<aux[0]/2+today;n++) //estamos a verificar se cada dia está livre
            {

                if (verify(con,1,(int)(aux[1]+aux[0]/2+x)) == 1) {//é menos dois porque é o dia antes da entrega
                    System.out.println("day is free");

                    int i=update_line(con, 1, (int)(aux[1]+aux[0]/2+x), order);
                    days[x]=today+aux[1]+aux[0]/2+x;
                }
                else if (verify(con,1,(int)(aux[1]+aux[0]/2+x)) == 0)
                {
                    System.out.println("day is ocupied");
                    day=recursive(con,aux[1]+aux[0]/2+x,2, order);
                    aux[2]+=day;
                    days[x]=today+aux[2]-1-aux[0]/2+x;
                }
                else if(verify(con,1,(int)(aux[1]+aux[0]/2+x)) == 2){
                    System.out.println("day doesnt exist");
                    int i=insert_day(con, 1, (int)(aux[1]+aux[0]/2+x), order);
                    days[x]=today+aux[1]+aux[0]/2+x;
                }
                x++;
            }
        }
        else
        {
            //verificar producing 1
            x=0;
            for(int n=today;n<aux[0]+today;n++) //estamos a verificar se cada dia está livre
            {

                if (verify(con,1,(int)(aux[1]+x)) == 1) {//é menos dois porque é o dia antes da entrega
                   System.out.println("day is free");
                    days[x]=aux[1]+x;
                    int i=update_line(con, 1, (int)(aux[1]+x), order);
                }
                else if (verify(con,1,(int)(aux[1]+x)) == 0)
                {
                    System.out.println("day is ocupied");
                    System.out.println(aux[1]+x+1);
                    day=recursive(con,aux[1]+x+1,1, order);
                    aux[2]+=day;
                    days[x]=aux[1]+x+day;

                }
                else if(verify(con,1,(int)(aux[1]+x)) == 2){
                    System.out.println("day doesnt exist");
                    int i=insert_day(con, 1, (int)(aux[1]+x), order);
                    System.out.println(aux[1]+x);
                    days[x]=aux[1]+x;

                }
                x++;
            }
        }
        return days;
    }

    public int verify(Connection con, int prod, int day) throws SQLException {
        Statement stmt=con.createStatement();
        String sql="select producing_"+prod+" from infi.scheduling where day='"+day+"'";
        ResultSet re= stmt.executeQuery(sql);
        while(re.next()){
            String str;
            str=re.getString("producing_"+prod+"");
            if(str.equals(String.valueOf(0))) {
                //atualizo o valor para reservado
                return 1;
            }
            else{//aqui já está ocupado
                return 0;
            }
        }
        //se o dia não existir, crio já o dia e reservo já
        return 2;
    }

    // Aqui estou a tentar fazer uma função recursiva que procura o dia
    //mais proximo que esteja livre, ainda é um prototipo
    public int recursive(Connection con,int day, int type, String order) throws SQLException {

        if(verify(con,type,day) == 1 )
        { //esta livre
            System.out.println(day+" esta livre");
            int i=update_line(con, type, day, order);
            return 1;
        }
        if(verify(con,type,day) == 2)
        {
            System.out.println(day+" nao existe");
            int i=insert_day(con, type, day, order);
            i=update_line(con, type, day, order);
            return 1;
        }
        System.out.println(day+" esta ocupado");
        return 1+recursive(con, day+1,type, order);
    }

    public int update_line(Connection con, int prod, int day, String order) throws SQLException{
        Statement stmt=con.createStatement();
        String sql="update infi.scheduling set producing_"+prod+"='"+order+"' where day='"+day+"'";
        stmt.executeUpdate(sql);
        return 0;
    }
    public int insert_day(Connection con, int prod, int day, String order) throws SQLException{//acrescentar ordem à tabela
        Statement stmt=con.createStatement();
        int aux=0;
        if(prod==1){
            aux=2;
        }
        else if(prod==2){
            aux=1;
        }
        String sql="insert into infi.scheduling(day, producing_"+prod+", delivering, arriving, producing_"+aux+") values('"+day+"', '"+order+"', '"+0+"', '"+0+"', '"+0+"')";
        stmt.executeUpdate(sql);
        return 0;
    }
    public void sch_suplier(Connection con, int day) throws SQLException{
        Statement stmt=con.createStatement();
        String sql="update infi.scheduling set arriving='"+1+"' where day='"+day+"'";
        stmt.executeUpdate(sql);
    }

    public int check_day(Connection con, int day) throws SQLException{
        Statement stmt=con.createStatement();
        String sql="select arriving from infi.scheduling where day='"+day+"'";
        ResultSet re= stmt.executeQuery(sql);
        while(re.next()){
            int i=re.getInt("arriving");
            if(i==0){
                return 0;
            }
            else if(i!=0){
                return 1;
            }
        }
        return -1;
    }
    public void sch_new_suplier(Connection con, int day) throws SQLException{
        Statement stmt=con.createStatement();
        String sql="insert into infi.scheduling(day, producing_1, delivering, arriving, producing_2) values('"+day+"', '"+0+"', '"+0+"', '"+1+"', '"+0+"')";
        stmt.executeUpdate(sql);
    }
}

