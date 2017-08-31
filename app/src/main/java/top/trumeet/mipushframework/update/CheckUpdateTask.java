package top.trumeet.mipushframework.update;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import top.trumeet.mipushframework.utils.Network;
import top.trumeet.mipushframework.utils.ParseUtils;

/**
 * Created by Trumeet on 2017/8/30.
 * @author Trumeet
 */

public class CheckUpdateTask extends AsyncTask<Void, Void, UpdateResult> {
    public static abstract class CheckListener {
        public abstract void done (UpdateResult result);

        public void start () {}
    }

    private CheckListener callback;
    private boolean includePreRelease;

    public CheckUpdateTask(boolean includePreRelease, @NonNull CheckListener callback) {
        this.callback = callback;
        this.includePreRelease = includePreRelease;
    }

    @Override
    protected void onPreExecute () {
        if (callback != null)
            callback.start();
    }

    @Override
    protected UpdateResult doInBackground(Void... voids) {
        // FIXME: 2017/8/30 Hardcoded repo
        String link =
                includePreRelease ? "https://api.github.com/repos/Trumeet/MiPushFramework/releases"
                        : "https://api.github.com/repos/Trumeet/MiPushFramework/releases/latest";
        try {
            String resp = Network.getDefault().execute(link);
            if (includePreRelease) {
                JSONArray rootArray = new JSONArray(resp);
                if (rootArray.length() > 0) {
                    return parse(rootArray.getJSONObject(0));
                } else {
                    return null;
                }
            } else {
                JSONObject rootObject = new JSONObject(resp);
                return parse(rootObject);
            }
        } catch (IOException|JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static UpdateResult parse (JSONObject object) throws JSONException {
        return new UpdateResult(object.getString("tag_name"),
                object.getString("name"),
                object.getBoolean("prerelease"),
                ParseUtils.parseDate(object.getString("published_at")),
                object.getString("html_url"),
                object.getInt("id"),
                object.getString("body"));
    }

    @Override
    protected void onPostExecute (UpdateResult result) {
        callback.done(result);
    }
}
