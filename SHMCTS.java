import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Collections;
import java.util.Comparator;

public class SHMCTS extends AI {

	private final boolean debug = false;
	private static final double DISCOUNT_FACTOR = 0.999;
	private static final long MINIMUM_SMART_THINKING_TIME = 500l;
	private static final long MAXIMUM_SMART_THINKING_TIME = 60000l;
	private final boolean USE_HALVE;
	private final boolean USE_TIME_ADAPTATIVE;
	
	
	protected Node root = null;
	protected static SelectionPolicy policy;
	protected long smartThinkLimit = 0;
	protected int lastActionHistorySize = 0;

	protected String armsStats="";
	
	public SHMCTS(final boolean tr, final boolean h, final boolean ta) {
		this.friendlyName = "UCT";
		USE_HALVE = h;
		USE_TIME_ADAPTATIVE = ta;
	}

	@Override
	public void initAI(final int playerID) {
        super.initAI(playerID);
    	SHMCTS.policy = new UCB1();
		smartThinkLimit = MAXIMUM_SMART_THINKING_TIME;
	}

	public boolean isHalveTime(final long pass_time, final double TIME_LIMIT, final int halve) {
		return (pass_time >= (TIME_LIMIT - (double) TIME_LIMIT / (Math.pow(2, halve + 1))));

	}

	// gives a bonus for thinking time if it is allowed
	public long bonusTT(long minimumTime, double avgDepth, long remainingTime, int numberPlayers) {
		return (long) Math.max(minimumTime,
				Math.min(2000, Math.floor(remainingTime / Math.max(1, avgDepth / numberPlayers)))) - minimumTime;
	}

	@Override
	public Move selectAction(
			final Othello game,
			final double maxSeconds,
			final int maxIterations,
			final int maxDepth) {

		final long start_time = System.currentTimeMillis();
		final int maxIts = (maxIterations >= 0) ? maxIterations : Integer.MAX_VALUE;
		final int initialPlayDepth = game.numTurn;
		
		stats.clear();
        stats.startCronometer();
		super.countNodes=0;
        
		/* both can change if estimaTimeBonus is true */
		long smartThink = MINIMUM_SMART_THINKING_TIME;
		long stopTime = smartThink + start_time; // (maxSeconds > 0.0) ? start_time + (long) smartThink/*(maxSeconds *
													// smartThink)*/ : Long.MAX_VALUE;

		/* both can be turned off when a certain condition is reach */
		Boolean needHalve = USE_HALVE;
		Boolean useTimeAdaptative = USE_TIME_ADAPTATIVE;

		int sumDepths = 0;
		int validDepths = 0;
		int halve = 0;
		int numIterations = 0;

		long pass_time = System.currentTimeMillis() - start_time;
		this.root = new Node(null, null, game);
		this.root.virtualN = root.children.size() + root.unexpandedMoves.size();
		while (numIterations < maxIts ) {



			/* TIME BONUS */
			if (useTimeAdaptative && pass_time >= smartThink / 2) {
				double avgDepth = validDepths > 0 ? sumDepths / Math.max(1, validDepths) : 0;
				useTimeAdaptative = false;
				smartThink += bonusTT(smartThink, avgDepth, smartThinkLimit, Othello.PLAYERS_COUNT - 1);
				stopTime = start_time + (long) (maxSeconds * smartThink);
			}

			/* :: SEQUENTIAL HALVING */
			if (needHalve && (isHalveTime(numIterations, maxIts, halve) /*|| isHalveTime(pass_time, smartThink, halve)*/)) {
				//double v = maxIts / (Math.pow(2, halve + 1));
				//double v2 = maxIts - v;
				
				if (root.virtualN <= 4)
					needHalve = false;
				root.sort(Math.min(root.children.size(), root.virtualN));
				halve += 1;
				root.virtualN = (int) Math
						.max(Math.ceil((root.children.size() + root.unexpandedMoves.size()) / Math.pow(2, halve)), 4);
			}

			/* :: SEARCH | EXPAND */
			Node current = search(root);

			/* :: SIMULATION */
			Othello playoutGame = current.game.copy();
			int preTurn = playoutGame.numTurn;
			playout(playoutGame, maxDepth);
			int finalTurn = playoutGame.numTurn;

			if (playoutGame.isTerminal()) {
				sumDepths += (finalTurn - preTurn);
				validDepths++;
			}

			/* :: BACKPROPAGATION */
			backpropagation(playoutGame, current, finalTurn - preTurn);

			++numIterations;
			pass_time = System.currentTimeMillis() - start_time;


			
			if(numIterations%100==0){
				for(Node child: root.children){
					if(child.stats==null)child.stats=new Statistics();
					child.stats.computeMean(child);
				}
				stats.computeStandardDeviation(root);
				stats.computeMean(root);
				stats.computeMedian(root);
				stats.computeEntropy(root);
				stats.computeVarCoef(root);
			}
		}
		//System.out.println(var);
		final long end_time = System.currentTimeMillis();

		// :: FINAL SELECTION
		Node decidedNode = finalMoveSelection(root);
		//stats.computeEntropy(decidedNode);
		smartThinkLimit -= System.currentTimeMillis() - start_time;
		
        stats.endCronometer();
		root.sort(root.virtualN);
		
		if(root.children.size()>4){
			armsStats  = root.children.get(0).moveFromParent + root.children.get(0).stats.toStringMean()+"\n";
			armsStats += root.children.get(1).moveFromParent + root.children.get(1).stats.toStringMean()+"\n";
			armsStats += root.children.get(2).moveFromParent + root.children.get(2).stats.toStringMean()+"\n";
			armsStats += root.children.get(3).moveFromParent + root.children.get(3).stats.toStringMean()+"\n";
		}
		printEvaluation(root);
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
				final Othello game = current.game.copy();
				game.apply(move);
				return new Node(current, move, game);
			}

			current = policy.select(current);

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

			if(utilities[current.game.mover-1] == 1){
				current.wins += 1;
			}else if(utilities[current.game.mover-1] == -1){
				current.loss +=1;
			}else{
				current.drawn+=1;
			}
			learning *= DISCOUNT_FACTOR;
			current = current.parent;
		}
	}

	private void printEvaluation(Node n) {
		System.out.println(String.format("ROOT  %d nodes", n.N));
		for (Node ch : n.children) {
			System.out.println(String.format("\t%s | (%.0f/%d) %.4f", ch.moveFromParent,
			ch.Q[this.player-1],
			ch.N,ch.Q[this.player-1]/ch.N));
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
