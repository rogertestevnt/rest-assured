package utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class ReadJsonFile {

    public static JsonObject readJson () throws FileNotFoundException {

        String filePath = "./src/test/java/utils/data.json";

        Gson gson = new Gson();

        JsonReader fileContents = new JsonReader(new FileReader(filePath));

        JsonObject jsonObject = gson.fromJson(fileContents, JsonObject.class);

        return jsonObject;
    }
}
