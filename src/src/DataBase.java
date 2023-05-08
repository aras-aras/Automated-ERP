import java.sql.*;

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
    public void new_order(Connection con, String client_name, String order_num, String work_piece, String quantity, String duedate, String late_pen, String early_pen) throws SQLException{
            Statement stmt=con.createStatement();
            String sql="insert into infi.order(order_number, client_name, nr_pieces, type_piece, duedate, late_penalty, early_penalty) values ('"+order_num+"', '"+client_name+"', '"+quantity+"', '"+work_piece+"', '"+duedate+"', '"+late_pen+"', '"+early_pen+"')";
            stmt.executeUpdate(sql);
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



    /////////////////////////////////                 WAREHOUSE                   //////////////////////////////////////
    public void book_pieces(Connection con, String quantity, String type) throws SQLException{
            Statement stmt=con.createStatement();
            String sql="update infi.warehouse ";
    }
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


    ////////////////////////////////////////////////////////catalogo///////////////
    /* para cada tipo peça:
    processo de formação de cada peça, e fornos 1 p1-p3-p6, t1-t3
    processo de formaçao de peça, e fornos  2 p2-p5-p6, t1-t2 ou nulo
    melhor peça de origem
    maximo tempo a chegar ao forno
    maximo de tempo do ultimo forno utilizado a plataforma
    quantas peças posso produzir deste tipo por dia
     */
}
