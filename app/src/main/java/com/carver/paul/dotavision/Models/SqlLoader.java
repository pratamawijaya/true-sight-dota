/**
 * True Sight for Dota 2
 * Copyright (C) 2016 Paul Broadbent
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 */

package com.carver.paul.dotavision.Models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SqlLoader {
    private static final String TAG = "SqlLoader";

    private List<HeroAndAdvantages> mHeroes;

    protected SqlLoader(Context context) {
        Log.d(TAG, "1");
        DataBaseHelper dbHelper = new DataBaseHelper(context);
        Log.d(TAG, "2");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Log.d(TAG, "3");

        mHeroes = new ArrayList<>();

        Cursor c = db.rawQuery("SELECT * FROM Heroes", null);
        c.moveToFirst();
        while(!c.isAfterLast()) {
            mHeroes.add(new HeroAndAdvantages(c));
            c.moveToNext();
        }

        c.close();
        db.close();
        Log.d(TAG, "4");

        List<String> testNames = new ArrayList<>();
        testNames.add("Disruptor");
        testNames.add("Lich");
        testNames.add("Queen of Pain");
        testNames.add("Nature's Prophet");
        testNames.add("Io");

        calculateAdvantages(context, testNames);
    }

    protected void calculateAdvantages(Context context, List<String> heroesInPhoto) {
        DataBaseHelper dbHelper = new DataBaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        for(HeroAndAdvantages hero : mHeroes) {
            int heroId = hero.getId();
            List<Double> advantages = new ArrayList<>();
            for(String enemyName : heroesInPhoto) {
                // If the the enemy hero is the same as this one then neither has any advantage
                if(enemyName.equals(hero.getName())) {
                    advantages.add(0d);
                } else {
                    String sqlSafeEnemyName = enemyName.replace("'", "");
                    Cursor c = db.rawQuery(
                            "SELECT advantage " +
                                    "FROM Heroes, Advantages " +
                                    "WHERE Heroes.name = '" + sqlSafeEnemyName + "' " +
                                    "  AND Heroes._id = Advantages.enemy_id " +
                                    "  AND Advantages.hero_id = " + heroId, null);
                    c.moveToFirst();
                    advantages.add(c.getDouble(c.getColumnIndexOrThrow("advantage")));
                    c.close();
                }
            }
            hero.setAdvantages(advantages);
        }
        
        Collections.sort(mHeroes);

        db.close();
    }

    // This method seems ~ 5% quicker, but the code is less nice, so might as well use the method
    // with a join
/*    protected void calculateAdvantagesNoJoin(Context context, List<String> heroesInPhoto) {
        DataBaseHelper dbHelper = new DataBaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        List<Integer> enemyIds = new ArrayList<>();
        //Cursor c;
        for(String enemyName : heroesInPhoto) {
            String sqlSafeEnemyName = enemyName.replace("'", "");
            Cursor c = db.rawQuery(
                    "SELECT _id " +
                            "FROM Heroes " +
                            "WHERE Heroes.name = '" + sqlSafeEnemyName + "'", null);
            c.moveToFirst();
            enemyIds.add(c.getInt(c.getColumnIndexOrThrow("_id")));
            c.close();
        }

        for(HeroAndAdvantages hero : mHeroes) {
            int heroId = hero.getId();
            List<Double> advantages = new ArrayList<>();
            for(Integer enemyId : enemyIds) {
                // If the the enemy hero is the same as this one then neither has any advantage
                if(enemyId == heroId) {
                    advantages.add(0d);
                } else {
                    Cursor c = db.rawQuery(
                            "SELECT advantage " +
                                    "FROM Advantages " +
                                    "WHERE hero_id = " + heroId + " " +
                                    "  AND enemy_id = " + enemyId, null);
                    c.moveToFirst();
                    advantages.add(c.getDouble(c.getColumnIndexOrThrow("advantage")));
                    c.close();
                }
            }
            hero.setAdvantages(advantages);
        }

        Collections.sort(mHeroes);

        db.close();
    }*/
}
