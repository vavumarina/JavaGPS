package com.example.myapplicationgps;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        setupExceptionHandler();
    }

    private void setupExceptionHandler() {
        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            // 1. Logar o erro
            Log.e("APP_CRASH", "=== CRASH REPORT ===");
            Log.e("APP_CRASH", "Thread: " + thread.getName());
            Log.e("APP_CRASH", "Exception: " + ex.toString());
            ex.printStackTrace();

            // 2. Tentar reiniciar a aplicação
            try {
                Intent restartIntent = new Intent(this, MainActivity.class);
                restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(restartIntent);
            } catch (Exception e) {
                Log.e("APP_CRASH", "Failed to restart app", e);
            }

            // 3. Chamar o handler padrão se existir
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, ex);
            }

            // 4. Finalizar o processo
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        });
    }
}