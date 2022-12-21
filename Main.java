import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.io.IOException;
import java.util.Scanner;
/* 
it:530
curr state: {{0,1,1,1,1,1,1,0},{2,1,1,2,2,2,0,0},{2,1,1,1,1,1,1,0},{2,1,2,1,1,1,1,2},{2,1,2,2,1,1,0,2},{2,1,2,2,1,1,1,0},{1,1,1,1,0,1,1,0},{0,1,1,0,0,2,2,2}}
var: 0,022361
bfactor: 5
*/
public class Main {
    public static void main(String args[]) throws Exception {
        varTest();
        // simulate();
        // searchBoardConfig();
    }

    private static void searchBoardConfig() {
        int bfactor = 40;
        final int maxNodes = 1000;
        final int boardSize= 22;
        int internal = 800;
        double maxVar = Integer.MIN_VALUE;
        String state = "";
        int i = 0;
        while (true) {
            Othello cp = new Othello(boardSize, Othello.WHITE_UP);

            SHMCTS ag = new SHMCTS(false, false, false);
            ag.initAI(cp, Othello.WHITE_UP);

            while (!cp.isTerminal()) {
                ArrayList<Move> moves = new ArrayList<Move>();
                moves = cp.moves();
                if(cp.mover == ag.player && moves.size()>=bfactor){
                    ag.selectAction(cp.copy(), -1, maxNodes, 1000);
                    if(SHMCTS.internal >= internal && ag.var.get(ag.var.size()-1)>maxVar){
                        state=cp.toStringFormat();
                        maxVar = ag.var.get(ag.var.size()-1);
                        // System.out.print("\033[H\033[2J");
                        // System.out.flush();
                        cp.paintLegalMoves(moves);
                        System.out.printf("it:%d\ncurr state: %s\nvar: %.6f\nbfactor: %d\n", i, state, maxVar, moves.size());

                    }
                }
                i++;
                Move m = moves.get(ThreadLocalRandom.current().nextInt(moves.size()));
                cp.apply(m);
            }
        }

    }

    private static void varTest() throws Exception {
        int[] othelloSizeArr = new int[] { 8, 12, 16, 22, 24 };
        String outputCSV = "";// "bfactor;1000;2000;3000;4000;5000;6000;7000;8000;9000;10000\n";
        
        for (final int size : othelloSizeArr) {
            final int othelloSize = size;
            Othello ot = new Othello(othelloSize, Othello.WHITE_UP);
            SHMCTS ag = new SHMCTS(false, false, false);
            ag.initAI(ot, Othello.WHITE_UP);
            Move m = ag.selectAction(ot.copy(), -1, 10000, 1000);
            outputCSV += String.valueOf(calcBFactor(othelloSize));
            for (Double v : ag.var) {
                outputCSV += String.format(";%.4f", v);
            }
            outputCSV += "\n";
        }
        System.out.println(outputCSV);

    }

    private static float calcBFactor(int boardSize) throws Exception {
        Othello ot = new Othello(boardSize, Othello.WHITE_UP);
        final int numberPlayouts = 100;
        int d = 0;
        int b = 0;
        for (int i = 0; i < numberPlayouts; i++) {
            Othello cp = ot.copy();
            while (!cp.isTerminal()) {
                d++;
                ArrayList<Move> moves = cp.moves();
                b += moves.size();
                
                Move m = moves.get(ThreadLocalRandom.current().nextInt(moves.size()));
                cp.apply(m);
            }
        }
        return b / (float) (d);
    }

    private static void simulate() throws Exception {
        final int numberPlayouts = 1;
        final int othelloSize = 8;
        final int budget = 1000;
        final boolean debug = true;

        Othello ot = new Othello(othelloSize, Othello.BLACK_UP);

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
                    int player = cp.mover;
                    //System.out.print("\033[H\033[2J");
                    //System.out.flush();
                    cp.paintLegalMoves(moves);

                    System.out
                            .println(String.format("\nP%d\nplayouts: %d\navg bfactor:%.0f\nchildren: %d\navg depth: %.0f\n",
                                    player, i + 1, aux, moves.size(),
                                    d / (float) (i + 1)));
                    Thread.sleep(500);

                    Move m;
                    if (cp.mover == ag.player)
                        m = ag.selectAction(cp.copy(), -1, budget, 1000);
                    else
                        m = ag.selectAction(cp.copy(), -1, budget, 1000);
                    // m = moves.get(ThreadLocalRandom.current().nextInt(moves.size()));
                    cp.apply(m);

                    //System.out.print("\033[H\033[2J");
                    //System.out.flush();
                    cp.printfBoard();

                    System.out.println(
                            String.format("\nP%d\nplayouts: %d\navg bfactor:%.0f\nchildren: %d\navg depth: %.0f\n",
                                    player, i + 1, aux, moves.size(),
                                    d / (float) (i + 1)));
                    Thread.sleep(1000);

                } else {
                    if (cp.mover == ag.player) {
                        cp.apply(ag.selectAction(cp, -1, 10000, 500));
                    } else
                        cp.apply(op.selectAction(cp, -1, 10000, 500));

                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                    System.out.println(String.format("\nwins: %d|%d\nplayouts: %d\navg depth: %.0f\n", agentWin,
                            opponentWin, i + 1,
                            d / (float) (i + 1)));

                }
                System.out.println();
            }
            if (cp.utilities()[ag.player - 1] == 1) {
                agentWin++;
            } else if (cp.utilities()[Othello.WHITE_UP - 1] == 1) {
                opponentWin++;
            }
        }
        final long end_time = System.currentTimeMillis();
        System.out.printf("wins:%d time:%.4f s depth:%.0f bfactor:%.0f\n", agentWin, (end_time - start_time) / 1000f,
                d / 25000f,
                b / (float) d);
    }
}
