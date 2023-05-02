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
    public int order_not_processed(Connection con) throws SQLException{
            Statement stmt=con.createStatement();
            String sql="select order_number from infi.order where processed='"+0+"'";
            ResultSet re=stmt.executeQuery(sql);
            while(re.next()){
                return re.getInt("order_number");
            }
            return -1;
    }
    public void processed_status(Connection con, int order_num) throws SQLException{
        Statement stmt=con.createStatement();
        String sql="update infi.order set processed='"+1+"' where order_number='"+order_num+"'";
        stmt.executeUpdate(sql);
    }

    public void cancelling_order(Connection con, int order_num) throws  SQLException{
            Statement stmt=con.createStatement();
            String sql="update infi.order set canceled='"+1+"' where order_number='"+order_num+"'";
            stmt.executeUpdate(sql);
    }



    /////////////////////////////////                 WAREHOUSE                   //////////////////////////////////////
    public void book_pieces(Connection con, int quantity, int type) throws SQLException{
            Statement stmt=con.createStatement();
            String sql="update infi.warehouse "
    }
    public int check_pieces(Connection con, String p, int day) throws SQLException{
            Statement stmt=con.createStatement();
            String sql="select '"+p+"'_existing, '"+p+"'_reserved from infi.warehouse where day='"+day+"'";
            ResultSet re=stmt.executeQuery(sql);
            while(re.next()){
                int exis=re.getInt("'"+p+"'_existing");
                int reserv=re.getInt("'"+p+"'_reserved");
                return exis-reserv;
            }
            return -1;
    }

    //////////////////////////////////////////////////////////////////armazem///////
    //numero de peças p1
    //numero de peças p2
    //...
    //numero de peças p9
    // numero de peças p1 reservadas
    //...
    //numero de peças p9 reservadas;
    //done



    //////////////////////////////////////////////////////////////////pedido///////



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
