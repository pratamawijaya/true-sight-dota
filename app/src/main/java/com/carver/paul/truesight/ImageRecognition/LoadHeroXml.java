/**
 * True Sight for Dota 2
 * Copyright (C) 2015 Paul Broadbent
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

package com.carver.paul.truesight.ImageRecognition;

import android.content.res.XmlResourceParser;
import android.util.Log;

import com.carver.paul.truesight.BuildConfig;
import com.carver.paul.truesight.Models.HeroAbilityInfo;
import com.carver.paul.truesight.Models.HeroInfo;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

//TODO-someday: Consider switching to sqlite database for hero info instead of an XML file

public class LoadHeroXml {

    static final String sNullString = null;

    private static final String TAG = "LoadHeroXml";

    private LoadHeroXml() {}

    public static void Load(XmlResourceParser parser, List<HeroInfo> heroInfoList) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Starting XML Load.");

        if(heroInfoList == null)
            throw new RuntimeException("Trying to load XML, but the list to store it in hasn't " +
                    "been initialised");

        if(!heroInfoList.isEmpty())
            heroInfoList.clear();

        try {
            parser.next();
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, sNullString, "listOfHeroInfo");
            while (parser.next() != XmlPullParser.END_TAG) {
                parser.require(XmlPullParser.START_TAG, sNullString, "heroInfo");
                heroInfoList.add(LoadIndividualHeroInfo(parser));
                parser.require(XmlPullParser.END_TAG, sNullString, "heroInfo");
            }

        } catch (XmlPullParserException e) {
            Log.e(TAG, "XmlPullParserException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        }

        if (BuildConfig.DEBUG) Log.d(TAG, "Loaded " + heroInfoList.size() + " heroes from XML.");
    }

    static private HeroInfo LoadIndividualHeroInfo(XmlResourceParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, sNullString, "heroInfo");
        HeroInfo hero = new HeroInfo();

        while (parser.next() != XmlPullParser.END_TAG) {
            //String name = parser.getName();
            if (parser.getName().equals("name")) {
                hero.name = readText(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "name");
            } else if (parser.getName().equals("imageName")) {
                hero.imageName = readText(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "imageName");
            } else if (parser.getName().equals("bioRoles")) {
                hero.bioRoles = readText(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "bioRoles");
            } else if (parser.getName().equals("intelligence")) {
                hero.intelligence = readText(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "intelligence");
            } else if (parser.getName().equals("agility")) {
                hero.agility = readText(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "agility");
            } else if (parser.getName().equals("strength")) {
                hero.strength = readText(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "strength");
            } else if (parser.getName().equals("attack")) {
                hero.attack = readText(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "attack");
            } else if (parser.getName().equals("speed")) {
                hero.speed = readText(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "speed");
            } else if (parser.getName().equals("defence")) {
                hero.defence = readText(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "defence");
            } else if (parser.getName().equals("abilities")) {
                hero.abilities.add(LoadHeroAbilities(parser));
                parser.require(XmlPullParser.END_TAG, sNullString, "abilities");
            } else if (parser.getName().equals("talents")) {
                hero.talents.add(LoadHeroTalent(parser));
                parser.require(XmlPullParser.END_TAG, sNullString, "talents");
            } else {
                throw new RuntimeException("Loading XML Error, in LoadIndividualHeroInfo. Name:" + parser.getName());
            }
        }

        for(HeroAbilityInfo ability : hero.abilities)
            ability.heroName = hero.name;

        parser.require(XmlPullParser.END_TAG, sNullString, "heroInfo");

        return hero;
    }


    static private HeroAbilityInfo LoadHeroAbilities(XmlResourceParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, sNullString, "abilities");
        HeroAbilityInfo ability = new HeroAbilityInfo();


        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getName().equals("isStun")) {
                ability.isStun = readBoolean(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "isStun");
            } else if (parser.getName().equals("isDisable")) {
                ability.isDisable = readBoolean(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "isDisable");
            } else if (parser.getName().equals("isUltimate")) {
                ability.isUltimate = readBoolean(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "isUltimate");
            } else if (parser.getName().equals("isSilence")) {
                ability.isSilence = readBoolean(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "isSilence");
            } else if (parser.getName().equals("isMute")) {
                ability.isMute = readBoolean(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "isMute");
            } else if (parser.getName().equals("piercesSpellImmunity")) {
                ability.piercesSpellImmunity = readBoolean(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "piercesSpellImmunity");
            } else if (parser.getName().equals("piercesSIType")) {
                ability.piercesSIType = readText(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "piercesSIType");
            } else if (parser.getName().equals("piercesSIDetail")) {
                ability.piercesSIDetail = readText(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "piercesSIDetail");
            } else if (parser.getName().equals("name")) {
                ability.name = readText(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "name");
            } else if (parser.getName().equals("imageName")) {
                ability.imageName = readText(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "imageName");
            } else if (parser.getName().equals("description")) {
                ability.description = readText(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "description");
            } else if (parser.getName().equals("manaCost")) {
                ability.manaCost = readText(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "manaCost");
            } else if (parser.getName().equals("cooldown")) {
                ability.cooldown = readText(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "cooldown");
            } else if (parser.getName().equals("disableDuration")) {
                ability.disableDuration = readText(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "disableDuration");
            } else if (parser.getName().equals("abilityDetails")) {
                ability.abilityDetails.add(readText(parser));
                parser.require(XmlPullParser.END_TAG, sNullString, "abilityDetails");
            } else if (parser.getName().equals("removableDebuffs")) {
                ability.removableDebuffs.add(LoadHeroDebuffs(parser));
                parser.require(XmlPullParser.END_TAG, sNullString, "removableDebuffs");
            } else {
                throw new RuntimeException("Loading XML Error, in LoadHeroAbilities. Name:" + parser.getName());
            }
        }

        parser.require(XmlPullParser.END_TAG, sNullString, "abilities");
        return ability;
    }

    static private HeroAbilityInfo.RemovableBuff LoadHeroDebuffs(XmlResourceParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, sNullString, "removableDebuffs");
        HeroAbilityInfo.RemovableBuff debuff = new HeroAbilityInfo.RemovableBuff();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getName().equals("description")) {
                debuff.description = readText(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "description");
            } else if (parser.getName().equals("basicDispel")) {
                debuff.basicDispel = readBoolean(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "basicDispel");
            } else if (parser.getName().equals("strongDispel")) {
                debuff.strongDispel = readBoolean(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "strongDispel");
            } else {
                throw new RuntimeException("Loading XML Error, in LoadHeroDebuffs. Name:" + parser.getName());
            }
        }

        parser.require(XmlPullParser.END_TAG, sNullString, "removableDebuffs");
        return debuff;
    }

    static private HeroAbilityInfo.Talent LoadHeroTalent(XmlResourceParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, sNullString, "talents");
        HeroAbilityInfo.Talent talent = new HeroAbilityInfo.Talent();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getName().equals("optionOne")) {
                talent.optionOne = readText(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "optionOne");
            } else if (parser.getName().equals("optionTwo")) {
                talent.optionTwo = readText(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "optionTwo");
            } else if (parser.getName().equals("level")) {
                talent.level = readInt(parser);
                parser.require(XmlPullParser.END_TAG, sNullString, "level");
            } else {
                throw new RuntimeException("Loading XML Error, in LoadHeroTalents. Name:" + parser.getName());
            }
        }

        parser.require(XmlPullParser.END_TAG, sNullString, "talents");
        return talent;
    }

    private static boolean readBoolean(XmlPullParser parser) throws IOException, XmlPullParserException {
        boolean result = false;
        if (parser.next() == XmlPullParser.TEXT) {
            if (parser.getText().equals("true")) {
                result = true;
            }
            parser.nextTag();
        } else {
            throw new RuntimeException("Loading XML Error, in readBoolean. Text not found!");
        }
        return result;
    }

    private static int readInt(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (parser.next() == XmlPullParser.TEXT) {
            int result = Integer.parseInt(parser.getText());
            parser.nextTag();
            return result;
        }

        throw new RuntimeException("Loading XML Error, in readInt. Text not found!");
    }

    // Extracts text values. and goes to tag following the text
    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        } else {
            throw new RuntimeException("Loading XML Error, in readText. Text not found!");
        }
        return result;
    }
}
