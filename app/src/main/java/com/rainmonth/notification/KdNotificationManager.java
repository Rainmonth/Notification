package com.rainmonth.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

/**
 * @author RandyZhang
 * @date 2021/9/7 4:51 下午
 */
public class KdNotificationManager {

    public static final String TAG = "KdNotificationManager";
    public static final String NOTIFICATION_GROUP_PLAY = "com.hhdd.kada.notification_group_play";

    /**
     * 要保证channelId的唯一性
     */
    public static final String CHANNEL_ID_BOOK_PLAY = "CHANNEL_ID_BOOK_PLAY";
    public static final String CHANNEL_ID_STORY_PLAY = "CHANNEL_ID_STORY_PLAY";

    private static final int NOTIFY_TYPE_BOOK_PLAY = 0;
    private static final int NOTIFY_TYPE_STORY_PLAY = 1;

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
     * 创建通知对象（带有一些额外配置的通知
     * 因为Notification可配置的参数太多了，当配置一些不常用的配置时，直接将将其设置到 config 对象得到的 Builder中
     *
     * @param context context
     * @param config  通知通用设置
     * @param builder 实际上使用的Builder（Api>=26时使用的是{@link Notification.Builder}，Api<26时使用的时
     *                {@link NotificationCompat.Builder}
     * @return notification instance
     */
    public Notification makeNotificationWithExtras(Context context,
                                                   KdNotifyConfig config,
                                                   NotificationCompat.Builder builder) {

        int sdkInt = Build.VERSION.SDK_INT;
        Notification notification;
        if (sdkInt >= Build.VERSION_CODES.O) { // Api>=26
            notification = makeNotificationAboveV26(context, config);
        } else if (sdkInt >= Build.VERSION_CODES.LOLLIPOP_MR1) { // Api>=22
            notification = makeNotificationAboveV16(context, config);
        } else if (sdkInt >= Build.VERSION_CODES.JELLY_BEAN) { // Api>=16
            notification = makeNotificationAboveV16(context, config);
        } else {
            notification = makeNotificationBelowV16(config);
        }
        return notification;
    }


    /**
     * 创建通知对象
     *
     * @param context context
     * @param config  通知配置实例
     * @return notification instance
     */
    public Notification makeNotification(Context context,
                                         KdNotifyConfig config) {
        int sdkInt = Build.VERSION.SDK_INT;
        Notification notification;
        if (sdkInt >= Build.VERSION_CODES.O) { // Api>=26
            notification = makeNotificationAboveV26(context, config);
        } else if (sdkInt >= Build.VERSION_CODES.LOLLIPOP_MR1) { // Api>=22
            notification = makeNotificationAboveV16(context, config);
        } else if (sdkInt >= Build.VERSION_CODES.JELLY_BEAN) { // Api>=16
            notification = makeNotificationAboveV16(context, config);
        } else {
            notification = makeNotificationBelowV16(config);
        }
        return notification;
    }

    /**
     * 创建通知（API>=26)
     *
     * @param context context instance
     * @param config  通知配置实例
     * @return Notification instance
     */
    private Notification makeNotificationAboveV26(Context context, KdNotifyConfig config) {
        NotificationCompat.Builder builder = config.toRealBuilder(context);
        KdNotifyConfig.NotificationBuilderWrapper builderCompat
                = (KdNotifyConfig.NotificationBuilderWrapper) builder;
        if (config.mStyle instanceof Notification.Style) {
            builderCompat.builder.setStyle((Notification.Style) config.mStyle);
        }
        Notification notification = builderCompat.builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        return notification;

    }

    /**
     * 创建通知（26>API>=16)
     *
     * @param context context instance
     * @param config  通知配置实例
     * @return Notification instance
     */
    private Notification makeNotificationAboveV16(Context context, KdNotifyConfig config) {
        NotificationCompat.Builder builder = config.toRealBuilder(context);
        if (config.mStyle instanceof NotificationCompat.Style) {
            builder.setStyle((NotificationCompat.Style) config.mStyle);
        }
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
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
}
