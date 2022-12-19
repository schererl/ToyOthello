public abstract class AI{
	protected String friendlyName;
	
	public Move selectAction
	(
		final Othello game,
		final double maxSeconds, 
		final int maxIterations, 
		final int maxDepth
	){return null;}

	public void initAI(final Othello game, final int playerID) {}
}