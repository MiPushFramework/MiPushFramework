package top.trumeet.common.db;

import android.annotation.Nullable;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;

import java.util.List;

import top.trumeet.common.Constants;
import top.trumeet.common.event.Event;
import top.trumeet.common.utils.DatabaseUtils;
import top.trumeet.common.utils.Utils;

/**
 * Created by Trumeet on 2017/12/23.
 */

public class EventDb {
    public static final String AUTHORITY = "top.trumeet.mipush.providers.EventProvider";
    public static final String BASE_PATH = "EVENT";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    private static DatabaseUtils getInstance (Context context) {
        return new DatabaseUtils(CONTENT_URI, context.getContentResolver());
    }

    public static Uri insertEvent (Event event,
                                    Context context) {
        return getInstance(context)
                .insert(event.toValues());
    }

    public static Uri insertEvent (String pkg, @Event.Type int type,
                                    @Event.ResultType int result, String notificationTitle,
                                    String notificationSummary,
                                    Context context) {
        return insertEvent(new Event(null, pkg, type, Utils.getUTC().getTime()
                        , result, notificationTitle, notificationSummary), context);
    }

    public static List<Event> query (@Nullable Integer skip,
                                     @Nullable Integer limit,
                                     @Nullable String pkg,
                                     Context context,
                                     @Nullable CancellationSignal signal) {
        return getInstance(context)
                .queryAndConvert(signal, pkg == null ? null : Event.KEY_PKG + "=?",
                        pkg != null ? new String[]{pkg} : null,
                        DatabaseUtils.order(Event.KEY_DATE, "desc") +
                                DatabaseUtils.limitAndOffset(limit, skip),
                        new DatabaseUtils.Converter<Event>() {
                            @Override
                            @NonNull
                            public Event convert(@NonNull Cursor cursor) {
                                return Event.create(cursor);
                            }
                        });
    }

    public static List<Event> query (int page, Context context, CancellationSignal signal) {
        return query(null, page, context, signal);
    }

    public static List<Event> query (String pkg, int page, Context context,
                                     CancellationSignal cancellationSignal) {
        int skip;
        int limit;
        skip = Constants.PAGE_SIZE * (page - 1);
        limit = skip + Constants.PAGE_SIZE;
        return query(skip, limit, pkg, context, cancellationSignal);
    }
}
