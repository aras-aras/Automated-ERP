import java.sql.*;
import java.util.ArrayList;
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

    //public void updating_price(Connection con, String price)
    public int already_exists(Connection con,String id) throws SQLException {
        Statement stmt=con.createStatement();
        String sql="se  lect today from infi.today where id='"+id+"'";
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

    /////////////////////////////////                 WAREHOUSE                   //////////////////////////////////////
   /* public void book_pieces(Connection con, String quantity, String type) throws SQLException{
            Statement stmt=con.createStatement();
            String sql="update infi.warehouse ";
    }*/
    public int[] check_pieces(Connection con, String p, int day) throws SQLException{
            Statement stmt=con.createStatement();
            String sql="select '"+p+"'_existing, '"+p+"'_reserved from infi.warehouse where day='"+day+"'";
            ResultSet re=stmt.executeQuery(sql);
            while(re.next()){
                int[] arr=new int[2];
                arr[0]=re.getInt("'"+p+"'_existing");
                arr[1]=re.getInt("'"+p+"'_reserved");
                return arr;
            }
            return null;
    }
    public void reserving_pieces(Connection con, String p, int day, int new_reserved) throws SQLException{
            Statement stmt=con.createStatement();
            String sql="update infi.warehouse set '"+p+"'_reserved='"+new_reserved+"' where day='"+day+"'";
        stmt.executeUpdate(sql);
    }

    public void arriving_new_pieces(Connection con, String piece, int day, int quantity) throws SQLException{
            Statement stmt=con.createStatement();
            String sql="update infi.warehouse set '"+piece+"'_existing='"+quantity+"' where day='"+day+"'";
            stmt.executeUpdate(sql);
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
            String sql="insert into infi.pieces_trans(order, type_t, deliver) values ('"+order+"', '"+type1+"', '"+deliver+"')";
            stmt.executeUpdate(sql);
        }

    public void piece_update(Connection con, String order, String type_t, String machine1, String tool1, String work_time1, String type_out1,
                             String machine2, String tool2, String work_time2, String type_out2, String machine3, String tool3,
                             String work_time3, String type_out3, String machine4, String tool4, String work_time4, String type_out4,
                             String deliver, String day) throws SQLException{
            Statement stmt=con.createStatement();
            String sql="update infi.pieces_trans set machine1='"+machine1+"', tool1='"+tool1+"', work_time1='"+work_time1+"'," +
                    " type_out1='"+type_out1+"', machine2='"+machine2+"', tool2='"+tool2+"', " +
                    "work_time2='"+work_time2+"', type_out2='"+type_out2+"', machine3='"+machine3+"', tool3='"+tool3+"', " +
                    "work_time3='"+work_time3+"', type_out3='"+type_out3+"', machine4='"+machine4+"', tool4='"+tool4+"'," +
                    " work_time4='"+work_time4+"', type_out4='"+type_out4+"', day='"+day+"'" +
                    " where order='"+order+"', type_t='"+type_t+"', deliver='"+deliver+"')";
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
            str[10]=re.getString("machine3");
            str[11]=re.getString("tool3");
            str[12]=re.getString("work_time3");
            str[13]=re.getString("type_out3");
            str[14]=re.getString("machine4");
            str[15]=re.getString("tool4");
            str[16]=re.getString("work_time4");
            str[17]=re.getString("type_out4");
            str[18]=re.getString("deliver");
            str[19]=re.getString("day");
            str[20]=re.getString("id");
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
            String sql="insert into infi.suppliers(day,'"+sup+"') values ('"+day+"', '"+quantity+"')";
            stmt.executeUpdate(sql);
    }


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
        String sql="se  lect today from infi.today where id='"+id+"'";
        ResultSet re=stmt.executeQuery(sql);
        while(re.next()){
            String[] str= new String[1];
            str[0]=re.getString("today");

            return Integer.parseInt(str[0]);
        }
        return Integer.parseInt(null);
    }
}
