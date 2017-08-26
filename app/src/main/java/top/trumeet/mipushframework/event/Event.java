package top.trumeet.mipushframework.event;

import android.support.annotation.IntDef;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by Trumeet on 2017/8/26.
 * App event model
 * @author Trumeet
 */

@Entity
public class Event {
    @IntDef({Type.RECEIVE_PUSH, Type.REGISTER})
    @Retention(SOURCE)
    @Target({ElementType.PARAMETER, ElementType.TYPE,
            ElementType.FIELD, ElementType.METHOD})
    public @interface Type {
        int RECEIVE_PUSH = 0;
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
     * Operation result
     */
    @Property(nameInDb = "result")
    @ResultType
    private int result;

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

}
