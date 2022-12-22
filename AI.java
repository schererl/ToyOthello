import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AI{
	protected String friendlyName;
	protected int player;
	public Move selectAction
	(
		final Othello game,
		final double maxSeconds, 
		final int maxIterations, 
		final int maxDepth
	){return null;}

	public void initAI(final int playerID) {this.player = playerID;}

	public static void playout(Othello playoutGame, final int maxDepth) {
		while (!playoutGame.isTerminal() && playoutGame.numTurn < maxDepth) {
			ArrayList<Move> moves = playoutGame.moves();
			playoutGame.apply(moves.get(ThreadLocalRandom.current().nextInt(moves.size())));
		}
	}
}