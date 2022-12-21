import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.io.IOException;
import java.util.Scanner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/* 
it:530
curr state: {{0,1,1,1,1,1,1,0},{2,1,1,2,2,2,0,0},{2,1,1,1,1,1,1,0},{2,1,2,1,1,1,1,2},{2,1,2,2,1,1,0,2},{2,1,2,2,1,1,1,0},{1,1,1,1,0,1,1,0},{0,1,1,0,0,2,2,2}}
var: 0,022361
bfactor: 5
*/
public class Main {
    public static void main(String args[]) throws Exception {
        //varTest();
        //simulate();
        //searchBoardConfig();
        String[] output = statsTest(new int[][]{{0,0,0,1,2,2,2,0},{2,0,1,1,1,2,2,1},{0,1,0,1,1,1,2,1},{1,1,1,1,1,1,2,1},{0,0,1,2,2,2,1,1},{0,0,1,2,2,1,1,1},{0,1,2,0,1,1,1,2},{0,2,2,0,0,0,2,0}});
    
        try {
            FileWriter fileWriter = new FileWriter("statsFull.csv");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(output[0]);
            bufferedWriter.write(output[1]);
            bufferedWriter.write(output[2]);
            bufferedWriter.newLine();
            bufferedWriter.close();

            FileWriter fileWriter2 = new FileWriter("statsMean.csv");
            BufferedWriter bufferedWriter2 = new BufferedWriter(fileWriter2);
            bufferedWriter2.write(output[0]);
            bufferedWriter2.newLine();
            bufferedWriter2.close();

            FileWriter fileWriter3 = new FileWriter("statsMedian.csv");
            BufferedWriter bufferedWriter3 = new BufferedWriter(fileWriter3);
            bufferedWriter3.write(output[1]);
            bufferedWriter3.newLine();
            bufferedWriter3.close();

            FileWriter fileWriter4 = new FileWriter("statsSTDDev.csv");
            BufferedWriter bufferedWriter4 = new BufferedWriter(fileWriter4);
            bufferedWriter4.write(output[2]);
            bufferedWriter4.newLine();
            bufferedWriter4.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
         
    }

    private static void searchBoardConfig() {
        int bfactor = 4;
        final int maxNodes = 10000;
        final int boardSize= 8;
        int internal = 8000;
        double maxVar = Integer.MIN_VALUE;
        String state = "";
        int i = 0;
        while (true) {
            Othello cp = new Othello(boardSize, Othello.WHITE_UP);

            SHMCTS ag = new SHMCTS(false, false, false);
            ag.initAI(cp, Othello.WHITE_UP);
            int depth = 10;
            int it = 0;
                
            while (!cp.isTerminal()) {
                ArrayList<Move> moves = new ArrayList<Move>();
                moves = cp.moves();
                if(it > depth && cp.mover == ag.player && moves.size()>=bfactor){
                    
                    ag.selectAction(cp.copy(), -1, maxNodes, 1000);
                    if(SHMCTS.internal >= internal && ag.stats.stdDev>maxVar){
                        state=cp.toStringFormat();
                        maxVar = ag.stats.stdDev;
                        System.out.print("\033[H\033[2J");
                        System.out.flush();
                        //cp.paintLegalMoves(moves);
                        System.out.printf("SIZE:%d\n\tstd dev: %.6f\n\taverage: %.6f\n\tmedian: %.6f\n\n", boardSize, ag.stats.stdDev, ag.stats.mean, ag.stats.median);
                        System.out.printf("it:%d\ncurr state: %s\nbfactor: %d\n", i, state, moves.size());

                    }
                }
                it++;
                i++;
                Move m = moves.get(ThreadLocalRandom.current().nextInt(moves.size()));
                cp.apply(m);
            }
        }

    }
    
    //{{0,0,0,1,2,2,2,0},{2,0,1,1,1,2,2,1},{0,1,0,1,1,1,2,1},{1,1,1,1,1,1,2,1},{0,0,1,2,2,2,1,1},{0,0,1,2,2,1,1,1},{0,1,2,0,1,1,1,2},{0,2,2,0,0,0,2,0}}
    private static String[] statsTest(int[][] grid){
        final int size = 8;
        final int maxIterations = 10000;
        String[] outputCSV = new String[3];
        
        Othello ot = new Othello(size, Othello.WHITE_UP, grid);
        
        SHMCTS ag = new SHMCTS(false, false, false);
        ag.initAI(ot, Othello.WHITE_UP);
        ag.selectAction(ot.copy(), -1, maxIterations, 1000);
        
        outputCSV[0] = ag.stats.toStringMean();
        outputCSV[1] = ag.stats.toStringMedian();
        outputCSV[2] = ag.stats.toStringStdDev();
        
        return outputCSV;
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
            for (Double v : ag.stats.stdDevTrack) {
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
