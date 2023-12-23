package dev.calamp.chtomrnda;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

public class GlobalConfig extends Application {
    public static final String appCode = "W11";
    public static final String apiURL = "https://backend.madgamingdev.com/api/gameid";
    public static final String policyUrl = "https://docs.google.com/document/d/e/2PACX-1vRua8bNREM6Ov-zjcuAd9XWO_d-JzrHSTb9FOd5OdJ56RaRhWpK6he8LlHAWO8rIKxbLO-xvWfzNOfw/pub";
    public static String gameURL = "";
    public SharedPreferences sharedPref;
    public static boolean navStatus = false;
    private MCrypt mCrypt;
    @Override
    public void onCreate() {
        super.onCreate();
        setupURL();


        PusherOptions options = new PusherOptions();
        options.setCluster("ap1");

        Pusher pusher = new Pusher("fdcb398aff6445bc7bd6", options);

        pusher.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                Log.i("Pusher", "State changed from " + change.getPreviousState() +
                        " to " + change.getCurrentState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                Log.i("Pusher", "There was a problem connecting! " +
                        "\ncode: " + code +
                        "\nmessage: " + message +
                        "\nException: " + e
                );
            }
        }, ConnectionState.ALL);

        Channel channel = pusher.subscribe(getPackageName());

        channel.bind("my-event", event -> {
            try {
                JSONObject notifyMsg = new JSONObject(event.getData());

                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                if (!notificationManager.areNotificationsEnabled()) {
                    // Notifications are disabled, guide the user to enable them
                    // You can also open the app settings page for notifications
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    showNotification("Announcement", notifyMsg.getString("message"), notifyMsg.getString("url"));
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }


        });
    }
    private void showNotification(String title, String message, String link) {

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);
        remoteViews.setTextViewText(R.id.notificationTitle, title);
        remoteViews.setTextViewText(R.id.notificationMessage, message);

        // Create an intent to open the link when the button is clicked
        Intent openLinkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openLinkIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        remoteViews.setOnClickPendingIntent(R.id.openLinkButton, pendingIntent);

        NotificationChannel channel = new NotificationChannel("my-channel", "Announcements", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "my-channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteViews)
                .setAutoCancel(true);

        NotificationManagerCompat notificationMg = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationMg.notify(1, builder.build());

    }
    public static class MCrypt {
        static char[] HEX_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        private static SecretKeySpec keySpec;
        private static Cipher cipher;
        private static final String key = "21913618CE86B5D53C7B84A75B3774CD";
        private static final String transformation = "AES/CBC/NoPadding";

        public MCrypt() {
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

    public void setupURL() {
        this.mCrypt = new MCrypt();
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
            Log.e("MGD-DevTools", "JSON:Response - " + response.toString());

            try {
                MCrypt var10000 = this.mCrypt;
                String decryptedText = MCrypt.decrypt(response.getString("data"), "21913618CE86B5D53C7B84A75B3774CD");
                Log.e("MGD-DevTools", "Decrypted: " + decryptedText);
                JSONObject jsonData = new JSONObject(decryptedText);
                gameURL = jsonData.getString("gameURL");
                navStatus = Boolean.parseBoolean(jsonData.getString("gameKey"));
            } catch (Exception var6) {
                var6.printStackTrace();
            }
        }, error ->{
            // Handle the error here
            Log.e("VolleyError", "Error: " + error.getMessage());
        });
        requestQueue.add(myReq);
    }
}
