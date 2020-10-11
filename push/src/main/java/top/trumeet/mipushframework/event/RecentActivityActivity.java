package top.trumeet.mipushframework.event;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * Created by Trumeet on 2017/8/28.
 * @author Trumeet
 */

public class RecentActivityActivity extends AppCompatActivity {

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content,
                            EventFragment.newInstance(getIntent()
                            .getDataString()))
                    .commitAllowingStateLoss();
        }
        getSupportActionBar()
                .setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
