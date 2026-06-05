package main.java.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GameServer {
    private static final int PORT = 12345;
    private final ServerSocket serverSocket;
    private final ExecutorService clientPool;
    private final ConcurrentHashMap<String, GameRoom> rooms;
    private final AtomicInteger roomCounter;
    private volatile boolean running;
    
    public GameServer() throws IOException {
        this.serverSocket = new ServerSocket(PORT);
        this.clientPool = Executors.newCachedThreadPool();
        this.rooms = new ConcurrentHashMap<>();
        this.roomCounter = new AtomicInteger(1000);
        this.running = true;
    }
    
    public void start() {
        System.out.println("=== МОРСКОЙ БОЙ - СЕРВЕР ===");
        System.out.println("Сервер запущен на порту " + PORT);
        System.out.println("Ожидание подключений...\n");
        
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Новое подключение: " + clientSocket.getInetAddress());
                
                // Не создаём комнату сразу, ждём сообщение от клиента
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clientPool.submit(handler);
                
            } catch (IOException e) {
                if (running) e.printStackTrace();
            }
        }
    }
    
    public String createRoom(ClientHandler client) {
        String roomId = String.valueOf(roomCounter.getAndIncrement());
        GameRoom room = new GameRoom(roomId, this);
        rooms.put(roomId, room);
        room.addPlayer(client);
        System.out.println("Создана комната: " + roomId);
        return roomId;
    }
    
    public boolean joinRoom(String roomId, ClientHandler client) {
        GameRoom room = rooms.get(roomId);
        if (room != null && !room.isFull()) {
            room.addPlayer(client);
            System.out.println("Игрок подключился к комнате " + roomId);
            return true;
        }
        return false;
    }
    
    public static void main(String[] args) {
        try {
            new GameServer().start();
        } catch (IOException e) {
            System.err.println("Не удалось запустить сервер: " + e.getMessage());
        }
    }
}