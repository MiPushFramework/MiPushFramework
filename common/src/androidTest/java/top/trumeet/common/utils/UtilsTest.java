package top.trumeet.common.utils;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Trumeet on 2017/12/30.
 */
public class UtilsTest {
    private Context appContext;

    @Before
    public void prepareContext () {
        appContext = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void isAppInstalled() throws Exception {
        assertTrue(Utils.isAppInstalled(appContext.getPackageName()));
    }

    @Test
    public void getString() throws Exception {
        assertEquals(Utils.getString(android.R.string.ok, appContext).toString(),
                appContext.getString(android.R.string.ok));
    }

}