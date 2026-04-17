/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.contacteditor;
/**
 *
 * @author student
 */

    class RecIntegral {
    private static final double MIN_VALUE = 0.000001;
    private static final double MAX_VALUE = 1000000.0;
    
    private double LowerLimit;
    private double UpperLimit;
    private double Range;
    private double Result;

    public RecIntegral(double LowerLimit, double UpperLimit, double Range) throws InvalidRangeException {
       validateRange(LowerLimit);
       validateRange(UpperLimit);
       validateRange(Range);
       
        this.LowerLimit = LowerLimit;
        this.UpperLimit = UpperLimit;
        this.Range = Range;
        this.Result = 0.0;
    }
    
     public RecIntegral(double LowerLimit, double UpperLimit, double Range, double result) throws InvalidRangeException {
        validateRange(UpperLimit);
        validateRange(LowerLimit);
        validateRange(Range);
        
        this.LowerLimit = LowerLimit;
        this.UpperLimit = UpperLimit;
        this.Range = Range;
        this.Result = result;
    }
     
      private void validateRange(double value) throws InvalidRangeException{
        if (value < MIN_VALUE || value >MAX_VALUE){
            throw new InvalidRangeException(
                "Значение должно быть в диапазоне от " +
                MIN_VALUE + "до " + MAX_VALUE + "\n", value
            );
        }
    }

    public double getLowerLimit() {
        return LowerLimit;
    }

    public double getUpperLimit() {
        return UpperLimit;
    }

    public double getRange() {
        return Range;
    }

    public double getResult() {
        return Result;
    }

    public void setResult(double Result) {
        this.Result = Result;
    }
    
public double CalcIntegral(double LowerLimit, double UpperLimit, double Range){
        
        double start, h, sumS = 0;
        
        start = LowerLimit;
        
        do{
            h = Math.min(Range, (UpperLimit-start));
            sumS += h * (Math.cos(start) + Math.cos(start + h))/2;
            start += h;
        }while((start) < UpperLimit);
        
        return sumS;
} 
}  