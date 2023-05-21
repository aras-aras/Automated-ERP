import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

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
    public int Ta=4; //tempo que cada fornecedor demora a mandar vir as cenas
    public int Tb=2;
    public int Tc=1;
    public int today;


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

            try {
                if(data.order_not_processed(con)!=null) {
                    System.out.println("Starting processing");
                    ord = new Order();
                    Account acc = new Account();
                    String[] atr = new String[7];
                    try {
                        atr = data.order_not_processed(con);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    ord.Order_num = atr[0];
                    ord.Client_name = atr[1];
                    ord.Quantity = atr[2];
                    ord.Work_Piece = atr[3];
                    ord.DueDate = atr[4];
                    ord.Late_Pen = atr[5];
                    ord.Early_Pen = atr[6];
                    //acabar atributos// tavares
                    N = Integer.parseInt(ord.Quantity);
                    duedate = Integer.parseInt(ord.DueDate);
                    /*Aqui calculamos quantos dias vamos levar para fazer a encomenda e em que
                     * dia temos de encomendar as peças.
                     * FALTA VERIFICAR SE OS DIAS ESTÃO OCUPADOS E AS SOLUÇOES PARA
                     * ESSES DILEMAS.(dentro da função calculus), basicamente
                     * problemas para depois
                     * */

                    today=data.today_day(con);
                    int[] nr;
                    material = verify_raw(ord.Work_Piece);
                    System.out.println("material: "+material+" peça final: "+ord.Work_Piece);
                    String sub=data.sub_trans(con, material, ord.Work_Piece);

                    nr=calculus(N, Ne, duedate, today, ord.Work_Piece, material, sub, Integer.parseInt(ord.Quantity));
                    System.out.println("banana");

                    //aux[0]- numero total de dias em que as peças estao e produção
                    //aux[1]- a data em que começam a ser feitas
                    //aux[2]- duedate
                    //0_._._._.4.4.1.1.d
                    System.out.println("Result from calculating dates for order "+ord.Order_num);
                    System.out.println("number of days needed to produce:"+nr[0]);
                    System.out.println("day you need to start working"+(nr[1]+today));
                    System.out.println("day the material arrives"+(nr[1]+today-1));
                    Nd=nr[0];
                    Ne=nr[1];
                    Ne=Ne-1;
                    duedate=nr[2];
                    int size=nr.length-3;
                    int[] days_work = new int[size];
                    for(int i=0; i<size; i++){
                        days_work[i]=nr[3+i];
                    }
            /*Basicamente aqui tens de verificar se os dias que estao programados para fazer
            peças estao reservados, caso estejam começa a distribuir pelos dias livres
            * o Ne diz te o dia em que vais começar a cozinhar e prolonga se ate à duedate-1*/
                    /*Also, acho que vamos cagar no caso de conseguir várias encomendas
                     *ao mesmo tempo por isso mete so uma por dia para ja
                     * */
                /*Depois disto, verificas se está a ocorrer algum transporte nesse dia, se sim adias a duedate ate
                teres um dia livre, podes fazer um while em que dentro verificas e fazes duedate++;
                * */

                    if (Ne == 0) {
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



                    try {
                        data.processed_status(con, ord.Order_num);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }


                    /*Como ja fizemos o calculos as transformaçoes ja d«foram divididas pelos
                    * dias, ou seja, consegues sacar todos os dias dessa função. Dito isto, dentro de cada
                    * ifs anteriores fazes um for e de cada vez que crias a linha preenches ja tudo*/

                    /* A segunda coisa a fazer é verificar se já há peças feitas
                     * do tipo que nos queremos no armazém, o mais provével é que
                     * esta situação nunca aconteça.
                     * Usamos esta função para verificar quantas há e depois
                     * subtraimos a N(o numero total de peças para saber quantas faltam fazer
                     * */
                    try {
                        Na = verify_how_many(ord.Work_Piece, (duedate - 1));//verifica se ha já peças prontas (transformadas)
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    if(Na!=-1) {
                        int num = N;
                        N = N - Na;
                        if (N <= 0) { // CASO JÁ HAJA TODAS AS PEÇAS PRETENDIDAS JÁ TRANSFORMADAS E PRONTAS
                            /*Also, reservar na tabela
                             *  do armazem Na peças para aquele dia.
                             * */
                            int quantity = Integer.parseInt(ord.Quantity);//nr de peças já prontas q queremos
                            int[] arr;
                            try {
                                arr = data.check_pieces(con, ord.Work_Piece, String.valueOf(Ne));
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            int existing = arr[0];//nr de peças existentes
                            int reserved = arr[1];//nr de peças que estao reservadas
                            int new_reserved = reserved + quantity; /*isto vai atualizar a tabela da warehouse e atualizar a coluna das peças reserdas*/
                            try {
                                data.reserving_pieces(con, ord.Work_Piece, Ne, new_reserved);//ja reservou adicionei as N peças necessarias para acabar a encomenda
                                System.out.println("gabi");
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }

                        }
                        else//caso nao haja peças já prontas suficientes (ja transformadas) verificar raw material
                        {
                            if (Na != 0) {//numero de peças já feitas no armazem do tipo que nos queremos
                                //1º reservar as peças transformadas já existentes
                                int[] arr;
                                try {
                                    arr = data.check_pieces(con, ord.Work_Piece, String.valueOf(Ne));
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
                            ////////CRIAR LINHAS COM TRANSFORMAÇOES//////////////

                            if(ord.Work_Piece.equals("P3")==true ||ord.Work_Piece.equals("P4")==true
                                    ||ord.Work_Piece.equals("P6")==true ||ord.Work_Piece.equals("P7")==true )
                            {
                                /* Estes tipos de peças so precisam de uma linha por peça na
                                 * tabela piece_trans, basicamente aqui crias N linhas */
                                for (int i = 0; i < N; i++) {
                                    try {
                                        data.piece(con, ord.Order_num, material, Integer.toString(today+Integer.parseInt(ord.DueDate) - 1));
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                            else
                            {
                                /*Neste caso estas peças precisam de 2 dias para serem acabadas, ou seja
                                 * crias, N*2 linhas*/
                                for (int i = 0; i < 2*N; i++) {
                                    try {
                                        data.piece(con, ord.Order_num, material, Integer.toString(today+Integer.parseInt(ord.DueDate) - 1));
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }


                            /* Verifica qual é o melhor material para fazer cada peça e verifica
                             * se este está disponivel no armazem naquele dia*/

                            try {
                                Nb = verify_material(material, Ne); //verificar se há raw material no dia Ne, guarda em Nb o nr de raw material existente
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            N = N - Nb;
                        }
                    }
                    else{
                        if(ord.Work_Piece.equals("P3")==true ||ord.Work_Piece.equals("P4")==true
                                ||ord.Work_Piece.equals("P6")==true ||ord.Work_Piece.equals("P7")==true )
                        {
                            /* Estes tipos de peças so precisam de uma linha por peça na
                             * tabela piece_trans, basicamente aqui crias N linhas */
                            for (int i = 0; i < N; i++) {
                                try {
                                    data.piece(con, ord.Order_num, material, Integer.toString(today+Integer.parseInt(ord.DueDate) - 1));
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        else
                        {
                            /*Neste caso estas peças precisam de 2 dias para serem acabadas, ou seja
                             * crias, N*2 linhas*/
                            for (int i = 0; i < 2*N; i++) {
                                try {
                                    data.piece(con, ord.Order_num, material, Integer.toString(today+Integer.parseInt(ord.DueDate) - 1));
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }

                        try {
                            Nb = verify_material(material, Ne); //verificar se há raw material no dia Ne, guarda em Nb o nr de raw material existente
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        if(Nb==-1){
                        N=N;
                        }
                        else {
                            N = N - Nb;
                        }
                    }
                    if (N <= 0)// se houver raw material suficiente para cobrir o que falta é so indicar as transformações a fazer
                    {
                        work_days=writing_order(Nd, Ne, material, ord.Work_Piece, ord.Order_num, duedate, days_work);
                        System.out.println("dias de trabalho:"+work_days);
                    }
                    /*Nesta secção vai ser avaliado qual é o supplier que deverá ser
                     * escolhido conforme o número de dias que nos sobra para encomendar.
                     * */
                    if (Ne == 1) {
                        /*Caso apenas nos reste 1 dia para fazer a encomenda, apenas podemos encomendar
                         * do supplier C e o minimo de peças a encomendar é 4 e por isso podem sobrar e
                         * é necessario dizer na base de dados que naquele dia vao chegar 4 peças e que
                         * NF peças estao livres.
                         * */
                        if (N < 4) {
                            Nf = 4 - N; //encomendas 4 peças ao supplier C do tipo raw material X
                            if (material.equals("P1")) {
                                String aux = "p1";
                                try {
                                    today=data.today_day(con);
                                    data.sup(con, String.valueOf(Ne + today-Tc), "sc_" + aux, String.valueOf(4));
                                    data.just_arrived(con, aux, Ne+today, 4, ord.Order_num);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                String aux = "p2";
                                try {
                                    today=data.today_day(con);
                                    data.sup(con, String.valueOf(Ne + today-Tc), "sc_" + aux, String.valueOf(4));
                                    data.just_arrived(con, aux, Ne+today, 4, ord.Order_num);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            // teu armazem naquele dia vai ficar com Nf livres e N ocupadas
                            int[] aux;
                            if (material.equals("P1")) {
                                try {
                                    today=data.today_day(con);
                                    aux = data.check_pieces(con, "p1", String.valueOf(Ne + today));
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                int exit = aux[0];
                                int res = aux[1];
                                try {
                                    today=data.today_day(con);
                                    data.arriving_new_pieces(con, "p1", Ne + today, Nf + exit + N, ord.Order_num);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                try {
                                    today=data.today_day(con);
                                    data.reserving_pieces(con, "p1", Ne + today, res + N);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                try {
                                    today=data.today_day(con);
                                    aux = data.check_pieces(con, "p2", String.valueOf(Ne + today));
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                int exit = aux[0];
                                int res = aux[1];
                                try {
                                    today=data.today_day(con);
                                    data.arriving_new_pieces(con, "p2", Ne + today, Nf + exit + N, ord.Order_num);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                try {
                                    today=data.today_day(con);
                                    data.reserving_pieces(con, "p2", Ne + today, res + N);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                        } else {
                            Nf = 0; //endomendas N
                            if (material.equals("P1")) {
                                String aux = "p1";
                                try {
                                    today=data.today_day(con);
                                    data.sup(con, String.valueOf(Ne + today-Tc), "sc_p1" + aux, String.valueOf(N));
                                    data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                String aux = "p2";
                                try {
                                    today=data.today_day(con);
                                    data.sup(con, String.valueOf(Ne + today-Tc), "sc_p2", String.valueOf(N));
                                    data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            // o teu armazem naquele dia vai ficar com N ocupadas
                            int[] aux;
                            if (material.equals("P1")) {
                                try {
                                    today=data.today_day(con);
                                    aux = data.check_pieces(con, "p1", String.valueOf(Ne + today));
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                int exit = aux[0];
                                int res = aux[1];
                                try {
                                    today=data.today_day(con);
                                    data.arriving_new_pieces(con, "p1", Ne + today, N + exit, ord.Order_num);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                try {
                                    today=data.today_day(con);
                                    data.reserving_pieces(con, "p1", Ne + today, res + N);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                try {
                                    today=data.today_day(con);
                                    aux = data.check_pieces(con, "p2", String.valueOf(Ne + today));
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                if(aux!=null) {
                                    int exit = aux[0];
                                    int res = aux[1];
                                    try {
                                        today = data.today_day(con);
                                        data.arriving_new_pieces(con, "p2", Ne + today, N + exit, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                    try {
                                        today = data.today_day(con);
                                        data.reserving_pieces(con, "p2", Ne + today, res + N);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }

                                }
                            }
                        }
                        data = new DataBase();
                        con = data.create_connection();
                        int[] arr;
                        try {
                            today=data.today_day(con);
                            arr = data.check_pieces(con, material, String.valueOf(today));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        work_days=writing_order(Nd, Ne, material, ord.Work_Piece, ord.Order_num, duedate, days_work);
                        System.out.println("dias de trabalho:"+work_days);
                    } else if (Ne == 2) {
                        /*Caso sobrem 2 dias, há a possibilidade de encomendar do supplier B ou C.
                         * Dito isto é necessário avaliar qual será mais rentável.
                         *  A primeira opçao é pedir do C e levar uma penalidade a outra é mandar
                         * vir do B*/
                        Ng = acc.costSup(N, "C", material) + acc.Depre("C", material, Integer.parseInt(ord.DueDate), Ne);
                        Nh = acc.costSup(N, "B", material);
                        if (N <= 4) {
                            if (Ng > Nh) {
                                // mandar vir N peças do supplier B
                                Nf = 8 - Nf;
                                if (material.equals("P1")) {
                                    String aux = "p1";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            } else {
                                //mandar vir do supplier C
                                Nf = 4 - Nf;
                                if (material.equals("P1")) {
                                    String aux = "p1";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tc), "sc_" + aux, String.valueOf(4));
                                        data.just_arrived(con, aux, Ne+today, 4, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tc), "sc_" + aux, String.valueOf(4));
                                        data.just_arrived(con, aux, Ne+today, 4, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }

                            // sobram Nf peças do tipo material, é preciso acrescentar essas peças
                            //à base de dados no dia em q chegam, neste caso, chegam daui a um dia.
                            int[] aux;
                            if (material.equals("P1")) {
                                try {
                                    today=data.today_day(con);
                                    aux = data.check_pieces(con, "p1", String.valueOf(today + 1));
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                int exit = aux[0];
                                int res = aux[1];
                                try {
                                    today=data.today_day(con);
                                    data.arriving_new_pieces(con, "p1", 1 + today, Nf + exit + N, ord.Order_num);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                try {
                                    today=data.today_day(con);
                                    data.reserving_pieces(con, "p1", 1 + today, res + N);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                try {
                                    today=data.today_day(con);
                                    aux = data.check_pieces(con, "p2", String.valueOf(1 + today));
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                int exit = aux[0];
                                int res = aux[1];
                                try {
                                    today=data.today_day(con);
                                    data.arriving_new_pieces(con, "p2", 1 + today, Nf + exit + N, ord.Order_num);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                try {
                                    today=data.today_day(con);
                                    data.reserving_pieces(con, "p2", 1 + today, res + N);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        } else {
                            // mandar vir N peças do supplier B
                            Nf = 8 - Nf;
                            if (material.equals("P1")) {
                                String aux = "p1";
                                try {
                                    today=data.today_day(con);
                                    data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                    data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                String aux = "p2";
                                try {
                                    today=data.today_day(con);
                                    data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                    data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        work_days=writing_order(Nd, Ne, material, ord.Work_Piece, ord.Order_num,duedate, days_work);
                        System.out.println("dias de trabalho:"+work_days);
                    } else if (Ne == 3) {
                        /*Caso sobrem 3 dias, há a possibilidade de encomendar do supplier B ou C.
                         * Dito isto é necessário avaliar qual será mais rentável.
                         * A primeira opçao é pedir do C e levar uma penalidade a outra é mandar
                         * vir do B e tambem levar uma penalidade*/

                        Ng = acc.costSup(N, "C", material) + acc.Depre("C", material, Integer.parseInt(ord.DueDate), Ne);
                        Nh = acc.costSup(N, "B", material) + acc.Depre("B", material, Integer.parseInt(ord.DueDate), Ne);
                        if (N <= 4) {
                            if (Ng > Nh) {
                                // mandar vir N peças do supplier B
                                Nf = 8 - Nf;
                                if (material.equals("P1")) {
                                    String aux = "p1";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            } else {
                                //mandar vir do supplier C
                                Nf = 4 - Nf;
                                if (material.equals("P1")) {
                                    String aux = "p1";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tc), "sc_" + aux, String.valueOf(4));
                                        data.just_arrived(con, aux, Ne+today, 4, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tc), "sc_" + aux, String.valueOf(4));
                                        data.just_arrived(con, aux, Ne+today, 4, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }

                            // sobram Nf peças do tipo material, é preciso acrescentar essas peças
                            //à base de dados no dia em q chegam, neste caso, chegam daui a um dia.

                            int[] aux;
                            if (material.equals("P1")) {
                                try {
                                    today=data.today_day(con);
                                    aux = data.check_pieces(con, "p1", String.valueOf(today + 1));
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                int exit = aux[0];
                                int res = aux[1];
                                try {
                                    today=data.today_day(con);
                                    data.arriving_new_pieces(con, "p1", 1 + today, Nf + exit + N, ord.Order_num);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                try {
                                    today=data.today_day(con);
                                    data.reserving_pieces(con, "p1", 1 + today, res + N);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                try {
                                    today=data.today_day(con);
                                    aux = data.check_pieces(con, "p2", String.valueOf(1 + today));
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                int exit = aux[0];
                                int res = aux[1];
                                try {
                                    today=data.today_day(con);
                                    data.arriving_new_pieces(con, "p2", 1 + today, Nf + exit + N, ord.Order_num);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                try {
                                    today=data.today_day(con);
                                    data.reserving_pieces(con, "p2", 1 + today, res + N);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        } else {
                            // mandar vir N peças do supplier B
                            Nf = 8 - Nf;
                            if (material.equals("P1")) {
                                String aux = "p1";
                                try {
                                    today=data.today_day(con);
                                    data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                    data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                String aux = "p2";
                                try {
                                    today=data.today_day(con);
                                    data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                    data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        work_days=writing_order(Nd, Ne, material, ord.Work_Piece, ord.Order_num, duedate, days_work);
                        System.out.println("dias de trabalho:"+work_days);
                    } else if (Ne == 4) {
                        /*Caso sobrem 4 dias, há a possibilidade de encomendar do supplier B ou C.
                         * Dito isto é necessário avaliar qual será mais rentável.
                         * A primeira opçao é pedir do C e levar uma penalidade a outra é mandar
                         * vir do B e tambem levar uma penalidade e a ultima e do A*/

                        Ng = acc.costSup(N, "C", material) + acc.Depre("C", material, Integer.parseInt(ord.DueDate), Ne);
                        Nh = acc.costSup(N, "B", material) + acc.Depre("B", material, Integer.parseInt(ord.DueDate), Ne);
                        Ni = acc.costSup(N, "A", material);
                        if (N <= 4) {
                            if (Ng > Nh) {
                                // mandar vir N peças do supplier B
                                Nf = 8 - Nf;
                                if (material.equals("P1")) {
                                    String aux = "p1";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            } else {
                                //mandar vir do supplier C
                                Nf = 4 - Nf;
                                if (material.equals("P1")) {
                                    String aux = "p1";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tc), "sc_" + aux, String.valueOf(4));
                                        data.just_arrived(con, aux, Ne+today, 4, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tc), "sc_" + aux, String.valueOf(4));
                                        data.just_arrived(con, aux, Ne+today, 4, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }

                            // sobram Nf peças do tipo material, é preciso acrescentar essas peças
                            //à base de dados no dia em q chegam, neste caso, chegam daui a um dia.
                            int[] aux;
                            if (material.equals("P1")) {
                                try {
                                    today=data.today_day(con);
                                    aux = data.check_pieces(con, "p1", String.valueOf(today + 1));
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                int exit = aux[0];
                                int res = aux[1];
                                try {
                                    today=data.today_day(con);
                                    data.arriving_new_pieces(con, "p1", 1 + today, Nf + exit + N, ord.Order_num);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                try {
                                    today=data.today_day(con);
                                    data.reserving_pieces(con, "p1", 1 + today, res + N);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                try {
                                    today=data.today_day(con);
                                    aux = data.check_pieces(con, "p2", String.valueOf(1 + today));
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                int exit = aux[0];
                                int res = aux[1];
                                try {
                                    today=data.today_day(con);
                                    data.arriving_new_pieces(con, "p2", 1 + today, Nf + exit + N, ord.Order_num);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                try {
                                    today=data.today_day(con);
                                    data.reserving_pieces(con, "p2", 1 + today, res + N);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        } else if (N > 4 && N >= 8) {
                            if (Ni > Nh) {
                                // mandar vir N peças do supplier B
                                Nf = 8 - Nf;
                                if (material.equals("P1")) {
                                    String aux = "p1";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            } else {
                                //mandar vir do supplier A
                                Nf = 16 - Nf;
                                if (material.equals("P1")) {
                                    String aux = "p1";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Ta), "sa_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Ta), "sa_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }

                        }
                        work_days=writing_order(Nd, Ne, material, ord.Work_Piece, ord.Order_num, duedate, days_work);
                        System.out.println("dias de trabalho:"+work_days);
                    } else if(Ne>4){

                        /*Caso sobrem mais de 4 dias, há a possibilidade de encomendar do supplier A, B ou C.
                         * Dito isto é necessário avaliar qual será mais rentável.
                         * A primeira opçao é pedir do C e levar uma penalidade a outra é mandar
                         * vir do B e tambem levar uma penalidade e a ultima e do A
                         * */
                        Ng = acc.costSup(N, "C", material) + acc.Depre("C", material, Integer.parseInt(ord.DueDate), Ne);
                        Nh = acc.costSup(N, "B", material) + acc.Depre("B", material, Integer.parseInt(ord.DueDate), Ne);
                        Ni = acc.costSup(N, "A", material) + acc.Depre("A", material, Integer.parseInt(ord.DueDate), Ne);

                        if (N <= 4) {

                            if (Ng > Nh) {

                                // mandar vir N peças do supplier B
                                if (material.equals("P1")) {

                                    String aux = "p1";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Ta), "sa_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {

                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Ta), "sa_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                //custo do supplier = acc.costSup(N, "B",material)/N
                                Nf = 8 - Nf;
                            } else {

                                //mandar vir do supplier C
                                if (material.equals("P1")) {

                                    String aux = "p1";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tc), "sc_" + aux, String.valueOf(4));
                                        data.just_arrived(con, aux, Ne+today, 4, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {

                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tc), "sc_" + aux, String.valueOf(4));
                                        data.just_arrived(con, aux, Ne+today, 4, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                //custo do supplier
                                Nf = 4 - Nf;
                            }

                            // sobram Nf peças do tipo material, é preciso acrescentar essas peças
                            //à base de dados no dia em q chegam, neste caso, chegam daui a um dia.
                        } else if (N > 4 && N <= 8) {

                            if (Ni > Nh) {

                                // mandar vir N peças do supplier B
                                Nf = 8 - Nf;
                                if (material.equals("P1")) {

                                    String aux = "p1";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {

                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            } else {

                                //mandar vir do supplier A
                                Nf = 16 - Nf;
                                if (material.equals("P1")) {

                                    String aux = "p1";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Ta), "sa_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {

                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Ta), "sa_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N, ord.Order_num);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }

                        }

                    }
                    try {
                        work_days=writing_order(Nd, Ne, material, ord.Work_Piece, ord.Order_num, duedate, days_work);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("dias de trabalho:"+work_days);
                    try {
                        data.processed_status(con, ord.Order_num); // mudar o estado desta ordem para processada
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        data.leaving(con,today+duedate-1,ord.Order_num);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("dia de sair do armazem:"+(today+duedate-1));
                }


            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public int[] piece_perday(String origin, String end, String sub) {/* Da te quantas peças faz por dia(desde q sai ate entrar no armazem), se for 0.5
     * uma peça demora dois dias*/
        //RETORNA O Nc-> nr de peças a fazer por dia
        int[] p = new int[2];
        if (origin.equals("P2") == true) {
            if (end.equals("P3") == true) {
                p[0] = 3;
                p[1] = 0;
            }
            if (end.equals("P7") == true) {
                p[0] = 2;
                p[1] = 0;
            }
            if (sub.equals("P7") == true) {
                if (end.equals("P9") == true) {
                    p[0] = 2;
                    p[1] = 3;
                } else if (end.equals("P5") == true) {
                    p[0] = 2;
                    p[1] = 2;
                }
            }
            if (end.equals("P4") == true) {
                p[0] = 3;
                p[1] = 0;
            }
        }
        else if (origin.equals("P1") == true) {

            if (end.equals("P6") == true) {
                p[0] = 1;
                p[1] = 0;
            } else if (sub.equals("P6") == true) {
                if (end.equals("P8") == true) {
                    p[0] = 1;
                    p[1] = 1;
                }
            }
        } else {
            return null;
        }
            return p;
    }
public int[] calculus(int num1, int num3, int duedate,int today, String Workpiece, String material, String sub, int quantity)  {
        //calculus(N, Ne, duedate, today, ord.Work_Piece);
    /*aux[0]->Nd  // numero de dias que tens para fazer a peça
    aux[1]->Ne // numero que dias que tens no maximo para encomendar peças, aka quando tenho de começar a produzir
    aux[2]->duedate
    Nd=nr[0];
    Ne=nr[1];
    duedate=nr[2];*/
    int[] f=piece_perday(material, Workpiece, sub);
    System.out.println("numero de peças que conseguimos fazer " +
            "na primeira iteração: "+f[0]+" e com a segunda "+f[1]);
    int n1=f[0];//nr de peças que consigo fazer por dia na primeira transformação
    int n2=f[1];//nr de peças que consigo fazer por dia na segunda transformação
    int nr_days2,nr_days1;
    if(n1==0){
        nr_days1=0;
    }
    else if(n1% 2 != 0)
    {
        nr_days1 = (int) ((quantity / n1) + 0.5)+1;
    }
    else {
        nr_days1 = (int) ((quantity / n1) + 0.5);
    }
    if(n2==0){
        nr_days2=0;
    }
    else if(n2% 2 != 0)
    {
        nr_days2 = (int) ((quantity / n2) + 0.5)+1;
    }
    else {
        nr_days2 = (int) ((quantity / n2) + 0.5);
    }
    System.out.println("old duedate: " +duedate);
    int[] aux = new int[3];
    aux[0]= nr_days2 + nr_days1;
    System.out.println("dias necessarios para produzir: " +aux[0]);
    if(aux[0]>=duedate) {
        while (aux[0] > duedate) {
            duedate++;

        }
        duedate+=3;
    }
    System.out.println("new duedate: " +duedate);
    aux[1] = duedate - today - aux[0]-1;
    aux[2]=duedate;

    DataBase data=new DataBase();
    Connection con=data.create_connection();
    int[] work_daysnew = new int[aux[0]];
    try {
        work_days = data.calendar(con, today, aux, Workpiece, ord.Order_num);
    }catch (SQLException e) {
        throw new RuntimeException(e);
    }
    System.out.println(Arrays.toString(work_days));
    int length1=3;
    int length2=work_days.length;
    int [] concate=new int[length2+length1];
    System.arraycopy(aux, 0, concate, 0, length1);
    System.arraycopy(work_days, 0, concate, length1, length2);
    System.out.println("Vetor com calculos e dias de trabalho"+Arrays.toString(concate));
    return concate;
}

    public int verify_how_many(String X, int Ne) throws SQLException {
        //verificar quantas peças livres X há dia Ne
        DataBase data=new DataBase();
        Connection con=data.create_connection();
        int[] arr;
        int n;
        arr=data.check_pieces(con,X, String.valueOf(Ne));
        if(arr==null){
            n=-1;
        }
        else {
            int existing = arr[0];
            int reserved = arr[1];
            n = existing - reserved;
        }
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
        arr=data.check_pieces(con,X, String.valueOf(Ne));
        if(arr!=null){
        int existing=arr[0];
        int reserved=arr[1];
        int n=existing-reserved;
        return n;}
        else return -1;
    }
    public  int[] writing_order( int Nd, int Ne, String material, String work_piece, String order, int duedate, int[] work_days) throws SQLException{
        DataBase data=new DataBase();
        Connection con= data.create_connection();
        int day=0;
        /*Chamar funçao que lhe diz quais tranformçoes a fazer,
         * tools, os dias em que estao a fazer as coisas e
         * mandar para a base de dados organizada.
         * */
        String[] arr;
        try {
            arr = data.info(con, material, work_piece);
            for(int n=0;n<arr.length;n++)
            {
                System.out.println(arr[n]);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        /*Sabendo que temos Nd dias para fazer as peças.
         * */
        arr= prepare(work_piece,arr ,work_days, order);
        return work_days;
    }
    public String[]  prepare(String workpiece, String[] arr, int[]days, String order) throws SQLException {
        String [] arr1 = new String[0];
        String [] arr2    = new String[0];
        if(workpiece.equals("P8")==true || workpiece.equals("P6")==true ||workpiece.equals("P3")==true ||workpiece.equals("P4")==true )
        {
            arr1[1]= arr[1];
            arr1[2]= arr[2];
            arr1[3]= arr[3];
            arr1[4]= arr[4];
            arr1[5]= "0";
            arr1[6]= "0";
            arr1[7]= "0";
            arr1[8]= "0";

        }
        if(workpiece.equals("P7")==true || workpiece.equals("P5")==true ||workpiece.equals("P9")==true)
        {
            arr1[1]= arr[1];
            arr1[2]= arr[2];
            arr1[3]= arr[3];
            arr1[4]= arr[4];
            arr1[5]= arr[5];
            arr1[6]= arr[6];
            arr1[7]= arr[7];
            arr1[8]= arr[8];
        }
        if(workpiece.equals("P7")==true || workpiece.equals("P6")==true ||workpiece.equals("P3")==true ||workpiece.equals("P4")==true)
        {
            arr2[1]= "0";
        }
        if(workpiece.equals("P8")==true || workpiece.equals("P9")==true)
        {
            arr2[1]= arr[5];
            arr2[2]= arr[6];
            arr2[3]= arr[7];
            arr2[4]= arr[8];
            arr2[5]= "0";
            arr2[6]= "0";
            arr2[7]= "0";
            arr2[8]= "0";

        }
        if(workpiece.equals("P5")==true)
        {
            arr2[1]= arr[5];
            arr2[2]= arr[6];
            arr2[3]= arr[7];
            arr2[4]= arr[8];
            arr2[5]= arr[9];
            arr2[6]= arr[10];
            arr2[7]= arr[11];
            arr2[8]= arr[12];
        }

            update(arr1, arr2, days, order);

        return arr;
    }
    public void update(String[] arr1, String[] arr2, int[] days, String order) throws SQLException {
        DataBase data=new DataBase();
        Connection con= data.create_connection();
        int today = data.today_day(con);
        if (arr2[1].equals("0")==true)
        {
            for(int n=0; n<days.length; n++ )
            {
                data.piece_update(con, order, arr1[1], arr1[2]
                        , arr1[3], arr1[4], arr1[5], arr1[6], arr1[7], arr1[8],
                        String.valueOf(today+duedate-1), String.valueOf(days[n]));
            }
        }
        else
        {
            for(int n=0; n<days.length/2; n++ )
            {
                data.piece_update(con, order, arr1[1], arr1[2]
                        , arr1[3], arr1[4], arr1[5], arr1[6], arr1[7], arr1[8]
                        ,String.valueOf(today+duedate-1), String.valueOf(days[n]));
            }
            for(int n=0; n<days.length/2; n++ )
            {
                data.piece_update(con, order, arr1[1], arr1[2]
                        , arr1[3], arr1[4], arr1[5], arr1[6], arr1[7], arr1[8]
                        ,String.valueOf(today+duedate-1), String.valueOf(days[n+days.length/2]));
            }

        }
    }
    /*public void reserving_arriving(int day) throws SQLException{
        DataBase data=new DataBase();
        Connection con= data.create_connection();
        int i = data.check_day(con, day)
        if (i == 0) {//dia existe e o tapete está livre
            data.sch_suplier(con, day);
        } else if (i == 1) {//dia existe e tapete está ocupado
            int d = 1;
            while (data.check_day(con, day + d) == 1) {
                d++;
            }
            if(data.check_day(con, day+d)==0) {
                data.sch_suplier(con, day + d);
            }
            else if(data.check_day(con, day+d)==-1){
                data.sch_new_suplier(con, day+d);
            }
        } else if (i == -1) {// o dia nao existe
            data.sch_new_suplier(con, day);
        }
    }*/

}