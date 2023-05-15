import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Managment implements Runnable {
    public int N;// numero de peças a fazer
    public int Na;// numero de peças já feitas no armazem do tipo que nos queremos
    public int Nb;// numero de peças de raw material que ha naquele dia
    public int Nc;// numero de peças a fazer por dia
    public int Nd;// numero de dias que tens para fazer a peça
    public int Ne;// numero que dias que tens no maximo para encomendar peças
    public int Nf; // número de peças que sobram
    public int Ng;
    public int Nh;
    public int Nj; // dia em que a peça começa a ser feita

    public int Ni;
    public Order ord; // a ordem a ser processada
    public int duedate; // duedate da ordem
    public String material; // raw material da peça
    public int[] work_days; // vetor de dias em que estamos a cozinhar a peça
    public int deliver_day; // dia em que a peça esta a ser transportada para a plataforma
    public ModBusTCP server;


    public void run() {


        while(true){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        /* A primeira coisa a ser feita é verificar a lista das ordens.
        * Essa lista contem colunas com todas as caracteristicas das ordens
        * incluindo uma coluna que indica se esta ordem já foi processada ou não.
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
        String[] atr=new String[7];
            try {
                atr=data.order_not_processed(con);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
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
        calculus(Nd,N,Nc, Ne,duedate, server.today, ord.Work_Piece);
        /*Basicamente aqui tens de verificar se os dias que estao programados para fazer
        peças estao reservados, caso estejam começa a distribuir pelos dias livres
        * o Ne diz te o dia em que vais começar a cozinhar e prolonga se ate à duedate-1*/

        /*Also, acho que vamos cagar no caso de conseguir várias encomendas
         *ao mesmo tempo por isso mete so uma por dia para ja
        * */


        /*Depois disto, verificas se está a ocorrer algum transporte nesse dia, se sim adias a duedate ate
        teres um dia livre, podes fazer um while em que dentro verificas e fazes duedate++;
        * */
        if(Ne==0)
        {
            /* Aqui verificamos se temos 0 dias para mandar vir o raw
            * Neste caso verificamos se só temos 0 dias, nesse caso
            * cancelamos a encomenda
            * apaga-se A ENCOMENDA DA DATABASE OU INDICAR DE ALGUMA FORMA
            * QUE ELA FOI CANCELADA (ordens canceladas têm a coluna canceled a 1)
            * */
            try {
                data.cancelling_order(con, ord.Order_num);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        //if(verify_how_many(ord.Work_Piece, (duedate-1))>0) //verifica se ha já peças prontas (transformadas)
       // {

            try {
                data.processed_status(con, ord.Order_num);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            //aqui criar as linhas com as peças


            /* A segunda coisa a fazer é verificar se já há peças feitas
            * do tipo que nos queremos no armazém, o mais provével é que
            * esta situação nunca aconteça.
            * Usamos esta função para verificar quantas há e depois
            * subtraimos a N(o numero total de peças para saber quantas faltam fazer
            * */
            try {
                Na=verify_how_many(ord.Work_Piece,(duedate-1));//verifica se ha já peças prontas (transformadas)
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            int num=N;
            N=N-Na;

            if(N<=0) { // CASO JÁ HAJA TODAS AS PEÇAS PRETENDIDAS JÁ TRANSFORMADAS E PRONTAS
                /*Also, reservar na tabela
                 *  do armazem Na peças para aquele dia.
                 * */

                int quantity = Integer.parseInt(ord.Quantity);//nr de peças já prontas q queremos
                int[] arr;
                try {
                    arr = data.check_pieces(con, ord.Work_Piece, Ne);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                int existing = arr[0];//nr de peças existentes
                int reserved = arr[1];//nr de peças que estao reservadas
                int new_reserved = reserved + quantity; /*isto vai atualizar a tabela da warehouse e atualizar a coluna das peças reserdas*/
                try {
                    data.reserving_pieces(con, ord.Work_Piece, Ne, new_reserved);//ja reservou adicionei as N peças necessarias para acabar a encomenda
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            }
            else//caso nao haja peças já prontas suficientes (ja transformadas) verificar raw material
            {
                if(Na!=0){
                  //1º reservar as peças transformadas já existentes
                  int[] arr;
                    try {
                        arr = data.check_pieces(con, ord.Work_Piece, Ne);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    int existing = arr[0];//nr de peças existentes
                  int reserved = arr[1];//nr de peças que estao reservadas
                  int new_reserved = reserved + Na; /*isto vai atualizar a tabela da warehouse e atualizar a coluna das peças reservadas*/
                    try {
                        data.reserving_pieces(con, ord.Work_Piece, Ne, new_reserved);//ja reservou adicionei as N peças necessarias para acabar a encomenda
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }

                  material = verify_raw(ord.Work_Piece);

                /* ----TABELA PIECES--------
                 *
                 * Neste caso, Na peças tem type_1=material
                 * */
                for (int i = 0; i < N; i++){
                    try {
                        data.piece(con, atr[0], material, Integer.toString(Integer.parseInt(atr[4])-1));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }

                /* Verifica qual é o melhor material para fazer cada peça e verifica
                   * se este está disponivel no armazem naquele dia*/

                try {
                    Nb = verify_material(material, Ne); //verificar se há raw material no dia Ne
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                N = N - Nb;
              }
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

                   String raw=verify_raw(ord.Work_Piece); //peça origem
                   /*
                   String[] str=new String[2];
                   str=data.transformation(con, raw, ord.Work_Piece); //work_piece é a peça final
                   String time=str[0];//tempo de transformaçao
                   String tool=str[1];//tool da transformaçao*/

                   String[] arr=new String[18];
                   try {
                       arr=data.info(con, material, ord.Work_Piece);
                   }
                   catch (SQLException e) {
                       throw new RuntimeException(e);
                   }

                   /*Sabendo que temos Nd dias para fazer as peças.
                    * */
                   try {
                       data.piece_update(con, ord.Order_num, material, arr[1], arr[2], arr[3], arr[4], arr[5], arr[6], arr[7], arr[8], arr[9], arr[10], arr[11], arr[12], arr[13], arr[14], arr[15], arr[16], ord.DueDate, String.valueOf(Ne+server.today));
                   } catch (SQLException e) {
                       throw new RuntimeException(e);
                   }
                   

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
                   if(N<4)
                   {
                       Nf=4-N; //encomendas 4
                       // teu armazem naquele dia vai ficar com Nf livres e N ocupadas

                   }
                   else
                   {
                       Nf=0; //endomendas N
                       // o teu armazem naquele dia vai ficar com N ocupadas
                   }
                   /*encomendar 4 peças do tipo raw_material */
                   // ??

                   /* tavares, pfv acrescentar estas Nf peças do tipo X ao dia atual+1 ( ao armazem(?) )*/
                   // eliminei a funçao supplier c porque achei q n fazia sentido agr é so fazer o mesmo em todos
                   data = new DataBase();
                   con = data.create_connection();
                   int[] arr;
                   try {
                       arr=data.check_pieces(con,material,server.today);//???
                   } catch (SQLException e) {
                       throw new RuntimeException(e);
                   }
                   int actual_existing=arr[0];
                   actual_existing=actual_existing+Nf;
                   try {
                       data.arriving_new_pieces(con, material, server.today, actual_existing);
                   } catch (SQLException e) {
                       throw new RuntimeException(e);
                   }
                   //aqui o dia atual vai depender de como está o contador, VER ISTO


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
                        //custo do supplier = acc.costSup(N, "B",material)/N
                        Nf=8-Nf;
                    }
                    else
                    {
                        //mandar vir do supplier C
                        //custo do supplier
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

        //}


            try {
                data.processed_status(con, ord.Order_num); // mudar o estado desta ordem para processada
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        /* Criar uma coluna na ordem que é uma data
        prevista de duedate que é o valor que esta neste momento na duedate*/
    }
}
public void calculus(int num, int num1,int num2, int num3, int duedate,int today, String Workpiece)
    {
        if((Workpiece.equals("P3") && (num1==1 || num1==2 || num1==3)) ||
                (Workpiece.equals("P4") && (num1==1 || num1==2  || num1==3))||
                    (Workpiece.equals("P6") && (num1==1)) ||
                         (Workpiece.equals("P7") && (num1==1 || num1==2)))
        {
            num=1;
        }
        else if((Workpiece.equals("P3") && (num1==4 || num1==5 || num1==6 || num1==7 || num1==8)) ||
                    (Workpiece.equals("P4") && (num1==4 || num1==5 || num1==6 || num1==7 || num1==8))||
                        (Workpiece.equals("P5") && (num1==1 || num1==2 || num1==3)) ||
                            (Workpiece.equals("P6") && (num1==4 || num1==2 || num1==3)) ||
                                (Workpiece.equals("P7") && ( num1==3 ||num1==4 || num1==5 || num1==6 || num1==7 || num1==8)) ||
                                    (Workpiece.equals("P8") && (num1==1)) ||
                                        (Workpiece.equals("P9") && (num1==4 ||num1==1 || num1==2 || num1==3)))
        {
            num=2;
        }
        else if ((Workpiece.equals("P5") && (num1==4 || num1==5 || num1==6 || num1==7 || num1==8)) ||
                    (Workpiece.equals("P6") && ( num1==5 || num1==6 || num1==7))||
                        (Workpiece.equals("P8") && (num1==4 || num1==2 || num1==3)) ||
                            (Workpiece.equals("P9") && ( num1==5 || num1==6 || num1==7 || num1==8)))
        {
            num=3;
        }
        else if((Workpiece.equals("P6") && (num1==8)) ||
                    (Workpiece.equals("P8") && ( num1==5 || num1==6 || num1==7)))
        {
            num=4;
        }
        else
        {
            num=5;
        }

        num3 = duedate - today - num - 1;
        while(num3>duedate)
        {
            duedate++;
        }

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
    public int verify_material(String X, int Ne) throws SQLException {
        // verificar se ha raw material para essa peça naquele dia
        DataBase data=new DataBase();
        Connection con=data.create_connection();
        int[] arr;
        arr=data.check_pieces(con,X, Ne);
        int existing=arr[0];
        int reserved=arr[1];
        int n=existing-reserved;
        return n;
    }
    public void days()
    {

    }

}

