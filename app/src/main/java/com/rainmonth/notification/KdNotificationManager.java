package com.rainmonth.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
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
    public static final String NOTIFICATION_GROUP_PLAY = "com.hhdd.kada.notification_group_play";

    /**
     * 要保证channelId的唯一性
     */
    private static final String CHANNEL_ID_BOOK_PLAY = "CHANNEL_ID_BOOK_PLAY";
    private static final String CHANNEL_ID_STORY_PLAY = "CHANNEL_ID_STORY_PLAY";

    private static final int NOTIFY_TYPE_BOOK_PLAY = 0;
    private static final int NOTIFY_TYPE_STORY_PLAY = 1;

    private static volatile KdNotificationManager sInstance;

    private NotificationManager mNotificationMgr;


    private KdNotificationManager(Context context) {
        mNotificationMgr = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private KdNotificationManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (KdNotificationManager.class) {
                if (sInstance == null) {
                    sInstance = new KdNotificationManager(context);
                }
            }
        }
        return sInstance;
    }

    private Notification makeNotification(Context context, KdNotifyConfig config) {
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

    private Notification makeNotificationAboveV26(Context context, KdNotifyConfig config) {
        return makeNotificationAboveV26(config.toNotificationBuilder(context), config.mGroupId,
                config.mGroupName, config.mChannelId, config.mChannelName, config.mPriority);
    }

    @TargetApi(26)
    private Notification makeNotificationAboveV26(Notification.Builder builder, String groupId,
                                                  String groupName, String channelId,
                                                  String channelName, int importance) {
        // 创建组
        NotificationChannelGroup channelGroup = new NotificationChannelGroup(groupId, groupName);
        mNotificationMgr.createNotificationChannelGroup(channelGroup);

        // 创建channel
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setGroup(groupId); // 设置Group
        channel.setSound(null, null);// 禁用通知声音
        channel.enableLights(true); // 开启呼吸灯
        channel.enableVibration(true);// 是否震动
        mNotificationMgr.createNotificationChannel(channel);

        builder.setChannelId(channelId);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        return notification;
    }

    private Notification makeNotificationAboveV16(Context context, KdNotifyConfig config) {
        return makeNotificationAboveV16(config.toNotificationCompatBuilder(context));
    }

    private Notification makeNotificationAboveV16(NotificationCompat.Builder builder) {
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        return notification;
    }

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
