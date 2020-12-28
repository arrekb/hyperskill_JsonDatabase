package server;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final int PORT = 34522;

    public static void main(String[] args) {
        Storage storage = new Storage();
        DBEngine dbEngine = new DBEngine(storage);

        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Server started!");
        while (!serverSocket.isClosed()) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
                Session session = new Session(clientSocket, dbEngine, serverSocket);
                pool.submit(session);
            } catch (IOException e) {
                // e.printStackTrace();
                break;
            }
        }

        try {
            pool.shutdown();
            pool.awaitTermination(100, TimeUnit.MILLISECONDS);
            pool.shutdownNow();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Session implements Runnable {
    private final Socket socket;
    private final DBEngine dbEngine;
    private ServerSocket serverSocket;

    public Session(Socket socketForClient, DBEngine dbEngine, ServerSocket serverSocket) {
        this.socket = socketForClient;
        this.dbEngine = dbEngine;
        this.serverSocket = serverSocket;
    }

    public void run() {
        try (
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            String msg = input.readUTF();
            // System.out.println("S: Received msg = " + msg);

            JsonElement jsonElement = JsonParser.parseString(msg);
            String queryType = jsonElement.getAsJsonObject().get("type").getAsString();
            // System.out.println("queryType = " + queryType);

            String response;
            if ("exit".equals(queryType)) {
                serverSocket.close();
                // System.out.println("S: Shutdown requested...");
                response = "{\"response\":\"OK\"}";
            } else {
                response = dbEngine.runCommand(msg);
                // System.out.println("S: response = " + response);
            }
            output.writeUTF(response);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
