package com.rainmonth.notification;

import static android.content.Context.APP_OPS_SERVICE;

import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 1. todo Android 6.0的手机上为什么Action不展示
 * 2. todo 各个系统打开应用通知设置的方法
 * 3. todo 判断通知是否打开的方法
 * 4. todo 判断悬浮通知是否打开的方法
 *
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

    /**
     * 应用是否打开通知权限
     *
     * @return true if notification is enable
     */
    public boolean isNotificationEnable(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        boolean areNotificationsEnabled = notificationManagerCompat.areNotificationsEnabled();
        Log.i(TAG, "通知权限是否打开：" + areNotificationsEnabled);
        return areNotificationsEnabled;
    }

    /*
     * 判断通知权限是否打开
     */
    private boolean isNotificationEnableAbove19(Context context) {
        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(APP_OPS_SERVICE);
        ApplicationInfo appInfo = context.getApplicationInfo();

        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;

        Class appOpsClass = null; /* Context.APP_OPS_MANAGER */

        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod("checkOpNoThrow", Integer.TYPE, Integer.TYPE, String.class);

            Field opPostNotificationValue = appOpsClass.getDeclaredField("OP_POST_NOTIFICATION");
            int value = (int) opPostNotificationValue.get(Integer.class);
            return ((int) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public void openNotificationSettingsForApp(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, context.getApplicationInfo().uid);
            context.startActivity(intent);
        } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
            context.startActivity(intent);
        } else if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        } else {
            Intent localIntent = new Intent();
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= 9) {
                localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
            } else if (Build.VERSION.SDK_INT <= 8) {
                localIntent.setAction(Intent.ACTION_VIEW);
                localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
            }
            context.startActivity(localIntent);
        }
    }


    /**
     * 应用是否打开悬浮通知权限 （即悬浮在屏幕顶端的通知样式）
     *
     * @return true if float notification is enable
     */
    public boolean isFloatNotificationEnable() {
        // todo
        throw new RuntimeException("to be impl");
    }
}
