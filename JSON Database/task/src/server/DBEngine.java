package server;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;

public class DBEngine {

    final private Storage storage;

    public DBEngine(Storage storage) {
        this.storage = storage;
    }

    public String runCommand(String msg) {
        String[] keys;
        // System.out.println("S: runCommand msg= " + msg);
        JsonElement jsonElement = JsonParser.parseString(msg);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        if (jsonObject.get("key").isJsonArray()) {
            JsonArray jsonArray = (JsonArray) jsonObject.get("key");
            List<String> keysList = new ArrayList<>();
            for (JsonElement item : jsonArray) {
                keysList.add(item.getAsString());
            }
            keys = new String[keysList.size()];
            keysList.toArray(keys);
        } else {
            keys = new String[1];
            keys[0] = jsonObject.get("key").getAsString();
        }

        switch (jsonObject.get("type").getAsString()) {
            case "set":
                return storage.cmdSet(keys, jsonObject.get("value"));
            //break;
            case "get":
                return storage.cmdGet(keys);
            //break;
            case "delete":
                return storage.cmdDelete(keys);
            //break;
            default:
                DBResponseModel dbResponse = new DBResponseModel();
                Gson gson = new Gson();
                dbResponse.response = "ERROR";
                dbResponse.reason = "Unknown command";
                return gson.toJson(dbResponse);
        }
    }
}
