import java.util.ArrayList;

public class Statistics {
    ArrayList<Double> stdDevTrack;
    ArrayList<Double> meanTrack;
    ArrayList<Double> medianTrack;

    Double stdDev;
    Double mean;
    Double median;

    public Statistics(){
        stdDev = 0.0;
        mean=0.0;
        median=0.0;
        stdDevTrack = new ArrayList<>();
        meanTrack = new ArrayList<>();
        medianTrack = new ArrayList<>();
    }

    public void computeMean(SHMCTS.Node node){
        double sumRw = 0;
		double sumN = 0;
		
		for(SHMCTS.Node ch:node.children){
			sumRw+= ch.scoreSums[node.game.mover-1];
			sumN+= ch.visitCount;
		}
		mean = sumRw/sumN;
        meanTrack.add(mean);
    }

    public void computeMedian(SHMCTS.Node node){
        int chLen = node.children.size();
		if(chLen%2==0){
			SHMCTS.Node m1 = node.children.get(chLen/2);
			SHMCTS.Node m2 = node.children.get(chLen/2 + 1);
			median = (m1.scoreSums[m1.game.mover]/m1.visitCount + m2.scoreSums[m1.game.mover]/m2.visitCount)/2f;
		}else{
		    SHMCTS.Node m = node.children.get((int)Math.ceil(chLen/2));
		    median = m.scoreSums[m.game.mover]/m.visitCount;
        }
        medianTrack.add(median);
    }

    public void computeStandardDeviation(SHMCTS.Node node){
        double sumRw = 0;
		double sumN = 0;
		double average = 0;

		for(SHMCTS.Node ch:node.children){
			sumRw+= ch.scoreSums[node.game.mover-1];
			sumN+= ch.visitCount;
		}
		average = sumRw/sumN;
		double sumDist=0;
		for(SHMCTS.Node ch:node.children){
			sumDist+= Math.pow((ch.scoreSums[node.game.mover-1]/ch.visitCount)-average,2);
		}
		
        stdDev = Math.abs(Math.sqrt(sumDist/sumN));
        stdDevTrack.add(stdDev);
    }

    public String toStringMean(){
        String outputCSV = "";
        outputCSV += String.valueOf("Mean");
        for (Double v : meanTrack) {
            outputCSV += String.format(";%.4f", v);
        }
        outputCSV += "\n";
        return outputCSV;
    }
    public String toStringMedian(){
        String outputCSV = "";
        outputCSV += String.valueOf("Median");
        for (Double v : medianTrack) {
            outputCSV += String.format(";%.4f", v);
        }
        outputCSV += "\n";
        return outputCSV;
    }
    public String toStringStdDev(){
        String outputCSV = "";
        outputCSV += String.valueOf("Standard Deviation");
        for (Double v : stdDevTrack) {
            outputCSV += String.format(";%.4f", v);
        }
        outputCSV += "\n";
        return outputCSV;
    }
}
