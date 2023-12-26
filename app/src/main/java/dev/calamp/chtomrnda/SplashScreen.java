package dev.calamp.chtomrnda;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
//        ((GlobalConfig) getApplication()).checkUserConsent(this, this, true);
//        hasUserConsent = ((GlobalConfig) getApplication()).getUserConsent();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent webApp = new Intent(this, GameContent.class);
                webApp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(webApp);
                finish();
            }, 1800);
    }
}