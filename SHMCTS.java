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
	
	
	protected int player = -1;
	protected Node root = null;
	protected static SelectionPolicy policy;
	protected long smartThinkLimit = 0;
	protected int lastActionHistorySize = 0;

	ArrayList<Double> var = new ArrayList<Double>();
	static int internal = 0;
	public SHMCTS(final boolean tr, final boolean h, final boolean ta) {
		this.friendlyName = "SHMCTS";
		USE_HALVE = h;
		USE_TIME_ADAPTATIVE = ta;
	}

	@Override
	public void initAI(final Othello game, final int playerID) {
		this.player = playerID;
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
		//int[] itVar = new int[]{1000,2000,3000,4000,5000,6000,7000,8000,9000,10000};
		//int idxVar = 0;
		internal=0;
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
		this.root = new Node(root, null, game);
		this.root.virtualCHLen = root.children.size() + root.unexpandedMoves.size();
		while (numIterations < maxIts ) {

			/* TIME BONUS */
			if (useTimeAdaptative && pass_time >= smartThink / 2) {
				double avgDepth = validDepths > 0 ? sumDepths / Math.max(1, validDepths) : 0;
				useTimeAdaptative = false;
				smartThink += bonusTT(smartThink, avgDepth, smartThinkLimit, Othello.PLAYERS_COUNT - 1);
				stopTime = start_time + (long) (maxSeconds * smartThink);
			}

			/* :: SEQUENTIAL HALVING */
			if (needHalve && isHalveTime(pass_time, smartThink, halve)) {
				if (root.virtualCHLen <= 4)
					needHalve = false;
				root.sort(Math.min(root.children.size(), root.virtualCHLen), this.player);
				halve += 1;
				root.virtualCHLen = (int) Math
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

			if(root.unexpandedMoves.size()==0 && numIterations%100==0){
			//if(root.unexpandedMoves.size()==0 && numIterations==9999){
				//var.add(this.childrenRewardSTDDEV(root));
				var.add(this.childrenRewardSTDDEV(root));
				//idxVar++;
			}
		}
		//System.out.println(var);
		final long end_time = System.currentTimeMillis();

		// :: FINAL SELECTION
		Node decidedNode = finalMoveSelection(root);
		smartThinkLimit -= System.currentTimeMillis() - start_time;

		return decidedNode.moveFromParent;
	}

	public static Node search(final Node root) {
		Node current = root;
		while (true) {

			if (current.game.isTerminal())
				break;

			else if (!current.unexpandedMoves.isEmpty()) {
				final Move move = current.unexpandedMoves.remove(
						ThreadLocalRandom.current().nextInt(current.unexpandedMoves.size()));
				final Othello game = current.game.copy();
				game.apply(move);
				internal++;
				return new Node(current, move, game);
			}

			current = policy.select(current);

		}
		return current;
	} 

	public static void backpropagation(final Othello playoutGame, Node current, final int numTurns) {

		final double[] utilities = playoutGame.utilities();
		for (int i = 0; i < utilities.length; i++) {
			utilities[i] = utilities[i] * Math.pow(0.999, numTurns);
		}
		// :: BACKPROP
		double learning = 1;
		while (current != null) {
			current.visitCount += 1;
			for (int p = 0; p < Othello.PLAYERS_COUNT; p++) {
				double discUt = utilities[p] * learning;
				current.scoreSums[p] += discUt;
			}
			learning *= DISCOUNT_FACTOR;
			current = current.parent;
		}
	}

	public static void playout(Othello playoutGame, final int maxDepth) {
		while (!playoutGame.isTerminal() && playoutGame.numTurn < maxDepth) {
			ArrayList<Move> moves = playoutGame.moves();
			playoutGame.apply(moves.get(ThreadLocalRandom.current().nextInt(moves.size())));
		}
	}

	private void printEvaluation(Node n, long init, long end) {
		System.out.println(String.format("ROOT  %d nodes", n.visitCount));
		for (Node ch : n.children) {
			// System.out.println(String.format("\t%s | (%.0f/%d) %.4f", ch.moveFromParent,
			// ch.scoreSums[this.player],
			// ch.visitCount,ch.scoreSums[this.player]/ch.visitCount));
		}
		System.out.printf("ELAPSED TIME: %d\n", end - init);
	}

	public static Node finalMoveSelection(final Node rootNode) {
		Node bestChild = null;
		int bestVisitCount = Integer.MIN_VALUE;
		int numBestFound = 0;

		final int numChildren = rootNode.children.size();

		for (int i = 0; i < numChildren; ++i) {
			final Node child = rootNode.children.get(i);
			final int visitCount = child.visitCount;

			if (visitCount > bestVisitCount) {
				bestVisitCount = visitCount;
				bestChild = child;
				numBestFound = 1;
			} else if (visitCount == bestVisitCount &&
					ThreadLocalRandom.current().nextInt() % ++numBestFound == 0) {
				bestChild = child;
			}
		}

		return bestChild;
	}

	public double childrenRewardSTDDEV(final Node node){
		double sumRw = 0;
		double sumN = 0;
		double average = 0;

		for(Node ch:node.children){
			sumRw+= ch.scoreSums[this.player-1];
			sumN+= ch.visitCount;
		//	System.out.printf("\tch: %.4f\n",ch.scoreSums[this.player-1]);
		}
		average = sumRw/sumN;
		//System.out.printf("\tavg: %.4f (%.4f/%.4f)\n",sumRw/sumN, sumRw,sumN);
		double sumDist=0;
		for(Node ch:node.children){
			sumDist+= Math.pow((ch.scoreSums[this.player-1]/ch.visitCount)-average,2);
		}
		//System.out.printf("tddev: %.4f (%.4f/%.4f)\n", Math.abs(Math.sqrt(sumDist/sumN)), sumDist,sumN);
		
		return sumDist/sumN;//Math.abs(Math.sqrt(sumDist/sumN));
	}

	public double childrenArmPullsSTDDEV(final Node node){
		double sumN = 0;
		double average = 0;

		for(Node ch:node.children){
			sumN+= ch.visitCount;
		}
		average = sumN/node.visitCount;
		
		int sumDist=0;
		for(Node ch:node.children){
			sumDist+= Math.pow(ch.visitCount-average,2);
		}
		return Math.sqrt(sumDist/sumN);
	}

	public static class Node {
		public Node parent;
		public Move concreteMoveFromParent;
		public Move moveFromParent;
		public final Othello game;
		public int visitCount = 0;
		public final double[] scoreSums;
		public final List<Node> children = new ArrayList<Node>();
		public final ArrayList<Move> unexpandedMoves;
		public int virtualCHLen;

		public Node(final Node parent, Move moveFromParent, final Othello game) {
			this.parent = parent;
			this.moveFromParent = moveFromParent;
			this.game = game;
			scoreSums = new double[game.PLAYERS_COUNT + 1];
			unexpandedMoves = new ArrayList<Move>(game.moves());
			virtualCHLen = unexpandedMoves.size() + children.size(); // reset VirtualCHLen

			if (parent != null)
				parent.children.add(this);
		}

		public Node findChildForMove(final Move move) {
			for (final Node child : children) {

				if (child != null && child.concreteMoveFromParent.equals(move)) {
					child.parent = null;
					return child;
				}
			}

			return null;
		}

		private void sort(final int interval, final int agent) {
			List<Node> childrenCopy = children.subList(0, interval);
			final int mover = game.mover;
			Collections.sort(childrenCopy, new Comparator<Node>() {
				@Override
				public int compare(Node o1, Node o2) {
					double f1 = o1.visitCount;
					double f2 = o2.visitCount;
					return Double.compare(-f1, -f2);
				}
			});
			for (int i = 0; i < childrenCopy.size(); i++) {
				children.set(i, childrenCopy.get(i));
			}
		}

	}

	// -------------------------------------------------------------------------

}
