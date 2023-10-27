package sdx.talk.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

public class GsonExt {
    public static final Gson gson = new GsonBuilder().setLenient().create();

    public static final String TAG = "GSON";

    public static <T> T fromJson(String json, Type type) {
        try {
            return gson.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "fromJson error: " + e.getMessage() + ", json: " + json);
            return null;
        }
    }

    public static <T> T fromJson(JsonElement jsonElement, Type type) {
        try {
            return gson.fromJson(jsonElement, type);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "fromJson error: " + e.getMessage() + ", json: " + jsonElement);
            return null;
        }
    }

    public static String toJson(Object any) {
        return gson.toJson(any);
    }

    public static String toJson(JsonElement jsonElement) {
        return gson.toJson(jsonElement);
    }
}
