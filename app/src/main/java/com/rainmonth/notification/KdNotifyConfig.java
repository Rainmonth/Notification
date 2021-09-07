package com.rainmonth.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
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
    public CharSequence mContentTitle;
    public CharSequence mContentText;
    public CharSequence mContentInfo;


    /**
     * 通道id
     * Api >= 26 才需要设置
     */
    public String mChannelId;
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

    public Notification.Style mStyle;
    public NotificationCompat.Style mCompatStyle;

    public boolean mAutoCancel = false;

    public KdNotifyConfig(Builder builder) {
        mPendingIntent = builder.pendingIntent;
        mRemoteViews = builder.remoteViews;
        mSmallIcon = builder.smallIcon;
        mLargeIcon = builder.largeIcon;
        mContentTitle = builder.contentTitle;
        mContentText = builder.contentText;
        mContentInfo = builder.contentInfo;
        mChannelId = builder.channelId;
        mStyle = builder.style;
        mAutoCancel = builder.autoCancel;
    }


    public Notification.Builder toNotificationBuilder(Context context) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContent(mRemoteViews);
        builder.setSmallIcon(mSmallIcon);
        builder.setLargeIcon(mLargeIcon);
        builder.setContentTitle(mContentTitle);
        builder.setContentText(mContentText);
        builder.setContentInfo(mContentInfo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(mChannelId);
        }
        builder.setPriority(mPriority);
        builder.setStyle(mStyle);
        builder.setAutoCancel(mAutoCancel);
        return builder;
    }

    public NotificationCompat.Builder toNotificationCompatBuilder(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContent(mRemoteViews);
        builder.setSmallIcon(mSmallIcon);
        builder.setLargeIcon(mLargeIcon);
        builder.setContentTitle(mContentTitle);
        builder.setContentText(mContentText);
        builder.setContentInfo(mContentInfo);
        builder.setPriority(mPriority);
        builder.setStyle(mCompatStyle);
        builder.setAutoCancel(mAutoCancel);
        return builder;
    }

    public static class Builder {

        private Context context;
        public PendingIntent pendingIntent;
        public RemoteViews remoteViews;
        public int smallIcon;
        public Bitmap largeIcon;
        public CharSequence contentTitle;
        public CharSequence contentText;
        public CharSequence contentInfo;

        public String channelId;
        public String channelName;

        public String groupId;
        public String groupName;
        public int priorityOrImportance = Notification.PRIORITY_DEFAULT;
        public Notification.Style style;
        public NotificationCompat.Style compatStyle;

        public boolean autoCancel = false;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder pendingIntent(PendingIntent pendingIntent) {
            this.pendingIntent = pendingIntent;
            return this;
        }

        public Builder remoteViews(RemoteViews remoteViews) {
            this.remoteViews = remoteViews;
            return this;
        }

        public Builder smallIcon(int smallIcon) {
            this.smallIcon = smallIcon;
            return this;
        }

        public Builder largeIcon(Bitmap largeIcon) {
            this.largeIcon = largeIcon;
            return this;
        }

        public Builder contentTitle(CharSequence contentTitle) {
            this.contentTitle = contentTitle;
            return this;
        }

        public Builder contentText(CharSequence contentText) {
            this.contentText = contentText;
            return this;
        }

        public Builder contentInfo(CharSequence contentInfo) {
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

        public Builder priorityOrImportance(int priorityOrImportance) {
            this.priorityOrImportance = priorityOrImportance;
            return this;
        }

        public Builder style(Notification.Style style) {
            this.style = style;
            return this;
        }

        public Builder compatStyle(NotificationCompat.Style compatStyle) {
            this.compatStyle = compatStyle;
            return this;
        }

        public Builder autoCancel(boolean autoCancel) {
            this.autoCancel = autoCancel;
            return this;
        }

        KdNotifyConfig build() {
            return new KdNotifyConfig(this);
        }
    }
}
