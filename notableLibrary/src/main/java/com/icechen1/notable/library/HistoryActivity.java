package com.icechen1.notable.library;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.icechen1.notable.library.utils.NotificationAdapter;
import com.icechen1.notable.library.utils.NotificationDataSource;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private NotificationAdapter mAdapter;
    private NotificationDataSource mDb;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.refreshCursor(mDb.query());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false)){
            setTheme(R.style.AppThemeDark);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        int mScrollPosition = 0;
        if (savedInstanceState != null) {
            //restore state
            mScrollPosition = savedInstanceState.getInt("currentIndex", 0);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = setRecyclerViewLayoutManager(mRecyclerView);

        mDb = new NotificationDataSource(this);
        mDb.open();
        Cursor c = mDb.query();
        if(c.getCount() > 0){
            //hide placeholder
            findViewById(R.id.emptyView).setVisibility(View.GONE);
        }
        mAdapter = new NotificationAdapter(this, c);
        mRecyclerView.setAdapter(mAdapter);

        //scroll to saved position
        int count = mLayoutManager.getChildCount();
        if (mScrollPosition != RecyclerView.NO_POSITION && mScrollPosition < count) {
            mLayoutManager.scrollToPosition(mScrollPosition);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onResume(){
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.icechen1.notable.DATABASE_CHANGED");
        registerReceiver(receiver, filter);
        setUpSwipeToDismiss();
    }

    public void onStop(){
        super.onStop();
        mDb.close();
        unregisterReceiver(receiver);
    }

    public void setUpSwipeToDismiss(){
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof NotificationAdapter.ViewHolder &&
                        !((NotificationAdapter.ViewHolder) viewHolder).mDismissed){
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Remove item
                final boolean[] undone = {false};
                if (viewHolder instanceof NotificationAdapter.ViewHolder){
                    final int id = ((NotificationAdapter.ViewHolder) viewHolder).mId;
                    Snackbar.make(viewHolder.itemView, getResources().getString(R.string.deleted_item), Snackbar.LENGTH_LONG)
                        .setAction(getResources().getString(R.string.undo), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // undo
                                undone[0] = true;
                                mAdapter.notifyDataSetChanged();
                            }
                        })
                        .setCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                if (!undone[0]) {
                                    mDb.deleteItem(id);
                                    mAdapter.refreshCursor(mDb.query());
                                    mAdapter.notifyDataSetChanged();
                                }
                                super.onDismissed(snackbar, event);
                            }
                        })
                        .show();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    /**
     * Set RecyclerView's LayoutManager
     */
    public LinearLayoutManager setRecyclerViewLayoutManager(RecyclerView recyclerView){
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (recyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.scrollToPosition(scrollPosition);
        return linearLayoutManager;
    }
}
