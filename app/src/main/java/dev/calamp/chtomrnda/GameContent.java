package dev.calamp.chtomrnda;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class GameContent extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_content);

        WebView game = findViewById(R.id.game_view);

        game.setWebViewClient(new WebViewClient());
        game.getSettings().setJavaScriptEnabled(true);
        game.loadUrl(GlobalConfig.gameURL);
        BottomNavigationView navUI = findViewById(R.id.navBar);


        if(!GlobalConfig.navStatus)
        {
            navUI.setVisibility(View.VISIBLE);
            navUI.setOnNavigationItemSelectedListener(item -> {
                if(item.getItemId() == R.id.nav_home)
                {
                    recreate();
                }
                else if(item.getItemId() == R.id.nav_notify)
                {
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else if(item.getItemId() == R.id.nav_policy)
                {
                     game.loadUrl(GlobalConfig.policyUrl);
                }

                return true;
            });
        }
        else
        {
            navUI.setVisibility(View.GONE);
        }

    }
}