import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import Agents.MCTS;
import Games.Move;
import Games.Othello;
import Games.Amazons;
import Games.Game;

import java.io.IOException;
import java.util.Scanner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class Main {
    public static void main(String args[]) throws Exception {
        //Game game = new Othello(8, Othello.BLACK_UP);
        Game game = new Amazons();
        MCTS uctAgent = new MCTS(false);
        MCTS sqrtshAgent = new MCTS(true);
        int uctWins = 0;
        int sqrtshWins = 0;

        uctAgent.initAI(Game.WHITE_UP);
        sqrtshAgent.initAI(Game.BLACK_UP);
        for (int i = 0; i < 100; i++) {
            Game cp = game.copy();

            Move m;
            while (!cp.isTerminal()){
                if (cp.mover == uctAgent.player)
                        m = uctAgent.selectAction(cp.copy(), -1, 1000, 1000);
                    else
                        m = sqrtshAgent.selectAction(cp.copy(), -1, 1000, 1000);
                    cp.apply(m);
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                    System.out.println(cp);
                    System.out.printf("%s %d %s %d\n", uctAgent.getFrindlyName(), uctWins, sqrtshAgent.getFrindlyName(), sqrtshWins);
            
            
            }

            if (cp.utilities()[uctAgent.player-1] == 1) {
                uctWins++;
            } else if (cp.utilities()[sqrtshAgent.player-1] == 1) {
                sqrtshWins++;
            }

            
            System.out.print("\033[H\033[2J");
            System.out.flush();
            System.out.printf("%s %s %d %d\n", uctAgent.getFrindlyName(), sqrtshAgent.getFrindlyName(), uctWins, sqrtshWins);
            
            if(uctAgent.player ==  Othello.WHITE_UP){
                sqrtshAgent.initAI(Othello.WHITE_UP);
                uctAgent.initAI(Othello.BLACK_UP);
            }else{
                uctAgent.initAI(Othello.WHITE_UP);
                sqrtshAgent.initAI(Othello.BLACK_UP);
            }
        }
    }
}

    