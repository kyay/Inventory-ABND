package com.example.android.inventory.other;

import android.content.ContentResolver;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.SearchRecentSuggestions;

import com.example.android.inventory.model.FirebaseDatabaseHelper;
import com.example.android.inventory.model.InventoryDbHelper;
import com.example.android.inventory.model.InventoryFirebaseDatabaseHelper;
import com.example.android.inventory.presenter.EditorPresenter;
import com.example.android.inventory.presenter.EditorPresenterImpl;
import com.example.android.inventory.presenter.MainPresenter;
import com.example.android.inventory.presenter.MainPresenterImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * A {@link Module} that provides all the dependencies that the {@link AppComponent} needs
 */
@SuppressWarnings("WeakerAccess")
@Module
class AppModule {

    private final App app;

    public AppModule(App app) {
        this.app = app;
    }

    @Provides
    @Singleton
    public App provideApp() {
        return app;
    }

    @Provides
    @Singleton
    public ContentResolver provideContentResolver(App context) {
        return context.getContentResolver();
    }

    @Provides
    @Singleton
    public SearchRecentSuggestions provideSearchRecentSuggestions(App context) {
        return new SearchRecentSuggestions(context,
                ItemsRecentSuggestionsProvider.AUTHORITY,
                ItemsRecentSuggestionsProvider.MODE);
    }

    @Provides
    @Singleton
    public MainPresenter provideMainPresenter(ContentResolver contentResolver,
                                              SearchRecentSuggestions searchRecentSuggestions) {
        return new MainPresenterImpl(contentResolver, searchRecentSuggestions);
    }

    @Provides
    @Singleton
    public EditorPresenter provideEditorPresenter(ContentResolver contentResolver) {
        return new EditorPresenterImpl(contentResolver);
    }

    @Provides
    @Singleton
    public FirebaseDatabaseHelper provideFirebaseDatabaseHelper() {
        return new InventoryFirebaseDatabaseHelper();
    }

    @Provides
    @Singleton
    public SQLiteOpenHelper provideSQLiteOpenHelper(App context) {
        return new InventoryDbHelper(context);
    }
}