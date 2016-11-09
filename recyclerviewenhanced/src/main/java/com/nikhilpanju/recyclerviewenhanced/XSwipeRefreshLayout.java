package com.nikhilpanju.recyclerviewenhanced;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.RecyclerView.OnScrollListener;
import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * Created by laxian on 16-11-9
 * 为SwipeRefreshLayout增加上拉加载功能
 */
public class XSwipeRefreshLayout extends SwipeRefreshLayout {

    public interface AdapterCallback {
        void shouldShowLoadItem(boolean show);
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    private RecyclerView rView;
    private ListView lView;
    private boolean mIsLoading = false;
    private OnLoadMoreListener listener;
    private AdapterCallback adapter;

    public XSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public XSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener lis) {
        this.listener = lis;
    }

    public void bindAdapter(AdapterCallback callback) {
        this.adapter = callback;
    }

    private boolean isRv() {
        return true;
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    public void setLoadMore(boolean loadmore) {
        mIsLoading = loadmore;
        if (loadmore) {
            if (listener != null) {
                listener.onLoadMore();
            }
        } else {
        }
        adapter.shouldShowLoadItem(loadmore);
    }

    private void init() {
        findScrollChild();
        checkAdapter();
        if (isRv()) {
            rView.addOnScrollListener(new OnScrollListener() {

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                    // item 总数
                    int totalItemCount = layoutManager.getItemCount();

                    // 最后可见item位置
                    int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                    if (dy > 0 && !isRefreshing() && !isLoading() && totalItemCount <= lastVisibleItem+1) {
                        setLoadMore(true);
                    }
                }
            });
        } else {
            lView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {

                }

                @Override
                public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                }
            });
        }
    }

    private void checkAdapter() {
        if (adapter == null) {
            adapter = (AdapterCallback) rView.getAdapter();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        init();
    }

    private void findScrollChild() {
        findView();
        if (rView == null && lView == null) {
            throw new IllegalArgumentException("SwipeRefreshLayout 必须有一个ListView或者Recycler子View");
        }
    }

    private void findView() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAtI = getChildAt(i);
            if (childAtI instanceof RecyclerView) {
                rView = (RecyclerView) childAtI;
            } else if (childAtI instanceof ListView) {
                lView = (ListView) childAtI;
            } else {}
        }
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        super.setRefreshing(refreshing);
        if (!refreshing) {
            setLoadMore(refreshing);
        }
    }

    public static abstract class LoadMoreRvAdapter<T> extends RecyclerView.Adapter<ViewHolder> implements AdapterCallback, OnClickListener {

        protected List<T> mDatas = new ArrayList<>();
        protected OnRecyclerViewItemClickListener mOnItemClickListener = null;
        protected static final int TYPE_ITEM = 0;
        protected static final int TYPE_MORE = 1;
        protected boolean showLoadMore = false;


        //define interface
        public interface OnRecyclerViewItemClickListener {
            void onItemClick(View view, Object data);
        }

        public void setData(List<T> lists) {
            mDatas = lists;
            notifyDataSetChanged();
        }

        public void addData(List<T> lists) {
            if (lists != null && lists.size() != 0) {
                mDatas.addAll(lists);
                notifyDataSetChanged();
            }
        }

        public void setOnItemClickListener(OnRecyclerViewItemClickListener mOnItemClickListener) {
            this.mOnItemClickListener = mOnItemClickListener;
        }

        @Override
        public int getItemCount() {
            int more = showLoadMore?1:0;
            return (mDatas == null ? 0 : mDatas.size()) + more;
        }

        @Override
        public int getItemViewType(int position) {
            if (showLoadMore && position == mDatas.size()) {
                return TYPE_MORE;
            }
            return TYPE_ITEM;
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                //注意这里使用getTag方法获取数据
                mOnItemClickListener.onItemClick(v, v.getTag());
            }
        }


        @Override
        public void shouldShowLoadItem(boolean show) {
            this.showLoadMore = show;
            notifyDataSetChanged();
        }
    }
}
