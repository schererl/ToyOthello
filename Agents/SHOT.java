package Agents;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import Games.Move;
import Games.Othello;
import Games.Game;

public class SHOT extends AI {
    final boolean debug = false;
    int player;
    final boolean TIME_BASED;

    public SHOT(boolean timeBased) {
        TIME_BASED = timeBased;
        friendlyName = "SHOT";
    }

    @Override
    public void initAI(final int playerID) {
        super.initAI(playerID);
        stats = new Statistics();
    }

    @Override
    public Move selectAction(final Game game,
            final double maxSeconds,
            final int maxIterations,
            final int maxDepth) {
        stats.clear();
        stats.startCronometer();
        super.countNodes = 0;

        SHOTNode root = new SHOTNode(null, null, game);
        int budget;
        root.openLayer();

        if (TIME_BASED) {
            budget = 0; // estimateBudget(game, root, maxSeconds);
        } else {
            budget = maxIterations;
        }

        expand(root, budget);
        root.closeLayer();
        root.sort(root.children.size());

        if (root.children.size() == 0)
            return root.unexpandedMoves.get(0);
        else if (budget == 0)
            return root.children.get(0).moveFromParent;

        root.openLayer();
        search(root, budget);
        root.closeLayer();

        if (root.children.size() > 1) {
            int unspendBudget = budget - root.N;
            search(root.children.get(1), unspendBudget);
            root.sort(2);
        }

        stats.endCronometer();
        return root.children.get(0).moveFromParent;

    }

    private void search(SHOTNode node, int budget) {

        if (node.game.isTerminal()) {
            double[] ut = node.game.utilities();
            for (int i = 0; i < ut.length; i++) {
                ut[i] *= budget;
            }
            node.updateLayerValues(ut, budget);
            return;
        }

        if (budget == 1) {
            Game gameCpy = node.game.copy();
            super.playout(gameCpy, 1000);
            node.updateLayerValues(gameCpy.utilities());
            return;
        }

        /* UNVISITED CHILDREN CASE */
        if (node.unexpandedMoves.size() > 0) {
            budget = expand(node, budget);
            node.sort(node.children.size());
            if (budget == 0) {
                return;
            }
        }

        /* MOVE FORWARD CASE */
        if (node.children.size() == 1 && node.unexpandedMoves.size() == 0) {
            SHOTNode child = node.children.get(0);
            child.openLayer();
            search(child, budget);
            node.updateLayerValues(child);
            child.closeLayer();
            return;
        }

        /* SEQUENTIAL HALVING CASE */
        int virtualChildrenLenght = node.children.size();
        int childBudget = 0;
        double layerBudget = budget / (Math.ceil(Math.log(node.children.size()) / Math.log(2)));
        while (virtualChildrenLenght > 1) {
            childBudget = Math.max(1, (int) Math.floor(layerBudget / virtualChildrenLenght));

            int itChildren = (layerBudget < virtualChildrenLenght ? (int) Math.floor(layerBudget)
                    : virtualChildrenLenght);
            for (int i = 0; i < itChildren; i++) {

                SHOTNode child = node.children.get(i);
                child.openLayer();
                search(child, childBudget);

                node.updateLayerValues(child);
                child.closeLayer();
            }
            node.sort(virtualChildrenLenght);
            virtualChildrenLenght = (int) Math.ceil(virtualChildrenLenght / 2f);
        }
    }

    private int expand(SHOTNode node, int budget) {
        int budgetLeft = budget;
        while (node.unexpandedMoves.size() > 0 && !checkTimeout()) {
            if (budgetLeft == 0)
                break;

            Move childAction = node.unexpandedMoves
                    .remove(ThreadLocalRandom.current().nextInt(node.unexpandedMoves.size()));

            Game childGame = node.game.copy();
            childGame.apply(childAction);
            SHOTNode childNode = new SHOTNode(null, childAction, childGame);
            node.children.add(childNode);

            childNode.openLayer();

            Game gameCpy = childNode.game.copy();
            super.playout(gameCpy, 1000);
            childNode.updateLayerValues(gameCpy.utilities());

            node.updateLayerValues(childNode);

            childNode.closeLayer();

            budgetLeft--;
        }
        return budgetLeft;
    }

    // used for root budget calculation for SHOT based on time
    public int budget() {
        return 0; // (int)Math.floor((STTValue*1000*1000)/(playoutTime/countPlayouts));
    }

    // used for interrupting execution for SHOT based on time
    public Boolean checkTimeout() {
        return false; // return this.TIME_BASED ? (System.currentTimeMillis() - TInit) >= (STTValue *
                      // 1.2):false;
    }

}
