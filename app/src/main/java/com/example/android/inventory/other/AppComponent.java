package com.example.android.inventory.other;

import com.example.android.inventory.model.InventoryProvider;
import com.example.android.inventory.view.EditorActivity;
import com.example.android.inventory.view.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * A {@link Component} that is used to inject the {@link MainActivity} and the {@link EditorActivity}
 */
@Component(modules = AppModule.class)
@Singleton
public interface AppComponent {
    void inject(MainActivity activity);

    void inject(EditorActivity activity);

    void inject(InventoryProvider provider);
}
