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
                    System.out.println("ananas4");
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
                    int[] nr=new int[3];
                    material = verify_raw(ord.Work_Piece);
                    String sub=data.sub_trans(con, material, ord.Work_Piece);
                    nr=calculus(N, Ne, duedate, today, ord.Work_Piece, material, sub, ord.Quantity);
                    //calculus(N, Ne, duedate, today, ord.Work_Piece);
                    /*aux[0]->Nd // numero de dias que tens para fazer a peça
                    aux[1]->Ne // numero que dias que tens no maximo para encomendar peças
                    aux[2]->duedate*/
                    Nd=nr[0];
                    Ne=nr[1];
                    duedate=nr[2];
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
                        Na = verify_how_many(ord.Work_Piece, (duedate - 1));//verifica se ha já peças prontas (transformadas)
                        System.out.println("Na: "+Na);
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
                                System.out.println("bom dia");
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
                            System.out.println("boa tarde");
                            for (int i = 0; i < N; i++) {
                                try {
                                    data.piece(con, ord.Order_num, material, Integer.toString(today+Integer.parseInt(ord.DueDate) - 1));

                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
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
                                System.out.println("boa tarde");
                        for (int i = 0; i < N; i++) {
                            try {
                                data.piece(con, ord.Order_num, material, Integer.toString(today+Integer.parseInt(ord.DueDate) - 1));

                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        System.out.println("boa tarde");
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
                        work_days=writing_order(Nd, Ne, material, ord.Work_Piece, ord.Order_num);

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
                                    data.just_arrived(con, aux, Ne+today, 4);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                String aux = "p2";
                                try {
                                    today=data.today_day(con);
                                    data.sup(con, String.valueOf(Ne + today-Tc), "sc_" + aux, String.valueOf(4));
                                    data.just_arrived(con, aux, Ne+today, 4);
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
                                    data.arriving_new_pieces(con, "p1", Ne + today, Nf + exit + N);
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
                                    data.arriving_new_pieces(con, "p2", Ne + today, Nf + exit + N);
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
                            System.out.println("tou aqui 10");
                            if (material.equals("P1")) {
                                String aux = "p1";
                                try {
                                    today=data.today_day(con);
                                    data.sup(con, String.valueOf(Ne + today-Tc), "sc_p1" + aux, String.valueOf(N));
                                    data.just_arrived(con, aux, Ne+today, N);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                String aux = "p2";
                                try {
                                    today=data.today_day(con);
                                    data.sup(con, String.valueOf(Ne + today-Tc), "sc_p2", String.valueOf(N));
                                    data.just_arrived(con, aux, Ne+today, N);
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
                                    data.arriving_new_pieces(con, "p1", Ne + today, N + exit);
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
                                        data.arriving_new_pieces(con, "p2", Ne + today, N + exit);
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
                        work_days=writing_order(Nd, Ne, material, ord.Work_Piece, ord.Order_num);
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
                                        data.just_arrived(con, aux, Ne+today, N);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N);
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
                                        data.just_arrived(con, aux, Ne+today, 4);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tc), "sc_" + aux, String.valueOf(4));
                                        data.just_arrived(con, aux, Ne+today, 4);
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
                                    data.arriving_new_pieces(con, "p1", 1 + today, Nf + exit + N);
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
                                    data.arriving_new_pieces(con, "p2", 1 + today, Nf + exit + N);
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
                                    data.just_arrived(con, aux, Ne+today, N);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                String aux = "p2";
                                try {
                                    today=data.today_day(con);
                                    data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                    data.just_arrived(con, aux, Ne+today, N);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        work_days=writing_order(Nd, Ne, material, ord.Work_Piece, ord.Order_num);
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
                                        data.just_arrived(con, aux, Ne+today, N);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N);
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
                                        data.just_arrived(con, aux, Ne+today, 4);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tc), "sc_" + aux, String.valueOf(4));
                                        data.just_arrived(con, aux, Ne+today, 4);
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
                                    data.arriving_new_pieces(con, "p1", 1 + today, Nf + exit + N);
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
                                    data.arriving_new_pieces(con, "p2", 1 + today, Nf + exit + N);
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
                                    data.just_arrived(con, aux, Ne+today, N);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                String aux = "p2";
                                try {
                                    today=data.today_day(con);
                                    data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                    data.just_arrived(con, aux, Ne+today, N);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        work_days=writing_order(Nd, Ne, material, ord.Work_Piece, ord.Order_num);
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
                                        data.just_arrived(con, aux, Ne+today, N);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N);
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
                                        data.just_arrived(con, aux, Ne+today, 4);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tc), "sc_" + aux, String.valueOf(4));
                                        data.just_arrived(con, aux, Ne+today, 4);
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
                                    data.arriving_new_pieces(con, "p1", 1 + today, Nf + exit + N);
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
                                    data.arriving_new_pieces(con, "p2", 1 + today, Nf + exit + N);
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
                                        data.just_arrived(con, aux, Ne+today, N);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N);
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
                                        data.just_arrived(con, aux, Ne+today, N);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Ta), "sa_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }

                        }
                        work_days=writing_order(Nd, Ne, material, ord.Work_Piece, ord.Order_num);
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
                                        data.just_arrived(con, aux, Ne+today, N);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {

                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Ta), "sa_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N);
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
                                        data.just_arrived(con, aux, Ne+today, 4);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {

                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tc), "sc_" + aux, String.valueOf(4));
                                        data.just_arrived(con, aux, Ne+today, 4);
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
                                        data.just_arrived(con, aux, Ne+today, N);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {

                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Tb), "sb_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N);
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
                                        data.just_arrived(con, aux, Ne+today, N);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {

                                    String aux = "p2";
                                    try {
                                        today=data.today_day(con);
                                        data.sup(con, String.valueOf(Ne + today-Ta), "sa_" + aux, String.valueOf(N));
                                        data.just_arrived(con, aux, Ne+today, N);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }

                        }

                    }
                    work_days=writing_order(Nd, Ne, material, ord.Work_Piece, ord.Order_num);
                    try {
                        data.processed_status(con, ord.Order_num); // mudar o estado desta ordem para processada
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                else
                {

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
public float[] calculus(int num1, int num3, int duedate,int today, String Workpiece, String material, String sub, int quantity) {
        //calculus(N, Ne, duedate, today, ord.Work_Piece);
    /*aux[0]->Nd  // numero de dias que tens para fazer a peça
    aux[1]->Ne // numero que dias que tens no maximo para encomendar peças, aka quando tenho de começar a produzir
    aux[2]->duedate
    Nd=nr[0];
    Ne=nr[1];
    duedate=nr[2];*/
    int[] f=piece_perday(material, Workpiece, sub);
    int n1=f[0];//nr de peças que consigo fazer por dia na primeira transformação    1
    int n2=f[1];//nr de peças que consigo fazer por dia na segunda transformação     1
    int nr_days1=(int)((quantity/n1)+0.5);
    int nr_days2;
    if(n2==0){
        nr_days2=0;
    }
    else {
        nr_days2 = (int) ((quantity / n2) + 0.5);
    }
    float[] aux = new float[3];
    aux[0]= nr_days2 + nr_days1;
    aux[1] = duedate - today - aux[0]-1;
    if(num3>duedate) {
        while (num3 > duedate) {
            duedate++;
            aux[2] = duedate;
        }
    }
    else{
        aux[2]=duedate;
    }
    //chamar função
    DataBase data=new DataBase();
    Connection con=data.create_connection();
    aux[2]=data.calendar(con,today, aux, Workpiece);

    return aux;
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
    public  int[] writing_order( int Nd, int Ne, String material, String work_piece, String order) throws SQLException{
        DataBase data=new DataBase();
        Connection con= data.create_connection();
        int[] work_days = new int[Nd];
        for (int n = Nd; n > 0; n--) {
            work_days[Nd - n] = Ne - 1 - n;
        }
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
        try {
            int today = data.today_day(con);
            System.out.println("1");
            data.piece_update(con, order, arr[1], arr[2], arr[3], arr[4], arr[5], arr[6], arr[7], arr[8], arr[9], arr[10], arr[11], arr[12], arr[13], arr[14], arr[15], arr[16], String.valueOf(Ne + today));
            System.out.println("2");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return work_days;
    }


}