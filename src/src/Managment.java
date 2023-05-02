import java.sql.Connection;
import java.sql.SQLException;

public class Managment {
    public int N;// numero de peças a fazer
    public int Na;// numero de peças já feitas no armazem do tipo que nos queremos
    public int Nb;// numero de peças de raw material que ha naquele dia
    public int Nc;// numero de peças a fazer por dia
    public int Nd;// numero de dias que tens para fazer a peça
    public int Ne;// numero que dias que tens no maximo para encomendar peças
    public int Nf; // número de peças not sure?
    public int Dpcost; // depreciation cost
    public int Ng;// depreciation cost + N* Custo do supplier C
    public int Nh;// N* Custo do supplier B
    public int Ni;// depreciation cost + N* Custo do supplier B
    public int Nj;// N* Custo do supplier A
    public Order ord; // a ordem a ser processada
    public int duedate; // duedate da ordem
    public String material; // raw material da peça
    public int[] work_days; // vetor de dias em que estamos a cozinhar a peça
    public int deliver_day; // dia em que a peça esta a ser transportada para a plataforma
    public void check() throws SQLException {

        /* A primeira coisa a ser feita é verificar a lista das ordens.
        * Essa lista contem colunas com todas as caracteristicas das ordens
        * incluindo uma coluna que indica se esta ordem ján foi processada ou não.
        * Uma ordem ser processada significa que já foi metida no calendario das
        * coisas a fazer aka cozinhar e ser transportada etc
        * */
        DataBase data=new DataBase();
        Connection con=data.create_connection();
        int order_number= data.order_not_processed(con);

        /* Com isto, verificamos a primeira ordem que está na lista de
        * ordens que se encontra num estado não processado e atribuimos-lhe
        * todos os seus atributos.
        * */
        ord= new Order();
        N=Integer.parseInt(ord.Quantity);
        duedate=Integer.parseInt(ord.DueDate);
        /*Aqui calculamos quantos dias vamos levar para fazer a encomenda e em que
        * dia temos de encomendar as peças.
        * FALTA VERIFICAR SE OS DIAS ESTÃO OCUPADOS E AS SOLUÇOES PARA
        * ESSES DILEMAS.(dentro da função calculus), basicamente
        * problemas para depois
        * */
        calculus(Nd,N,Nc, Ne,duedate);
        if(Ne==0)
        {
            /* Aqui verificamos se temos 0 dias para mandar vir o raw
            * Neste caso verificamos se só temos 0 dias, nesse caso
            * cancelamos a encomenda
            * FALTA APAGAR A ENCOMENDA DA DATABASE OU INDICAR DE ALGUMA FORMA
            * QUE ELA FOI CANCELADA (ordens canceladas têm a coluna canceled a 1)
            * */
            data.cancelling_order(con, order_number);
            return;
        }
        if(verify_how_many(ord.Work_Piece, Ne)>0)
        {
            data.processed_status(con, order_number);
            /* A segunda coisa a fazer é verificar se já há peças feitas
            * do tipo que nos queremos no armazém, o mais provével é que
            * esta situação nunca aconteça.
            * Usamos esta função para verificar quantas há e depois
            * subtraimos a N(o numero total de peças para saber quantas faltam fazer */
            Na=verify_how_many(ord.Work_Piece,Ne);
            N=N-Na;
            if(N<=0)
            {
                //reservar no armazem(deixam de estar livres)
                int quantity=Integer.parseInt(ord.Quantity);

            }
            else
            {
               material=verify_raw(ord.Work_Piece);
               Nb=verify_material(material,Ne);
               N=N-Nb;
               if(N<=0)
               {
                   for(int n=Nd; n>0; n--)
                   {
                       work_days[Nd-n]=Ne-1-n;
                   }
                   //marcar na base de dados que neste dia temos de fazer a encomenda os dias estao no vetor work_days
               }
               if(Ne==1)
               {
                   //Supplier C
                   if(N<4)
                   {
                       Nf=4-Nf;
                       // sobram Nf peças do tipo material, é preciso acrescentar essas peças
                       //à base de dados no dia em q chegam, neste caso, chegam daui a um dia.
                   }
               }
               else if(Ne==2 || Ne==3)
               {
                   if(N<=4)
                   {
                       if(Ng>Nh)
                       {
                           // mandar vir N peças do supplier B
                           Nf=8-Nf;

                       }
                       else
                       {
                           //mandar vir do supplier C
                           Nf=4-Nf;
                       }

                       // sobram Nf peças do tipo material, é preciso acrescentar essas peças
                       //à base de dados no dia em q chegam, neste caso, chegam daui a um dia.
                   }
               }
            }
        }
        data.processed_status(con, order_number); // mudar o estado desta ordem para processada
    }
    public void calculus(int num, int num1,int num2, int num3, int duedate)
    {
        num= (int)(num1/num2 + 0.5);
        num3 = duedate-num-1;
    }
    public int verify_pieces(String X)
    {
        //verificar se ha peças do tipo X, se há retorna 1, senao retorna 0;

        return 1;
    }
    public int verify_how_many(String X, int Ne) throws SQLException {
        //verificar quantas peças livres X há dia Ne
        DataBase data=new DataBase();
        Connection con=data.create_connection();
        int n=data.check_pieces(con, X, Ne);
        return n;
    }
    public String verify_raw( String piece)
    {
        if(piece.equals("P6")==true || piece.equals("P8")==true)
        {
            return "P1";
        }
        else return "P2";
    }
    public int verify_material(String X, int Ne) {
        int n=0;
        // verificar se ha material para essa peça naquele dia

        return n;
    }

}

