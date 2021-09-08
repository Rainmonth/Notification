package com.rainmonth.notification;

import android.app.Notification;
import android.support.v4.app.NotificationCompat;

/**
 * @author RandyZhang
 * @date 2021/9/8 6:43 下午
 */
public class KdStyleWrapper extends NotificationCompat.Style {
    public Notification.Style style;

    public KdStyleWrapper(Notification.Style style) {
        this.style = style;
    }
}
