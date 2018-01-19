package top.trumeet.mipush.provider.event;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import top.trumeet.mipush.provider.DatabaseUtils;
import top.trumeet.mipush.provider.gen.db.RegisteredApplicationDao;

import static top.trumeet.common.db.EventDb.AUTHORITY;
import static top.trumeet.common.db.EventDb.BASE_PATH;
import static top.trumeet.common.db.EventDb.CONTENT_URI;
import static top.trumeet.mipush.provider.DatabaseUtils.daoSession;

/**
 * Created by Trumeet on 2017/12/23.
 */

public class EventProvider extends ContentProvider {
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/" + BASE_PATH;
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/" + BASE_PATH;

    private static final String TABLENAME = "Event";
    private static final String PK = RegisteredApplicationDao.Properties.Id.columnName;

    private static final int EVENT = 0;

    private static final UriMatcher sURIMatcher;

    static {
        sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, EVENT);
        //sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", APP_ID);
    }

    @Override
    public boolean onCreate() {
        DatabaseUtils.init(getContext());
        return true;
    }

    protected SQLiteDatabase getDatabase() {
        if(daoSession == null) {
            throw new IllegalStateException("DaoSession must be set during content provider is active");
        }
        return (SQLiteDatabase)
                daoSession.getDatabase().getRawDatabase();
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = getDatabase();
        Uri returnUri;
        long id;
        switch (sURIMatcher.match(uri)) {
            case EVENT:
                id = db.insert(TABLENAME
                        , null, values);
                if (id > 0)
                    returnUri = ContentUris.withAppendedId(CONTENT_URI, id);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = getDatabase();
        switch (sURIMatcher.match(uri)) {
            case EVENT :
                return db.delete(TABLENAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = getDatabase();
        switch (sURIMatcher.match(uri)) {
            case EVENT :
                return db.update(TABLENAME, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case EVENT:
                queryBuilder.setTables(TABLENAME);
                break;
                /*
            case APP_ID:
                queryBuilder.setTables(TABLENAME);
                queryBuilder.appendWhere(PK + "="
                        + uri.getLastPathSegment());
                break;
            */
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = getDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public final String getType(@NonNull Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case EVENT:
                return CONTENT_TYPE;
            //case APP_ID:
            //    return CONTENT_ITEM_TYPE;
            default :
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}
