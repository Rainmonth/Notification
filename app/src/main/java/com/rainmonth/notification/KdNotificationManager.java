package com.rainmonth.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

/**
 * @author RandyZhang
 * @date 2021/9/7 4:51 下午
 */
public class KdNotificationManager {
    public static final String TAG = "KdNotificationManager";

    private static volatile KdNotificationManager sInstance;

    private final NotificationManager mNotificationMgr;

    private KdNotificationManager(Context context) {
        mNotificationMgr = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static KdNotificationManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (KdNotificationManager.class) {
                if (sInstance == null) {
                    sInstance = new KdNotificationManager(context);
                }
            }
        }
        return sInstance;
    }

    /**
     * 创建通知对象
     *
     * @param builder instance of {@link NotificationCompat.Builder}
     * @return notification instance
     */
    public Notification makeNotification(NotificationCompat.Builder builder) {
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        return notification;
    }

    /**
     * 创建通知对象
     *
     * @param context context
     * @param config  通知配置实例
     * @return notification instance
     */
    public Notification makeNotification(Context context, KdNotifyConfig config) {
        int sdkInt = Build.VERSION.SDK_INT;
        NotificationCompat.Builder builder = config.toRealBuilder(context);
        Notification notification;
        if (sdkInt >= Build.VERSION_CODES.O) { // Api>=26
            notification = makeNotification(builder);
        } else if (sdkInt >= Build.VERSION_CODES.LOLLIPOP_MR1) { // Api>=22
            notification = makeNotification(builder);
        } else if (sdkInt >= Build.VERSION_CODES.JELLY_BEAN) { // Api>=16
            notification = makeNotification(builder);
        } else {
            notification = makeNotificationBelowV16(config);
        }
        return notification;
    }

    /**
     * 创建通知（API<16)
     *
     * @param config 通知配置实例
     * @return Notification instance
     */
    private Notification makeNotificationBelowV16(KdNotifyConfig config) {
        return makeNotificationBelowV16(config.mPendingIntent, config.mRemoteViews,
                config.mSmallIcon, config.mContentText, config.mPriority);
    }

    private Notification makeNotificationBelowV16(PendingIntent pendingIntent,
                                                  RemoteViews remoteViews,
                                                  int iconResId, CharSequence contentText,
                                                  int importance) {
        Notification notification;
        notification = new Notification();
        notification.contentView = remoteViews;
        notification.contentIntent = pendingIntent;
        notification.tickerText = contentText;
        notification.priority = importance;
        notification.icon = iconResId;
        notification.when = System.currentTimeMillis();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        return notification;
    }

    public void openNotificationSetting(Context context, NotificationChannel channel) {
        if (isAboveO()) {
            openChannelSetting(context, channel);
        } else {

        }
    }


    /**
     * 是否Android O 以上版本
     *
     * @return true if is above Api 26(include api = 26)
     */
    public boolean isAboveO() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    /**
     * Android Api >= 26 打开应用对应的通知设置页码
     *
     * @param context context instance
     * @param channel 通知渠道
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void openChannelSetting(Context context, NotificationChannel channel) {
        openChannelSetting(context, channel.getId());
    }

    /**
     * Android Api >= 26 打开应用对应的通知设置页码
     *
     * @param context   context instance
     * @param channelId 通知渠道id
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void openChannelSetting(Context context, String channelId) {
        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelId);
        context.startActivity(intent);
    }
}
