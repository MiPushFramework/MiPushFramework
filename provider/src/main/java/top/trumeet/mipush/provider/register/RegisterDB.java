package top.trumeet.mipush.provider.register;

import java.util.List;

import top.trumeet.mipush.provider.DatabaseUtils;
import top.trumeet.mipush.provider.gen.db.RegisteredApplicationDao;

/**
 * Created by Trumeet on 2017/8/26.
 * DBUtils to manage registers.
 * 数据库内部实现，所有外部数据库操作均通过 {@link top.trumeet.common.db.RegisteredApplicationDb} 以及 Provider 实现。
 * @see RegisteredApplication
 * @author Trumeet
 */

public class RegisterDB {
    private static RegisteredApplicationDao getDao () {
        return DatabaseUtils.daoSession.getRegisteredApplicationDao();
    }

    public static RegisteredApplication
    registerApplication (String pkg, boolean autoCreate) {
        List<RegisteredApplication> list = getDao()
                .queryBuilder()
                .where(RegisteredApplicationDao.Properties.PackageName.eq(pkg))
                .build().list();
        return list == null || list.isEmpty() ?
                (autoCreate ? create(pkg) : null) : list.get(0);
    }

    public static List<RegisteredApplication>
    getList () {
        return getDao()
                .queryBuilder()
                .build().list();
    }

    public static void update (RegisteredApplication application) {
        getDao()
                .update(application);
    }

    private static RegisteredApplication create (String pkg) {
        RegisteredApplication registeredApplication =
                new RegisteredApplication(null, pkg
                        , RegisteredApplication.Type.ASK,
                        true /* Allow push */,
                        true /* Allow receive result */,
                        true /* Allow receive command */,
                        true /* Notification on register */);
        getDao()
                .insert(registeredApplication);
        return registeredApplication;
    }
}
