package com.rainmonth.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;

/**
 * @author RandyZhang
 * @date 2021/9/8 6:42 下午
 */
public class KdActionWrapper extends NotificationCompat.Action {
    public Notification.Action action;

    public KdActionWrapper(int icon, CharSequence title, PendingIntent intent, Notification.Action action) {
        super(icon, title, intent);
        this.action = action;
    }
}
