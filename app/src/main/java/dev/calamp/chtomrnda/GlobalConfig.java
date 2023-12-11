package dev.calamp.chtomrnda;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AlertDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GlobalConfig extends Application {

    public static final String appCode = "W11";
    private static boolean hasUserConsent = false;
    public static String apiURL = "https://backend.madgamingdev.com/api/gameid";
    public static String policyUrl = "";
    public static String gameURL = "";
    public static String jsInterface = "jsBridge";
    public static String success = "";
    private static final String USER_CONSENT = "userConsent";
    public SharedPreferences sharedPref;
    public static boolean navStatus = false;
    private CryptURL urlCrypt;
    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs = getSharedPreferences(appCode, Context.MODE_PRIVATE);
        hasUserConsent = prefs.getBoolean(USER_CONSENT, false);

    }

    public void setupRemoteConfig(Context context, Activity activity, Boolean hasPolicy) {

        this.urlCrypt = new CryptURL();
        VolleyCall.init(this);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("appid", appCode);
            requestBody.put("package", this.getPackageName());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        String endPoint = apiURL + "?appid=" + appCode + "&package=" + this.getPackageName();
        Log.d("urlResult", endPoint);

        JsonObjectRequest myReq = new JsonObjectRequest(0, endPoint, requestBody, (response) -> {
            Log.e("urlResponse", "JSON:Response - " + response.toString());

            try {
                CryptURL var10000 = this.urlCrypt;
                String decryptedText = CryptURL.decrypt(response.getString("data"), "21913618CE86B5D53C7B84A75B3774CD");
                JSONObject jsonData = new JSONObject(decryptedText);
                GlobalConfig.gameURL = jsonData.getString("gameURL");
                GlobalConfig.policyUrl = jsonData.getString("policyURL");
                GlobalConfig.navStatus = Boolean.parseBoolean(jsonData.getString("gameKey"));

                Log.e("decrypURL", "Decrypted: " + decryptedText);

            } catch (Exception var6) {
                var6.printStackTrace();
            }

        }, (error) -> {
            Log.e("VolleyError", "Error: " + error.getMessage());
        });
        requestQueue.add(myReq);
    }
    public static class CryptURL {
        static char[] HEX_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        private static SecretKeySpec keySpec;
        private static Cipher cipher;
        private static final String key = "21913618CE86B5D53C7B84A75B3774CD";
        private static final String transformation = "AES/CBC/NoPadding";

        public CryptURL() {
            keySpec = new SecretKeySpec("21913618CE86B5D53C7B84A75B3774CD".getBytes(), "AES");

            try {
                cipher = Cipher.getInstance("AES/CBC/NoPadding");
            } catch (NoSuchPaddingException | NoSuchAlgorithmException var2) {
                var2.printStackTrace();
            }

        }

        public static String decrypt(String encryptedData, String secretKey) throws Exception {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] iv = new byte[16];
            System.arraycopy(encryptedBytes, 0, iv, 0, 16);
            byte[] ciphertext = new byte[encryptedBytes.length - 16];
            System.arraycopy(encryptedBytes, 16, ciphertext, 0, ciphertext.length);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(2, secretKeySpec, new IvParameterSpec(iv));
            byte[] decryptedBytes = cipher.doFinal(ciphertext);
            return (new String(decryptedBytes, StandardCharsets.UTF_8)).trim();
        }
    }

    public void checkUserConsent(Context context, Activity activity, Boolean hasPolicy) {
        setupRemoteConfig(context, activity, hasPolicy);
    }

    private void showConsentDialog(Context context, Activity activity) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_consent, null);
        WebView userConsent = dialogView.findViewById(R.id.userConsent);

        userConsent.setWebViewClient(new WebViewClient());
        userConsent.loadUrl("https://5gbapps.site/policy");

        builder.setTitle("Data Privacy Policy");
        builder.setView(dialogView);

        builder.setPositiveButton("I Agree", (dialog, which) -> {
            setConsentValue(true);
            loadActivity(activity);
        });
        builder.setNegativeButton("Don't Agree", (dialog, which) -> {
            activity.finishAffinity();
        });

        builder.show();
    }

    private void setConsentValue(boolean userChoice) {
        hasUserConsent = userChoice;
        SharedPreferences prefs = getSharedPreferences(appCode, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(USER_CONSENT, userChoice);
        editor.apply();
    }

    public Boolean getUserConsent() {
        SharedPreferences prefs = getSharedPreferences(appCode, Context.MODE_PRIVATE);
        hasUserConsent = prefs.getBoolean(USER_CONSENT, false);
        return hasUserConsent;
    }

    private void loadActivity(Activity activity) {
        Intent newActivity = new Intent(activity, GameContent.class);
        newActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(newActivity);
    }
}
