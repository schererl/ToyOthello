import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SHOTNode extends Node {
    public double[] Lq; 
    public int Ln;
    public final List<SHOTNode> children = new ArrayList<SHOTNode>();
    

    public SHOTNode(final Node parent, Move moveFromParent, final Othello game) {
        super(parent, moveFromParent, game);
        Lq = new double[game.PLAYERS_COUNT];
        Ln = 0;
    }

    public void openLayer() {
        this.Ln = 0;
        for (int i = 0; i < Lq.length; i++) {
            this.Lq[i] = 0;
        }

    }

    public void updateLayerValues(double[] Lq) {
        for (int i = 0; i < Lq.length; i++) {
            this.Lq[i] += Lq[i] * 0.999;
        }
        this.Ln += 1;
    }

    public void updateLayerValues(double[] Lq, int Ln) {
        this.Ln += Ln;
        for (int i = 0; i < Lq.length; i++) {
            this.Lq[i] += Lq[i] * 0.999;
        }
    }

    public void updateLayerValues(SHOTNode child) {
        this.Ln += child.Ln;
        for (int i = 0; i < Lq.length; i++) {
            this.Lq[i] += child.Lq[i] * 0.999;
        }
    }

    public void closeLayer() {
        this.N += Ln;
        for (int i = 0; i < Lq.length; i++) {
            this.Q[i] += Lq[i];
        }

        this.Ln = 0;
        for (int i = 0; i < Lq.length; i++) {
            this.Lq[i] = 0;
        }
    }

    public void sort(final int interval) {
        List<SHOTNode> childrenCopy = children.subList(0, interval);
        final int mover = game.mover;
        Collections.sort(childrenCopy, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                double f1 = o1.Q[mover-1]/o1.N;
                double f2 = o2.Q[mover-1]/o2.N;
                return Double.compare(-f1, -f2);
            }
        });
        for (int i = 0; i < childrenCopy.size(); i++) {
            children.set(i, childrenCopy.get(i));
        }
    }

}