package com.hamami.musictrywithmitch.persistence;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hamami.musictrywithmitch.Models.Songs;

import java.lang.reflect.Type;
import java.util.ArrayList;

import androidx.room.TypeConverter;

public class Converter {
    @TypeConverter
    public static ArrayList<Songs> fromString(String value) {
        Type listType = new TypeToken<ArrayList<Songs>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }
    @TypeConverter
    public static String fromArrayList(ArrayList<Songs> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}
