package main.java.server;

import main.java.common.model.Board;
import main.java.common.model.GameState;

import java.util.concurrent.atomic.AtomicBoolean;

public class GameRoom {
    private final String roomId;
    private ClientHandler player1;
    private ClientHandler player2;
    private Board board1;
    private Board board2;
    private ClientHandler currentTurn;
    private final AtomicBoolean gameStarted;
    private boolean player1Ready, player2Ready, board1Received, board2Received;
    
    public GameRoom(String roomId, GameServer server) {
        this.roomId = roomId;
        this.gameStarted = new AtomicBoolean(false);
        this.board1 = new Board();
        this.board2 = new Board();
    }
    
    public synchronized void addPlayer(ClientHandler player) {
        if (player1 == null) {
            player1 = player;
            player.setRoom(this);
            player.sendMessage("WAIT", "Ожидание второго игрока... ID комнаты: " + roomId);
        } else if (player2 == null) {
            player2 = player;
            player.setRoom(this);
            player.sendMessage("WAIT", "Подключение к игре...");
            checkStart();
        }
    }
    
    public synchronized void setBoard(ClientHandler player, Board board) {
        if (player == player1) { 
            board1 = board; 
            board1Received = true;
            System.out.println("Доска игрока 1 получена, кораблей: " + board1.getShipsRemaining());
        } else if (player == player2) { 
            board2 = board; 
            board2Received = true;
            System.out.println("Доска игрока 2 получена, кораблей: " + board2.getShipsRemaining());
        }
        checkStart();
    }
    
    public synchronized void playerReady(ClientHandler player) {
        if (player == player1) {
            player1Ready = true;
            System.out.println("Игрок 1 готов");
        } else if (player == player2) {
            player2Ready = true;
            System.out.println("Игрок 2 готов");
        }
        checkStart();
    }
    
    private void checkStart() {
        System.out.println("Проверка старта: p1Ready=" + player1Ready + ", p2Ready=" + player2Ready + 
                           ", board1=" + board1Received + ", board2=" + board2Received + 
                           ", gameStarted=" + gameStarted.get());
        
        if (player1Ready && player2Ready && board1Received && board2Received && !gameStarted.get()) {
            startGame();
        }
    }
    
    private void startGame() {
        gameStarted.set(true);
        currentTurn = player1;
        
        player1.sendMessage("START", new GameState(board1, board2, true));
        player2.sendMessage("START", new GameState(board2, board1, false));
        System.out.println("🎮 Игра началась! Комната: " + roomId);
        System.out.println("Корабли игрока 1: " + board1.getShipsRemaining());
        System.out.println("Корабли игрока 2: " + board2.getShipsRemaining());
    }
    
    public synchronized void processShot(ClientHandler shooter, int x, int y) {
        if (!gameStarted.get()) {
            shooter.sendMessage("ERROR", "Игра ещё не началась!");
            return;
        }
        
        if (shooter != currentTurn) {
            shooter.sendMessage("ERROR", "Не ваш ход!");
            return;
        }
        
        ClientHandler opponent = (shooter == player1) ? player2 : player1;
        Board opponentBoard = (shooter == player1) ? board2 : board1;
        
        // Проверка, что клетка ещё не была обстреляна
        if (opponentBoard.getCell(x, y).isShot()) {
            shooter.sendMessage("ERROR", "Сюда уже стреляли!");
            return;
        }
        
        Board.ShotResult result = opponentBoard.shoot(x, y);
        
        System.out.println("Выстрел: " + shooter.getPlayerName() + " (" + x + "," + y + ") = " + result);
        System.out.println("У противника осталось кораблей: " + opponentBoard.getShipsRemaining());
        
        shooter.sendMessage("RESULT", new Object[]{result, x, y, false});
        opponent.sendMessage("RESULT", new Object[]{result, x, y, true});
        
        // Проверка на победу
        if (opponentBoard.allShipsSunk()) {
            gameStarted.set(false);
            String winner = shooter.getPlayerName();
            player1.sendMessage("OVER", winner);
            player2.sendMessage("OVER", winner);
            System.out.println("🏆 Победитель: " + winner);
            return;
        }
        
        // Если не потопил и не попал - смена хода
        if (result != Board.ShotResult.HIT && result != Board.ShotResult.SUNK) {
            currentTurn = opponent;
            player1.sendMessage("TURN", currentTurn == player1);
            player2.sendMessage("TURN", currentTurn == player2);
        } else {
            // При попадании ход не меняется, стреляет снова
            shooter.sendMessage("TURN", true);
            opponent.sendMessage("TURN", false);
        }
    }
    
    public synchronized void playerDisconnected(ClientHandler player) {
        if (gameStarted.get()) {
            String winner = (player == player1) ? 
                (player2 != null ? player2.getPlayerName() : "Unknown") : 
                (player1 != null ? player1.getPlayerName() : "Unknown");
            if (player1 != null) player1.sendMessage("OVER", winner);
            if (player2 != null) player2.sendMessage("OVER", winner);
        }
    }
    
    public String getRoomId() { return roomId; }
    public boolean isFull() { return player1 != null && player2 != null; }
}