package com.example.traveling.path;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesManager {
    private static final String PREFS_NAME = "travelling_favs_v2";
    private static final String KEY_FAVS = "full_parcours_list";

    public static void saveParcours(Context context, Parcours parcours) {
        List<Parcours> currentFavs = getFavorites(context);
        boolean exists = false;
        for (Parcours p : currentFavs) {
            if (p.getTitre().equals(parcours.getTitre())) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            currentFavs.add(parcours);
            saveList(context, currentFavs);
        }
    }

    public static void removeParcours(Context context, String title) {
        List<Parcours> currentFavs = getFavorites(context);
        currentFavs.removeIf(p -> p.getTitre().equals(title));
        saveList(context, currentFavs);
    }

    public static boolean isFavorite(Context context, String title) {
        for (Parcours p : getFavorites(context)) {
            if (p.getTitre().equals(title)) return true;
        }
        return false;
    }

    public static List<Parcours> getFavorites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> set = prefs.getStringSet(KEY_FAVS, new HashSet<>());
        List<Parcours> list = new ArrayList<>();
        for (String s : set) {
            Parcours p = deserialize(s);
            if (p != null) list.add(p);
        }
        return list;
    }

    private static void saveList(Context context, List<Parcours> list) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>();
        for (Parcours p : list) {
            String s = serialize(p);
            if (s != null) set.add(s);
        }
        prefs.edit().putStringSet(KEY_FAVS, set).apply();
    }

    private static String serialize(Parcours p) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(p);
            so.flush();
            return Base64.encodeToString(bo.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }

    private static Parcours deserialize(String s) {
        try {
            byte b[] = Base64.decode(s, Base64.DEFAULT);
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);
            return (Parcours) si.readObject();
        } catch (Exception e) {
            return null;
        }
    }
}
