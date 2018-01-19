package top.trumeet.common.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Trumeet on 2017/12/29.
 * 操作 Provider 的 Util
 */

public class DatabaseUtils {
    public static final String KEY_ID = "_id";

    private final Uri uri;
    private final ContentResolver resolver;

    public DatabaseUtils(@NonNull Uri uri, @NonNull ContentResolver resolver) {
        this.uri = uri;
        this.resolver = resolver;
    }

    public Uri insert (ContentValues values) {
        return resolver.insert(uri, values);
    }

    public Cursor query (@Nullable CancellationSignal cancellationSignal,
                         @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return resolver.query(uri, null
                , selection, selectionArgs, sortOrder,
                cancellationSignal);
    }

    public int delete (@Nullable String where, @Nullable String[] selectionArgs) {
        return resolver.delete(uri, where, selectionArgs);
    }

    public int update (@Nullable ContentValues values,
                        @Nullable String where, @Nullable String[] selectionArgs) {
        return resolver.update(uri, values, where, selectionArgs);
    }

    /**
     * Convert cursor to your own object
     */
    public interface Converter<T> {
        @NonNull
        T convert (@NonNull Cursor cursor);
    }

    @NonNull
    public <T> List<T> queryAndConvert (@Nullable CancellationSignal cancellationSignal,
                                        @Nullable String selection,
                                        @Nullable String[] selectionArgs,
                                        @Nullable String sortOrder,
                                        @NonNull Converter<T> converter) {
        Cursor cursor = query(cancellationSignal, selection, selectionArgs, sortOrder);
        if (cursor == null)
            return new ArrayList<>(0);
        List<T> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(converter.convert(cursor));
        }
		cursor.close();
        return list;
    }

    @NonNull
    public static String order (@NonNull String column, @NonNull String order) {
        StringBuilder builder = new StringBuilder();
        return builder.append(column)
                .append(" ")
                .append(order)
                .toString();
    }

    @NonNull
    public static String limitAndOffset (@Nullable Integer limit,
                                         @Nullable Integer offset) {
        if (limit == null && offset == null)
            return "";
        StringBuilder builder = new StringBuilder();
        if (limit != null) {
            builder.append(" LIMIT ");
            builder.append(limit);
        }
        if (offset != null) {
            builder.append(" OFFSET ");
            builder.append(offset);
        }
        return builder.toString();
    }
}
