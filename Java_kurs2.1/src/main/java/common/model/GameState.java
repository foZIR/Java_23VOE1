package main.java.common.model;

import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Board myBoard;
    private final Board enemyBoard;
    private final boolean myTurn;
    
    public GameState(Board myBoard, Board enemyBoard, boolean myTurn) {
        this.myBoard = myBoard;
        this.enemyBoard = enemyBoard;
        this.myTurn = myTurn;
    }
    
    public Board getMyBoard() { return myBoard; }
    public Board getEnemyBoard() { return enemyBoard; }
    public boolean isMyTurn() { return myTurn; }
}