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
    public void check()
    {
        //vai buscar a primeira peça que ainda nao está processada da tabela order
        ord= new Order();
        N=Integer.parseInt(ord.Quantity);
        duedate=Integer.parseInt(ord.DueDate);
        calculus(Nd,N,Nc, Ne,duedate);
        if(Ne==0)
        {
            // mudar o estado desta ordem para processada
            return;
        }
        if(verify_pieces(ord.Work_Piece)==1)
        {
            Na=verify_how_many(ord.Work_Piece,Ne);
            N=N-Na;
            if(N<=0)
            {
                //reservar no armazem(deixam de estar livres)
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
        // mudar o estado desta ordem para processada
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
    public int verify_how_many(String X, int Ne)
    {   int n=0;
        //verificar quantas peças livres X há dia Ne

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

