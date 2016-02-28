package com.carver.paul.dotavision.Ui.widget;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.carver.paul.dotavision.Ui.AbilityInfo.AbilityInfoFragment;
import com.carver.paul.dotavision.Ui.AbilityInfo.AbilityInfoPresenter;
import com.carver.paul.dotavision.Ui.CounterPicker.CounterPickerFragment;
import com.carver.paul.dotavision.Ui.CounterPicker.CounterPickerPresenter;

import java.util.List;

/**
 * Based on code from :
 * http://www.android4devs.com/2015/01/how-to-make-material-design-sliding-tabs.html
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    static private List<String> mTitles;
    static private final int NUMBER_OF_TABS = 2;

    CounterPickerFragment tab1;
    AbilityInfoFragment tab2;

    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapter(FragmentManager fm, List<String> titles) {
        super(fm);

        mTitles = titles;
        tab1 = new CounterPickerFragment();
        tab2 = new AbilityInfoFragment();
    }

    public CounterPickerPresenter getCounterPickerPresenter() {
        return tab1.getPresenter();
    }

    public AbilityInfoPresenter getAbilityInfoPresenter() {
        return tab2.getPresenter();
    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
        if(position == 0) { // if the position is 0 we are returning the First tab
            return tab1;
        }
        else {            // As we are having 2 tabs if the position is now 0 it must be 1 so we are returning second tab
            return tab2;
        }
    }

    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return NUMBER_OF_TABS;
    }
}