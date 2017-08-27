package top.trumeet.mipushframework.event;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;
import top.trumeet.mipushframework.utils.OnLoadMoreListener;

import static top.trumeet.mipushframework.Constants.TAG;

/**
 * Created by Trumeet on 2017/8/26.
 * @author Trumeet
 */

public class EventFragment extends Fragment {
    private MultiTypeAdapter mAdapter;

    /**
     * Already load page
     */
    private int mLoadPage;

    private LoadTask mLoadTask;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(Event.class, new EventItemBinder());
    }

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
        view.setOnScrollListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadPage();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadPage();
    }

    private void loadPage () {
        Log.d(TAG, "loadPage");
        if (mLoadTask != null && !mLoadTask.isCancelled())
            return;
        mLoadTask = new LoadTask(mLoadPage + 1);
        mLoadTask.execute();
    }

    @Override
    public void onDetach () {
        if (mLoadTask != null && !mLoadTask.isCancelled()) {
            mLoadTask.cancel(true);
            mLoadTask = null;
        }
        super.onDetach();
    }

    private class LoadTask extends AsyncTask<Integer, Void, List<Event>> {
        private int mTargetPage;

        LoadTask (int page) {
            mTargetPage = page;
        }

        @Override
        protected List<Event> doInBackground(Integer... integers) {
            return EventDB.query(mTargetPage,
                    getActivity());
        }

        @Override
        protected void onPostExecute (List<Event> list) {
            int start = mAdapter.getItemCount();
            Items items = new Items(mAdapter.getItems());
            items.addAll(list);
            mAdapter.setItems(items);
            mAdapter.notifyItemRangeInserted(start, list.size());
            mLoadPage = mTargetPage;
        }
    }
}
