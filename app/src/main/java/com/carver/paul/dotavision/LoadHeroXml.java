package com.carver.paul.dotavision;

import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoadHeroXml {

    static final String ns = null;

    public static List<HeroInfo> Load(XmlResourceParser parser) {
        System.out.println("Starting XML Load.");

        List<HeroInfo> heroInfoList = new ArrayList<>();

        try {
            parser.next();
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, ns, "listOfHeroInfo");
            while (parser.next() != XmlPullParser.END_TAG) {
                parser.require(XmlPullParser.START_TAG, ns, "heroInfo");
                heroInfoList.add(LoadIndividualHeroInfo(parser));
                parser.require(XmlPullParser.END_TAG, ns, "heroInfo");
            }

        } catch (XmlPullParserException e) {
            System.err.println("XmlPullParserException: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }

        System.out.println("Loaded " + heroInfoList.size() + " heroes.");
        return heroInfoList;
    }

    static private HeroInfo LoadIndividualHeroInfo(XmlResourceParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "heroInfo");
        HeroInfo hero = new HeroInfo();

        while (parser.next() != XmlPullParser.END_TAG) {
            //String name = parser.getName();
            if (parser.getName().equals("name")) {
                hero.name = readText(parser);
                parser.require(XmlPullParser.END_TAG, ns, "name");
            } else if (parser.getName().equals("bioRoles")) {
                hero.bioRoles = readText(parser);
                parser.require(XmlPullParser.END_TAG, ns, "bioRoles");
            } else if (parser.getName().equals("intelligence")) {
                hero.intelligence = readText(parser);
                parser.require(XmlPullParser.END_TAG, ns, "intelligence");
            } else if (parser.getName().equals("agility")) {
                hero.agility = readText(parser);
                parser.require(XmlPullParser.END_TAG, ns, "agility");
            } else if (parser.getName().equals("strength")) {
                hero.strength = readText(parser);
                parser.require(XmlPullParser.END_TAG, ns, "strength");
            } else if (parser.getName().equals("attack")) {
                hero.attack = readText(parser);
                parser.require(XmlPullParser.END_TAG, ns, "attack");
            } else if (parser.getName().equals("speed")) {
                hero.speed = readText(parser);
                parser.require(XmlPullParser.END_TAG, ns, "speed");
            } else if (parser.getName().equals("defence")) {
                hero.defence = readText(parser);
                parser.require(XmlPullParser.END_TAG, ns, "defence");
            } else if (parser.getName().equals("abilities")) {
                hero.abilities.add(LoadHeroAbilities(parser));
                parser.require(XmlPullParser.END_TAG, ns, "abilities");
            } else {
                throw new RuntimeException("Loading XML Error, in LoadIndividualHeroInfo. Name:" + parser.getName());
            }
        }

        parser.require(XmlPullParser.END_TAG, ns, "heroInfo");

        return hero;
    }


    static private HeroAbility LoadHeroAbilities(XmlResourceParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "abilities");
        HeroAbility ability = new HeroAbility();


        while (parser.next() != XmlPullParser.END_TAG) {
            //String name = parser.getName();
            if (parser.getName().equals("isStun")) {
                if (readText(parser).equals("false"))
                    ability.isStun = false;
                else
                    ability.isStun = true;
                parser.require(XmlPullParser.END_TAG, ns, "isStun");
            } else if (parser.getName().equals("name")) {
                ability.name = readText(parser);
                parser.require(XmlPullParser.END_TAG, ns, "name");
            } else if (parser.getName().equals("description")) {
                ability.description = readText(parser);
                parser.require(XmlPullParser.END_TAG, ns, "description");
            } else if (parser.getName().equals("manaCost")) {
                ability.manaCost = readText(parser);
                parser.require(XmlPullParser.END_TAG, ns, "manaCost");
            } else if (parser.getName().equals("abilityDetails")) {
                ability.abilityDetails.add(readText(parser));
                parser.require(XmlPullParser.END_TAG, ns, "abilityDetails");
            } else {
                throw new RuntimeException("Loading XML Error, in LoadHeroAbilities. Name:" + parser.getName());
            }
        }

        parser.require(XmlPullParser.END_TAG, ns, "abilities");
        return ability;
    }

    private static boolean realBoolean(XmlPullParser parser) throws IOException, XmlPullParserException {
        boolean result = false;
        if (parser.next() == XmlPullParser.TEXT) {
            if (parser.getText().equals("true")) {
                result = true;
            }
            parser.nextTag();
        } else {
            throw new RuntimeException("Loading XML Error, in realBoolean. Text not found!");
        }
        return result;
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
