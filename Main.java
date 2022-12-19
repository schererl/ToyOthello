import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String args[]) throws Exception {
        final int numberPlayouts = 1;
        final int othelloSize = 22;
        final boolean debug = false;

        Othello ot = new Othello(othelloSize, Othello.WHITE_UP);

        SHMCTS ag = new SHMCTS(false, false, false);
        ag.initAI(ot, Othello.BLACK_UP);

        SHMCTS op = new SHMCTS(false, false, false);
        op.initAI(ot, Othello.WHITE_UP);

        final long start_time = System.currentTimeMillis();

        int d = 0;
        int b = 0;
        int agentWin = 0;
        int opponentWin = 0;
        for (int i = 0; i < numberPlayouts; i++) {
            Othello cp = ot.copy();
            while (!cp.isTerminal()) {
                d++;
                ArrayList<Move> moves = cp.moves();
                b += moves.size();

                if (debug) {
                    float aux = b / (float) (d);

                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                    cp.paintLegalMoves(moves);

                    System.out.println(String.format("\nplayouts: %d\navg bfactor:%.0f\navg depth: %.0f\n", i + 1, aux,
                            d / (float) (i + 1)));
                    Thread.sleep(500);

                    Move m;
                    if (cp.mover == ag.player)
                        m = ag.selectAction(cp.copy(), -1, 10000, 1000);
                    else
                        m = ag.selectAction(cp.copy(), -1, 10000, 1000);    
                    //m = moves.get(ThreadLocalRandom.current().nextInt(moves.size()));
                    cp.apply(m);

                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                    cp.printfBoard();

                    System.out.println(String.format("\nplayouts: %d\navg bfactor:%.0f\navg depth: %.0f\n", i + 1, aux,
                            d / (float) (i + 1)));
                    Thread.sleep(1000);

                } else {
                    if (cp.mover == ag.player) {
                        cp.apply(ag.selectAction(cp, -1, 10000, 500));
                    } else
                        cp.apply(op.selectAction(cp, -1, 10000, 500));
                        
                    
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                    System.out.println(String.format("\nwins: %d|%d\nplayouts: %d\navg depth: %.0f\n", agentWin, opponentWin, i + 1,
                            d / (float) (i + 1)));

                }
            }
            if (cp.utilities()[ag.player - 1] == 1) {
                agentWin++;
            }else if(cp.utilities()[Othello.WHITE_UP - 1] ==  1){
                opponentWin++;
            }
        }
        final long end_time = System.currentTimeMillis();
        System.out.printf("wins:%d time:%.4f s depth:%.0f bfactor:%.0f\n", agentWin, (end_time - start_time) / 1000f,
                d / 25000f,
                b / (float) d);
    }
}
