package dynasty.software.the.stylishly.ui.adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import dynasty.software.the.stylishly.ui.fragments.PeopleSearchFragment;
import dynasty.software.the.stylishly.ui.fragments.PostSearchFragment;
import dynasty.software.the.stylishly.ui.fragments.TagsSearchFragment;

/**
 * Author : Aduraline.
 */

public class SearchTabAdapter extends FragmentPagerAdapter {

    public SearchTabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return PostSearchFragment.newInstance();
            case 1:
                return PeopleSearchFragment.newInstance();
            case 2:
                return TagsSearchFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Posts";
            case 1:
                return "People";
            case 2:
                return "Tags";
        }
        return "";
    }
}
