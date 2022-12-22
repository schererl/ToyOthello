import java.util.concurrent.ThreadLocalRandom;

public class UCB1 implements SelectionPolicy{
    
    public Node select(final Node current){

        Node bestChild = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        int numBestFound = 0;
        
        final int numChildren = Math.min(current.virtualN, current.children.size());
        final int mover = current.game.mover;
        final int moverPos = mover-1;
        final double twoParentLog = 2.0 * Math.log(Math.max(1, current.N));
        
        for (int i = 0; i < numChildren; ++i) 
        {
        	final Node child = current.children.get(i);
        	final double exploit = child.Q[moverPos] / child.N;
        	final double explore = Math.sqrt(twoParentLog / child.N);
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
