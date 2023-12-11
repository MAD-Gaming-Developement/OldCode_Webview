package dev.calamp.chtomrnda;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class GameContent extends AppCompatActivity {
    SharedPreferences appSharedPref;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_content);
        appSharedPref = getSharedPreferences(GlobalConfig.appCode, MODE_PRIVATE);

        ((GlobalConfig) getApplication()).checkUserConsent(this, this, true);
        boolean hasUserConsent = ((GlobalConfig) getApplication()).getUserConsent();

        WebView game = findViewById(R.id.game_view);

        game.setWebViewClient(new WebViewClient());
        game.getSettings().setJavaScriptEnabled(true);
        if (hasUserConsent) {
            game.loadUrl(GlobalConfig.gameURL);
        }
    }
}