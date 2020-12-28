package server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Storage {
    // this file content is the only db copy
    private String PATHTOFILE = "src/server/data/db.json";
    // main database object
    private JsonObject jsonObjectDB;

    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock readLock = lock.readLock();
    private Lock writeLock = lock.writeLock();

    public String cmdSet(String[] keys, JsonElement value) {
        writeLock.lock();
        // search (and create if necessary) required node
        readJsonFromFile();
        JsonElement jsonRelativeElement = jsonObjectDB;
        for (int ii = 0; ii < keys.length - 1; ii++) {
            if (jsonRelativeElement.getAsJsonObject().has(keys[ii])) {
                jsonRelativeElement = jsonRelativeElement.getAsJsonObject().get(keys[ii]);
            } else {
                jsonRelativeElement.getAsJsonObject().add(keys[ii], null);
            }
        }

        // set value
        jsonRelativeElement.getAsJsonObject().add(keys[keys.length - 1], value);

        saveJsonToFile();
        writeLock.unlock();

        DBResponseModel dbResponse = new DBResponseModel();
        dbResponse.response = "OK";
        return new Gson().toJson(dbResponse);
    }

    public String cmdGet(String[] keys) {
        DBResponseModel dbResponse = new DBResponseModel();

        readLock.lock();
        readJsonFromFile();
        readLock.unlock();

        JsonElement jsonRelativeElement = jsonObjectDB;

        for (String key : keys) {
            if (jsonRelativeElement.isJsonObject() && jsonRelativeElement.getAsJsonObject().has(key)) {
                jsonRelativeElement = jsonRelativeElement.getAsJsonObject().get(key);
            } else {
                dbResponse.response = "ERROR";
                dbResponse.reason = "No such key";
                return new Gson().toJson(dbResponse);
            }
        }

        dbResponse.response = "OK";
        dbResponse.value = jsonRelativeElement;
        // System.out.println("S: cmd returns: " + jsonRelativeElement);
        return new Gson().toJson(dbResponse);
    }

    public String cmdDelete(String[] keys) {
        DBResponseModel dbResponse = new DBResponseModel();

        writeLock.lock();
        readJsonFromFile();
        JsonElement jsonRelativeElement = jsonObjectDB;
        for (int ii = 0; ii < keys.length - 1; ii++) {
            // System.out.println("S: keys in for = " + keys[ii]);
            if (jsonRelativeElement.isJsonObject() && jsonRelativeElement.getAsJsonObject().has(keys[ii])) {
                jsonRelativeElement = jsonRelativeElement.getAsJsonObject().get(keys[ii]);

            } else {
                writeLock.unlock();
                dbResponse.response = "ERROR";
                dbResponse.reason = "No such key";
                return new Gson().toJson(dbResponse);
            }
        }

        jsonRelativeElement.getAsJsonObject().remove(keys[keys.length - 1]);
        saveJsonToFile();
        writeLock.unlock();

        dbResponse.response = "OK";
        return new Gson().toJson(dbResponse);
    }

    private void saveJsonToFile() {
        try (PrintWriter out = new PrintWriter(PATHTOFILE)) {
            out.println(jsonObjectDB.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void readJsonFromFile() {
        StringBuilder fileContent = new StringBuilder();
        File file = new File(PATHTOFILE);

        // System.out.println("S: absolute path to db.json: " + file.getAbsolutePath());

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()) {
                fileContent.append(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("S: No file found: " + PATHTOFILE);
            return;
        }

        JsonElement jsonElement = JsonParser.parseString(fileContent.toString());
        if (jsonElement.isJsonObject()) {
            jsonObjectDB = jsonElement.getAsJsonObject();
        } else {
            jsonObjectDB = new JsonObject();
        }
    }
}
