package dynasty.software.the.stylishly.ui.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.parse.ParseUser;

import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.ui.base.BaseActivity;
import dynasty.software.the.stylishly.ui.fragments.IntroFragment;

/**
 * Author : Aduraline.
 */

public class EntryActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_activity_entry);

        ParseUser parseUser = ParseUser.getCurrentUser();

        /*
        * Check if user has been logged in.
        * */
        if (parseUser == null) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_entry_activity, IntroFragment.newInstance())
                    .commit();

        }else {
            new Handler()
                    .postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            HomeActivity.start(EntryActivity.this);
                        }
                    }, 2000);
        }
    }
}
