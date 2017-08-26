package top.trumeet.mipushframework.event;

import android.support.annotation.IntDef;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import top.trumeet.mipushframework.db.DaoSession;
import top.trumeet.mipushframework.db.EventDao;
import top.trumeet.mipushframework.db.NotificationInfoDao;
import top.trumeet.mipushframework.event.notification.NotificationInfo;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by Trumeet on 2017/8/26.
 * App event model
 * @author Trumeet
 */

@Entity
public class Event {
    @IntDef({Type.PUSH_MESSAGE, Type.PUSH_COMMAND, Type.REGISTER})
    @Retention(SOURCE)
    @Target({ElementType.PARAMETER, ElementType.TYPE,
            ElementType.FIELD, ElementType.METHOD})
    public @interface Type {
        int PUSH_MESSAGE = 0;
        int PUSH_COMMAND = 1;
        int REGISTER = 2;
    }

    @IntDef({ResultType.OK, ResultType.DENY_DISABLED, ResultType.DENY_USER})
    @Retention(SOURCE)
    @Target({ElementType.PARAMETER, ElementType.TYPE,
            ElementType.FIELD, ElementType.METHOD})
    public @interface ResultType {
        /**
         * Allowed
         */
        int OK = 0;

        /**
         * Deny because push is disabled by user
         */
        int DENY_DISABLED = 1;

        /**
         * User denied
         */
        int DENY_USER = 2;
    }

    /**
     * Id
     */
    @Id
    private Long id;

    /**
     * Package name
     */
    @Property(nameInDb = "pkg")
    @NotNull
    private String pkg;

    /**
     * Event type
     * @see Type
     */
    @Property(nameInDb = "type")
    @Type
    @NotNull
    private int type;

    /**
     * Event date time (UTC)
     */
    @Property(nameInDb = "date")
    @NotNull
    private long date;

    /**
     * Notification details. (Only for {@link Type#PUSH_MESSAGE} type)
     */
    @ToOne
    private NotificationInfo notificationInfo;

    /**
     * Operation result
     */
    @Property(nameInDb = "result")
    @ResultType
    private int result;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1542254534)
    private transient EventDao myDao;

    @Generated(hash = 1595411819)
    public Event(Long id, @NotNull String pkg, int type, long date, int result) {
        this.id = id;
        this.pkg = pkg;
        this.type = type;
        this.date = date;
        this.result = result;
    }

    @Generated(hash = 344677835)
    public Event() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPkg() {
        return this.pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getDate() {
        return this.date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getResult() {
        return this.result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    @Generated(hash = 1677798343)
    private transient boolean notificationInfo__refreshed;

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 846547152)
    public NotificationInfo getNotificationInfo() {
        if (notificationInfo != null || !notificationInfo__refreshed) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            NotificationInfoDao targetDao = daoSession.getNotificationInfoDao();
            targetDao.refresh(notificationInfo);
            notificationInfo__refreshed = true;
        }
        return notificationInfo;
    }

    /** To-one relationship, returned entity is not refreshed and may carry only the PK property. */
    @Generated(hash = 660856832)
    public NotificationInfo peakNotificationInfo() {
        return notificationInfo;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 869936050)
    public void setNotificationInfo(NotificationInfo notificationInfo) {
        synchronized (this) {
            this.notificationInfo = notificationInfo;
            notificationInfo__refreshed = true;
        }
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1459865304)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getEventDao() : null;
    }
}
