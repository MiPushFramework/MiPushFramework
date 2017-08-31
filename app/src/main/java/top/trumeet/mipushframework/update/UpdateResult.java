package top.trumeet.mipushframework.update;

import java.util.Date;

/**
 * Created by Trumeet on 2017/8/30.
 * @author Trumeet
 */

public class UpdateResult {
    private String tag;
    private String name;
    private boolean preRelease;
    private Date publishAt;
    private String htmlUrl;
    private int id;
    private String body;

    public UpdateResult(String tag, String name, boolean preRelease, Date publishAt, String htmlUrl, int id, String body) {
        this.tag = tag;
        this.name = name;
        this.preRelease = preRelease;
        this.publishAt = publishAt;
        this.htmlUrl = htmlUrl;
        this.id = id;
        this.body = body;
    }

    @Override
    public String toString() {
        return "UpdateResult{" +
                "tag='" + tag + '\'' +
                ", name='" + name + '\'' +
                ", preRelease=" + preRelease +
                ", publishAt=" + publishAt +
                ", htmlUrl='" + htmlUrl + '\'' +
                ", id=" + id +
                ", body='" + body + '\'' +
                '}';
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPreRelease() {
        return preRelease;
    }

    public void setPreRelease(boolean preRelease) {
        this.preRelease = preRelease;
    }

    public Date getPublishAt() {
        return publishAt;
    }

    public void setPublishAt(Date publishAt) {
        this.publishAt = publishAt;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
