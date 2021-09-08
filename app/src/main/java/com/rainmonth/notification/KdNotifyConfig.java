package com.rainmonth.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.BuildConfig;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * 通知设置
 * 用来兼容不同版本的 Builder，如 {@link Notification.Builder}、{@link NotificationCompat.Builder}
 *
 * @author RandyZhang
 * @date 2021/9/7 5:09 下午
 */
public class KdNotifyConfig {
    public PendingIntent mPendingIntent;
    public RemoteViews mRemoteViews;
    public int mSmallIcon;
    public Bitmap mLargeIcon;
    public CharSequence mTickerText;
    public CharSequence mContentTitle;
    public CharSequence mContentText;
    public CharSequence mContentInfo;

    /**
     * 进度相关设置
     */
    public int mProgressMax;
    public int mProgress;
    public boolean mProgressIndeterminate;

    /**
     * 通道id
     * Api >= 26 才需要设置
     */
    public String mChannelId = "";
    /**
     * 通道名称
     */
    public String mChannelName;

    /**
     * 通道所属group Id
     */
    public String mGroupId;
    /**
     * 通道所属group Name
     */
    public String mGroupName;
    /**
     * 通知优先级
     * Api >= 24 {@link NotificationManager#IMPORTANCE_HIGH}, etc
     * Api < 24 {@link Notification#priority}、{@link Notification#PRIORITY_HIGH}, etc
     */
    public int mPriority = Notification.PRIORITY_DEFAULT;

    public Object mStyle;

    public long mWhen;

    public boolean mAutoCancel = false;
    public boolean mOngoing = false;

    public KdNotifyConfig(Builder builder) {
        mPendingIntent = builder.pendingIntent;
        mRemoteViews = builder.remoteViews;
        mSmallIcon = builder.smallIcon;
        mLargeIcon = builder.largeIcon;
        mTickerText = builder.tickerText;
        mContentTitle = builder.contentTitle;
        mContentText = builder.contentText;
        mContentInfo = builder.contentInfo;
        mChannelId = builder.channelId;
        mChannelName = builder.channelName;
        mStyle = builder.style;
        mWhen = builder.when;
        mAutoCancel = builder.autoCancel;
    }

    /**
     * 做一个适配
     */
    static class NotificationBuilderWrapper extends NotificationCompat.Builder {
        public Notification.Builder builder;

        public NotificationBuilderWrapper(Context context, Notification.Builder builder) {
            super(context);
            this.builder = builder;
        }

        public NotificationCompat.Builder addAction(int icon, CharSequence title, PendingIntent intent) {
            if (isAboveO()) {
                builder.addAction(icon, title, intent);
            } else {
                super.addAction(icon, title, intent);
            }
            return this;
        }

        public NotificationCompat.Builder addAction(KdActionWrapper actionWrapper) {
            if (isAboveO()) {
                builder.addAction(actionWrapper.action);
            } else {
                super.addAction(actionWrapper);
            }
            return this;
        }

        public NotificationCompat.Builder setStyle(KdStyleWrapper styleWrapper) {
            if (isAboveO()) {
                builder.setStyle(styleWrapper.style);
            } else {
                super.setStyle(styleWrapper);
            }
            return this;
        }
    }

    public static boolean isAboveO() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    /**
     * 将 配置转换成真正的 NotificationBuilder
     *
     * @param context content instance
     * @return NotificationCompat.Builder
     */
    public NotificationCompat.Builder toRealBuilder(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new NotificationBuilderWrapper(context, toNotificationBuilder(context));
        } else {
            return toNotificationCompatBuilder(context);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder toNotificationBuilder(Context context) {
        if (TextUtils.isEmpty(mChannelId) || TextUtils.isEmpty(mChannelName)) {
            if (BuildConfig.DEBUG) {
                throw new IllegalArgumentException("mChannelId or mChannelName should not be " +
                        "null or empty");
            }
            Log.e(KdNotificationManager.TAG, "Api>=26 you should specify channelId with " +
                    "channelName when use notification!!!");
        }
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (!TextUtils.isEmpty(mGroupId)) {
            // 创建组
            NotificationChannelGroup channelGroup = new NotificationChannelGroup(mGroupId, mGroupName);
            manager.createNotificationChannelGroup(channelGroup);
        } else {
            Log.w(KdNotificationManager.TAG, "mGroupId not specified!!!");
        }

        Log.i(KdNotificationManager.TAG, "mChannelId:" + mChannelId
                + ", mChannelName:" + mChannelName);
        // 创建channel
        NotificationChannel channel = new NotificationChannel(mChannelId, mChannelName,
                getImportanceFromPriority());
        if (!TextUtils.isEmpty(mGroupId)) {
            channel.setGroup(mGroupId); // 设置Group
        }
        channel.setSound(null, null);// 禁用通知声音
        channel.enableLights(true); // 开启呼吸灯
        channel.enableVibration(true);// 是否震动
        manager.createNotificationChannel(channel);

        Notification.Builder builder = new Notification.Builder(context, mChannelId);
        builder.setContent(mRemoteViews);
        builder.setContentIntent(mPendingIntent);
        builder.setSmallIcon(mSmallIcon);
        builder.setLargeIcon(mLargeIcon);
        builder.setTicker(mTickerText);
        builder.setContentTitle(mContentTitle);
        builder.setContentText(mContentText);
        builder.setContentInfo(mContentInfo);
        builder.setPriority(mPriority);
        builder.setWhen(mWhen);
        builder.setAutoCancel(mAutoCancel);
        builder.setProgress(mProgressMax, mProgress, mProgressIndeterminate);
        builder.setOngoing(mOngoing);
        return builder;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private int getImportanceFromPriority() {
        if (mPriority == Notification.PRIORITY_MIN) {
            return NotificationManager.IMPORTANCE_MIN;
        } else if (mPriority == Notification.PRIORITY_LOW) {
            return NotificationManager.IMPORTANCE_LOW;
        } else if (mPriority == Notification.PRIORITY_DEFAULT) {
            return NotificationManager.IMPORTANCE_DEFAULT;
        } else if (mPriority == Notification.PRIORITY_HIGH) {
            return NotificationManager.IMPORTANCE_HIGH;
        } else if (mPriority == Notification.PRIORITY_MAX) {
            return NotificationManager.IMPORTANCE_MAX;
        } else {
            return NotificationManager.IMPORTANCE_NONE;
        }
    }

    public NotificationCompat.Builder toNotificationCompatBuilder(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContent(mRemoteViews);
        builder.setContentIntent(mPendingIntent);
        builder.setTicker(mTickerText);
        builder.setSmallIcon(mSmallIcon);
        builder.setLargeIcon(mLargeIcon);
        builder.setContentTitle(mContentTitle);
        builder.setContentText(mContentText);
        builder.setContentInfo(mContentInfo);
        builder.setPriority(mPriority);
        builder.setWhen(mWhen);
        builder.setAutoCancel(mAutoCancel);
        builder.setProgress(mProgressMax, mProgress, mProgressIndeterminate);
        builder.setOngoing(mOngoing);
        return builder;
    }

    public static class Builder {

        public PendingIntent pendingIntent;
        public RemoteViews remoteViews;
        public int smallIcon;
        public Bitmap largeIcon;

        public CharSequence tickerText;
        public CharSequence contentTitle;
        public CharSequence contentText;
        public CharSequence contentInfo;

        public int progressMax;
        public int progress;
        public boolean progressIndeterminate;

        public String channelId;
        public String channelName;

        public String groupId;
        public String groupName;
        public int priorityOrImportance = Notification.PRIORITY_DEFAULT;
        public Object style;
        public long when;

        public boolean autoCancel = false;
        public boolean ongoing = false;

        public Builder() {

        }

        public Builder setContentIntent(PendingIntent pendingIntent) {
            this.pendingIntent = pendingIntent;
            return this;
        }

        public Builder setContent(RemoteViews remoteViews) {
            this.remoteViews = remoteViews;
            return this;
        }

        public Builder setSmallIcon(int smallIcon) {
            this.smallIcon = smallIcon;
            return this;
        }

        public Builder setLargeIcon(Bitmap largeIcon) {
            this.largeIcon = largeIcon;
            return this;
        }

        public Builder setTicker(CharSequence tickerText) {
            this.tickerText = tickerText;
            return this;
        }

        public Builder setContentTitle(CharSequence contentTitle) {
            this.contentTitle = contentTitle;
            return this;
        }

        public Builder setContentText(CharSequence contentText) {
            this.contentText = contentText;
            return this;
        }

        public Builder setContentInfo(CharSequence contentInfo) {
            this.contentInfo = contentInfo;
            return this;
        }

        public Builder channelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder channelName(String channelName) {
            this.channelName = channelName;
            return this;
        }

        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder groupName(String groupName) {
            this.groupName = groupName;
            return this;
        }

        public Builder setPriority(int priorityOrImportance) {
            this.priorityOrImportance = priorityOrImportance;
            return this;
        }

        public Builder setStyle(Object style) {
            this.style = style;
            return this;
        }


        public Builder setAutoCancel(boolean autoCancel) {
            this.autoCancel = autoCancel;
            return this;
        }

        public Builder setProgress(int max, int progress, boolean indeterminate) {
            this.progressMax = max;
            this.progress = progress;
            this.progressIndeterminate = indeterminate;
            return this;
        }

        public Builder setWhen(long when) {
            this.when = when;
            return this;
        }

        public Builder setOngoing(boolean ongoing) {
            this.ongoing = ongoing;
            return this;
        }

        KdNotifyConfig build() {
            return new KdNotifyConfig(this);
        }
    }
}
