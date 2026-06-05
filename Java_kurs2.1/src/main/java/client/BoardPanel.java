package main.java.client;

import main.java.common.model.Board;
import main.java.common.model.Cell;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class BoardPanel extends JPanel {
    private static final int CELL_SIZE = 42;
    private Board board;
    private final boolean isEnemy;
    private boolean myTurn;
    private ShotListener listener;
    
    // Фиолетовая палитра для поля
    private final Color BG_COLOR = new Color(55, 45, 75);
    private final Color CELL_EMPTY = new Color(75, 65, 95);
    private final Color SHIP_COLOR = new Color(100, 70, 130);
    private final Color HIT_COLOR = new Color(180, 70, 100);
    private final Color MISS_COLOR = new Color(65, 55, 85);
    private final Color GRID_COLOR = new Color(140, 120, 170);
    
    public interface ShotListener {
        void onShot(int x, int y);
    }
    
    public BoardPanel(Board board, boolean isEnemy, ShotListener listener) {
        this.board = board;
        this.isEnemy = isEnemy;
        this.listener = listener;
        this.myTurn = false;
        
        setPreferredSize(new Dimension(CELL_SIZE * Board.SIZE, CELL_SIZE * Board.SIZE));
        setBackground(BG_COLOR);
        
        if (isEnemy) {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (myTurn && listener != null) {
                        int x = e.getY() / CELL_SIZE;
                        int y = e.getX() / CELL_SIZE;
                        if (x >= 0 && x < Board.SIZE && y >= 0 && y < Board.SIZE) {
                            if (!board.getCell(x, y).isShot()) {
                                listener.onShot(x, y);
                            }
                        }
                    }
                }
            });
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Рисуем сетку
        g2d.setColor(GRID_COLOR);
        for (int i = 0; i <= Board.SIZE; i++) {
            g2d.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, getHeight());
            g2d.drawLine(0, i * CELL_SIZE, getWidth(), i * CELL_SIZE);
        }
        
        // Рисуем клетки
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                Cell cell = board.getCell(i, j);
                int x = j * CELL_SIZE;
                int y = i * CELL_SIZE;
                
                if (cell.isShot()) {
                    if (cell.hasShip()) {
                        g2d.setColor(HIT_COLOR);
                        g2d.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
                        g2d.setColor(Color.WHITE);
                        g2d.setStroke(new BasicStroke(2));
                        g2d.drawLine(x + 8, y + 8, x + CELL_SIZE - 8, y + CELL_SIZE - 8);
                        g2d.drawLine(x + CELL_SIZE - 8, y + 8, x + 8, y + CELL_SIZE - 8);
                    } else {
                        g2d.setColor(MISS_COLOR);
                        g2d.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
                        g2d.setColor(new Color(160, 140, 190));
                        g2d.fillOval(x + CELL_SIZE/2 - 4, y + CELL_SIZE/2 - 4, 8, 8);
                    }
                } else if (cell.hasShip() && !isEnemy) {
                    GradientPaint gradient = new GradientPaint(x, y, SHIP_COLOR, 
                        x + CELL_SIZE, y + CELL_SIZE, new Color(80, 55, 110));
                    g2d.setPaint(gradient);
                    g2d.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
                    g2d.setColor(new Color(120, 90, 150));
                    g2d.drawRect(x + 4, y + 4, CELL_SIZE - 9, CELL_SIZE - 9);
                } else {
                    GradientPaint gradient = new GradientPaint(x, y, CELL_EMPTY, 
                        x + CELL_SIZE, y + CELL_SIZE, new Color(65, 55, 85));
                    g2d.setPaint(gradient);
                    g2d.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
                }
                
                // Подсветка при наведении на вражеское поле
                if (isEnemy && myTurn && !cell.isShot()) {
                    Point mouse = getMousePosition();
                    if (mouse != null) {
                        int hoverX = mouse.y / CELL_SIZE;
                        int hoverY = mouse.x / CELL_SIZE;
                        if (hoverX == i && hoverY == j) {
                            g2d.setColor(new Color(200, 150, 220, 80));
                            g2d.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
                        }
                    }
                }
            }
        }
        
        // Рисуем координаты
        g2d.setColor(GRID_COLOR);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
        
        // Цифры для строк (слева) - от 0 до 9
        for (int i = 0; i < Board.SIZE; i++) {
            String num = String.valueOf(i);
            g2d.drawString(num, 4, i * CELL_SIZE + CELL_SIZE/2 + 5);
        }
        
        // Буквы для столбцов (сверху) - A до J
        for (int i = 0; i < Board.SIZE; i++) {
            g2d.drawString(letters[i], i * CELL_SIZE + CELL_SIZE/2 - 4, 14);
        }
    }
    
    public void markAroundSunkShip(int hitX, int hitY) {
        List<Cell> shipCells = new ArrayList<>();
        boolean[][] visited = new boolean[Board.SIZE][Board.SIZE];
        findShipCells(hitX, hitY, shipCells, visited);
        
        if (shipCells.isEmpty()) return;
        
        for (Cell cell : shipCells) {
            int x = cell.getX();
            int y = cell.getY();
            
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int nx = x + dx;
                    int ny = y + dy;
                    if (nx >= 0 && nx < Board.SIZE && ny >= 0 && ny < Board.SIZE) {
                        Cell neighbor = board.getCell(nx, ny);
                        if (!neighbor.hasShip() && !neighbor.isShot()) {
                            neighbor.setShot(true);
                        }
                    }
                }
            }
        }
        repaint();
    }
    
    private void findShipCells(int x, int y, List<Cell> shipCells, boolean[][] visited) {
        if (x < 0 || x >= Board.SIZE || y < 0 || y >= Board.SIZE) return;
        if (visited[x][y]) return;
        
        Cell cell = board.getCell(x, y);
        if (!cell.hasShip()) return;
        
        visited[x][y] = true;
        shipCells.add(cell);
        
        findShipCells(x + 1, y, shipCells, visited);
        findShipCells(x - 1, y, shipCells, visited);
        findShipCells(x, y + 1, shipCells, visited);
        findShipCells(x, y - 1, shipCells, visited);
    }
    
    public void updateBoard(Board newBoard) {
        this.board = newBoard;
        repaint();
    }
    
    public void setMyTurn(boolean turn) { 
        this.myTurn = turn; 
        repaint();
    }
    
    public void setBoard(Board board) { 
        this.board = board; 
        repaint();
    }
}