package top.trumeet.mipushframework.help;

import android.support.annotation.RawRes;

/**
 * Created by Trumeet on 2018/2/8.
 */

public class Article {
    @RawRes
    private final int titleRes;

    @RawRes
    private final int markdownRes;

    public Article(int titleRes, int markdownRes) {
        this.titleRes = titleRes;
        this.markdownRes = markdownRes;
    }

    @Override
    public String toString() {
        return "Article{" +
                "titleRes=" + titleRes +
                ", markdownRes=" + markdownRes +
                '}';
    }

    public int getMarkdownRes() {
        return markdownRes;
    }

    public int getTitleRes() {
        return titleRes;
    }
}
