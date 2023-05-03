import java.io.IOException;
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
    public int Ng;
    public int Nh;

    public int Ni;
    public Order ord; // a ordem a ser processada
    public int duedate; // duedate da ordem
    public String material; // raw material da peça
    public int[] work_days; // vetor de dias em que estamos a cozinhar a peça
    public int deliver_day; // dia em que a peça esta a ser transportada para a plataforma

    public void check() throws SQLException, IOException {

        /* A primeira coisa a ser feita é verificar a lista das ordens.
        * Essa lista contem colunas com todas as caracteristicas das ordens
        * incluindo uma coluna que indica se esta ordem ján foi processada ou não.
        * Uma ordem ser processada significa que já foi metida no calendario das
        * coisas a fazer aka cozinhar e ser transportada etc
        * */
        DataBase data=new DataBase();
        Connection con=data.create_connection();
        /* Com isto, verificamos a primeira ordem que está na lista de
        * ordens que se encontra num estado não processado e atribuimos-lhe
        * todos os seus atributos.
        * */
        ord= new Order();
        Account acc = new Account();
        ModBusTCP time= new ModBusTCP();
        String[] atr=new String[7];
        atr=data.order_not_processed(con);
        ord.Order_num= atr[0];
        ord.Client_name= atr[1];
        ord.Quantity=atr[2];
        ord.Work_Piece=atr[3];
        ord.DueDate=atr[4];
        ord.Late_Pen=atr[5];
        ord.Early_Pen=atr[6];
        //acabar atributos// tavares
        N=Integer.parseInt(ord.Quantity);
        duedate=Integer.parseInt(ord.DueDate);
        /*Aqui calculamos quantos dias vamos levar para fazer a encomenda e em que
        * dia temos de encomendar as peças.
        * FALTA VERIFICAR SE OS DIAS ESTÃO OCUPADOS E AS SOLUÇOES PARA
        * ESSES DILEMAS.(dentro da função calculus), basicamente
        * problemas para depois
        * */
        calculus(Nd,N,Nc, Ne,duedate, time.today);
        if(Ne==0)
        {
            /* Aqui verificamos se temos 0 dias para mandar vir o raw
            * Neste caso verificamos se só temos 0 dias, nesse caso
            * cancelamos a encomenda
            * FALTA APAGAR A ENCOMENDA DA DATABASE OU INDICAR DE ALGUMA FORMA
            * QUE ELA FOI CANCELADA (ordens canceladas têm a coluna canceled a 1)
            * */
            data.cancelling_order(con, ord.Order_num);
            return;
        }
        if(verify_how_many(ord.Work_Piece, (duedate-1))>0)
        {
            data.processed_status(con, ord.Order_num);
            /* A segunda coisa a fazer é verificar se já há peças feitas
            * do tipo que nos queremos no armazém, o mais provével é que
            * esta situação nunca aconteça.
            * Usamos esta função para verificar quantas há e depois
            * subtraimos a N(o numero total de peças para saber quantas faltam fazer */
            Na=verify_how_many(ord.Work_Piece,(duedate-1));
            N=N-Na;
            if(N<=0)
            {
                /*Este caso acontece quando há peças suficientes em
                * armazem para completar a ordem. Dito isto é apenas necessario
                * chamar funçao que te diz quais tranformçoes a fazer,
                * tools, os dias em que estao a fazer as coisas e
                * mandar para a base de dados organizada. Also, reservar na tabela
                *  do armazem Na peças para aquele dia.
                * */
                //inacabado
                int quantity=Integer.parseInt(ord.Quantity);//nr de peças q queremos
                int[] arr;
                arr=data.check_pieces(con,ord.Work_Piece, Ne);
                int existing=arr[0];//nr de peças existentes
                int reserved=arr[1];//nr de peças que estao reservadas
                int new_reserved=reserved+quantity; /*isto vai atualizar a tabela da warehouse e atualizar a coluna das peças reserdas*/
                data.reserving_pieces(con, ord.Work_Piece, Ne, new_reserved);//ja reservou adicionei as N peças necessarias para acabar a encomenda
                /* agora chamar a funçao que vai indicar qual a transformaçao a realizar*/
                String raw=verify_raw(ord.Work_Piece);
                String[] str=new String[2];
                str=data.transformation(con, raw, ord.Work_Piece);
                /* retorna num array as
                * transformaçoes e as tools necessarias para enviar pro mes*/

            }
            else//caso nao haja peças já prontas (ja transformadas) verificar raw material
            {
               material=verify_raw(ord.Work_Piece);
               /* Verifica qual é o melhor material para fazer cada peça e verifica
               * se este está disponivel no armazem naquele dia*/
               Nb=verify_material(material,Ne);
               N=N-Nb;
               if(N<=0)
               {
                   for(int n=Nd; n>0; n--)
                   {
                       work_days[Nd-n]=Ne-1-n;
                   }
                   /*Chamar funçao que lhe diz quais tranformçoes a fazer,
                   * tools, os dias em que estao a fazer as coisas e
                   * mandar para a base de dados organizada.
                   * */
                    //Inacabado
                   return;

               }
               /*Nesta secção vai ser avaliado qual é o supplier que deverá ser
               * escolhido conforme o número de dias que nos sobra para encomendar.
               * */
               if(Ne==1)
               {
                   /*Caso apenas nos reste 1 dia para fazer a encomenda, apenas podemos encomendar
                   * do supplier C e o minimo de peças a encomendar é 4 e por isso podem sobrar e
                   * é necessario dizer na base de dados que naquele dia vao chegar 4 peças e que
                   * NF peças estao livres.
                   * */
                   SupplierC(N,Ne);

               }
               else if(Ne==2)
               {
                   /*Caso sobrem 2 dias, há a possibilidade de encomendar do supplier B ou C.
                   * Dito isto é necessário avaliar qual será mais rentável.
                   *  A primeira opçao é pedir do C e levar uma penalidade a outra é mandar
                   * vir do B*/
                  Ng=acc.costSup(N, "C",material) + acc.Depre("C", material, Integer.parseInt(ord.DueDate), Ne);
                  Nh=acc.costSup(N, "B",material);
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
                   else
                   {
                       // mandar vir N peças do supplier B
                       Nf=8-Nf;
                   }
               }
               else if(Ne==3)
               {
                   /*Caso sobrem 3 dias, há a possibilidade de encomendar do supplier B ou C.
                    * Dito isto é necessário avaliar qual será mais rentável.
                    * A primeira opçao é pedir do C e levar uma penalidade a outra é mandar
                    * vir do B e tambem levar uma penalidade*/

                   Ng=acc.costSup(N, "C",material) + acc.Depre("C", material, Integer.parseInt(ord.DueDate), Ne);
                   Nh=acc.costSup(N, "B",material) +acc.Depre("B", material, Integer.parseInt(ord.DueDate), Ne);
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
                   else
                   {
                       // mandar vir N peças do supplier B
                       Nf=8-Nf;
                   }
               }
               else if(N==4)
               {
                   /*Caso sobrem 4 dias, há a possibilidade de encomendar do supplier B ou C.
                    * Dito isto é necessário avaliar qual será mais rentável.
                    * A primeira opçao é pedir do C e levar uma penalidade a outra é mandar
                    * vir do B e tambem levar uma penalidade e a ultima e do A*/

                   Ng=acc.costSup(N, "C",material) + acc.Depre("C", material, Integer.parseInt(ord.DueDate), Ne);
                   Nh=acc.costSup(N, "B",material) + acc.Depre("B", material, Integer.parseInt(ord.DueDate), Ne);
                   Ni=acc.costSup(N, "A",material);
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
                   else if(N>4 && N>=8)
                   {
                       if(Ni>Nh)
                       {
                           // mandar vir N peças do supplier B
                           Nf=8-Nf;
                       }
                       else
                       {
                           //mandar vir do supplier A
                           Nf=16-Nf;
                       }

                   }

               }
               else
               {
                   /*Caso sobrem 4 dias, há a possibilidade de encomendar do supplier B ou C.
                    * Dito isto é necessário avaliar qual será mais rentável.
                    * A primeira opçao é pedir do C e levar uma penalidade a outra é mandar
                    * vir do B e tambem levar uma penalidade e a ultima e do A
                    * */
                   Ng=acc.costSup(N, "C",material) + acc.Depre("C", material, Integer.parseInt(ord.DueDate), Ne);
                Nh=acc.costSup(N, "B",material) + acc.Depre("B", material, Integer.parseInt(ord.DueDate), Ne);
                Ni=acc.costSup(N, "A",material) + acc.Depre("A", material, Integer.parseInt(ord.DueDate), Ne);
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
                else if(N>4 && N>=8)
                {
                    if(Ni>Nh)
                    {
                        // mandar vir N peças do supplier B
                        Nf=8-Nf;
                    }
                    else
                    {
                        //mandar vir do supplier A
                        Nf=16-Nf;
                    }

                }

            }
            }
        }
        data.processed_status(con, ord.Order_num); // mudar o estado desta ordem para processada
    }
    public void calculus(int num, int num1,int num2, int num3, int duedate,int today)
    {
        num= (int)(num1/num2 + 0.5);
        num3 = duedate-today-num-1;
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
        int[] arr;
        arr=data.check_pieces(con,X, Ne);
        int existing=arr[0];
        int reserved=arr[1];
        int n=existing-reserved;
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
    public void days()
    {

    }
    public void SupplierC(int N,int Ne)
    {
        if(N<4)
        {
            Nf=4-N;
            /*encomendar 4*/
            /* tavares, pfv acrescentar estas Nf peças do tipo X ao dia atual+1*/
        }
        else
        {
            /*encomendar N*/
        }
    }


}

