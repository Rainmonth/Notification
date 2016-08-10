package com.rainmonth.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by RandyZhang on 16/8/9.
 */
public class BaseActivity extends AppCompatActivity {
    protected NotificationManager mNotificationManager;
    protected NotificationCompat.Builder mBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initService();
    }

    private void initService() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    protected NotificationCompat.Builder getInitNotificationBuilder() {
        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(getDefaultPendingIntent(0))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
    }

    protected void clearNotify(int notifyId) {
        mNotificationManager.cancel(notifyId);
    }

    protected void clearAllNotify() {
        mNotificationManager.cancelAll();
    }

    protected PendingIntent getDefaultPendingIntent(int flags) {

        Intent mIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://blog.csdn.net/rainmonth"));
        return PendingIntent.getActivity(this, 1, mIntent, flags);
    }
}
