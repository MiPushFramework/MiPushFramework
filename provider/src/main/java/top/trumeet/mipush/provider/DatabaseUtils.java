package top.trumeet.mipush.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.yuweiguocn.library.greendao.MigrationHelper;

import org.greenrobot.greendao.database.Database;

import top.trumeet.mipush.provider.gen.db.DaoMaster;
import top.trumeet.mipush.provider.gen.db.DaoSession;
import top.trumeet.mipush.provider.gen.db.EventDao;
import top.trumeet.mipush.provider.gen.db.RegisteredApplicationDao;

/**
 * Created by Trumeet on 2017/12/23.
 */

public class DatabaseUtils {
    public static DaoSession daoSession;
    public static void init (Context context) {
        if (daoSession != null)
            return;
        MigrationHelper.DEBUG = true;
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(context
                , "db",
                null);
        daoSession = new DaoMaster(helper.getWritableDatabase())
                .newSession();
    }


    private static class MySQLiteOpenHelper extends DaoMaster.OpenHelper {
        public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory);
        }
        @Override
        public void onUpgrade(Database db, int oldVersion, int newVersion) {
            MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {

                @Override
                public void onCreateAllTables(Database db, boolean ifNotExists) {
                    DaoMaster.createAllTables(db, ifNotExists);
                }

                @Override
                public void onDropAllTables(Database db, boolean ifExists) {
                    DaoMaster.dropAllTables(db, ifExists);
                }
            }, EventDao.class, RegisteredApplicationDao.class);
        }
    }

}
