package Agents;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import Games.Move;
import Games.Othello;
import Games.Game;
public abstract class AI{
	protected String friendlyName;
	public int player;
	protected Statistics stats;
	protected int countNodes;
	public Move selectAction
	(
		final Game game,
		final double maxSeconds, 
		final int maxIterations, 
		final int maxDepth
	){
		
		return null;
	}

	public void initAI(final int playerID) {
		this.player = playerID;
		stats = new Statistics();
	}

	public void setID(final int playerID){ this.player = playerID; }

	public void playout(Game playoutGame, final int maxDepth) {
		while (!playoutGame.isTerminal() && playoutGame.numTurn < maxDepth) {
			ArrayList<Move> moves = playoutGame.moves();
			playoutGame.apply(moves.get(ThreadLocalRandom.current().nextInt(moves.size())));
		}
		countNodes++;
	}

	public String getFrindlyName(){
		return friendlyName;
	}

	@Override
    public String toString(){
        return String.format("ag: %s\n\tavg time: %s\n\tnodes: %d", friendlyName, stats.toStringTime(), countNodes);
    }
}