package com.codecollab.client.ui;

import com.codecollab.client.config.AppState;
import javafx.concurrent.Task;

/**
 * Base class for all FXML controllers.
 * Provides helpers for background tasks and shared state access.
 */
public abstract class BaseController {

    protected AppState state() { return AppState.getInstance(); }

    protected SceneRouter router() { return state().getRouter(); }

    /**
     * Runs {@code work} on a background thread, then calls {@code onSuccess}
     * or {@code onError} on the JavaFX thread.
     */
    protected <T> void runAsync(ThrowingSupplier<T> work,
                                 java.util.function.Consumer<T> onSuccess,
                                 java.util.function.Consumer<Throwable> onError) {
        Task<T> task = new Task<>() {
            @Override protected T call() throws Exception { return work.get(); }
        };
        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed  (e -> onError.accept(task.getException()));
        new Thread(task, "api-thread").start();
    }

    @FunctionalInterface
    protected interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
