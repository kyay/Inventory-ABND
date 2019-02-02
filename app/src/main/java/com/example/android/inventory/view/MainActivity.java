package com.example.android.inventory.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.R;
import com.example.android.inventory.other.App;
import com.example.android.inventory.other.GlideApp;
import com.example.android.inventory.presenter.MainPresenter;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MainView {
    public static final String EXTRA_ID = "EXTRA_ID";
    private static final String EXTRA_POSITION = "EXTRA_POSITION";
    private static final int REQUEST_CODE_SHOW_ITEM = 3001;
    private static final int LENGTH_LONGER = 8000;
    private final List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.PhoneBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build());
    @BindView(R.id.item_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.empty_view)
    View mEmptyView;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.empty_inventory_image)
    View mInventoryImage;
    @BindView(R.id.loading_text)
    TextView mLoadingTextView;
    @BindView(R.id.loading_progress_bar)
    View mLoadingProgressBar;

    private Snackbar mSnackbar;

    private MainPresenter mPresenter;
    private MenuItem mSearchViewItem;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SHOW_ITEM && resultCode == Activity.RESULT_OK) {
            Bundle editorExtrasBundle = data.getExtras();
            assert editorExtrasBundle != null;
            if (editorExtrasBundle.containsKey(EditorActivity.RESULT_ITEM_DELETED) &&
                    editorExtrasBundle.getBoolean(EditorActivity.RESULT_ITEM_DELETED) &&
                    editorExtrasBundle.containsKey(EXTRA_ID)) {
                final long id = editorExtrasBundle.getLong(EXTRA_ID);
                final int position = editorExtrasBundle.getInt(EXTRA_POSITION, mPresenter.getItemCount());
                showItemDeletedSnackBar(position, id);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        mPresenter.setQuery("");
                        LoaderManager.getInstance(MainActivity.this).restartLoader(0, null, mPresenter);
                        return true;
                    }
                });
                return true;
            case R.id.menu_clear_history:
                showHistoryClearConfirmationDialog();
                return true;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        mSearchViewItem = menu.findItem(R.id.menu_search);

        SearchView searchView = (SearchView) mSearchViewItem.getActionView();
        searchView.setSearchableInfo(((SearchManager) getSystemService(Context.SEARCH_SERVICE))
                .getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryRefinementEnabled(true);

        return true;
    }

    @Override
    protected void onStart() {
        mPresenter.bindView(this);
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        // If the device has a keyboard (i.e a bluetooth keyboard or an emulator)
        // then the search VIEW will appear.
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        if (getLastNonConfigurationInstance() instanceof MainPresenter)
            mPresenter = (MainPresenter) getLastNonConfigurationInstance();
        if (mPresenter == null) {
            ((App) getApplication()).getAppComponent().inject(this);
        }
        mPresenter.setOnItemClickListener((id, position) -> {
            Intent intent = new Intent(MainActivity.this, EditorActivity.class);
            intent.putExtra(MainActivity.EXTRA_ID, id);
            intent.putExtra(MainActivity.EXTRA_POSITION, position);
            startActivityForResult(intent, REQUEST_CODE_SHOW_ITEM);
        });
        mPresenter.bindView(this);
        mFab.setOnClickListener(v -> startActivityForResult(
                new Intent(MainActivity.this, EditorActivity.class),
                REQUEST_CODE_SHOW_ITEM));
        int spanCount = getResources().getInteger(R.integer.recycler_view_span_count);
        mRecyclerView.setLayoutManager(spanCount > 0 ?
                new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL) :
                new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(
                new InnerMarginItemDecorator(
                        (int) getResources().getDimension(R.dimen.preferred_margin)));
        mRecyclerView.addOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                        if (newState == RecyclerView.SCROLL_STATE_IDLE)
                            // Fix the bug when the StaggeredGridLayoutManager moves items
                            // which messes up with item decoration by invalidating Item decorations
                            // Note: this method will get called AFTER the LayoutManager and The RecyclerView
                            // perform their onScrollStateChanged, so the LayoutManager surely had already
                            // called checkForGaps.
                            recyclerView.invalidateItemDecorations();
                    }
                });
        Objects.requireNonNull(mRecyclerView.getAdapter()).registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                mRecyclerView.post(() -> mRecyclerView.invalidateItemDecorations());
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mRecyclerView.post(() -> mRecyclerView.invalidateItemDecorations());
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                mRecyclerView.post(() -> mRecyclerView.invalidateItemDecorations());
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                mRecyclerView.post(() -> mRecyclerView.invalidateItemDecorations());
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
                mRecyclerView.post(() -> mRecyclerView.invalidateItemDecorations());
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                mRecyclerView.post(() -> mRecyclerView.invalidateItemDecorations());
            }
        });

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(
                0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT,
                this::showDeleteConfirmationDialog);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);

        mNavigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.log_in_out) {
                if (mPresenter.isSignedIn()) {
                    AuthUI.getInstance()
                            .signOut(MainActivity.this)
                            .addOnCompleteListener(task -> Toast.makeText(MainActivity.this,
                                    R.string.msg_log_out,
                                    Toast.LENGTH_SHORT).show());
                } else {
                    showSignInActivity();
                }
            }
            mDrawerLayout.closeDrawers();
            return true;
        });

        // Using LoaderManager.getInstance()
        // because getSupportLoaderManager() got deprecated in API 28
        LoaderManager.getInstance(this).initLoader(0, null, mPresenter);

        if (!mPresenter.isSignedIn())
            showSignInActivity();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return mPresenter;
    }

    @Override
    protected void onStop() {
        if (mSnackbar != null) mSnackbar.dismiss();
        mPresenter.unBindView();
        super.onStop();
    }

    @SuppressLint("WrongConstant")
    private void showItemDeletedSnackBar(final int position, final long id) {
        mPresenter.hideItem(position, id);
        mSnackbar = Snackbar.make(mCoordinatorLayout,
                R.string.message_item_deleted,
                LENGTH_LONGER)
                .setAction(R.string.message_undo, v -> {

                })
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int dismissType) {
                        super.onDismissed(snackbar, dismissType);
                        if (dismissType != DISMISS_EVENT_ACTION) {
                            mPresenter.deleteItem(id);
                        } else {
                            mPresenter.showItem(position);
                        }
                    }
                });
        mSnackbar.show();
    }

    private void showSignInActivity() {
        startActivity(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build());
    }

    @Override
    public void setAccountDetails(Uri pictureUri, String name, String email) {
        View headerView = mNavigationView.getHeaderView(0);
        GlideApp
                .with(MainActivity.this)
                .load(pictureUri)
                .placeholder(R.drawable.ic_account_default)
                .into(headerView.<ImageView>findViewById(R.id.profile_image));
        headerView.<TextView>findViewById(R.id.name_text)
                .setText(name);
        headerView.<TextView>findViewById(R.id.email_text).setText(email);
        mNavigationView.getMenu().findItem(R.id.log_in_out)
                .setIcon(TextUtils.isEmpty(name) ?
                        R.drawable.ic_login :
                        R.drawable.ic_logout)
                .setTitle(TextUtils.isEmpty(name) ?
                        R.string.label_sign_in :
                        R.string.label_log_out);
    }

    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void setEmpty(boolean empty) {
        mEmptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        mRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public Loader<Cursor> createLoader(Uri uri, String[] projection, String selection,
                                       String[] selectionArgs, String sortOrder) {
        return new CursorLoader(this,
                uri,
                projection,
                selection, selectionArgs, sortOrder);
    }

    @Inject
    public void setPresenter(MainPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void reloadLoader() {
        LoaderManager.getInstance(this).restartLoader(0, null, mPresenter);
    }

    private void showHistoryClearConfirmationDialog() {
        Button positiveButton = new AlertDialog.Builder(this)
                .setMessage(R.string.msg_history_clear)
                .setPositiveButton(R.string.label_history_clear, (dialog, id) -> mPresenter.clearSearchHistory())
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> {
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

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mPresenter.setQuery(intent.getStringExtra(SearchManager.QUERY));
        }
        mPresenter.bindView(this);
        LoaderManager.getInstance(this).restartLoader(0, null, mPresenter);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void triggerSearch(String query, @Nullable Bundle appSearchData) {
        mSearchViewItem.expandActionView();
        SearchView searchView = (SearchView) mSearchViewItem.getActionView();
        searchView.setAppSearchData(appSearchData);
        searchView.setQuery(query, true);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void startSearch(@Nullable String initialQuery, boolean selectInitialQuery, @Nullable Bundle appSearchData, boolean globalSearch) {
        if (globalSearch) {
            super.startSearch(initialQuery, selectInitialQuery, appSearchData, true);
            return;
        }
        mSearchViewItem.expandActionView();
        SearchView searchView = (SearchView) mSearchViewItem.getActionView();
        searchView.setAppSearchData(appSearchData);
        searchView.setQuery(initialQuery, false);
    }

    @Override
    public void setLoading(boolean loading) {
        if (loading)
            setEmpty(true);
        mInventoryImage.setVisibility(loading ? View.GONE : View.VISIBLE);
        mLoadingTextView.setText(loading ? R.string.msg_loading : R.string.msg_empty);
        mLoadingProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showDeleteConfirmationDialog(final int itemPosition, final long itemId) {
        Button positiveButton = new AlertDialog.Builder(this)
                .setMessage(R.string.msg_item_delete)
                .setPositiveButton(R.string.label_delete,
                        (dialog, id) -> showItemDeletedSnackBar(itemPosition, itemId))
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    Objects.requireNonNull(mRecyclerView.getAdapter()).notifyItemChanged(itemPosition);
                })
                .show()
                .getButton(AlertDialog.BUTTON_POSITIVE);
        int margin = (int) getResources().getDimension(R.dimen.preferred_margin);
        MarginLayoutParamsCompat.setMarginStart((ViewGroup.MarginLayoutParams) positiveButton.getLayoutParams(), margin);
        MarginLayoutParamsCompat.setMarginEnd((ViewGroup.MarginLayoutParams) positiveButton.getLayoutParams(), margin);
    }
}
