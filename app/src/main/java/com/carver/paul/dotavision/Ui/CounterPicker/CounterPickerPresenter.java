/**
 * True Sight for Dota 2
 * Copyright (C) 2015 Paul Broadbent
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 */

package com.carver.paul.dotavision.Ui.CounterPicker;

import android.util.Log;
import android.util.Pair;

import com.carver.paul.dotavision.ImageRecognition.ImageTools;
import com.carver.paul.dotavision.Models.HeroAndAdvantages;
import com.carver.paul.dotavision.Models.HeroInfo;
import com.carver.paul.dotavision.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;

public class CounterPickerPresenter {
    private static final String TAG = "CounterPickerPresenter";

    private CounterPickerFragment mView;
    private int mRoleFilter = R.string.all_roles;
    private List<HeroAndAdvantages> mHeroesAndAdvantages = new ArrayList<>();
    private List<HeroInfo> mEnemyHeroes = new ArrayList<>();

    CounterPickerPresenter(CounterPickerFragment view) {
        mView = view;
    }

    public void showAdvantages(List<HeroAndAdvantages> heroesAndAdvantages,
                               List<HeroInfo> enemyHeroes) {
        mHeroesAndAdvantages = heroesAndAdvantages;
        mEnemyHeroes = enemyHeroes;
        reset();
        mView.endLoadingAnimation();
    }

    public void reset() {
        mView.reset();
    }

    public void hide() {
        mView.hide();
    }

    public void show() {
        mView.show();
    }

    public void startLoadingAnimation() {
        mView.startLoadingAnimation();
    }

    protected void setRoleFilter(int roleFilter) {
        if (roleFilter == mRoleFilter) {
            return;
        } else {
            mRoleFilter = roleFilter;
            reset();
            showHeadings();
            showAdvantages();
        }
    }

    protected void loadingAnimationFinished() {
        showHeadings();
        showAdvantages();
    }

    private void showHeadings() {
        List<Integer> imageIds = new ArrayList<>();
        for (HeroInfo hero : mEnemyHeroes) {
            imageIds.add(ImageTools.getDrawableForHero(hero.imageName));
        }
        mView.showHeadings(imageIds);
    }

    /**
     * Shows a row for each hero selected of the role selected in the spinner mRoleFilter with
     * the advantage it has against the five heroes in the photo.
     *
     * A little RxJava is used here to only show one row every 10 milliseconds, this is to prevent
     * the UI from locking up (which would happen if we attempt to add all ~120 rows at once).
     */
    private void showAdvantages() {
        //Create an observable that filters out the heroes which don't have the roe specified in the
        // spinner filter.
        Observable<HeroAndAdvantages> rowsToShowRx = Observable.from(mHeroesAndAdvantages)
                .filter(new Func1<HeroAndAdvantages, Boolean>() {
                    @Override
                    public Boolean call(HeroAndAdvantages hero) {
                        // don't show heroes with the same name as one in the photo
                        for(HeroInfo enemy : mEnemyHeroes) {
                            if(hero.getName().equals(enemy.name)) {
                                return false;
                            }
                        }

                        // implement the role selection made in the spinner
                        switch (mRoleFilter) {
                            case R.string.all_roles:
                                return true;
                            case R.string.carry_role:
                                return hero.isCarry();
                            case R.string.support_role:
                                return hero.isSupport();
                            case R.string.mid_role:
                                return hero.isMid();
                            case R.string.off_lane_role:
                                return hero.isOffLane();
                            case R.string.jungler_role:
                                return hero.isJunger();
                        }
                        Log.e(TAG, "mRoleFilter has invalid value.");
                        return true;
                    }
                });

        // Show a new row every 10 milliseconds (if we attempt to show them all at once then the UI
        // locks up because adding 120 rows at once takes too long!)
        Observable.interval(10, TimeUnit.MILLISECONDS)
                .zipWith(rowsToShowRx, new Func2<Long, HeroAndAdvantages, HeroAndAdvantages>() {
                    @Override
                    public HeroAndAdvantages call(Long count, HeroAndAdvantages heroAndAdvantages) {
                        return heroAndAdvantages;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<HeroAndAdvantages>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

//TODO-soon: fix row adding subscriber so that it gets unsubscribed when the view is changed or
// removed. Things will probably go wrong if this is still adding rows and we select a different
// role in the spinner or load a new photo.
                    @Override
                    public void onNext(HeroAndAdvantages hero) {
                        addRow(hero);
                    }
                });
    }

    private void addRow(HeroAndAdvantages hero) {
        List<Pair<String, Boolean>> advantages = new ArrayList<>();

        for(Double advantage : hero.getAdvantages()) {
            String string;
            Boolean boldText = false;

            if(advantage == HeroAndAdvantages.NEUTRAL_ADVANTAGE) {
                string = "  - ";
            } else {
                string = String.format(Locale.US, "%.1f", advantage);
                // add an empty space to offset the lack of a minus sign
                if(advantage >= 0 && advantage < 10) {
                    string = " " + string;
                }
            }

            if(advantage > 1) {
                boldText = true;
            }

            advantages.add(new Pair<>(string, boldText));
        }
        mView.addRow(hero.getName(), advantages, String.format(Locale.US, "%.1f", hero.getTotalAdvantage()));
    }
}