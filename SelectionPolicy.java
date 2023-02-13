
public class SelectionPolicy {
    public final double EXPLORATION_COEFFICIENT = 1/Math.sqrt(2);
    protected double expCoef = EXPLORATION_COEFFICIENT;


    public Node select(final Node current){return null;}
    public void setExpCoef(double coef){
        expCoef = coef;
    }
    public void resetExpCoef(){
        this.expCoef = EXPLORATION_COEFFICIENT;
    }
}