package main.java.server;

import main.java.common.model.Board;
import main.java.common.network.Message;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final GameServer server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private GameRoom room;
    private String playerName;
    private boolean connected;
    
    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
        this.connected = true;
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            connected = false;
        }
    }
    
    @Override
    public void run() {
        if (!connected) return;
        
        try {
            while (connected) {
                Message msg = (Message) in.readObject();
                if (msg != null) {
                    processMessage(msg);
                }
            }
        } catch (EOFException e) {
            System.out.println("Клиент " + playerName + " отключился");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }
    
    private void processMessage(Message msg) {
        String type = msg.getType();
        Object data = msg.getData();
        
        switch (type) {
            case "CREATE":
                // Создание новой комнаты
                String roomId = server.createRoom(this);
                sendMessage("ROOM_CREATED", roomId);
                break;
                
            case "JOIN":
                // Подключение к существующей комнате
                String joinRoomId = (String) data;
                if (server.joinRoom(joinRoomId, this)) {
                    sendMessage("ROOM_JOINED", joinRoomId);
                } else {
                    sendMessage("ERROR", "Комната не найдена или заполнена");
                    disconnect();
                }
                break;
                
            case "NAME":
                playerName = (String) data;
                System.out.println("Игрок " + playerName + " подключился");
                if (room != null) {
                    System.out.println("  в комнате " + room.getRoomId());
                }
                break;
                
            case "BOARD":
                Board board = (Board) data;
                System.out.println("Получена доска от " + playerName + ", кораблей: " + board.getShipsRemaining());
                if (room != null) {
                    room.setBoard(this, board);
                }
                break;
                
            case "READY":
                if (room != null) {
                    room.playerReady(this);
                }
                break;
                
            case "SHOT":
                int[] coords = (int[]) data;
                if (room != null) {
                    room.processShot(this, coords[0], coords[1]);
                }
                break;
        }
    }
    
    public void sendMessage(String type, Object data) {
        try {
            if (out != null && connected) {
                out.writeObject(new Message(type, data));
                out.flush();
            }
        } catch (IOException e) {
            connected = false;
        }
    }
    
    public void disconnect() {
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {}
        
        if (room != null) {
            room.playerDisconnected(this);
        }
        System.out.println("Клиент " + playerName + " отключён");
    }
    
    public void setRoom(GameRoom room) { this.room = room; }
    public String getPlayerName() { return playerName; }
}