package com.nikhilpanju.recyclerviewsample;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nikhilpanju.recyclerviewenhanced.MyItemTouchCallback;
import com.nikhilpanju.recyclerviewenhanced.OnActivityTouchListener;
import com.nikhilpanju.recyclerviewenhanced.RecyclerTouchListener;
import com.nikhilpanju.recyclerviewenhanced.XSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecyclerTouchListener.RecyclerTouchListenerHelper, XSwipeRefreshLayout.OnLoadMoreListener, SwipeRefreshLayout.OnRefreshListener {

    RecyclerView mRecyclerView;
    ItemTouchHelper mItenTouchHelper;
    MainAdapter mAdapter;
    String[] dialogItems;
    List<Integer> unclickableRows, unswipeableRows;
    private RecyclerTouchListener onTouchListener;
    private int openOptionsPosition;
    private OnActivityTouchListener touchListener;
    private XSwipeRefreshLayout mXsrRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("RecyclerViewEnhanced");

        unclickableRows = new ArrayList<>();
        unswipeableRows = new ArrayList<>();
        dialogItems = new String[25];
        for (int i = 0; i < 25; i++) {
            dialogItems[i] = String.valueOf(i + 1);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAdapter = new MainAdapter(this, getData());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mXsrRv = (XSwipeRefreshLayout) findViewById(R.id.xsr_rv);
        mXsrRv.setOnLoadMoreListener(this);
        mXsrRv.setOnRefreshListener(this);

        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new MyItemTouchCallback(mAdapter));
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        onTouchListener = new RecyclerTouchListener(this, mRecyclerView);
        onTouchListener
                .setIndependentViews(R.id.rowButton)
                .setViewsToFade(R.id.rowButton)
                .setClickable(new RecyclerTouchListener.OnRowClickListener() {
                    @Override
                    public void onRowClicked(int position) {
                        ToastUtil.makeToast(getApplicationContext(), "Row " + (position + 1) + " clicked!");
                    }

                    @Override
                    public void onIndependentViewClicked(int independentViewID, int position) {
                        ToastUtil.makeToast(getApplicationContext(), "Button in row " + (position + 1) + " clicked!");
                    }
                })
                .setLongClickable(true, new RecyclerTouchListener.OnRowLongClickListener() {
                    @Override
                    public void onRowLongClicked(int position, RecyclerView.ViewHolder vh) {
                        ToastUtil.makeToast(getApplicationContext(), "Row " + (position + 1) + " long clicked!");
                        itemTouchHelper.startDrag(vh);
                    }
                })
                .setSwipeOptionViews(R.id.add, R.id.edit, R.id.change)
                .setSwipeable(R.id.rowFG, R.id.rowBG, new RecyclerTouchListener.OnSwipeOptionsClickListener() {
                    @Override
                    public void onSwipeOptionClicked(int viewID, int position) {
                        String message = "";
                        if (viewID == R.id.add) {
                            message += "Add";
                        } else if (viewID == R.id.edit) {
                            message += "Edit";
                        } else if (viewID == R.id.change) {
                            message += "Change";
                        }
                        message += " clicked for row " + (position + 1);
                        ToastUtil.makeToast(getApplicationContext(), message);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRecyclerView.addOnItemTouchListener(onTouchListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRecyclerView.removeOnItemTouchListener(onTouchListener);
    }

    private List<RowModel> getData() {
        List<RowModel> list = new ArrayList<>(25);
        for (int i = 0; i < 25; i++) {
            list.add(new RowModel("Row " + (i + 1), "Some Text... "));
        }
        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean currentState = false;
        if (item.isCheckable()) {
            currentState = item.isChecked();
            item.setChecked(!currentState);
        }
        switch (item.getItemId()) {
            case R.id.menu_swipeable:
                onTouchListener.setSwipeable(!currentState);
                return true;
            case R.id.menu_clickable:
                onTouchListener.setClickable(!currentState);
                return true;
            case R.id.menu_unclickableRows:
                showMultiSelectDialog(unclickableRows, item.getItemId());
                return true;
            case R.id.menu_unswipeableRows:
                showMultiSelectDialog(unswipeableRows, item.getItemId());
                return true;
            case R.id.menu_openOptions:
                showSingleSelectDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showMultiSelectDialog(final List<Integer> list, final int menuId) {
        boolean[] checkedItems = new boolean[25];
        for (int i = 0; i < list.size(); i++) {
            checkedItems[list.get(i)] = true;
        }

        String title = "Select {} Rows";
        if (menuId == R.id.menu_unclickableRows) title = title.replace("{}", "Unclickable");
        else title = title.replace("{}", "Unswipeable");

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMultiChoiceItems(dialogItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked)
                            list.add(which);
                        else
                            list.remove(which);
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Integer[] tempArray = new Integer[list.size()];
                        if (menuId == R.id.menu_unclickableRows)
                            onTouchListener.setUnClickableRows(list.toArray(tempArray));
                        else
                            onTouchListener.setUnSwipeableRows(list.toArray(tempArray));
                    }
                });
        builder.create().show();
    }

    private void showSingleSelectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Open Swipe Options for row: ")
                .setSingleChoiceItems(dialogItems, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openOptionsPosition = which;
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onTouchListener.openSwipeOptions(openOptionsPosition);
                    }
                });
        builder.create().show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (touchListener != null) touchListener.getTouchCoordinates(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void setOnActivityTouchListener(OnActivityTouchListener listener) {
        this.touchListener = listener;
    }

    @Override
    public void onLoadMore() {
        getData();
        mXsrRv.setLoadMore(false);
    }

    @Override
    public void onRefresh() {
        getData();
        mXsrRv.setRefreshing(false);
    }

    private class MainAdapter extends XSwipeRefreshLayout.LoadMoreRvAdapter<RowModel> implements MyItemTouchCallback.ItemTouchAdapter {
        LayoutInflater inflater;

        public MainAdapter(Context context, List<RowModel> list) {
            inflater = LayoutInflater.from(context);
            mDatas = new ArrayList<>(list);
        }

        @Override
        public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            if (TYPE_ITEM == viewType) {
                view = inflater.inflate(R.layout.recycler_row, parent, false);
            } else {
                view = inflater.inflate(R.layout.footer_view, parent, false);
            }
            return new MainViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position == mDatas.size() && showLoadMore) {
                //
            } else {
                ((MainViewHolder) holder).bindData(mDatas.get(position));
            }
        }

        @Override
        public void onMove(int fromPosition, int toPosition) {
            if (fromPosition == mDatas.size() - 1 || toPosition == mDatas.size() - 1) {
                return;
            }
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mDatas, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mDatas, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onSwiped(int position) {
            mDatas.remove(position);
            notifyItemRemoved(position);
        }

        class MainViewHolder extends RecyclerView.ViewHolder {

            TextView mainText, subText;

            public MainViewHolder(View itemView) {
                super(itemView);
                mainText = (TextView) itemView.findViewById(R.id.mainText);
                subText = (TextView) itemView.findViewById(R.id.subText);
            }

            public void bindData(RowModel rowModel) {
                mainText.setText(rowModel.getMainText());
                subText.setText(rowModel.getSubText());
            }
        }
    }
}
