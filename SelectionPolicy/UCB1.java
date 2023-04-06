package SelectionPolicy;
import java.util.concurrent.ThreadLocalRandom;

import Agents.Node;

public class UCB1 implements SelectionPolicy{
    private final double STD_COEFICIENT = 1/Math.sqrt(2);
    private double coef = STD_COEFICIENT;
    public Node select(final Node current){

        Node bestChild = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        int numBestFound = 0;
        
        final int numChildren = Math.min(current.virtualN, current.children.size());
        final int mover = current.game.mover;
        final int moverPos = current.game.playerIndex();
        final double twoParentLog = 2.0 * Math.log(Math.max(1, current.N));
        
        for (int i = 0; i < numChildren; ++i) 
        {
        	final Node child = current.children.get(i);
        	final double exploit = child.Q[moverPos] / child.N;
        	final double explore = Math.sqrt(twoParentLog / child.N);
        	final double ucb1Value = exploit + (Math.sqrt(2.0)*explore);
            
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

    public void setCoeficient(double c){ coef = c; }
    public void resetCoeficient(){ coef = STD_COEFICIENT; }
}
