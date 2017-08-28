package top.trumeet.mipushframework.event;

import android.content.Context;
import android.support.annotation.Nullable;

import com.xiaomi.xmsf.XmsfApp;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import top.trumeet.mipushframework.Constants;
import top.trumeet.mipushframework.db.EventDao;

/**
 * Created by Trumeet on 2017/8/26.
 * @author Trumeet
 */

public class EventDB {
    private static EventDao getDao (Context context) {
        return XmsfApp.getDaoSession(context)
                .getEventDao();
    }

    public static long insertEvent (Event event,
                                    Context context) {
        return getDao(context)
                .insert(event);
    }

    public static long insertEvent (String pkg, @Event.Type int type,
                                    @Event.ResultType int result,
                                    Context context) {
        Event event = new Event(null, pkg, type, getUTC().getTime()
                , result);
        return insertEvent(event, context);
    }

    public static List<Event> query (@Nullable Integer skip,
                                     @Nullable Integer limit,
                                     Context context) {
        QueryBuilder<Event> queryBuilder = getDao(context).queryBuilder();
        if (skip != null)
            queryBuilder.offset(skip);
        if (limit != null)
            queryBuilder.limit(limit);
        return queryBuilder.orderDesc(EventDao.Properties.Date).
                build().list();
    }

    public static List<Event> query (int page, Context context) {
        int skip;
        int limit;
        skip = Constants.PAGE_SIZE * (page - 1);
        limit = skip + Constants.PAGE_SIZE;
        return query(skip, limit, context);
    }

    public static Date getUTC (Date date) {
        // 1、取得本地时间：
        Calendar cal = Calendar.getInstance() ;
        cal.setTime(date);
        // 2、取得时间偏移量：
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        // 3、取得夏令时差：
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        // 4、从本地时间里扣除这些差量，即可以取得UTC时间：
        cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        return cal.getTime();
    }

    public static Date getUTC () {
        return getUTC(new Date());
    }
}
