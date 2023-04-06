package SelectionPolicy;
import Agents.Node;

public interface SelectionPolicy {
    public Node select(final Node current);
    public void setCoeficient(double c);
    public void resetCoeficient();
}