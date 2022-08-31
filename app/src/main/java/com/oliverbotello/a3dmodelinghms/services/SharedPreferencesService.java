package com.oliverbotello.a3dmodelinghms.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.oliverbotello.a3dmodelinghms.model.ItemEnt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SharedPreferencesService {
    private final static String NAME_PREFERENCES = "Models3D";
    private static SharedPreferences SHARED_PREFERENCES = null;

    public SharedPreferencesService(Context context) {
        if (SHARED_PREFERENCES == null)
            SHARED_PREFERENCES = context
                    .getSharedPreferences(NAME_PREFERENCES, Context.MODE_PRIVATE);
    }

    public List<ItemEnt> getListModels() {
        ArrayList<ItemEnt> lstItems = new ArrayList<>();

        for (String item : ((Map<String, String>) SHARED_PREFERENCES.getAll()).values())
            lstItems.add(new ItemEnt(item));

        return lstItems;
    }

    public void putNewModel(ItemEnt itemEnt) {
        SharedPreferences.Editor editor = SHARED_PREFERENCES.edit();

        editor.putString(itemEnt.getPath(), itemEnt.toString());
        editor.apply();
    }

    public void popModel(ItemEnt itemEnt) {
        SharedPreferences.Editor editor = SHARED_PREFERENCES.edit();

        editor.remove(itemEnt.getPath());
        editor.apply();
    }
}
