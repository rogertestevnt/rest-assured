package utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class JsonUtils {

        public static void main(String[] args) throws FileNotFoundException {
            String path = "/home/vntrool/Downloads/MOCK_DATA.json";

            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));

            Gson gson = new Gson();
            Object json = gson.fromJson(bufferedReader, Object.class);

            System.out.println(json.getClass());
            System.out.println(json.toString());

            testGson(gson, path);
        }

        public static void testGson(Gson gson, String filePath) throws FileNotFoundException {

            JsonReader reader = new JsonReader(new FileReader(filePath));

            JsonArray jsonObject = gson.fromJson(reader, JsonArray.class);

            JsonElement line;

            for (int i=0;i < jsonObject.size(); i++ ){

                line = jsonObject.get(i);
                System.out.println("Json file line:" + line);
                JsonObject jsonString = gson.fromJson( jsonObject.get(i), JsonObject.class);
                System.out.println("Value for email key:" + jsonString.get("email"));

            }
        }
}
