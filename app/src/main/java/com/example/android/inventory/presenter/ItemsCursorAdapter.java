package com.example.android.inventory.presenter;

import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.android.inventory.R;
import com.example.android.inventory.model.InventoryContract.ItemsEntry;
import com.example.android.inventory.view.ItemHolder;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A {@link RecyclerView.Adapter}
 * that mimics a {@link android.widget.CursorAdapter} but for a {@link RecyclerView}. I consider it
 * a Presenter because it gets the data from the model and gives the View a Holder that is just
 * processed data. I got so confused when I was refactoring to MVP as to whether include it as a
 * Presenter or as a view, but I realized that it is a presenter.
 */
@SuppressWarnings("WeakerAccess")
public class ItemsCursorAdapter extends RecyclerView.Adapter<ItemHolder> {
    private Cursor mCursor;

    private OnItemClickListener mOnItemClickListener;
    private OnSellButtonClickListener mOnSellButtonClickListener;
    private ReloadCursorCallback mReloadCursorCallback;

    /**
     * Copied from the {@link android.widget.CursorAdapter code}
     */
    private ChangeObserver mChangeObserver;
    /**
     * Copied from the {@link android.widget.CursorAdapter code}
     */
    private DataSetObserver mDataSetObserver;

    private int mIdColIndex;
    private int mNameColIndex;
    private int mDescColIndex;
    private int mPriceColIndex;
    private int mQuantityColIndex;
    private int mPicColIndex;

    private long mInvisibleId = -1;

    public ItemsCursorAdapter(Cursor cursor, OnItemClickListener onItemClickListener,
                              OnSellButtonClickListener onSellButtonClickListener,
                              ReloadCursorCallback reloadCursorCallback) {
        changeCursor(cursor);
        setOnItemClickListener(onItemClickListener);
        setOnSellButtonClickListener(onSellButtonClickListener);
        setReloadCursorCallback(reloadCursorCallback);
    }

    public ItemsCursorAdapter(Cursor cursor, OnSellButtonClickListener onSellButtonClickListener,
                              ReloadCursorCallback reloadCursorCallback) {
        this(cursor, null, onSellButtonClickListener, reloadCursorCallback);
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        if (mCursor == null || mCursor.isClosed()) return;
        mCursor.moveToPosition(position);
        String picturePath = mCursor.getString(mPicColIndex);
        if (mCursor.getInt(mIdColIndex) == mInvisibleId) mCursor.moveToNext();

        holder.bindItem(
                TextUtils.isEmpty(picturePath) ? null :
                        new File(holder.itemView.getContext().getFilesDir(), picturePath),
                mCursor.getString(mNameColIndex),
                mCursor.getString(mDescColIndex),
                mCursor.getDouble(mPriceColIndex),
                mCursor.getInt(mQuantityColIndex),
                mCursor.getInt(mIdColIndex),
                mOnItemClickListener,
                mOnSellButtonClickListener);
    }

    @Override
    public int getItemCount() {
        return mCursor != null && !mCursor.isClosed() ? mCursor.getCount() - (mInvisibleId != -1 ? 1 : 0) : 0;
    }

    /**
     * Changes the {@link Cursor} that this adapter uses and closes the old Cursor
     *
     * @param newCursor the new Cursor to use for this adapter.
     */
    public void changeCursor(Cursor newCursor) {
        if (mCursor == newCursor) return;

        if (mCursor != null) {
            if (mChangeObserver != null) mCursor.unregisterContentObserver(mChangeObserver);
            if (mDataSetObserver != null) mCursor.unregisterDataSetObserver(mDataSetObserver);
            mCursor.close();
        }

        if (mChangeObserver == null) mChangeObserver = new ChangeObserver();
        if (mDataSetObserver == null) mDataSetObserver = new AdapterDataSetObserver();

        mCursor = newCursor;
        if (mCursor != null) {
            mCursor.registerContentObserver(mChangeObserver);
            mCursor.registerDataSetObserver(mDataSetObserver);

            mNameColIndex = mCursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_NAME);
            mDescColIndex = mCursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_DESCRIPTION);
            mPriceColIndex = mCursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_PRICE);
            mQuantityColIndex = mCursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_QUANTITY);
            mPicColIndex = mCursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_DATE_CREATED);
            mIdColIndex = mCursor.getColumnIndex(ItemsEntry._ID);

            notifyDataSetChanged();
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    private void setOnSellButtonClickListener(OnSellButtonClickListener onSellButtonClickListener) {
        mOnSellButtonClickListener = onSellButtonClickListener;
    }

    private void setReloadCursorCallback(ReloadCursorCallback reloadCursorCallback) {
        mReloadCursorCallback = reloadCursorCallback;
    }

    public void setItemInvisible(long id) {
        mInvisibleId = id;
    }

    public interface OnItemClickListener {
        void onItemClick(long id, int position);
    }

    public interface ReloadCursorCallback {
        void reloadCursor();
    }

    public interface OnSellButtonClickListener {
        void onSellButtonClick(long id, int currentQuantity);
    }

    /**
     * Copied from the {@link android.widget.CursorAdapter code}
     */
    private class ChangeObserver extends ContentObserver {
        ChangeObserver() {
            super(new Handler());
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            mReloadCursorCallback.reloadCursor();
        }
    }

    /**
     * Copied from the {@link android.widget.CursorAdapter code}
     */
    private class AdapterDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {

        }
    }
}
