public class Account {
    int cost; // custo por supplier
    int Price;
    int depreciation0; // estimativa do depreciation cost
    public int costSup(int N, String L, String raw)
    {
        if (L.equals('C')==true) {
            if(raw.equals("P1")==true)
            {
                Price=55;
            }
            else
            {
                Price=18;
            }
        }
        if (L.equals('B')==true) {
            if(raw.equals("P1")==true)
            {
                Price=45;
            }
            else
            {
                Price=15;
            }

        }
        if (L.equals('A')==true) {
            if(raw.equals("P1")==true)
            {
                Price=30;
            }
            else
            {
                Price=10;
            }
        }

        cost=Price*N;

        return cost;
    }
    public int Depre(String L, String raw, int dispatchd, int arrivald )
    {
        if (L.equals('C')==true) {
            if(raw.equals("P1")==true)
            {
                Price=55;
            }
            else
            {
                Price=18;
            }
        }
        if (L.equals('B')==true) {
            if(raw.equals("P1")==true)
            {
                Price=45;
            }
            else
            {
                Price=15;
            }
        }
        if (L.equals('A')==true) {
            if(raw.equals("P1")==true)
            {
                Price=30;
            }
            else
            {
                Price=10;
            }
        }

        depreciation0= Price*(dispatchd-arrivald)/100;
        return depreciation0;
    }

}
