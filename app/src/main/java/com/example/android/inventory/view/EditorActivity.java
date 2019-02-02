package com.example.android.inventory.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.example.android.inventory.R;
import com.example.android.inventory.other.App;
import com.example.android.inventory.other.GlideApp;
import com.example.android.inventory.presenter.EditorPresenter;
import com.example.android.inventory.presenter.InsertUpdateService;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.Objects;

import javax.inject.Inject;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.core.view.ViewCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.palette.graphics.Palette;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import butterknife.BindView;
import butterknife.ButterKnife;

// Using the new androidx library because it is the only library that is gonna update

public class EditorActivity extends AppCompatActivity implements EditorView {
    public static final String RESULT_ITEM_DELETED = "RESULT_ITEM_DELETED";
    private static final String SAVED_STATE_IS_EDITING = "SAVED_STATE_IS_EDITING";
    private static final int REQUEST_CODE_PICK_CONTACT = 1001;
    private static final int REQUEST_CODE_PICK_IMAGE = 2001;
    private static final int JOB_ID_INSERT_OR_UPDATE = 5001;
    @BindView(R.id.name_field)
    FieldView mNameFieldView;
    @BindView(R.id.quantity_field)
    FieldView mQuantityFieldView;
    @BindView(R.id.supplier_field)
    FieldView mSupplierFieldView;
    @BindView(R.id.supplier_no_field)
    FieldView mSupplierNoFieldView;
    @BindView(R.id.price_field)
    FieldView mPriceFieldView;
    @BindView(R.id.desc_field)
    FieldView mDescFieldView;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.real_toolbar)
    Toolbar mRealToolbar;
    @BindView(R.id.dummy_toolbar)
    Toolbar mDummyToolbar;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout mToolbarLayout;
    @BindView(R.id.app_bar)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.image_id)
    ImageView mImageView;
    @BindView(R.id.add_pic_button)
    TextView mAddPicButton;
    @BindView(R.id.import_export_button)
    TextView mImportExportButton;
    @BindView(R.id.order_button)
    View mOrderButton;
    private int mShortAnimationDuration;

    private int mTopComplementaryColor;
    private int mBottomComplementaryColor;
    private EditorPresenter mPresenter;

    private static int getComplementaryColor(int colorToInvert) {
        int range = 20;
        int result;
        int red = Color.red(colorToInvert);
        int green = Color.green(colorToInvert);
        int blue = Color.blue(colorToInvert);
        // First use an H 180 rotation
        float[] hsv = new float[3];
        Color.RGBToHSV(red, green,
                blue, hsv);
        hsv[0] = (hsv[0] + 180) % 360;
        result = Color.HSVToColor(hsv);
        int newRed = Color.red(result);
        int newGreen = Color.green(result);
        int newBlue = Color.blue(result);
        // If it didn't work, reverse the colors.
        if ((newRed + range >= red && red >= newRed - range) ||
                (newGreen + range >= green && green >= newGreen - range) ||
                (newBlue + range >= blue && blue >= newBlue - range)) {
            result = Color.rgb(255 - red, 255 - green, 255 - blue);
            newRed = Color.red(result);
            newGreen = Color.green(result);
            newBlue = Color.blue(result);
        }
        // As a last resort, use the Y value of the color to decide whether to use black or white.
        if ((newRed + range >= red && red >= newRed - range) ||
                (newGreen + range >= green && green >= newGreen - range) ||
                (newBlue + range >= blue && blue >= newBlue - range)) {
            result = (299 * red + 587 * green + 114 * blue) / 1000 >= 128 ?
                    Color.BLACK :
                    Color.WHITE;
        }
        return result;
    }

    private static int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.6f;
        return Color.HSVToColor(hsv);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_STATE_IS_EDITING, !mNameFieldView.isReadOnly());
    }

    @Override
    public void setQuantity(int quantity) {
        getInputViewFromFieldView(mQuantityFieldView).setText(String.valueOf(quantity));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);
        if (getLastNonConfigurationInstance() instanceof EditorPresenter)
            mPresenter = (EditorPresenter) getLastNonConfigurationInstance();
        if (mPresenter == null) {
            ((App) getApplication()).getAppComponent().inject(this);
        }
        mPresenter.bindView(this);
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        mTopComplementaryColor = getResources().getColor(android.R.color.white);
        mBottomComplementaryColor = getResources().getColor(android.R.color.white);
        mRealToolbar.setBackground(mRealToolbar.getBackground().mutate());
        setSupportActionBar(mRealToolbar);
        mAppBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            verticalOffset = -verticalOffset;
            int difference = appBarLayout.getTotalScrollRange() - mRealToolbar.getHeight();
            if (verticalOffset >= difference) {
                float flexibleSpace = appBarLayout.getTotalScrollRange() - verticalOffset;
                float ratio = 1 - (flexibleSpace / mRealToolbar.getHeight());
                Objects.requireNonNull(getSupportActionBar()).setElevation(ratio * ViewCompat.getElevation(appBarLayout));
            }
            double ratio = 1 - (double) verticalOffset / appBarLayout.getTotalScrollRange();
            mImageView.getDrawable().setAlpha(
                    (int) (255 * (ratio)));
            int newBottomColor = ColorUtils.blendARGB(
                    getResources().getColor(android.R.color.white),
                    mBottomComplementaryColor,
                    (float) (ratio));
            ToolbarColorizeHelper.colorizeToolbar(mRealToolbar, ColorUtils.blendARGB(
                    getResources().getColor(android.R.color.white),
                    mTopComplementaryColor,
                    (float) (ratio)));
            mToolbarLayout.setExpandedTitleColor(mBottomComplementaryColor);
            mAddPicButton.setTextColor(newBottomColor);
            PorterDuffColorFilter bottomColorFilter =
                    new PorterDuffColorFilter(
                            darkenColor(newBottomColor),
                            PorterDuff.Mode.MULTIPLY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mAddPicButton.getCompoundDrawablesRelative()[0].setColorFilter(bottomColorFilter);
            } else {
                mAddPicButton.getCompoundDrawables()[0].setColorFilter(bottomColorFilter);
            }
            mAddPicButton.getBackground().setColorFilter(new PorterDuffColorFilter(
                    darkenColor(getComplementaryColor(newBottomColor)),
                    PorterDuff.Mode.MULTIPLY));
        });
        mDummyToolbar.setNavigationIcon(android.R.color.transparent);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mOrderButton.setOnClickListener(v -> startActivity(new Intent(
                Intent.ACTION_DIAL, Uri.parse("tel:" +
                getInputViewFromFieldView(mSupplierNoFieldView).getText()))));
        mImageView.setOnClickListener(v -> {
            if (!mNameFieldView.isReadOnly()) {
                startActivityForResult(
                        new Intent(Intent.ACTION_PICK, Media.INTERNAL_CONTENT_URI),
                        REQUEST_CODE_PICK_IMAGE);
            }
        });
        mImportExportButton.setOnClickListener(v -> {
            if (mNameFieldView.isReadOnly()) {
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setType(ContactsContract.Contacts.CONTENT_TYPE);

                intent.putExtra(ContactsContract.Intents.Insert.NAME,
                        getInputViewFromFieldView(mSupplierFieldView).getText().toString());
                intent.putExtra(ContactsContract.Intents.Insert.PHONE,
                        getInputViewFromFieldView(mSupplierNoFieldView).getText());

                startActivity(intent);
            } else {
                startActivityForResult(
                        new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI),
                        REQUEST_CODE_PICK_CONTACT);
            }
        });
        View.OnClickListener quantityButtonsOnClickListener = v -> {
            int newQuantity;
            try {
                newQuantity = Integer.valueOf(
                        getInputViewFromFieldView(mQuantityFieldView)
                                .getText().toString()
                                .trim())
                        + (((TextView) v).getText() == getString(R.string.quantity_plus) ? 1 : -1);
            } catch (NumberFormatException e) {
                newQuantity = ((TextView) v).getText() == getString(R.string.quantity_plus) ? 1 : -1;
            }

            mPresenter.setQuantity(newQuantity);
        };
        mQuantityFieldView.getInputView().findViewById(R.id.quantity_increment)
                .setOnClickListener(quantityButtonsOnClickListener);
        mQuantityFieldView.getInputView().findViewById(R.id.quantity_decrement)
                .setOnClickListener(quantityButtonsOnClickListener);
        mFab.setOnClickListener(v -> {
            if (!mNameFieldView.isReadOnly()) {
                FieldView incorrectFieldView = getIncorrectFieldView();
                if (incorrectFieldView != null) {
                    incorrectFieldView.getInputView().requestFocus();
                    Toast.makeText(
                            EditorActivity.this,
                            getString(R.string.message_provide_value),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                InsertUpdateService.enqueueWork(
                        EditorActivity.this,
                        InsertUpdateService.class,
                        JOB_ID_INSERT_OR_UPDATE,
                        mPresenter.getServiceIntent(
                                getInputViewFromFieldView(mNameFieldView).getText().toString(),
                                Long.valueOf(getInputViewFromFieldView(mQuantityFieldView)
                                        .getText().toString()),
                                getInputViewFromFieldView(mSupplierFieldView)
                                        .getText().toString(),
                                getInputViewFromFieldView(mSupplierNoFieldView)
                                        .getText().toString(),
                                Double.valueOf(
                                        getInputViewFromFieldView(mPriceFieldView)
                                                .getText().toString()),
                                getInputViewFromFieldView(mDescFieldView).getText().toString(),
                                mImageView.getDrawable()));
            }
            setEditing(mNameFieldView.isReadOnly(), true);
        });
        Bundle intentBundle = getIntent().getExtras();
        boolean isEditing = true;
        if (intentBundle != null && intentBundle.containsKey(MainActivity.EXTRA_ID)) {
            mPresenter.setId(intentBundle.getLong(MainActivity.EXTRA_ID));
            // Using LoaderManager.getInstance()
            // because getSupportLoaderManager() got deprecated in API 28
            if (LoaderManager.getInstance(this).getLoader(0) != null) {
                LoaderManager.getInstance(this).destroyLoader(0);
            }
            LoaderManager.getInstance(this).restartLoader(0, null, mPresenter);
            isEditing = false;
        }
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_STATE_IS_EDITING)) {
            isEditing = savedInstanceState.getBoolean(SAVED_STATE_IS_EDITING);
        }
        setEditing(isEditing, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    protected void onStop() {
        mPresenter.unBindView();
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mNameFieldView.isReadOnly()) {
                    showDiscardDialog();
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mNameFieldView.isReadOnly()) showDiscardDialog();
        else super.onBackPressed();
    }

    private void setEditing(boolean isEditing, boolean animate) {
        mNameFieldView.setReadOnly(!isEditing);
        mQuantityFieldView.setReadOnly(!isEditing);
        mSupplierFieldView.setReadOnly(!isEditing);
        mSupplierNoFieldView.setReadOnly(!isEditing);
        mPriceFieldView.setReadOnly(!isEditing);
        mDescFieldView.setReadOnly(!isEditing);
        mOrderButton.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        mImportExportButton.setText(isEditing ? R.string.button_import : R.string.button_export);
        if (animate) {
            // Mutate the AnimatedVectorDrawableCompat to prevent a bug where the drawable will show
            // at its end state if the animation was played before and then set on another view.
            mFab.setImageDrawable(
                    Objects.requireNonNull(AnimatedVectorDrawableCompat.create(this,
                            isEditing ? R.drawable.avd_anim_edit_to_checkmark :
                                    R.drawable.avd_anim_checkmark_to_edit))
                            .mutate());
            ((Animatable) mFab.getDrawable()).start();
        } else {
            mFab.setImageResource(isEditing ? R.drawable.avd_anim_checkmark_to_edit : R.drawable.avd_anim_edit_to_checkmark);
        }
        if (isEditing) {
            getInputViewFromFieldView(mNameFieldView).requestFocus();
            mAddPicButton.setAlpha(0);
            mAddPicButton.setVisibility(View.VISIBLE);
            mAddPicButton.setTranslationX(mAddPicButton.getWidth());
        }
        mAddPicButton
                .animate()
                .alpha(isEditing ? 1 : 0)
                .translationX(isEditing ? 0 : mAddPicButton.getWidth())
                .setDuration(mShortAnimationDuration)
                .setListener(isEditing ? null : new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mAddPicButton.setVisibility(View.GONE);
                    }
                });
        mDummyToolbar.setTitle(mPresenter.getTitle(isEditing));
        mToolbarLayout.setTitle(mDummyToolbar.getTitle());
    }

    private FieldView getIncorrectFieldView() {
        if (TextUtils.isEmpty(getInputViewFromFieldView(mNameFieldView).getText())) {
            return mNameFieldView;
        }
        if (TextUtils.isEmpty(getInputViewFromFieldView(mQuantityFieldView).getText())) {
            return mQuantityFieldView;
        }
        if (TextUtils.isEmpty(getInputViewFromFieldView(mSupplierFieldView).getText())) {
            return mSupplierFieldView;
        }
        if (TextUtils.isEmpty(getInputViewFromFieldView(mSupplierNoFieldView).getText())) {
            return mSupplierNoFieldView;
        }
        if (TextUtils.isEmpty(getInputViewFromFieldView(mPriceFieldView).getText())) {
            return mPriceFieldView;
        }
        if (TextUtils.isEmpty(getInputViewFromFieldView(mDescFieldView).getText())) {
            return mDescFieldView;
        }
        return null;
    }

    private TextView getInputViewFromFieldView(FieldView fieldView) {
        return fieldView.getInputChildId() != 0 ?
                (TextView) fieldView.getInputView().findViewById(fieldView.getInputChildId()) :
                (TextView) fieldView.getInputView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = data.getData();
            mPresenter.setImageUri(imageUri);
            GlideApp.with(this).asBitmap().load(imageUri)
                    .placeholder(R.drawable.ic_inventory).into(new BitmapImageViewTarget(mImageView) {
                @Override
                protected void setResource(Bitmap resource) {
                    if (resource == null)
                        super.setResource(null);
                    else setImageViewBitmap(resource);
                }
            });
        }

        if (requestCode == REQUEST_CODE_PICK_CONTACT && resultCode == RESULT_OK) {
            // Get the URI and query the content provider for the phone number
            Uri contactUri = data.getData();
            String[] projection = new String[]{
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
            assert contactUri != null;
            Cursor cursor = getContentResolver().query(contactUri, projection,
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                getInputViewFromFieldView(mSupplierFieldView).setText(
                        cursor.getString(
                                cursor.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                getInputViewFromFieldView(mSupplierNoFieldView).setText(
                        cursor.getString(
                                cursor.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.NUMBER)));
                cursor.close();
            }
        }
    }

    private void setImageViewBitmap(Bitmap newBitmap) {
        mImageView.setImageBitmap(newBitmap);
        mAddPicButton.setText(R.string.item_picture_edit);
        Palette.Builder paletteBuilder = new Palette.Builder(newBitmap);
        // Remove the default filter that removes colors like white.
        paletteBuilder.clearFilters();
        int toolbarHeight = mRealToolbar.getHeight();
        if (toolbarHeight == 0) {
            final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                    new int[]{R.attr.actionBarSize});
            toolbarHeight = (int) styledAttributes.getDimension(0, 0);
            styledAttributes.recycle();
        }
        // Grab the dominant color from the bitmap
        paletteBuilder.setRegion(0, 0, newBitmap.getWidth(), toolbarHeight);
        Palette topPalette = paletteBuilder.generate();
        paletteBuilder.setRegion(0, newBitmap.getHeight() - toolbarHeight, newBitmap.getWidth(), newBitmap.getHeight());
        Palette bottomPalette = paletteBuilder.generate();
        mTopComplementaryColor = getComplementaryColor(
                topPalette.getDominantColor(getResources().getColor(android.R.color.black)));
        mBottomComplementaryColor = getComplementaryColor(
                bottomPalette.getDominantColor(getResources().getColor(android.R.color.black)));
    }

    private void showDiscardDialog() {
        Button positiveButton = new AlertDialog.Builder(this)
                .setMessage(R.string.msg_discard)
                .setPositiveButton(R.string.button_discard, (dialog, which) -> finish())
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                })
                .show()
                .getButton(Dialog.BUTTON_POSITIVE);
        int margin = (int) getResources().getDimension(R.dimen.preferred_margin);
        MarginLayoutParamsCompat.setMarginStart((ViewGroup.MarginLayoutParams) positiveButton.getLayoutParams(), margin);
        MarginLayoutParamsCompat.setMarginEnd((ViewGroup.MarginLayoutParams) positiveButton.getLayoutParams(), margin);
    }

    @Override
    public void updateUi(String name, int quantity, String supplierName,
                         String supplierNumber, double price, String description,
                         @StringRes int pictureButtonStringResource, File pictureFile) {
        getInputViewFromFieldView(mNameFieldView).setText(name);
        getInputViewFromFieldView(mNameFieldView).append("");
        getInputViewFromFieldView(mQuantityFieldView).setText(String.valueOf(quantity));
        getInputViewFromFieldView(mQuantityFieldView).append("");
        getInputViewFromFieldView(mSupplierFieldView).setText(supplierName);
        getInputViewFromFieldView(mSupplierFieldView).append("");
        getInputViewFromFieldView(mSupplierNoFieldView).setText(supplierNumber);
        getInputViewFromFieldView(mSupplierNoFieldView).append("");
        getInputViewFromFieldView(mPriceFieldView).setText(
                String.valueOf(price));
        getInputViewFromFieldView(mPriceFieldView).append("");
        getInputViewFromFieldView(mDescFieldView).setText(description);
        getInputViewFromFieldView(mDescFieldView).append("");
        GlideApp.with(this)
                .asBitmap()
                .load(pictureFile)
                .placeholder(R.drawable.ic_inventory)
                .into(new BitmapImageViewTarget(mImageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        if (resource == null)
                            super.setResource(null);
                        else {
                            mPresenter.setOriginalBitmap(resource);
                            setImageViewBitmap(resource);
                        }
                    }
                });
        mAddPicButton.setText(pictureButtonStringResource);
    }

    @Override
    public File getFilesDirectory() {
        return getFilesDir();
    }

    @Override
    public Loader<Cursor> createLoader(Uri uri, String[] projection) {
        return new CursorLoader(this, uri, projection,
                null, null, null);
    }

    @Override
    public LocalBroadcastManager getLocalBroadcastManager() {
        return LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void showToast(@StringRes int msgResId, int duration) {
        Toast.makeText(this, msgResId, duration).show();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return mPresenter;
    }

    @Override
    public boolean isEditing() {
        return !mNameFieldView.isReadOnly();
    }

    @Inject
    public void setPresenter(EditorPresenter presenter) {
        mPresenter = presenter;
    }


    private void showDeleteConfirmationDialog() {
        Button positiveButton = new AlertDialog.Builder(this)
                .setMessage(R.string.msg_item_delete)
                .setPositiveButton(R.string.label_delete,
                        (dialog, id) -> {
                            getIntent().putExtra(RESULT_ITEM_DELETED, true);
                            getIntent().putExtra(MainActivity.EXTRA_ID, mPresenter.getId());
                            setResult(RESULT_OK, getIntent());
                            finish();
                        })
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> {
                    // User clicked the "Cancel" button, so dismiss the dialog
                    // and continue editing the pet.
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                })
                .show()
                .getButton(AlertDialog.BUTTON_POSITIVE);
        int margin = (int) getResources().getDimension(R.dimen.preferred_margin);
        MarginLayoutParamsCompat.setMarginStart((ViewGroup.MarginLayoutParams) positiveButton.getLayoutParams(), margin);
        MarginLayoutParamsCompat.setMarginEnd((ViewGroup.MarginLayoutParams) positiveButton.getLayoutParams(), margin);
    }
}
