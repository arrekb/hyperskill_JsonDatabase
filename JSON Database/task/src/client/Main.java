package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Main {

    final MainCLIParameters mainArgs = new MainCLIParameters();
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 34522;
    String PATHTOFILES = "src/client/data/";

    public static void main(String[] args) {
        Main main = new Main();
        main.handleInputArgs(args);
        main.run();
    }

    void handleInputArgs(String[] args) {
        JCommander jCommander = new JCommander(mainArgs);
        // jCommander.setProgramName("archive");

        try {
            jCommander.parse(args);
        } catch (ParameterException exception) {
            System.out.println("Error parsing command line!");
            System.out.println(exception.getMessage());
            showUsage(jCommander);
        }

        if (mainArgs.isHelp()) {
            showUsage(jCommander);
        }
    }

    void showUsage(JCommander jCommander) {
        jCommander.usage();
        System.exit(0);
    }

    void run() {
        // System.out.println("Running client with \n" + mainArgs.toString());
        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            System.out.println("C: Client started!");

            String msg = buildQuery();
            output.writeUTF(msg); // sending message to the server
            System.out.println("C: Sent: " + msg);
            String receivedMsg = input.readUTF(); // response message
            System.out.println("C: Received: " + receivedMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
         System.out.println("C: end");
    }

    private String buildQuery() {

        JsonObject query = new JsonObject();

        Gson gson = new Gson();
        // Gson gson = new GsonBuilder().setLenient().create();

        if (!mainArgs.getFileWithRequest().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            File file = new File(PATHTOFILES + mainArgs.getFileWithRequest());

            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNext()) {
                    sb.append(scanner.nextLine());
                }
            } catch (FileNotFoundException e) {
                System.out.println("C: No file found: " + mainArgs.getFileWithRequest());
            }
            System.out.println("C: File content:" + sb.toString());
            query = gson.fromJson(sb.toString(), query.getClass());
        } else {
            query.addProperty("type", mainArgs.getRequestType());
            if (!"exit".equals(mainArgs.getRequestType())) {
                query.addProperty("key", mainArgs.getKey());
                if ("set".equals(mainArgs.getRequestType())) {
                    query.addProperty("value", mainArgs.getValue());
                }
            }
        }
        return gson.toJson(query);
    }
}
