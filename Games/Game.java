package Games;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/* TODO: APPLY (virar tudo) */
public class Game {
    public final static int EMPTY = 0;
    public final static int BLACK_UP = 1;
    public final static int WHITE_UP = 2;
    public final static int ARROW = 3;
    
    
    public int numTurn;
    public int mover;
    public final int playersCount;
    public Game(int m, final int p){
        numTurn=0;
        mover=m;
        playersCount=p;
    }

    public Game(int t, int m, int p){
        numTurn=t;
        mover=m;
        playersCount=p;
    }

    public Game copy(){return new Game(this.numTurn, this.mover, this.playersCount);};
    public ArrayList<Move> moves(){return null;};
    public void apply(final Move Move){};
    public Boolean isTerminal(){return false;};
    public double[] utilities(){return new double[1];};
    public int playerIndex(){return 0;};

}