package main.java.client;

import main.java.common.model.Board;
import main.java.common.model.Cell;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ShipPlacementPanel extends JDialog {
    private static final int CELL_SIZE = 40;
    private Board board;
    private List<ShipToPlace> shipsToPlace;
    private ShipToPlace currentShip;
    private boolean horizontal;
    private int currentX, currentY;
    private PlacementListener listener;
    private JPanel boardPanel;
    private JPanel shipsPanel;
    private JLabel statusLabel;
    
    public interface PlacementListener {
        void onPlacementComplete(Board board);
    }
    
    public ShipPlacementPanel(JFrame parent, Board board, PlacementListener listener) {
        super(parent, "Расстановка кораблей", true);
        this.board = board;
        this.listener = listener;
        this.horizontal = true;
        this.currentX = -1;
        this.currentY = -1;
        
        initShips();
        initUI();
    }
    
    private void initShips() {
        shipsToPlace = new ArrayList<>();
        shipsToPlace.add(new ShipToPlace(4, 1, "Авианосец (4 клетки)"));
        shipsToPlace.add(new ShipToPlace(3, 2, "Крейсер (3 клетки)"));
        shipsToPlace.add(new ShipToPlace(2, 3, "Эсминец (2 клетки)"));
        shipsToPlace.add(new ShipToPlace(1, 4, "Катер (1 клетка)"));
        currentShip = shipsToPlace.get(0);
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(45, 35, 65));
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(45, 35, 65));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        statusLabel = new JLabel("Расставьте корабли", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(new Color(200, 150, 220));
        topPanel.add(statusLabel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        
        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
            }
        };
        boardPanel.setPreferredSize(new Dimension(CELL_SIZE * 10, CELL_SIZE * 10));
        boardPanel.setBackground(new Color(55, 45, 75));
        boardPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                currentX = e.getY() / CELL_SIZE;
                currentY = e.getX() / CELL_SIZE;
                boardPanel.repaint();
            }
        });
        boardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getY() / CELL_SIZE;
                int y = e.getX() / CELL_SIZE;
                if (canPlaceShip(x, y)) {
                    placeShip(x, y);
                }
            }
        });
        
        add(boardPanel, BorderLayout.CENTER);
        
        shipsPanel = new JPanel();
        shipsPanel.setLayout(new BoxLayout(shipsPanel, BoxLayout.Y_AXIS));
        shipsPanel.setBackground(new Color(55, 45, 75));
        shipsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        updateShipsPanel();
        
        add(shipsPanel, BorderLayout.EAST);
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(new Color(45, 35, 65));
        
        JButton rotateButton = new JButton("🔄 Повернуть (R)");
        rotateButton.setBackground(new Color(100, 70, 130));
        rotateButton.setForeground(Color.WHITE);
        rotateButton.addActionListener(e -> {
            horizontal = !horizontal;
            boardPanel.repaint();
        });
        
        JButton autoButton = new JButton("🎲 Авторасстановка");
        autoButton.setBackground(new Color(100, 70, 130));
        autoButton.setForeground(Color.WHITE);
        autoButton.addActionListener(e -> {
            board.autoPlaceShips();
            listener.onPlacementComplete(board);
            dispose();
        });
        
        JButton clearButton = new JButton("🗑️ Очистить поле");
        clearButton.setBackground(new Color(150, 70, 100));
        clearButton.setForeground(Color.WHITE);
        clearButton.addActionListener(e -> {
            board = new Board();
            initShips();
            boardPanel.repaint();
            updateShipsPanel();
            statusLabel.setText("Расставьте корабли");
        });
        
        bottomPanel.add(rotateButton);
        bottomPanel.add(autoButton);
        bottomPanel.add(clearButton);
        add(bottomPanel, BorderLayout.SOUTH);
        
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_R) {
                horizontal = !horizontal;
                boardPanel.repaint();
                return true;
            }
            return false;
        });
        
        pack();
        setLocationRelativeTo(getParent());
    }
    
    private void drawBoard(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setColor(new Color(140, 120, 170));
        for (int i = 0; i <= 10; i++) {
            g2d.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, getHeight());
            g2d.drawLine(0, i * CELL_SIZE, getWidth(), i * CELL_SIZE);
        }
        
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int x = j * CELL_SIZE;
                int y = i * CELL_SIZE;
                
                if (board.getCell(i, j).hasShip()) {
                    g2d.setColor(new Color(100, 70, 130));
                    g2d.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
                } else {
                    g2d.setColor(new Color(75, 65, 95));
                    g2d.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
                }
            }
        }
        
        if (currentShip != null && currentShip.getCount() > 0 && currentX >= 0 && currentY >= 0) {
            if (canPlaceShip(currentX, currentY)) {
                g2d.setColor(new Color(100, 255, 100, 100));
            } else {
                g2d.setColor(new Color(255, 100, 100, 100));
            }
            
            for (int i = 0; i < currentShip.getLength(); i++) {
                int x = currentX;
                int y = currentY;
                if (horizontal) {
                    y = currentY + i;
                } else {
                    x = currentX + i;
                }
                if (x < 10 && y < 10) {
                    g2d.fillRect(y * CELL_SIZE + 1, x * CELL_SIZE + 1, CELL_SIZE - 2, CELL_SIZE - 2);
                }
            }
        }
        
        g2d.setColor(new Color(140, 120, 170));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
        String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
        for (int i = 0; i < 10; i++) {
            g2d.drawString(String.valueOf(i), 4, i * CELL_SIZE + CELL_SIZE/2 + 5);
            g2d.drawString(letters[i], i * CELL_SIZE + CELL_SIZE/2 - 4, 14);
        }
    }
    
    private boolean canPlaceShip(int x, int y) {
        if (currentShip == null || currentShip.getCount() == 0) return false;
        
        for (int i = 0; i < currentShip.getLength(); i++) {
            int nx = x;
            int ny = y;
            if (horizontal) {
                ny = y + i;
            } else {
                nx = x + i;
            }
            if (nx >= 10 || ny >= 10) return false;
            if (board.getCell(nx, ny).hasShip()) return false;
            
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int nnx = nx + dx;
                    int nny = ny + dy;
                    if (nnx >= 0 && nnx < 10 && nny >= 0 && nny < 10) {
                        if (board.getCell(nnx, nny).hasShip()) return false;
                    }
                }
            }
        }
        return true;
    }
    
    private void placeShip(int x, int y) {
        if (!canPlaceShip(x, y)) return;
        
        // Создаём временный список клеток для корабля
        List<Cell> shipCells = new ArrayList<>();
        
        for (int i = 0; i < currentShip.getLength(); i++) {
            int nx = x;
            int ny = y;
            if (horizontal) {
                ny = y + i;
            } else {
                nx = x + i;
            }
            Cell cell = board.getCell(nx, ny);
            cell.setShip(true);
            shipCells.add(cell);
        }
        
        // Добавляем корабль в список ships через метод addShip
        board.addShip(shipCells);
        
        currentShip.placeOne();
        updateShipsPanel();
        boardPanel.repaint();
        
        if (allShipsPlaced()) {
            statusLabel.setText("✅ Все корабли расставлены!");
            
            int totalShips = countShipsOnBoard();
            int shipCount = board.getShipsRemaining();
            System.out.println("Ручная расстановка завершена. Клеток с кораблями: " + totalShips);
            System.out.println("Кораблей в списке ships: " + shipCount);
            
            int option = JOptionPane.showConfirmDialog(this, 
                "Все корабли расставлены! Начать игру?", 
                "Готово", 
                JOptionPane.YES_NO_OPTION);
            
            if (option == JOptionPane.YES_OPTION) {
                listener.onPlacementComplete(board);
                dispose();
            }
        } else {
            statusLabel.setText("Осталось: " + getRemainingShipsCount() + " кораблей");
            updateCurrentShip();
        }
    }
    
    private int countShipsOnBoard() {
        int count = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (board.getCell(i, j).hasShip()) count++;
            }
        }
        return count;
    }
    
    private void updateCurrentShip() {
        for (ShipToPlace ship : shipsToPlace) {
            if (ship.getCount() > 0) {
                currentShip = ship;
                return;
            }
        }
        currentShip = null;
    }
    
    private void updateShipsPanel() {
        shipsPanel.removeAll();
        
        for (ShipToPlace ship : shipsToPlace) {
            JPanel shipPanel = new JPanel();
            shipPanel.setLayout(new BoxLayout(shipPanel, BoxLayout.Y_AXIS));
            shipPanel.setBackground(new Color(65, 55, 85));
            shipPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(140, 120, 170), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            
            JLabel nameLabel = new JLabel(ship.getName());
            nameLabel.setForeground(new Color(200, 150, 220));
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            
            JLabel countLabel = new JLabel("Осталось: " + ship.getCount());
            countLabel.setForeground(Color.WHITE);
            countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            
            JPanel previewPanel = new JPanel();
            previewPanel.setBackground(new Color(65, 55, 85));
            for (int i = 0; i < ship.getLength(); i++) {
                JLabel cell = new JLabel("■");
                cell.setFont(new Font("Segoe UI", Font.BOLD, 16));
                cell.setForeground(new Color(100, 70, 130));
                previewPanel.add(cell);
            }
            
            shipPanel.add(nameLabel);
            shipPanel.add(previewPanel);
            shipPanel.add(countLabel);
            shipsPanel.add(shipPanel);
            shipsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        
        shipsPanel.revalidate();
        shipsPanel.repaint();
    }
    
    private boolean allShipsPlaced() {
        return shipsToPlace.stream().allMatch(s -> s.getCount() == 0);
    }
    
    private int getRemainingShipsCount() {
        return shipsToPlace.stream().mapToInt(ShipToPlace::getCount).sum();
    }
    
    class ShipToPlace {
        private int length;
        private int count;
        private String name;
        
        ShipToPlace(int length, int count, String name) {
            this.length = length;
            this.count = count;
            this.name = name;
        }
        
        int getLength() { return length; }
        int getCount() { return count; }
        String getName() { return name; }
        void placeOne() { count--; }
    }
}