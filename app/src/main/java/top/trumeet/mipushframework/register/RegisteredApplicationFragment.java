package top.trumeet.mipushframework.register;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;
import top.trumeet.common.db.RegisteredApplicationDb;
import top.trumeet.common.register.RegisteredApplication;

import static android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES;
import static top.trumeet.common.Constants.TAG;

/**
 * Created by Trumeet on 2017/8/26.
 *
 * @author Trumeet
 */

public class RegisteredApplicationFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private MultiTypeAdapter mAdapter;
    private LoadTask mLoadTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(RegisteredApplication.class, new RegisteredApplicationBinder());
    }

    SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView view = new RecyclerView(getActivity());
        view.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false));
        view.setAdapter(mAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(view.getContext(),
                LinearLayoutManager.VERTICAL);
        view.addItemDecoration(dividerItemDecoration);


        swipeRefreshLayout = new SwipeRefreshLayout(getActivity());
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.addView(view);

        loadPage();
        return swipeRefreshLayout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadPage();
    }

    private void loadPage() {
        Log.d(TAG, "loadPage");
        if (mLoadTask != null && !mLoadTask.isCancelled())
            return;
        mLoadTask = new LoadTask(getActivity().getApplicationContext());
        mLoadTask.execute();
    }

    @Override
    public void onDetach() {
        if (mLoadTask != null && !mLoadTask.isCancelled()) {
            mLoadTask.cancel(true);
            mLoadTask = null;
        }
        super.onDetach();
    }

    @Override
    public void onRefresh() {
        loadPage();

    }

    private class LoadTask extends AsyncTask<Integer, Void, List<RegisteredApplication>> {
        private CancellationSignal mSignal;

        public LoadTask(Context context) {
            this.context = context;
        }

        private Context context;

        @Override
        protected List<RegisteredApplication> doInBackground(Integer... integers) {
            mSignal = new CancellationSignal();

            List<ApplicationInfo> installedApplications = context.getPackageManager().getInstalledApplications(GET_UNINSTALLED_PACKAGES);

            Map<String, ApplicationInfo> applicationInfoMap = new HashMap<>();
            for (ApplicationInfo installedApplication : installedApplications) {
                applicationInfoMap.put(installedApplication.packageName, installedApplication);
            }

            List<RegisteredApplication> res = new ArrayList<>();
            for (RegisteredApplication application : RegisteredApplicationDb.getList(getActivity(), null, mSignal)) {
                if (applicationInfoMap.containsKey(application.getPackageName())) {
                    res.add(application);
                }
            }

            return res;
        }

        @Override
        protected void onPostExecute(List<RegisteredApplication> list) {
            mAdapter.notifyItemRangeRemoved(0, mAdapter.getItemCount());
            mAdapter.getItems().clear();

            int start = mAdapter.getItemCount();
            Items items = new Items(mAdapter.getItems());
            items.addAll(list);
            mAdapter.setItems(items);
            mAdapter.notifyItemRangeInserted(start, list.size());

            swipeRefreshLayout.setRefreshing(false);
            mLoadTask = null;
        }

        @Override
        protected void onCancelled() {
            if (mSignal != null) {
                if (!mSignal.isCanceled())
                    mSignal.cancel();
                mSignal = null;
            }

            swipeRefreshLayout.setRefreshing(false);
            mLoadTask = null;
        }
    }
}
