/**
 * True Sight for Dota 2
 * Copyright (C) 2015 Paul Broadbent
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 */

package com.carver.paul.dotavision;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.carver.paul.dotavision.ImageRecognition.HeroAndSimilarity;
import com.carver.paul.dotavision.ImageRecognition.HeroFromPhoto;
import com.carver.paul.dotavision.ImageRecognition.ImageTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Shows the heroes which have been seen in the image, and allows the user to change them.
 */
public class FoundHeroesFragment extends Fragment {
    private OnHeroChangedListener mHeroChangedListener;
    private List<TextView> mHeroNamesTextViews;
    private List<CenterLockListener> mHeroRecyclerViewListeners;
    private List<RecyclerView> mHeroRecyclerViews;
    private List<String> mAllHeroNames;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_found_heroes, container, false);
    }

    /**
     * Ensures the parent activity implements the OnHeroChangedListener
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mHeroChangedListener = (OnHeroChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeroChangedListener");
        }
    }

    /**
     * For use when the user scrolls to select a different hero, changing from oldHero to newHero
     */
    public interface OnHeroChangedListener {
        public void onHeroChanged(int posInList, String newHero);
    }

    public void showFoundHeroes(List<HeroFromPhoto> heroes,
                                List<HeroInfo> heroInfoList,
                                List<HeroInfo> heroInfoFromXml) {
        LinearLayout parent = (LinearLayout) getActivity().findViewById(
                R.id.layout_found_hero_pictures);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        mHeroNamesTextViews = new ArrayList<>();
        mHeroRecyclerViewListeners = new ArrayList<>();
        mHeroRecyclerViews = new ArrayList<>();

        if(mAllHeroNames == null)
            mAllHeroNames = getHeroNames(heroInfoFromXml);

        int posInList = 0;
        for (HeroFromPhoto hero : heroes) {
            LinearLayout foundPicturesView = (LinearLayout) inflater.inflate(
                    R.layout.item_found_hero_picture, parent, false);
            ImageView leftImage = (ImageView) foundPicturesView.findViewById(R.id.image_left);
            leftImage.setImageBitmap(ImageTools.GetBitmapFromMat(hero.image));


            //TODO-now do something for when losing focus? Change text back?
            AutoCompleteTextView heroNameTextView
                    = (AutoCompleteTextView) foundPicturesView.findViewById(R.id.text_hero_name);
            mHeroNamesTextViews.add(heroNameTextView);

            heroNameTextView.setText(heroInfoList.get(posInList).name);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_dropdown_item_1line, mAllHeroNames);
            heroNameTextView.setAdapter(adapter);

            heroNameTextView.addTextChangedListener(new HeroTextWatcher(
                    heroNameTextView.getText().toString(), mHeroChangedListener, mAllHeroNames,
                    posInList));


            RecyclerView recyclerView = (RecyclerView) foundPicturesView.findViewById(
                    R.id.recycler_correct_image);

            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(new HeroImageAdapter(hero.getSimilarityList()));

            //TODO-beauty: make the FoundHeroesFragment not depend on the screen width for finding
            // its centre, should instead use the Fragment's width. It also goes wrong if the
            // image on the left hand side is too wide! I don't really understand the math here!
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            // This makes the recyclerView automatically lock on the image which has been
            // scrolled to. Thanks stackoverflow and Github :)
            int center = (11 * metrics.widthPixels / 24);
            CenterLockListener recyclerViewListener = new CenterLockListener(center,
                    mHeroChangedListener, layoutManager, hero.getSimilarityList(), posInList);
            recyclerView.addOnScrollListener(recyclerViewListener);
            mHeroRecyclerViewListeners.add(recyclerViewListener);
            mHeroRecyclerViews.add(recyclerView);


            // Animate the recycler view in from the right to indicate that you can slide it to
            // change the hero
            int xPos = (int) recyclerView.getX();
            recyclerView.setX(metrics.widthPixels);
            recyclerView.animate()
                    .setStartDelay(50 * posInList)
                    .translationX(xPos)
                    .setInterpolator(new DecelerateInterpolator())
                    .setDuration(200);


            parent.addView(foundPicturesView);

            if (BuildConfig.DEBUG && MainActivity.sDebugMode)
                showSimilarityInfo(hero.getSimilarityList());

            posInList++;
        }
    }

    /**
     * Changes the hero which is posInList of those visible. niceHeroName specifies the name of the
     * hero to display in the box. heroImageName is the name of the hero's image, for use when
     * scrolling to the hero with the recyclerView.
     * @param posInList
     * @param niceHeroName
     * @param heroImageName
     */
    public void changeHero(int posInList, String niceHeroName, String heroImageName) {
        mHeroNamesTextViews.get(posInList).setText(niceHeroName);
        mHeroRecyclerViewListeners.get(posInList).setHero(heroImageName);

        // Hide the keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

        // Give the relevant recyclerview focus. This ensures none of the text views have focus
        // after setting the name of a hero
        mHeroRecyclerViews.get(posInList).requestFocus();
    }

    public void reset() {
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.layout_found_hero_pictures);
        layout.removeAllViews();

        if (BuildConfig.DEBUG && MainActivity.sDebugMode) {
            List<Integer> textViewIds = Arrays.asList(R.id.text_debug_similarity_info, R.id.text_image_debug);
            ResetTextViews(textViewIds);
        }
    }

    private List<String> getHeroNames(List<HeroInfo> heroInfoFromXml) {
        List<String> names = new ArrayList<>();
        for(HeroInfo heroInfo : heroInfoFromXml) {
            names.add(heroInfo.name);
        }
        return names;
    }

    private void showSimilarityInfo(List<HeroAndSimilarity> similarityList) {
        TextView infoText = (TextView) getActivity().findViewById(R.id.text_debug_similarity_info);
        infoText.setText("");
        infoText.setVisibility(View.VISIBLE);
        HeroAndSimilarity matchingHero = similarityList.get(0);

        infoText.append(matchingHero.hero.name + ", " + matchingHero.similarity);

        // poor result, so lets show some alternatives
        if (matchingHero.similarity < 0.65) {
            infoText.append(". (Alternatives: ");
            for (int i = 1; i < 6; i++) {
                infoText.append(similarityList.get(i).hero.name + ","
                        + similarityList.get(i).similarity + ". ");
            }
            infoText.append(")");
        }

        infoText.append(System.getProperty("line.separator")
                + System.getProperty("line.separator"));
    }

    private void ResetTextViews(List<Integer> ids) {
        for (Integer id : ids) {
            TextView tv = (TextView) getActivity().findViewById(id);
            tv.setText("");
            tv.setVisibility(View.GONE);
        }
    }
}

class HeroTextWatcher implements TextWatcher {

    private String mCurrentHeroName;
    private final FoundHeroesFragment.OnHeroChangedListener mHeroChangedListener;
    private final List<String> mAllHeroNames;
    private final int mPosInHeroList;

    HeroTextWatcher(String currentHeroName,
                    FoundHeroesFragment.OnHeroChangedListener heroChangedListener,
                    List<String> allHeroNames,
                    int posInHeroList){
        mCurrentHeroName = currentHeroName;
        mHeroChangedListener = heroChangedListener;
        mAllHeroNames = allHeroNames;
        mPosInHeroList = posInHeroList;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(!mCurrentHeroName.equalsIgnoreCase(s.toString())
                && containsIgnoreCase(mAllHeroNames, (s.toString()))) {
            mCurrentHeroName = s.toString();
            mHeroChangedListener.onHeroChanged(mPosInHeroList, s.toString());
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private boolean containsIgnoreCase(List<String> list, String string) {
        for(String s : list)
            if(s.equalsIgnoreCase(string))
                return true;

        return false;
    }
}