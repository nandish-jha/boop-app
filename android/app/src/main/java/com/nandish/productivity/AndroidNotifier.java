package com.nandish.productivity;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.webkit.JavascriptInterface;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class AndroidNotifier {
    public static final String CHANNEL_ID = "prodash_default";
    private final Activity activity;

    public AndroidNotifier(Activity a) {
        this.activity = a;
        ensureChannel();
    }

    private void ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "ProDash", NotificationManager.IMPORTANCE_DEFAULT);
            ch.setDescription("Reminders and nudges from ProDash");
            NotificationManager nm = activity.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    @JavascriptInterface
    public boolean isSupported() { return true; }

    @JavascriptInterface
    public boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    @JavascriptInterface
    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission()) {
            activity.runOnUiThread(() -> ActivityCompat.requestPermissions(
                    activity, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001));
        }
    }

    @JavascriptInterface
    public void notify(String title, String body) {
        if (!hasPermission()) return;
        Context ctx = activity.getApplicationContext();
        Intent launch = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        PendingIntent pi = null;
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags |= PendingIntent.FLAG_IMMUTABLE;
            pi = PendingIntent.getActivity(ctx, 0, launch, flags);
        }
        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title == null ? "ProDash" : title)
                .setContentText(body == null ? "" : body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if (pi != null) b.setContentIntent(pi);
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify((int) (System.currentTimeMillis() & 0x7fffffff), b.build());
    }
}
