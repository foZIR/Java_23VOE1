package my.contacteditor;

public class IntegralThread extends Thread {
    
    private double lowerLimit;
    private double upperLimit;
    private double range;
    private double result;
    private boolean completed;
    private Exception error;
    
    public IntegralThread(double lowerLimit, double upperLimit, double range) {
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.range = range;
        this.result = 0.0;
        this.completed = false;
        this.error = null;
    }
    
    @Override
    public void run() {
        try {
            double start = lowerLimit;
            double h, sumS = 0;
            
            do {
                h = Math.min(range, upperLimit - start);
                sumS += h * (Math.cos(start) + Math.cos(start + h)) / 2;
                start += h;
            } while (start < upperLimit);
            
            result = sumS;
            completed = true;
        } catch (Exception ex) {
            error = ex;
            completed = false;
        }
    }
    
    public double getResult() {
        return result;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public Exception getError() {
        return error;
    }
}