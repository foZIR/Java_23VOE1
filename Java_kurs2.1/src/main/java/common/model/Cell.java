package main.java.common.model;

import java.io.Serializable;

public class Cell implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int x;
    private final int y;
    private boolean hasShip;
    private boolean isShot;
    
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.hasShip = false;
        this.isShot = false;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean hasShip() { return hasShip; }
    public void setShip(boolean hasShip) { this.hasShip = hasShip; }
    public boolean isShot() { return isShot; }
    public void setShot(boolean shot) { isShot = shot; }
}