package com.example.android.inventory.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.inventory.R;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.drawable.DrawableCompat;

/**
 * A custom view that looks the same as the rows from the Pets app but put into a custom view
 * to make re-usability easier and neater (and to not copy and paste a lot of code).
 */
@SuppressWarnings("unused")
public class FieldView extends LinearLayout {
    private static final int ALPHA_NOT_INITIALIZED = -1;
    private View mTitleView;
    private View mInputView;
    private int mInputChildId;
    private int mInputDrawableAlpha = ALPHA_NOT_INITIALIZED;

    public FieldView(Context context) {
        super(context);
    }

    public FieldView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.FieldView,
                0, 0);
        LayoutInflater inflater = LayoutInflater.from(context);
        mTitleView = inflater.inflate(
                a.getResourceId(R.styleable.FieldView_titleLayout, R.layout.editor_title_default),
                this,
                false);
        mInputView = inflater.inflate(
                a.getResourceId(R.styleable.FieldView_inputLayout, R.layout.editor_input_default),
                this,
                false);

        setInputChildId(a.getResourceId(R.styleable.FieldView_input_childId, 0));
        setTitleText(a.getText(R.styleable.FieldView_title_text));
        setInputHint(a.getText(R.styleable.FieldView_input_hint));
        setInputType(a.getInt(R.styleable.FieldView_android_inputType, InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS));
        setReadOnly(a.getBoolean(R.styleable.FieldView_readOnly, false));

        LinearLayout.LayoutParams titleLayoutParams = new LayoutParams(mTitleView.getLayoutParams());
        LinearLayout.LayoutParams inputLayoutParams = new LayoutParams(mInputView.getLayoutParams());
        titleLayoutParams.width = 0;
        titleLayoutParams.weight = 1;
        inputLayoutParams.width = 0;
        inputLayoutParams.weight = 2;
        mTitleView.setLayoutParams(titleLayoutParams);
        mInputView.setLayoutParams(inputLayoutParams);

        addView(mTitleView);
        addView(mInputView);

        a.recycle();
    }

    public FieldView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FieldView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public int getInputChildId() {
        return mInputChildId;
    }

    public void setInputChildId(int inputChildId) {
        mInputChildId = inputChildId;
    }

    public View getTitleView() {
        return mTitleView;
    }

    public void setTitleView(View titleView) {
        mTitleView = titleView;
        invalidate();
        requestLayout();
    }

    public View getInputView() {
        return mInputView;
    }

    public void setInputView(View inputView) {
        mInputView = inputView;
        invalidate();
        requestLayout();
    }

    public void setInputType(int inputType) {
        View inputView = mInputChildId != 0 ? mInputView.findViewById(mInputChildId) : mInputView;
        if (inputView instanceof TextView) ((TextView) (inputView)).setInputType(inputType);
    }

    public void setInputHint(CharSequence hint) {
        View inputView = mInputChildId != 0 ? mInputView.findViewById(mInputChildId) : mInputView;
        if (inputView instanceof TextView) ((TextView) inputView).setHint(hint);
    }

    public void setTitleText(CharSequence text) {
        if (mTitleView instanceof TextView) ((TextView) mTitleView).setText(text);
    }

    public boolean isReadOnly() {
        return !(mInputChildId != 0 ? mInputView.findViewById(mInputChildId) : mInputView).isFocusable();
    }

    public void setReadOnly(boolean readOnly) {
        View inputView = mInputChildId != 0 ? mInputView.findViewById(mInputChildId) : mInputView;
        inputView.setFocusable(!readOnly);
        inputView.setFocusableInTouchMode(!readOnly);
        if (inputView instanceof TextView) {
            ((TextView) inputView).setCursorVisible(!readOnly);
        }
        int newAlpha = readOnly ? 0 : mInputDrawableAlpha;
        if (readOnly || mInputDrawableAlpha == ALPHA_NOT_INITIALIZED) {
            mInputDrawableAlpha = DrawableCompat.getAlpha(inputView.getBackground());
        }
        if (newAlpha != DrawableCompat.getAlpha(inputView.getBackground())) {
            inputView.getBackground().setAlpha(newAlpha);
        }
    }
}
