import java.util.ArrayList;

public class Statistics {
    ArrayList<Double> stdDevTrack;
    ArrayList<Double> meanTrack;
    ArrayList<Double> medianTrack;
    ArrayList<Double> entropyTrack;
    ArrayList<Double> varCoefTrack;


    Double stdDev;
    Double mean;
    Double median;
    Double entropy;
    Double varCoef;

    public Statistics(){
        stdDev = 0.0;
        mean=0.0;
        median=0.0;
        varCoef=0.0;
        stdDevTrack = new ArrayList<>();
        meanTrack = new ArrayList<>();
        medianTrack = new ArrayList<>();        
        entropyTrack = new ArrayList<>();
        varCoefTrack = new ArrayList<>();
    }

    public void computeMean(final Node node){
        double sumRw = 0;
		double sumN = 0;
		
		for(Node ch:node.children){
			sumRw+= ch.Q[node.game.mover-1];
			sumN+= ch.N;
		}
		mean = sumRw/sumN;
        meanTrack.add(mean);
    }

    public void computeMedian(final Node node){
        int chLen = node.children.size();
        
		if(chLen%2==0){
			Node m1 = node.children.get( (chLen/2) - 1);
			Node m2 = node.children.get( (chLen/2 + 1) - 1);
			median = (m1.Q[m1.game.mover]/m1.N + m2.Q[m1.game.mover]/m2.N)/2f;
		}else{
		    Node m = node.children.get((int)Math.ceil(chLen/2)-1);
		    median = m.Q[m.game.mover]/m.N;
        }
        medianTrack.add(median);
    }

    public void computeStandardDeviation(final Node node){
        double sumRw = 0;
		double sumN = 0;
		double average = 0;

		for(Node ch:node.children){
			sumRw+= ch.Q[node.game.mover-1];
			sumN+= ch.N;
		}
		average = sumRw/sumN;
		double sumDist=0;
		for(Node ch:node.children){
			sumDist+= Math.pow((ch.Q[node.game.mover-1]/ch.N)-average,2);
		}
		//divide pela média: coeficiente de variação
        stdDev = Math.sqrt(sumDist/sumN);
        stdDevTrack.add(stdDev);
    }

    public void computeVarCoef(final Node node){
        double sumRw = 0;
		double sumN = 0;
		double average = 0;

		for(Node ch:node.children){
			sumRw+= ch.Q[node.game.mover-1];
			sumN+= ch.N;
		}
		average = sumRw/sumN;
		double sumDist=0;
		for(Node ch:node.children){
			sumDist+= Math.pow((ch.Q[node.game.mover-1]/ch.N)-average,2);
		}
		//divide pela média: coeficiente de variação
        varCoef = Math.sqrt(sumDist/sumN)/average;
        varCoefTrack.add(varCoef);
    }

    public void computeEntropy(final Node node){


        double winProb = node.wins/(double)node.N;
        double drawnProb = node.drawn/(double)node.N;
        double lossProb = node.loss/(double)node.N;
        entropy = -1 * ((winProb * Math.log(winProb)) + (drawnProb * Math.log(drawnProb)) + (lossProb * Math.log(lossProb)));
        entropyTrack.add(entropy);
    }

    public String toStringMean(){
        String outputCSV = "";
        outputCSV += String.valueOf("Mean");
        for (Double v : meanTrack) {
            outputCSV += String.format(";%.4f", v);
        }
        //outputCSV += "\n";
        return outputCSV;
    }
    public String toStringMedian(){
        String outputCSV = "";
        outputCSV += String.valueOf("Median");
        for (Double v : medianTrack) {
            outputCSV += String.format(";%.4f", v);
        }
        //outputCSV += "\n";
        return outputCSV;
    }
    public String toStringStdDev(){
        String outputCSV = "";
        outputCSV += String.valueOf("Standard Deviation");
        for (Double v : stdDevTrack) {
            outputCSV += String.format(";%.4f", v);
        }
        //outputCSV += "\n";
        return outputCSV;
    }

    public String toStringVarCoef(){
        String outputCSV = "";
        outputCSV += String.valueOf("Variance Coeficient");
        for (Double v : varCoefTrack) {
            outputCSV += String.format(";%.4f", v);
        }
        //outputCSV += "\n";
        return outputCSV;
    }

    public String toStringEntropy(){
        String outputCSV = "";
        outputCSV += String.valueOf("Entropy");
        for (Double v : entropyTrack) {
            outputCSV += String.format(";%.4f", v);
        }
        //outputCSV += "\n";
        return outputCSV;
    }
}
