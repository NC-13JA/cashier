package com.nc13ja.cashier;

import android.app.Application;
import android.util.Log;

public class CashierApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        CrashLogger.initialize(this);
        final Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            try {
                CrashLogger.crash("Uncaught exception on thread " + thread.getName(), error);
            } catch (Throwable loggingError) {
                Log.e("CashierDiagnostics", "Failed to write crash log", loggingError);
            }
            if (previous != null) previous.uncaughtException(thread, error);
            else android.os.Process.killProcess(android.os.Process.myPid());
        });
        CrashLogger.log("APP", "Global uncaught-exception handler installed");
    }
}
