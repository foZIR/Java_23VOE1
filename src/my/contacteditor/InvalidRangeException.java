package my.contacteditor;

public class InvalidRangeException extends Exception { 
    double errVal; 
    
    public double getErrVal(){
        return errVal;
    }
    
    public InvalidRangeException(String message, double num){
        super(message);  
        errVal = num;
    }
}