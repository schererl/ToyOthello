import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Node {
    public final Othello game;
    public final Move moveFromParent;
    
    public Node parent;
    public double[] Q;
    public int N;
    public int virtualN;

    public List<Node> children;
    public ArrayList<Move> unexpandedMoves;
    
    public Node(final Node parent, Move moveFromParent, final Othello game) {
        children = new ArrayList<Node>();
        
        this.parent = parent;
        this.moveFromParent = moveFromParent;
        this.game = game;
        Q = new double[game.PLAYERS_COUNT + 1];
        N= 0;
        
        unexpandedMoves = new ArrayList<Move>(game.moves());
        virtualN = unexpandedMoves.size() + children.size(); // reset VirtualCHLen

        if (parent != null)
            parent.children.add(this);
    }

    // :: TREE REUSE
    public Node findChildForMove(final Move move) {
        for (final Node child : children) {

            if (child != null) {
                child.parent = null;
                return child;
            }
        }

        return null;
    }

    public void sort(final int interval) {
        List<Node> childrenCopy = children.subList(0, interval);
        final int mover = game.mover;
        Collections.sort(childrenCopy, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                double f1 = o1.N;
                double f2 = o2.N;
                return Double.compare(-f1, -f2);
            }
        });
        for (int i = 0; i < childrenCopy.size(); i++) {
            children.set(i, childrenCopy.get(i));
        }
    }

}