package my.contacteditor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class IntegralClient {

    public static void main(String[] args) {
        String serverAddress = "127.0.0.1";
        int registrationPort = 5000;
        int resultPort = 5001;
        int clientPort = 0;

        if (args.length >= 1) {
            serverAddress = args[0];
        }
        if (args.length >= 2) {
            clientPort = Integer.parseInt(args[1]);
        }

        try (DatagramSocket socket = new DatagramSocket(clientPort)) {
            InetAddress serverInetAddress = InetAddress.getByName(serverAddress);

            // Регистрация на сервере
            String hello = "Ni Hao";
            byte[] helloData = hello.getBytes();
            DatagramPacket helloPacket = new DatagramPacket(helloData, helloData.length,
                serverInetAddress, registrationPort);
            socket.send(helloPacket);
            System.out.println("Отправлено Ni Hao на сервер " + serverAddress + ":" + registrationPort);

            // Бесконечный цикл обработки заданий
            while (true) {
                System.out.println("Ожидание задания...");

                byte[] buffer = new byte[1024];
                DatagramPacket taskPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(taskPacket);

                String task = new String(taskPacket.getData(), 0, taskPacket.getLength()).trim();
                System.out.println("Получено задание: " + task);

                if (task.startsWith("TASK")) {
                    String[] parts = task.split(" ");
                    double a = Double.parseDouble(parts[1]);
                    double b = Double.parseDouble(parts[2]);
                    double step = Double.parseDouble(parts[3]);

                    // Вычисление интеграла в 3 потоках
                    double subInterval = (b - a) / 3.0;
                    IntegralCalcThread[] threads = new IntegralCalcThread[3];

                    for (int i = 0; i < 3; i++) {
                        double start = a + i * subInterval;
                        double end = (i == 2) ? b : start + subInterval;
                        threads[i] = new IntegralCalcThread(start, end, step);
                        threads[i].start();
                    }

                    // Ожидание завершения всех потоков
                    double totalResult = 0;
                    for (int i = 0; i < 3; i++) {
                        threads[i].join();
                        totalResult += threads[i].getResult();
                    }

                    // Отправка результата
                    String resultMsg = "RESULT " + totalResult;
                    byte[] resultData = resultMsg.getBytes();
                    DatagramPacket resultPacket = new DatagramPacket(resultData, resultData.length,
                        serverInetAddress, resultPort);
                    socket.send(resultPacket);
                    System.out.println("Отправлен результат: " + totalResult);
                }
            }
        } catch (Exception ex) {
            System.err.println("Ошибка клиента: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}