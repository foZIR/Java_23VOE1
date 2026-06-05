package main.java.common.model;

import java.io.Serializable;
import java.util.*;
import java.util.stream.IntStream;

public class Board implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int SIZE = 10;
    private final Cell[][] cells;
    private final List<Ship> ships;
    private int shipsRemaining;
    
    public enum ShotResult { MISS, HIT, SUNK, ALREADY_SHOT }
    
    public Board() {
        cells = new Cell[SIZE][SIZE];
        IntStream.range(0, SIZE).forEach(i ->
            IntStream.range(0, SIZE).forEach(j ->
                cells[i][j] = new Cell(i, j)
            )
        );
        ships = new ArrayList<>();
        shipsRemaining = 0;
    }
    
    public boolean placeShip(int x, int y, int length, boolean horizontal) {
        List<Cell> newCells = new ArrayList<>();
        
        for (int i = 0; i < length; i++) {
            int nx = x + (horizontal ? 0 : i);
            int ny = y + (horizontal ? i : 0);
            if (nx >= SIZE || ny >= SIZE) return false;
            if (!isCellAvailable(nx, ny)) return false;
            newCells.add(cells[nx][ny]);
        }
        
        Ship ship = new Ship();
        for (Cell cell : newCells) {
            ship.addCell(cell);
        }
        ships.add(ship);
        shipsRemaining++;  // ← ЭТА СТРОЧКА УЖЕ ЕСТЬ, НО ПРОВЕРЬТЕ!
        return true;
    }
    
    private boolean isCellAvailable(int x, int y) {
        if (cells[x][y].hasShip()) return false;
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < SIZE && ny >= 0 && ny < SIZE) {
                    if (cells[nx][ny].hasShip()) return false;
                }
            }
        }
        return true;
    }
    
    public ShotResult shoot(int x, int y) {
        Cell cell = cells[x][y];
        if (cell.isShot()) return ShotResult.ALREADY_SHOT;
        
        cell.setShot(true);
        if (!cell.hasShip()) return ShotResult.MISS;
        
        Optional<Ship> hitShip = ships.stream()
            .filter(ship -> ship.getCells().stream().anyMatch(c -> c == cell))
            .findFirst();
        
        if (hitShip.isPresent() && hitShip.get().isSunk()) {
            shipsRemaining--;
            return ShotResult.SUNK;
        }
        return ShotResult.HIT;
    }
    
    public boolean allShipsSunk() {
        return shipsRemaining == 0;
    }
    
    public int getShipsRemaining() { return shipsRemaining; }
    public Cell getCell(int x, int y) { return cells[x][y]; }
    
    public void autoPlaceShips() {

        ships.clear();
        shipsRemaining = 0;
        
        IntStream.range(0, SIZE).forEach(i ->
            IntStream.range(0, SIZE).forEach(j ->
                cells[i][j] = new Cell(i, j)
            )
        );
        
        int[][] shipsConfig = {{4, 1}, {3, 2}, {2, 3}, {1, 4}};
        Random random = new Random();
        
        for (int[] config : shipsConfig) {
            int length = config[0];
            int count = config[1];
            
            for (int i = 0; i < count; i++) {
                boolean placed = false;
                int attempts = 0;
                
                while (!placed && attempts < 1000) {
                    int x = random.nextInt(SIZE);
                    int y = random.nextInt(SIZE);
                    boolean horizontal = random.nextBoolean();
                    
                    if (placeShip(x, y, length, horizontal)) {
                        placed = true;
                    }
                    attempts++;
                }
                if (!placed) { autoPlaceShips(); return; }
            }
        }
    }

    // Также добавьте метод для отладки
    public void printDebug() {
        System.out.println("shipsRemaining: " + shipsRemaining);
        System.out.println("ships.size(): " + ships.size());
        int cellCount = 0;
        for (Ship ship : ships) {
            cellCount += ship.getCells().size();
        }
        System.out.println("Всего клеток в кораблях: " + cellCount);
    }

    public void addShip(List<Cell> cells) {
        Ship ship = new Ship();
        for (Cell cell : cells) {
            ship.addCell(cell);
        }
        ships.add(ship);
        shipsRemaining++;
    }
}