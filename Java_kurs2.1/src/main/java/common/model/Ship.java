package main.java.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Ship implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<Cell> cells;
    
    public Ship() {
        this.cells = new ArrayList<>();
    }
    
    public void addCell(Cell cell) {
        cells.add(cell);
        cell.setShip(true);
    }
    
    public boolean isSunk() {
        return cells.stream().allMatch(Cell::isShot);
    }
    
    public List<Cell> getCells() { return cells; }
}