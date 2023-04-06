package Games;

public class Move {
    public int[] posInit;
    public int[] posFinal;
    public int[] posInsertion;
    public int[] posArrow;

    public Move() {
        posInit = new int[2];
        posFinal= new int[2];
        posInsertion= new int[2];
        posArrow= new int[2];
    }

    public void setPosInit(int x, int y){
        posInit[0] = x; posInit[1]=y;
    }

    public void setPosFinal(int x, int y){
        posFinal[0] = x; posFinal[1]=y;
    }

    public void setPosInsertion(int x, int y){
        posInsertion[0] = x; posInsertion[1]=y;
    }

    public void setPosArrow(int x, int y){
        posArrow[0] = x; posArrow[1]=y;
    }
    
}
