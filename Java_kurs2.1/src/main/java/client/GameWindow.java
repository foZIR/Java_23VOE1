package main.java.client;

import main.java.common.model.Board;
import main.java.common.model.Board.ShotResult;

import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {
    private GameClient client;
    private BoardPanel myPanel;
    private BoardPanel enemyPanel;
    private JLabel statusLabel;
    private JLabel shipsLabel;
    private JLabel roomLabel;
    private JButton readyButton;
    private JButton autoButton;
    private boolean shipsPlaced;
    
    // Фиолетовая палитра
    private final Color BG_COLOR = new Color(45, 35, 65);
    private final Color PANEL_COLOR = new Color(55, 45, 75);
    private final Color BUTTON_COLOR = new Color(120, 80, 140);
    private final Color BUTTON_HOVER = new Color(140, 100, 160);
    private final Color TEXT_COLOR = new Color(220, 200, 240);
    private final Color ACCENT_COLOR = new Color(200, 150, 220);
    
    public GameWindow(GameClient client) {
        this.client = client;
        this.shipsPlaced = false;
        initUI();
    }
    
    private void initUI() {
        setTitle("Морской бой - " + client.getPlayerName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG_COLOR);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        
        statusLabel = new JLabel("Расстановка кораблей...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(ACCENT_COLOR);
        topPanel.add(statusLabel, BorderLayout.CENTER);
        
        JPanel rightTopPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        rightTopPanel.setBackground(BG_COLOR);
        
        shipsLabel = new JLabel("🚢 Корабли: 10", SwingConstants.RIGHT);
        shipsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        shipsLabel.setForeground(TEXT_COLOR);
        
        roomLabel = new JLabel("🏠 Комната: " + (client.getRoomId() != null ? client.getRoomId() : "---"), SwingConstants.RIGHT);
        roomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roomLabel.setForeground(TEXT_COLOR);
        
        rightTopPanel.add(shipsLabel);
        rightTopPanel.add(roomLabel);
        topPanel.add(rightTopPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 25, 0));
        centerPanel.setBackground(BG_COLOR);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        
        JPanel myBoardContainer = createBoardContainer("⚓ МОИ КОРАБЛИ");
        myPanel = new BoardPanel(client.getMyBoard(), false, null);
        customizeBoardPanel(myPanel);
        myBoardContainer.add(myPanel, BorderLayout.CENTER);
        
        JPanel enemyBoardContainer = createBoardContainer("🎯 ПОЛЕ ПРОТИВНИКА");
        enemyPanel = new BoardPanel(client.getEnemyBoard(), true, (x, y) -> {
            if (client.isMyTurn() && shipsPlaced) {
                client.sendShot(x, y);
            } else if (!client.isMyTurn()) {
                statusLabel.setText("⏳ Сейчас ход противника!");
            }
        });
        customizeBoardPanel(enemyPanel);
        enemyBoardContainer.add(enemyPanel, BorderLayout.CENTER);
        
        centerPanel.add(myBoardContainer);
        centerPanel.add(enemyBoardContainer);
        add(centerPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 12));
        bottomPanel.setBackground(BG_COLOR);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 15, 10));
        
        autoButton = createStyledButton("🎲 РАССТАВИТЬ КОРАБЛИ");
        autoButton.addActionListener(e -> showPlacementDialog());
        
        readyButton = createStyledButton("✅ ГОТОВ К БОЮ");
        readyButton.setEnabled(false);
        readyButton.addActionListener(e -> {
            client.sendReady();
            readyButton.setEnabled(false);
            autoButton.setEnabled(false);
            statusLabel.setText("⏳ Ожидание второго игрока...");
        });
        
        bottomPanel.add(autoButton);
        bottomPanel.add(readyButton);
        add(bottomPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    private void showPlacementDialog() {
        int choice = JOptionPane.showConfirmDialog(this, 
            "Расставить корабли автоматически?", 
            "Расстановка", 
            JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            client.getMyBoard().autoPlaceShips();
            completePlacement();
        } else {
            // Создаём новую чистую доску для ручной расстановки
            Board newBoard = new Board();
            client.setMyBoard(newBoard);
            myPanel.setBoard(newBoard);
            
            ShipPlacementPanel placementPanel = new ShipPlacementPanel(this, newBoard, board -> {
                client.setMyBoard(board);
                completePlacement();
            });
            placementPanel.setVisible(true);
        }
    }

    private void completePlacement() {
        shipsPlaced = true;
        readyButton.setEnabled(true);
        autoButton.setEnabled(false);
        myPanel.updateBoard(client.getMyBoard());
        statusLabel.setText("✅ Корабли расставлены! Нажмите 'Готов к бою'");
        
        int shipCount = client.getMyBoard().getShipsRemaining();
        shipsLabel.setText("🚢 Корабли: " + shipCount);
        
        System.out.println("Отправка доски на сервер. Кораблей: " + shipCount);
    client.sendBoard();
}
    
    private JPanel createBoardContainer(String title) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(PANEL_COLOR);
        container.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(ACCENT_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        container.add(titleLabel, BorderLayout.NORTH);
        
        return container;
    }
    
    private void customizeBoardPanel(BoardPanel panel) {
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 1));
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_COLOR);
            }
        });
        
        return button;
    }
    
    public void onGameStart() {
        statusLabel.setText("🎮 ИГРА НАЧАЛАСЬ! 🎮");
        statusLabel.setForeground(new Color(200, 150, 220));
        shipsLabel.setText("🚢 Корабли: " + client.getMyBoard().getShipsRemaining());
        updateBoards();
    }
    
    public void updateBoards() {
        myPanel.updateBoard(client.getMyBoard());
        enemyPanel.updateBoard(client.getEnemyBoard());
        enemyPanel.setMyTurn(client.isMyTurn());
        shipsLabel.setText("🚢 Корабли: " + client.getMyBoard().getShipsRemaining());
        
        if (client.isMyTurn()) {
            statusLabel.setText("🔫 ВАШ ХОД! Кликните по полю противника");
            statusLabel.setForeground(new Color(200, 150, 220));
        } else {
            statusLabel.setText("⏳ ХОД ПРОТИВНИКА... Ждите");
            statusLabel.setForeground(new Color(180, 160, 200));
        }
    }
    
    public void onShotResult(ShotResult result, boolean isOpponent, int x, int y) {
        updateBoards();
        
        if (!isOpponent) {
            switch (result) {
                case HIT:
                    statusLabel.setText("💥 ПОПАДАНИЕ! Стреляйте ещё раз!");
                    statusLabel.setForeground(new Color(100, 255, 150));
                    break;
                case SUNK:
                    statusLabel.setText("💀 КОРАБЛЬ ПОТОПЛЕН! Отличный выстрел!");
                    statusLabel.setForeground(new Color(255, 200, 100));
                    enemyPanel.markAroundSunkShip(x, y);
                    break;
                case MISS:
                    statusLabel.setText("❌ МИМО! Ход переходит противнику");
                    statusLabel.setForeground(new Color(255, 150, 150));
                    break;
                case ALREADY_SHOT:
                    statusLabel.setText("⚠️ Сюда уже стреляли!");
                    statusLabel.setForeground(new Color(255, 200, 100));
                    break;
            }
        } else {
            if (result == ShotResult.SUNK) {
                myPanel.markAroundSunkShip(x, y);
            }
        }
    }
    
    public void onTurnChange(boolean myTurn) {
        enemyPanel.setMyTurn(myTurn);
        if (myTurn) {
            statusLabel.setText("🔫 ВАШ ХОД! Кликните по полю противника");
            statusLabel.setForeground(new Color(200, 150, 220));
        } else {
            statusLabel.setText("⏳ ХОД ПРОТИВНИКА... Ждите");
            statusLabel.setForeground(new Color(180, 160, 200));
        }
    }
    
    public void onGameOver(String winner) {
        String msg = winner.equals(client.getPlayerName()) ? 
            "🎉 ПОБЕДА! Поздравляем! 🎉" : 
            "💔 ПОРАЖЕНИЕ! В следующий раз повезёт! 💔";
        
        UIManager.put("OptionPane.background", PANEL_COLOR);
        UIManager.put("Panel.background", PANEL_COLOR);
        UIManager.put("OptionPane.messageForeground", TEXT_COLOR);
        
        JOptionPane.showMessageDialog(this, 
            msg + "\nПобедитель: " + winner,
            "Конец игры", 
            JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }
    
    public void onError(String error) {
        JOptionPane.showMessageDialog(this, error, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
    
    public void onRoomCreated(String roomId) {
        roomLabel.setText("🏠 Комната: " + roomId);
        UIManager.put("OptionPane.background", PANEL_COLOR);
        UIManager.put("Panel.background", PANEL_COLOR);
        UIManager.put("OptionPane.messageForeground", TEXT_COLOR);
        
        JOptionPane.showMessageDialog(this,
            "Комната создана!\nID: " + roomId + "\n\nПередайте этот ID другу для подключения.",
            "Информация", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void onWait(String message) {
        statusLabel.setText(message);
        if (message.contains("ожидание")) {
            statusLabel.setForeground(new Color(180, 160, 200));
        }
    }
}