package com.example.android.inventory.view;

import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A {@link ItemTouchHelper.SimpleCallback} class that deletes an item on swipe
 */
@SuppressWarnings("WeakerAccess")
class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {
    private final OnItemDeletedListener listener;

    public RecyclerItemTouchHelper(int dragDirs, int swipeDirs, OnItemDeletedListener listener) {
        super(dragDirs, swipeDirs);
        this.listener = listener;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {
            getDefaultUIUtil().onSelected(((ItemHolder) viewHolder).foregroundView);
        }
    }

    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
        getDefaultUIUtil().onDrawOver(c, recyclerView, ((ItemHolder) viewHolder).foregroundView, dX,
                dY, actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        final View foregroundView = ((ItemHolder) viewHolder).foregroundView;
        getDefaultUIUtil().clearView(foregroundView);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        ((ItemHolder) viewHolder).backgroundView.setVisibility(View.VISIBLE);
        if (dX > 0) {
            ((ItemHolder) viewHolder).rightDeleteView.setVisibility(View.GONE);
            ((ItemHolder) viewHolder).leftDeleteView.setVisibility(View.VISIBLE);
        } else if (dX < 0) {
            ((ItemHolder) viewHolder).rightDeleteView.setVisibility(View.VISIBLE);
            ((ItemHolder) viewHolder).leftDeleteView.setVisibility(View.GONE);
        } else {
            ((ItemHolder) viewHolder).backgroundView.setVisibility(View.GONE);
        }
        getDefaultUIUtil().onDraw(c, recyclerView, ((ItemHolder) viewHolder).foregroundView, dX, dY,
                actionState, isCurrentlyActive);
        getDefaultUIUtil().onDraw(c, recyclerView, ((ItemHolder) viewHolder).backgroundView, 0, 0,
                actionState, isCurrentlyActive);
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onItemDeleted(viewHolder.getAdapterPosition(), ((ItemHolder) viewHolder).getId());
    }

    public interface OnItemDeletedListener {
        void onItemDeleted(int position, long id);
    }
}
