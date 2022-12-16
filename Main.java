import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String args[]) throws Exception {
        final int numberPlayouts = 25000;
        final int othelloSize = 24;
        final boolean debug = true;

        Othello ot = new Othello(othelloSize, 1);

        final long start_time = System.currentTimeMillis();
        int d = 0;
        int b = 0;
        for (int i = 0; i < numberPlayouts; i++) {
            Othello cp = new Othello(ot.grid, ot.player);
            while (!cp.isTerminal()) {
                d++;
                ArrayList<int[]> moves = cp.moves();
                b += moves.size();
                cp.apply(moves.get(ThreadLocalRandom.current().nextInt(moves.size())));

                if (debug) {

                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                    cp.printfBoard();
                    System.out.println();
                    float aux = b / (float) (d);
                    System.out.println(String.format("playouts: %d\navg bfactor:%.0f\navg depth: %.0f\n", i+1, aux, d / (float)(i+1)));
                    Thread.sleep(1000);

                }
            }
        }
        final long end_time = System.currentTimeMillis();
        System.out.printf("time:%.4f s depth:%.0f bfactor:%.0f\n", (end_time - start_time) / 1000f, d / 25000f,
                b / (float) d);
        /*
         * for(int[] m:moves){
         * Othello cp = new Othello(ot.grid, ot.player);
         * cp.apply(m);
         * cp.printfBoard();
         * System.out.println();
         * }
         */
    }
}
