import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class SHOT extends AI {
    final boolean debug = false;
    int player;
    final boolean TIME_BASED;

    public SHOT(boolean timeBased) {
        TIME_BASED = timeBased;
    }

    @Override
    public void initAI(final int playerID) {
        super.initAI(playerID);
    }

    @Override
    public Move selectAction(final Othello game,
            final double maxSeconds,
            final int maxIterations,
            final int maxDepth) {

        SHOTNode root = new SHOTNode(null, null, game);
        int budget;
        root.openLayer();

        if (TIME_BASED) {
            budget = 0; //estimateBudget(game, root, maxSeconds);
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

        int virtualChildrenLenght = root.children.size();
        int layerBudget = 0;
        while (virtualChildrenLenght > 1) {
            //
            layerBudget = getLayerBudget(this.TIME_BASED ? budget() : budget, root.children.size(),
                    virtualChildrenLenght);
            for (int i = 0; i < virtualChildrenLenght; i++) {
                if (root.Ln > budget || checkTimeout()) {
                    root.sort(virtualChildrenLenght);
                    break;
                }
                SHOTNode child = root.children.get(i);
                child.openLayer();
                search(child, layerBudget);

                root.updateLayerValues(child);
                child.closeLayer();
            }
            root.sort(virtualChildrenLenght);
            virtualChildrenLenght = (int) Math.ceil(virtualChildrenLenght / 2f);
        }

        root.closeLayer();

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
            Othello gameCpy = node.game.copy();
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
            search(child, budget - 1);
            node.updateLayerValues(child);
            child.closeLayer();
            return;
        }

        /* SEQUENTIAL HALVING CASE */
        int virtualChildrenLenght = node.children.size();
        int layerBudget = 0;
        while (virtualChildrenLenght > 1) {
            layerBudget = getLayerBudget(budget, node.children.size(), virtualChildrenLenght);

            for (int i = 0; i < virtualChildrenLenght; i++) {
                if (node.Ln > budget || checkTimeout()) {
                    node.sort(virtualChildrenLenght);
                    return;
                }

                SHOTNode child = node.children.get(i);
                child.openLayer();
                search(child, layerBudget);

                node.updateLayerValues(child);
                child.closeLayer();
            }
            node.sort(virtualChildrenLenght);
            virtualChildrenLenght = (int) Math.ceil(virtualChildrenLenght / 2f);
        }
    }

    private int getLayerBudget(int budget, int childrenLenght, int virtualChildrenLenght) {
        double totalLayers = Math.log(childrenLenght) / Math.log(2) * virtualChildrenLenght;
        return Math.max(1, (int) Math.floor(budget / totalLayers));
    }

    private int expand(SHOTNode node, int budget) {
        int budgetLeft = budget;
        while (node.unexpandedMoves.size() > 0 && !checkTimeout()) {
            if (budgetLeft == 0)
                break;

            Move childAction = node.unexpandedMoves
                    .remove(ThreadLocalRandom.current().nextInt(node.unexpandedMoves.size()));

            Othello childGame = node.game.copy();
            childGame.apply(childAction);
            SHOTNode childNode = new SHOTNode(null, childAction, childGame);
            node.children.add(childNode);

            childNode.openLayer();

            Othello gameCpy = childNode.game.copy();
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
