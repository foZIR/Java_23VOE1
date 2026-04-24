package my.contacteditor;

public class IntegralCalcThread extends Thread {
    
    private double lowerLimit;
    private double upperLimit;
    private double range;
    private double result;
    
    public IntegralCalcThread(double lowerLimit, double upperLimit, double range) {
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.range = range;
        this.result = 0.0;
    }
    
    @Override
    public void run() {
        double start = lowerLimit;
        double h, sumS = 0;
        
        do {
            h = Math.min(range, upperLimit - start);
            sumS += h * (Math.cos(start) + Math.cos(start + h)) / 2;
            start += h;
        } while (start < upperLimit);
        
        result = sumS;
    }
    
    public double getResult() {
        return result;
    }
}