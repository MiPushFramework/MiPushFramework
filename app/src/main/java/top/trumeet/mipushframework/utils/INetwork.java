package top.trumeet.mipushframework.utils;

import android.support.annotation.WorkerThread;

import java.io.IOException;

/**
 * Created by Trumeet on 2017/8/31.
 * @author Trumeet
 */

public interface INetwork {
    @WorkerThread
    String execute (String baseUrl) throws IOException;
}
