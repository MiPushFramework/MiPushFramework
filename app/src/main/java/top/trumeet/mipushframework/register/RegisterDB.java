package top.trumeet.mipushframework.register;

import android.content.Context;

import com.xiaomi.xmsf.XmsfApp;

import java.util.List;

import top.trumeet.mipushframework.db.RegisteredApplicationDao;

/**
 * Created by Trumeet on 2017/8/26.
 * DBUtils to manage registers.
 * @see RegisteredApplication
 * @author Trumeet
 */

public class RegisterDB {
    private static RegisteredApplicationDao getDao (Context context) {
        return XmsfApp.getSession(context).getDaoSession()
                .getRegisteredApplicationDao();
    }

    public static RegisteredApplication
    registerApplication (String pkg, boolean autoCreate, Context context) {
        List<RegisteredApplication> list = getDao(context)
                .queryBuilder()
                .where(RegisteredApplicationDao.Properties.PackageName.eq(pkg))
                .build().list();
        return list == null || list.isEmpty() ?
                (autoCreate ? create(pkg, context) : null) : list.get(0);
    }

    public static List<RegisteredApplication>
    getList (Context context) {
        return getDao(context)
                .queryBuilder()
                .build().list();
    }

    public static void update (RegisteredApplication application,
                               Context context) {
        getDao(context)
                .update(application);
    }

    private static RegisteredApplication create (String pkg,
                                                 Context context) {
        RegisteredApplication registeredApplication =
                new RegisteredApplication(null, pkg
                        , RegisteredApplication.Type.ASK,
                        true /* Allow push */,
                        true /* Allow receive result */);
        getDao(context)
                .insert(registeredApplication);
        return registeredApplication;
    }
}
