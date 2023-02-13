import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Collections;
import java.util.Comparator;

public class SHMCTS extends AI {

	private final boolean debug = false;
	private static final double DISCOUNT_FACTOR = 0.999;
	private final boolean USE_HALVE;
	private final boolean USE_UCB_SQRT;

	protected Node root = null;
	protected static SelectionPolicy searchPolicy;
	protected static SelectionPolicy rootPolicy; // SHMCTS
	protected long smartThinkLimit = 0;
	protected int lastActionHistorySize = 0;

	protected String armsStats = "";

	public SHMCTS(final boolean h, final boolean ucb_sqrt) {
		USE_HALVE = h;
		USE_UCB_SQRT = ucb_sqrt;

		if (!USE_HALVE && !USE_UCB_SQRT)
			this.friendlyName = "UCT";
		else if (USE_HALVE && !USE_UCB_SQRT)
			this.friendlyName = "UCT_SH";
		else if (!USE_HALVE && USE_UCB_SQRT)
			this.friendlyName = "UCT_SQRT";
		else
			this.friendlyName = "UCT_SH_SQRT";

		SHMCTS.searchPolicy = new UCB1();
		if (ucb_sqrt) {
			rootPolicy = new UCB_sqrt();

		} else { // UCT_SH
			rootPolicy = new UCB1();
			rootPolicy.setExpCoef(2 * rootPolicy.EXPLORATION_COEFFICIENT);
		}

	}

	@Override
	public void initAI(final int playerID) {
		super.initAI(playerID);

	}

	public boolean isHalveTime(final long usedResources, final double globalResource, final int halve) {
		return (usedResources >= (globalResource - (double) globalResource / (Math.pow(2, halve + 1))));

	}

	@Override
	public Move selectAction(
			final Othello game,
			final double maxSeconds,
			final int maxIterations,
			final int maxDepth) {

		final long start_time = System.currentTimeMillis();
		final int maxIts = (maxIterations >= 0) ? maxIterations : Integer.MAX_VALUE;

		stats.clear();
		stats.startCronometer();
		super.countNodes = 0;
		/* both can be turned off when a certain condition is reach */
		Boolean needHalve = USE_HALVE;

		int halve = 0;
		int numIterations = 0;

		this.root = new Node(null, null, game);
		this.root.virtualN = root.children.size() + root.unexpandedMoves.size();
		while (numIterations < maxIts) {
			/* :: SEQUENTIAL HALVING */
			if (needHalve && root.virtualN >= 4 && root.unexpandedMoves.size() == 0
					&& isHalveTime(numIterations, maxIts, halve)) {
				root.sort(Math.min(root.children.size(), root.virtualN));
				halve += 1;
				root.virtualN = (int) Math
						.max(Math.ceil(root.children.size() / Math.pow(2, halve)), 4);
			}

			/* :: SEARCH | EXPAND */
			Node current;
			if (USE_HALVE || USE_UCB_SQRT)
				current = search(searchRootChild(root));
			else
				current = search(root);

			/* :: SIMULATION */
			Othello playoutGame = current.game.copy();
			int preTurn = playoutGame.numTurn;
			playout(playoutGame, maxDepth);
			int finalTurn = playoutGame.numTurn;

			/* :: BACKPROPAGATION */
			backpropagation(playoutGame, current, finalTurn - preTurn);

			++numIterations;

			if (numIterations % 100 == 0) {
				// for(Node child: root.children){
				// if(child.stats==null)child.stats=new Statistics();
				// child.stats.computeMean(child);
				// child.stats.computeAllocation(root, child);
				// }
				stats.computeStandardDeviation(root);
				stats.computeMean(root);
				stats.computeMedian(root);
				stats.computeEntropy(root);
				stats.computeVarCoef(root);
			}
		}

		// :: FINAL SELECTION
		Node decidedNode = finalMoveSelection(root);
		// stats.computeEntropy(decidedNode);
		smartThinkLimit -= System.currentTimeMillis() - start_time;

		stats.endCronometer();
		root.sort(root.virtualN);
		
		stats.computeAllocation(root);
		

		// if (root.children.size() > 4) {
		// 	armsStats = root.children.get(0).moveFromParent + root.children.get(0).stats.toStringMean() + "\n";
		// 	armsStats += root.children.get(1).moveFromParent + root.children.get(1).stats.toStringMean() + "\n";
		// 	armsStats += root.children.get(2).moveFromParent + root.children.get(2).stats.toStringMean() + "\n";
		// 	armsStats += root.children.get(3).moveFromParent + root.children.get(3).stats.toStringMean() + "\n";
		// }
		
		printEvaluation(root);
		return decidedNode.moveFromParent;
	}

	public Node searchRootChild(final Node root) {
		Node current = root;

		if (!current.unexpandedMoves.isEmpty()) {
			final Move move = current.unexpandedMoves.remove(
					ThreadLocalRandom.current().nextInt(current.unexpandedMoves.size()));
			final Othello game = current.game.copy();
			game.apply(move);
			return new Node(current, move, game);
		}

		return rootPolicy.select(current);
	}

	public Node search(final Node node) {
		Node current = node;
		while (true) {

			if (current.game.isTerminal())
				break;

			else if (!current.unexpandedMoves.isEmpty()) {
				final Move move = current.unexpandedMoves.remove(
						ThreadLocalRandom.current().nextInt(current.unexpandedMoves.size()));
				final Othello game = current.game.copy();
				game.apply(move);
				return new Node(current, move, game);
			}

			current = searchPolicy.select(current);

		}
		return current;
	}

	public void backpropagation(final Othello playoutGame, Node current, final int numTurns) {

		final double[] utilities = playoutGame.utilities();
		final double[] discUtilities = playoutGame.utilities();
		for (int i = 0; i < utilities.length; i++) {
			discUtilities[i] = utilities[i] * Math.pow(DISCOUNT_FACTOR, numTurns);
		}

		// :: BACKPROP
		double learning = 1;
		while (current != null) {
			current.N += 1;
			for (int p = 0; p < Othello.PLAYERS_COUNT; p++) {
				double discUt = discUtilities[p] * learning;
				current.Q[p] += discUt;
			}

			if (utilities[current.game.mover - 1] == 1) {
				current.wins += 1;
			} else if (utilities[current.game.mover - 1] == -1) {
				current.loss += 1;
			} else {
				current.drawn += 1;
			}
			learning *= DISCOUNT_FACTOR;
			current = current.parent;
		}
	}

	private void printEvaluation(Node n) {
		System.out.println(String.format("%s  %d nodes", this.friendlyName, n.N));
		for (Node ch : n.children) {
			System.out.println(String.format("\t%s | (%.0f/%d) %.4f", ch.moveFromParent,
					ch.Q[this.player - 1],
					ch.N, ch.Q[this.player - 1] / ch.N));
		}
	}

	public static Node finalMoveSelection(final Node rootNode) {
		Node bestChild = null;
		int bestVisitCount = Integer.MIN_VALUE;
		int numBestFound = 0;

		final int numChildren = rootNode.children.size();

		for (int i = 0; i < numChildren; ++i) {
			final Node child = rootNode.children.get(i);
			final int N = child.N;

			if (N > bestVisitCount) {
				bestVisitCount = N;
				bestChild = child;
				numBestFound = 1;
			} else if (N == bestVisitCount &&
					ThreadLocalRandom.current().nextInt() % ++numBestFound == 0) {
				bestChild = child;
			}
		}

		return bestChild;
	}

}
