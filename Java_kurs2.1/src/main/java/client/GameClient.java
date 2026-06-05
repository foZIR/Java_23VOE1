package main.java.client;

import main.java.common.model.Board;
import main.java.common.model.GameState;
import main.java.common.model.Board.ShotResult;
import main.java.common.network.Message;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class GameClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ServerListener listener;
    private String playerName;
    private GameWindow window;
    private Board myBoard;
    private Board enemyBoard;
    private boolean myTurn;
    private boolean connected;
    private String myRoomId;
    
    public void connect(String serverAddress, int port, String name, boolean createNew, String joinRoomId) throws IOException {
        this.playerName = name;
        this.myBoard = new Board();
        this.enemyBoard = new Board();
        this.connected = true;
        
        socket = new Socket(serverAddress, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        
        // Отправляем имя
        sendMessage("NAME", name);
        
        if (createNew) {
            // Создаём новую комнату
            sendMessage("CREATE", null);
        } else {
            // Подключаемся к существующей
            sendMessage("JOIN", joinRoomId);
        }
        
        listener = new ServerListener(in, this);
        listener.start();
        
        SwingUtilities.invokeLater(() -> {
            window = new GameWindow(this);
            window.setVisible(true);
        });
    }
    
    public void sendMessage(String type, Object data) {
        try {
            if (out != null && connected) {
                out.writeObject(new Message(type, data));
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void sendBoard() {
        System.out.println("Отправка доски на сервер. Кораблей: " + myBoard.getShipsRemaining());
        sendMessage("BOARD", myBoard);
    }
    
    public void sendReady() {
        sendMessage("READY", null);
    }
    
    public void sendShot(int x, int y) {
        sendMessage("SHOT", new int[]{x, y});
    }
    
    public void applyShotResult(ShotResult result, int x, int y, boolean isOpponent) {
        if (!isOpponent) {
            if (result == ShotResult.HIT || result == ShotResult.SUNK) {
                enemyBoard.getCell(x, y).setShot(true);
                enemyBoard.getCell(x, y).setShip(true);
            } else if (result == ShotResult.MISS) {
                enemyBoard.getCell(x, y).setShot(true);
            }
        } else {
            if (result == ShotResult.HIT || result == ShotResult.SUNK) {
                myBoard.getCell(x, y).setShot(true);
            } else if (result == ShotResult.MISS) {
                myBoard.getCell(x, y).setShot(true);
            }
        }
    }

    public void onShotResult(ShotResult result, boolean isOpponent, int x, int y) { 
        if (window != null) {
            SwingUtilities.invokeLater(() -> window.onShotResult(result, isOpponent, x, y));
        }
    }
    
    public void setMyTurn(boolean turn) { this.myTurn = turn; }
    public void setRoomId(String roomId) { this.myRoomId = roomId; }
    
    public void onGameStart() { if (window != null) window.onGameStart(); }
    public void onTurnChange(boolean myTurn) { if (window != null) window.onTurnChange(myTurn); }
    public void onGameOver(String winner) { if (window != null) window.onGameOver(winner); }
    public void onError(String error) { if (window != null) window.onError(error); }
    public void onWait(String message) { if (window != null) window.onWait(message); }
    public void onRoomCreated(String roomId) { 
        this.myRoomId = roomId;
        if (window != null) window.onRoomCreated(roomId); 
    }
    
    public Board getMyBoard() { return myBoard; }
    public Board getEnemyBoard() { return enemyBoard; }
    public boolean isMyTurn() { return myTurn; }
    public String getPlayerName() { return playerName; }
    public String getRoomId() { return myRoomId; }
    
    public static void main(String[] args) {
        String name = JOptionPane.showInputDialog(null, "Введите ваше имя:", "Морской бой", JOptionPane.QUESTION_MESSAGE);
        if (name == null || name.trim().isEmpty()) name = "Игрок" + (int)(Math.random() * 1000);
        
        String[] options = {"Создать новую игру", "Подключиться к игре"};
        int choice = JOptionPane.showOptionDialog(null, "Выберите действие:", "Морской бой",
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        
        boolean createNew = (choice == 0);
        String joinRoomId = null;
        
        if (!createNew) {
            joinRoomId = JOptionPane.showInputDialog(null, "Введите ID комнаты:");
            if (joinRoomId == null || joinRoomId.trim().isEmpty()) System.exit(0);
        }
        
        try {
            new GameClient().connect("localhost", 12345, name, createNew, joinRoomId);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Ошибка подключения: " + e.getMessage());
            System.exit(1);
        }
    }

    public void setGameState(GameState state) {
        this.myBoard = state.getMyBoard();
        this.enemyBoard = state.getEnemyBoard();
        this.myTurn = state.isMyTurn();
        
        System.out.println("Получены доски. Мои корабли: " + myBoard.getShipsRemaining() + 
                        ", Врага: " + enemyBoard.getShipsRemaining());
    }

    public void setMyBoard(Board board) {
        this.myBoard = board;
        if (window != null) {
            SwingUtilities.invokeLater(() -> window.updateBoards());
        }
    }
}