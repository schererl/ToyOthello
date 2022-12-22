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
        Lq = new double[game.PLAYERS_COUNT + 1];
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
    }
}