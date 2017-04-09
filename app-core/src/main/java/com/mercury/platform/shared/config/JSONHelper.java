package com.mercury.platform.shared.config;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class JSONHelper {
    private Logger logger = LogManager.getLogger(JSONHelper.class.getSimpleName());
    private DataSource dataSource;

    public JSONHelper(DataSource dataSource){
        this.dataSource = dataSource;
    }
    public <T> T readMapData(String key,TypeToken<T> typeToken){
        try {
            Gson gson = new Gson();
            JsonParser jsonParser = new JsonParser();
            try(JsonReader reader = new JsonReader(new FileReader(dataSource.getConfigurationFilePath()))) {
                return gson.fromJson(
                        jsonParser.parse(reader)
                                .getAsJsonObject()
                                .get(key),
                        typeToken.getType());
            }
        }catch (IOException e){
            logger.error(e);
            return null;
        }
    }
    public void writeMapObject(String key, Map<?,?> object){
        try {
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
            try(JsonWriter reader = new JsonWriter(new FileWriter(dataSource.getConfigurationFilePath()))) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.add(key,gson.toJsonTree(object));
                gson.toJson(jsonObject,reader);
            }
        }catch (IOException e){
            logger.error(e);
        }

    }
}
