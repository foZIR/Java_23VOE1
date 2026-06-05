package main.java.client;

import main.java.common.model.Board.ShotResult;
import main.java.common.model.GameState;
import main.java.common.network.Message;

import javax.swing.*;
import java.io.*;

public class ServerListener extends Thread {
    private ObjectInputStream in;
    private GameClient client;
    private boolean running;
    
    public ServerListener(ObjectInputStream in, GameClient client) {
        this.in = in;
        this.client = client;
        this.running = true;
    }
    
    @Override
    public void run() {
        try {
            while (running) {
                Message msg = (Message) in.readObject();
                processMessage(msg);
            }
        } catch (EOFException e) {
            System.out.println("Соединение закрыто");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    private void processMessage(Message msg) {
        String type = msg.getType();
        Object data = msg.getData();
        
        switch (type) {
            case "ROOM_CREATED":
                String roomId = (String) data;
                client.setRoomId(roomId);
                SwingUtilities.invokeLater(() -> client.onRoomCreated(roomId));
                break;
                
            case "ROOM_JOINED":
                String joinedRoomId = (String) data;
                client.setRoomId(joinedRoomId);
                break;
                
            case "WAIT":
                String waitMsg = (String) data;
                SwingUtilities.invokeLater(() -> client.onWait(waitMsg));
                break;
                
            case "START":
                client.setGameState((GameState) data);
                SwingUtilities.invokeLater(() -> client.onGameStart());
                break;
                
            case "RESULT":
                Object[] result = (Object[]) data;
                ShotResult shotResult = (ShotResult) result[0];
                int x = (int) result[1];
                int y = (int) result[2];
                boolean isOpponent = (boolean) result[3];
                client.applyShotResult(shotResult, x, y, isOpponent);
                SwingUtilities.invokeLater(() -> client.onShotResult(shotResult, isOpponent, x, y));
                break;
                
            case "TURN":
                boolean myTurn = (boolean) data;
                client.setMyTurn(myTurn);
                SwingUtilities.invokeLater(() -> client.onTurnChange(myTurn));
                break;
                
            case "OVER":
                String winner = (String) data;
                SwingUtilities.invokeLater(() -> client.onGameOver(winner));
                running = false;
                break;
                
            case "ERROR":
                String error = (String) data;
                SwingUtilities.invokeLater(() -> client.onError(error));
                break;
        }
    }
    
    public void stopListening() { running = false; }
}