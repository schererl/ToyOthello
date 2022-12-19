import java.util.concurrent.ThreadLocalRandom;

public class UCB1 implements SelectionPolicy{
    
    public SHMCTS.Node select(final SHMCTS.Node current){

        SHMCTS.Node bestChild = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        int numBestFound = 0;
        
        final int numChildren = Math.min(current.virtualCHLen, current.children.size());
        final int mover = current.game.mover;
        final int moverPos = mover-1;
        final double twoParentLog = 2.0 * Math.log(Math.max(1, current.visitCount));
        
        for (int i = 0; i < numChildren; ++i) 
        {
        	final SHMCTS.Node child = current.children.get(i);
        	final double exploit = child.scoreSums[moverPos] / child.visitCount;
        	final double explore = Math.sqrt(twoParentLog / child.visitCount);
        	final double ucb1Value = exploit + (Math.sqrt(2)*explore);
            
            if (ucb1Value > bestValue)
            {
                bestValue = ucb1Value;
                bestChild = child;
                numBestFound = 1;
            }
            else if 
            (
            	ucb1Value == bestValue && 
            	ThreadLocalRandom.current().nextInt() % ++numBestFound == 0
            )
            {
            	bestChild = child;
            }
        }

        return bestChild;
    }
}
