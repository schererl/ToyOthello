package Agents;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import Games.Move;
import Games.Game;
import SelectionPolicy.SelectionPolicy;
import SelectionPolicy.UCB1;
import SelectionPolicy.UCBSQRT;

import java.util.Collections;
import java.util.Comparator;

public class MCTS extends AI {

	private final boolean debug = false;
	private static final double DISCOUNT_FACTOR = 0.999;
	private static final long MINIMUM_SMART_THINKING_TIME = 500l;
	private static final long MAXIMUM_SMART_THINKING_TIME = 60000l;
	private final boolean USE_SQRTSH;
	//private final boolean USE_TIME_ADAPTATIVE;

	protected Node root = null;

	protected static SelectionPolicy rootPolicy;
	protected static SelectionPolicy policy;
	protected long smartThinkLimit = 0;
	protected int lastActionHistorySize = 0;

	protected String armsStats = "";

	public MCTS(final boolean h) {
		this.friendlyName = "UCT";
		USE_SQRTSH = h;
		if(USE_SQRTSH){
			setName("SQRTSH");
		}

		if(USE_SQRTSH)
			MCTS.rootPolicy = new UCBSQRT(); 
		MCTS.policy = new UCB1();
		
	}

	public void setName(String name){
		this.friendlyName = name;
	}

	@Override
	public void initAI(final int playerID) {
		super.initAI(playerID);
		smartThinkLimit = MAXIMUM_SMART_THINKING_TIME;
	}

	public boolean isHalveTime(final long usedResource, final double globalResource, final int halve) {
		return (usedResource >= (globalResource - (double) globalResource / (Math.pow(2, halve + 1))));

	}
	
	@Override
	public Move selectAction(
			final Game game,
			final double maxSeconds,
			final int maxIterations,
			final int maxDepth) {

		final long start_time = System.currentTimeMillis();
		final int maxIts = (maxIterations >= 0) ? maxIterations : Integer.MAX_VALUE;
		//final int initialPlayDepth = game.numTurn;

		super.countNodes = 0;

		/* both can be turned off when a certain condition is reach */
		Boolean needHalve = USE_SQRTSH;
		int halve = 0;
		int numIterations = 0;

		this.root = new Node(null, null, game);
		this.root.virtualN = root.unexpandedMoves.size();
		while (numIterations < maxIts) {

			/* :: SEQUENTIAL HALVING */
			if (needHalve && isHalveTime(numIterations, maxIts, halve) && root.unexpandedMoves.size() == 0){ 
				//System.out.printf("halve at %d (%d)\n", numIterations, root.virtualN);
				if (root.virtualN <= 4)
					needHalve = false;
				root.sort(root.virtualN);
				halve += 1;
				root.virtualN = (int) Math
						.max(Math.ceil(root.children.size() / Math.pow(2, halve)), 4);
			}

			/* :: SEARCH | EXPAND */
			Node current = root;
			if(USE_SQRTSH && current.unexpandedMoves.size()==0){
				current = rootPolicy.select(current);
			}
			current = search(current);

			/* :: SIMULATION */
			Game playoutGame = current.game.copy();
			int preTurn = playoutGame.numTurn;
			playout(playoutGame, maxDepth);
			int finalTurn = playoutGame.numTurn;
			/* :: BACKPROPAGATION */
			backpropagation(playoutGame, current, finalTurn - preTurn);

			++numIterations;
		}
		
		// :: FINAL SELECTION
		Node decidedNode = finalMoveSelection(root);
		smartThinkLimit -= System.currentTimeMillis() - start_time;
		
		// String output = getFrindlyName() + ": ";
		// for(Node n:root.children){
		// 	output += String.format("(%d|%.2f) ", n.N, n.Q[root.game.playerIndex()]);
		// }
		// output += String.format(" ===> (%d,%.2f) ", decidedNode.N, decidedNode.Q[root.game.playerIndex()]);
		// System.out.println(output);

		return decidedNode.moveFromParent;
	}

	public Node search(final Node root) {
		Node current = root;
		while (true) {

			if (current.game.isTerminal())
				break;

			else if (!current.unexpandedMoves.isEmpty()) {
				final Move move = current.unexpandedMoves.remove(
						ThreadLocalRandom.current().nextInt(current.unexpandedMoves.size()));
				final Game game = current.game.copy();
				game.apply(move);
				return new Node(current, move, game);
			}

			current = policy.select(current);

		}
		return current;
	}

	public void backpropagation(final Game playoutGame, Node current, final int numTurns) {

		final double[] utilities = playoutGame.utilities();
		final double[] discUtilities = playoutGame.utilities();
		for (int i = 0; i < utilities.length; i++) {
			discUtilities[i] = utilities[i] * Math.pow(DISCOUNT_FACTOR, numTurns);
		}

		// :: BACKPROP
		double learning = 1;
		while (current != null) {
			current.N += 1;
			for (int p = 0; p < current.game.playersCount; p++) {
				double discUt = discUtilities[p] * learning;
				current.Q[p] += discUt;
			}

			learning *= DISCOUNT_FACTOR;
			current = current.parent;
		}
	}

	private void printEvaluation(Node n) {
		System.out.println(String.format("ROOT  %d nodes", n.N));
		for (Node ch : n.children) {
			System.out.println(String.format("\t%s | (%.0f/%d) %.4f", ch.moveFromParent,
					ch.Q[this.player - 1],
					ch.N, ch.Q[this.player - 1] / ch.N));
		}
	}

	public Node finalMoveSelection(final Node rootNode) {
		Node bestChild = null;
		double bestWeightedReward = Integer.MIN_VALUE;
		int numBestFound = 0;

		final int numChildren = rootNode.children.size();
		final double pN = Double.valueOf(rootNode.N);
		for (int i = 0; i < numChildren; ++i) {
			final Node child = rootNode.children.get(i);
			final double weight= child.N/pN;
			final double weightedReward = weight * child.Q[rootNode.game.playerIndex()];
			if (weightedReward > bestWeightedReward) {
				bestWeightedReward = weightedReward;
				bestChild = child;
				numBestFound = 1;
			}
				// } else if (weightedReward == bestWeightedReward &&
			// 		ThreadLocalRandom.current().nextInt() % ++numBestFound == 0) {
			// 	bestChild = child;
			// }
		}

		return bestChild;
	}

}
