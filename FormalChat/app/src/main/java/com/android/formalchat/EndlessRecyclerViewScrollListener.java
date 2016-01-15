package com.android.formalchat;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by Sve on 1/12/16.
 */
public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {
    private int visibleThreshold = 6;       // The minimum amount of items to have below your current scroll position before loading more.
    private int currentPage = 0;            // The current offset index of data you have loaded
    private int previousTotalItemCount = 0; // The total number of items in the dataset after the last load
    private boolean loading = true;         // True if we are still waiting for the last set of data to load.
    private int startingPageIndex = 0;      // Sets the starting page index
    private int old_dy = 0;

    private GridLayoutManager gridLayoutManager;

    public EndlessRecyclerViewScrollListener(GridLayoutManager gridLayoutManager) {
        this.gridLayoutManager = gridLayoutManager;
    }

    public void resetCounter() {
        previousTotalItemCount = 0;
        currentPage = 0;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        int lastVisibleItemPosition = gridLayoutManager.findLastVisibleItemPosition();
        int totalItemCount = gridLayoutManager.getItemCount();

        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        if (totalItemCount < previousTotalItemCount) {
            this.currentPage = this.startingPageIndex;
            this.previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0) {
                this.loading = true;
            }
        }

        // If it’s still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count.
        if (loading && (totalItemCount > previousTotalItemCount)) {
            loading = false;
            previousTotalItemCount = totalItemCount;
        }

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        if (!loading && (lastVisibleItemPosition + MainActivity.DISPLAY_LIMIT) >= totalItemCount) {
            currentPage++;
            Log.e(ApplicationOfficially.TAG, "### currentPage AFTER  === " + currentPage);
            onLoadMore(currentPage, totalItemCount);
            loading = true;
        }


        if(dy > 0) {
            // Scrolling Up
            onScrolledUp(dy, true);
        }
        else if (dy < 0){
            // Scrolling Down
            onScrolledUp(dy, false);
        }

        old_dy = dy;
    }

    // Defines the process for actually loading more data based on page
    public abstract void onLoadMore(int page, int totalItemsCount);

    // Defines what to do when recyclerview has been scrolled up/down
    public abstract void onScrolledUp(int dy, boolean isUp);
}
