package top.trumeet.mipushframework.help;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.fragment.app.FragmentActivity;

/**
 * Created by Trumeet on 2018/2/8.
 */

public class HelpActivity extends FragmentActivity {
    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SupportFragment())
                .commitAllowingStateLoss();
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
