package Agents;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import Games.Move;
import Games.Game;
import Games.Othello;

public class Node {
    public Statistics stats;

    public final Game game;
    public final Move moveFromParent;
    
    public Node parent;
    public double[] Q;
    public int wins;
    public int loss;
    public int drawn;
    public int N;
    public int virtualN;

    public List<Node> children;
    public ArrayList<Move> unexpandedMoves;
    
    public Node(final Node parent, Move moveFromParent, final Game game) {
        children = new ArrayList<Node>();
        
        this.parent = parent;
        this.moveFromParent = moveFromParent;
        this.game = game;
        Q = new double[game.playersCount];
        N= 0;
        wins = 0;
        loss = 0;
        drawn = 0;
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
        final int moverPos = game.mover-1;
        Collections.sort(childrenCopy, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                double f1 = o1.Q[moverPos]/o1.N;
                double f2 = o2.Q[moverPos]/o2.N;
                return Double.compare(f2, f1);
            }
        });
        for (int i = 0; i < childrenCopy.size(); i++) {
            children.set(i, childrenCopy.get(i));
        }
    }

}