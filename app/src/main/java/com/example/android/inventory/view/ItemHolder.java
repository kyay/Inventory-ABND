package com.example.android.inventory.view;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventory.R;
import com.example.android.inventory.other.GlideApp;
import com.example.android.inventory.presenter.ItemsCursorAdapter.OnItemClickListener;
import com.example.android.inventory.presenter.ItemsCursorAdapter.OnSellButtonClickListener;

import java.io.File;

import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A {@link RecyclerView.ViewHolder} that holds the views
 * of an item list item.
 */
public class ItemHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.pic_image_view)
    ImageView mPicImageView;

    @BindView(R.id.name_text_view)
    TextView mNameTextView;

    @BindView(R.id.desc_text_view)
    TextView mDescTextView;

    @BindView(R.id.price_text_view)
    TextView mPriceTextView;

    @BindView(R.id.quantity_text_view)
    TextView mQuantityTextView;

    @BindView(R.id.sell_button)
    Button mSellButton;

    @BindView(R.id.item_foreground)
    View foregroundView;

    @BindView(R.id.item_background)
    View backgroundView;

    @BindView(R.id.right_delete)
    View rightDeleteView;

    @BindView(R.id.left_delete)
    View leftDeleteView;

    private long mId;
    private OnItemClickListener mOnItemClickListener;
    private OnSellButtonClickListener mOnSellButtonClickListener;

    public ItemHolder(final View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(mId, getAdapterPosition()));
        mSellButton.setOnClickListener(v -> {
            String quantityText = mQuantityTextView.getText().toString();
            int currentQuantity = Integer.valueOf(
                    quantityText.substring(
                            quantityText.lastIndexOf(
                                    v.getContext().getString(R.string.item_quantity_separator)) + 1)
                            .trim());
            mOnSellButtonClickListener.onSellButtonClick(mId, currentQuantity);
        });
    }

    public long getId() {
        return mId;
    }

    public void bindItem(File image, String name, String description, double price, int quantity, int id,
                         OnItemClickListener onItemClickListener,
                         OnSellButtonClickListener onSellButtonClickListener) {
        GlideApp.with(itemView).load(image).placeholder(R.drawable.ic_inventory).into(mPicImageView);
        mPicImageView.getDrawable().setAlpha(255);
        mNameTextView.setText(name);
        mDescTextView.setText(description);
        mPriceTextView.setText(mPriceTextView.getContext().getString(R.string.item_price, price));
        mQuantityTextView.setText(
                mQuantityTextView.getContext().getString(R.string.item_quantity, quantity));
        mId = id;
        mOnItemClickListener = onItemClickListener;
        mOnSellButtonClickListener = onSellButtonClickListener;
    }
}
