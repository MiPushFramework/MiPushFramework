package top.trumeet.mipushframework.event;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;
import top.trumeet.mipushframework.utils.OnLoadMoreListener;

/**
 * Created by Trumeet on 2017/8/26.
 * @author Trumeet
 */

public class EventFragment extends Fragment {
    private static final String EXTRA_TARGET_PACKAGE = EventFragment.class
            .getName() + ".EXTRA_TARGET_PACKAGE";

    private MultiTypeAdapter mAdapter;
    private Logger logger = LoggerFactory.getLogger(EventFragment.class);

    /**
     * Already load page
     */
    private int mLoadPage;
    private String mTargetPackage;

    private LoadTask mLoadTask;

    public static EventFragment newInstance (String targetPackage) {
        EventFragment fragment = new EventFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_TARGET_PACKAGE, targetPackage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTargetPackage = getArguments() == null ? null :
                getArguments().getString(EXTRA_TARGET_PACKAGE);
        mAdapter = new MultiTypeAdapter();
        mAdapter.register(Event.class, new EventItemBinder(mTargetPackage == null));
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
        logger.debug("loadPage");
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
            return EventDB.query(mTargetPackage, mTargetPage,
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
