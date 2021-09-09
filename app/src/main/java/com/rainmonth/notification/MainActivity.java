package com.rainmonth.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 关于通知的说明：
 * 兼容性方面：一般采用Support V4 包中的NotificationCompat.Builder类
 * 注意以下版本:
 * Android 3.0 API 11 添加了Notification.Builder类
 * Android 4.0 API 16 添加了大视图支持
 * Android 5.0 API 21 支持浮动通知
 * 1、3.0之前版本不支持自定义视图内的按钮点击事件
 * 2、4.1之前版本不支持大视图（这里的大视图指的是BigTextStyle、BigPictureStyle、InboxStyle和MessagingStyle等）
 * 3、addAction(int, String, PendingIntent)int的值为0时表示不显示action的图片
 * 4、自定义视图的通知不能再添加Action
 * 5、普通通知同样支持addAction
 * 6、todo remote input 的实现
 * 7、show float notification 利用setFullScreenIntent(PendingIntent)显示悬浮通知
 * <p/>
 * 出现的问题：
 * todo setSmallIcon无效；
 * todo MessagingStyle无效
 * todo Bad notification posted from package com.rainmonth.notification: Couldn't expand RemoteViews for: StatusBarNotification
 * todo 解决方法：clean project or the remoteView you used should be
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {
    private int normalCount, bigPicStyleCount, bigTextStyleCount, inboxStyleCount, messagingStyleCount,
            customLayoutCount;
    // TAG
    private final static String TAG = MainActivity.class.getSimpleName();
    // notification id
    private int notifyId = 100;
    // 是否在播放
    private boolean isPlay = false;
    // 通知栏按钮广播
    private ButtonBroadcastReceiver bReceiver;
    // 通知栏按钮点击事件对应的ACTION
    private final static String ACTION_BUTTON = "com.rainmonth.intent.action.ButtonClick";
    // intent button id tag
    private final static String INTENT_BUTTON_ID_TAG = "ButtonId";
    // 上一首 按钮点击 ID
    private final static int BUTTON_PREV_ID = 1;
    // 播放/暂停 按钮点击 ID
    private final static int BUTTON_PLAY_ID = 2;
    // 下一首 按钮点击 ID
    private final static int BUTTON_NEXT_ID = 3;
    // Progress Notification
    private int progress;
    // download thread
    private DownloadThread downloadThread;
    // true:确定样式的   false:不确定样式
    private boolean isStyleConfirmed = false;
    // 是否是自定义样式的progress
    private boolean isCustom = false;
    // 是否暂停
    private boolean isPause = false;
    // action pending intent
    private PendingIntent firstPendingIntent, secondPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViewsAndEvents();
        initButtonReceiver();
        Intent firstIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.baidu.com"));
        firstPendingIntent = PendingIntent.getActivity(this, 1, firstIntent, 0);
        Intent secondIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://csdn.net"));
        secondPendingIntent = PendingIntent.getActivity(this, 1, secondIntent, 0);
    }

    @Override
    protected void onDestroy() {
        if (bReceiver != null) {
            unregisterReceiver(bReceiver);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_btn_normal:
                try {
                    notifyId = 101;
                    showNormalNotify();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case R.id.tv_btn_big_picture_style:
                notifyId = 102;
                showBigPicStyleNotify();
                break;
            case R.id.tv_btn_big_text_style:
                notifyId = 103;
                showBigTextStyleNotify();
                break;
            case R.id.tv_btn_big_text_style_with_action_btn:
                notifyId = 111;
                showBigTextStyleWithActionBtnNotify();
                break;
            case R.id.tv_btn_inbox_style:
                try {
                    notifyId = 104;
                    showInboxStyleNotify();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.tv_btn_messaging_style:
                notifyId = 105;
                // todo it didn't works!
                showMessagingStyleNotify();
                break;
            case R.id.tv_btn_custom_layout_style:
                notifyId = 106;
                showCustomNotify();
                break;
            case R.id.tv_btn_custom_layout_with_btn_style:
                notifyId = 107;
                showCustomButtonNotify();
                break;
            case R.id.tv_btn_float_style:
                notifyId = 112;
                showFloatStyleNotify();
                break;
            case R.id.tv_btn_custom_layout_with_progress_style_not_confirmed:
                isCustom = false;
                isStyleConfirmed = false;
                notifyId = 108;
                showProgressNotify();
                break;
            case R.id.tv_btn_custom_layout_with_progress_style_confirmed:
                isCustom = false;
                isStyleConfirmed = true;
                notifyId = 109;
                showProgressNotify();
                break;
            case R.id.tv_btn_custom_layout_with_custom_progress_style:
                notifyId = 110;
                isCustom = true;
                isStyleConfirmed = false;
                String status = "已开始";
                showCustomProgressNotify(status);
                break;
            case R.id.tv_btn_start:
                startDownloadNotify();
                break;
            case R.id.tv_btn_pause:
                pauseDownloadNotify();
                break;
            case R.id.tv_btn_cancel:
                cancelDownloadNotify();
                break;
            case R.id.tv_btn_clear_special_id:
                EditText etClearNotificationId = (EditText) findViewById(R.id.et_clear_notify_id);
                int id = Integer.valueOf(etClearNotificationId.getText().toString());
                clearNotify(id);
                break;
            case R.id.tv_btn_clear_all:
                if (downloadThread != null && downloadThread.isAlive()) {
                    downloadThread = null;
                }
                clearAllNotify();
                break;
            default:

                break;
        }
    }

    /**
     * 初始化Views 和 Events
     */
    private void initViewsAndEvents() {
        TextView tvBtnNormal = (TextView) findViewById(R.id.tv_btn_normal);
        TextView tvBtnBigPicStyle = (TextView) findViewById(R.id.tv_btn_big_picture_style);
        TextView tvBtnBigTextStyle = (TextView) findViewById(R.id.tv_btn_big_text_style);
        TextView tvBtnBigTextStyleWithActionBtn = (TextView) findViewById(R.id.tv_btn_big_text_style_with_action_btn);
        TextView tvBtnInboxStyle = (TextView) findViewById(R.id.tv_btn_inbox_style);
        TextView tvBtnMessagingStyle = (TextView) findViewById(R.id.tv_btn_messaging_style);
        TextView tvBtnCustomLayoutStyle = (TextView) findViewById(R.id.tv_btn_custom_layout_style);
        TextView tvBtnCustomLayoutWithBtnStyle = (TextView) findViewById(R.id.tv_btn_custom_layout_with_btn_style);
        TextView tvBtnFloatStyle = (TextView) findViewById(R.id.tv_btn_float_style);
        TextView tvBtnCustomLayoutWithProgressStyleNotConfirmed = (TextView) findViewById(R.id.tv_btn_custom_layout_with_progress_style_not_confirmed);
        TextView tvBtnCustomLayoutWithProgressStyleConfirmed = (TextView) findViewById(R.id.tv_btn_custom_layout_with_progress_style_confirmed);
        TextView tvBtnCustomLayoutWithCustomProgressStyle = (TextView) findViewById(R.id.tv_btn_custom_layout_with_custom_progress_style);

        TextView tvBtnProgressStart = (TextView) findViewById(R.id.tv_btn_start);
        TextView tvBtnProgressPause = (TextView) findViewById(R.id.tv_btn_pause);
        TextView tvBtnProgressCancel = (TextView) findViewById(R.id.tv_btn_cancel);
        TextView tvBtnClearSpecialNotification = (TextView) findViewById(R.id.tv_btn_clear_special_id);
        TextView tvBtnClearAllNotification = (TextView) findViewById(R.id.tv_btn_clear_all);

        tvBtnNormal.setOnClickListener(this);
        tvBtnBigPicStyle.setOnClickListener(this);
        tvBtnBigTextStyle.setOnClickListener(this);
        tvBtnBigTextStyleWithActionBtn.setOnClickListener(this);
        tvBtnInboxStyle.setOnClickListener(this);
        tvBtnMessagingStyle.setOnClickListener(this);
        tvBtnCustomLayoutStyle.setOnClickListener(this);
        tvBtnCustomLayoutWithBtnStyle.setOnClickListener(this);
        tvBtnFloatStyle.setOnClickListener(this);
        tvBtnCustomLayoutWithProgressStyleNotConfirmed.setOnClickListener(this);
        tvBtnCustomLayoutWithProgressStyleConfirmed.setOnClickListener(this);
        tvBtnCustomLayoutWithCustomProgressStyle.setOnClickListener(this);
        tvBtnProgressStart.setOnClickListener(this);
        tvBtnProgressPause.setOnClickListener(this);
        tvBtnProgressCancel.setOnClickListener(this);
        tvBtnClearSpecialNotification.setOnClickListener(this);
        tvBtnClearAllNotification.setOnClickListener(this);
    }

    /**
     * 显示普通通知
     */
    private void showNormalNotify() {
        mConfigBuilder = getNotifyConfigBuilder()
                .channelId(String.valueOf(notifyId))
                .channelName(String.valueOf(notifyId))
                .groupName("showFloatStyleNotify")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentTitle("普通样式通知")
                .setContentText("普通样式通知内容（文本内容只能显示一行，且字数有限）")
                .setContentIntent(getDefaultPendingIntent(0))
                .setContentInfo(String.valueOf(++normalCount));
        NotificationCompat.Builder builder = mConfigBuilder.build().toRealBuilder(this)
                .addAction(0, "百度", firstPendingIntent)// todo 不显示图标传0即可
                .addAction(0, "SuperRandy", secondPendingIntent)// todo 不显示图标传0即可
                .setDefaults(Notification.DEFAULT_ALL);

        mNotificationManager.notify(notifyId, KdNotificationManager.getInstance(this)
                .makeNotification(builder));
//                mConfigBuilder.notify(); // 直接调用会抛出java.lang.IllegalMonitorStateException 提示object not locked by thread before notify()
    }

    /**
     * 显示BigPicture Notification
     */
    private void showBigPicStyleNotify() {
        mConfigBuilder = getNotifyConfigBuilder()
                .channelId(String.valueOf(notifyId))
                .channelName(String.valueOf(notifyId))
                .groupName("showFloatStyleNotify")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentTitle("BigPictureStyle样式通知")
                .setContentText("大图片样式通知内容")
                .setContentInfo(String.valueOf(++bigPicStyleCount))
//                .setDefaults(Notification.DEFAULT_ALL)
                .setTicker("BigPictureStyle通知");
        NotificationCompat.Builder builder = mConfigBuilder.build().toRealBuilder(this)
                .setStyle(new NotificationCompat.BigPictureStyle() // 设置通知样式为大型图片样式
                        .bigPicture(BitmapFactory.decodeResource(getResources(), R.mipmap.bg1)));
        mNotificationManager.notify(notifyId, KdNotificationManager.getInstance(this)
                .makeNotification(builder));
    }

    /**
     * 显示BigTextStyle Notification
     */
    private void showBigTextStyleNotify() {
        mConfigBuilder = getNotifyConfigBuilder()
                .channelId(String.valueOf(notifyId))
                .channelName(String.valueOf(notifyId))
                .groupName("showFloatStyleNotify")
                .setSmallIcon(R.mipmap.icon)
                .setContentTitle("BigTextStyle样式通知")
                .setContentText("大文本样式通知内容")
                .setContentInfo(String.valueOf(++bigTextStyleCount))
                .setTicker("BigTextStyle通知");
        NotificationCompat.Builder builder = mConfigBuilder.build().toRealBuilder(this)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("[遇见8，投了吧]投乐吧疯狂抢楼火热进行中！为中国加油，抢无门槛红包，仅此一天！" +
                                "前2000楼获奖用户的红包已经在路上了哟！"));
        mNotificationManager.notify(notifyId, KdNotificationManager.getInstance(this)
                .makeNotification(builder));
    }

    /**
     * 显示带action btn的BigTextStyle Notification
     */
    private void showBigTextStyleWithActionBtnNotify() {
        mConfigBuilder = getNotifyConfigBuilder();
        mConfigBuilder.setSmallIcon(R.mipmap.icon)
                .channelId(String.valueOf(notifyId))
                .channelName(String.valueOf(notifyId))
                .groupName("showFloatStyleNotify")
                .setContentTitle("带action btn的BigTextStyle 通知")
                .setContentText("带action btn的大文本样式通知内容")
                .setContentIntent(getDefaultPendingIntent(0))
                .setTicker("带action btn的BigTextStyle通知");

        NotificationCompat.Builder builder = mConfigBuilder.build().toRealBuilder(this)
                .addAction(0, "百度", firstPendingIntent)//  不显示图标传0即可
                .addAction(0, "SuperRandy", secondPendingIntent)//  不显示图标传0即可
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("[遇见8，投了吧]投乐吧疯狂抢楼火热进行中！为中国加油，抢无门槛红包，" +
                                "仅此一天！前2000楼获奖用户的红包已经在路上了哟！"));

        mNotificationManager.notify(notifyId, KdNotificationManager.getInstance(this)
                .makeNotification(builder));
    }

    /**
     * 显示InboxStyle Notification
     */
    private void showInboxStyleNotify() {
        mConfigBuilder = getNotifyConfigBuilder();
        mConfigBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .channelId(String.valueOf(notifyId))
                .channelName("信箱")
                .setContentTitle("InboxStyle样式通知")
                .setContentText("Inbox样式通知内容")
                .setContentIntent(getDefaultPendingIntent(0))
                .setContentInfo(String.valueOf(++inboxStyleCount))
                .setTicker("InboxStyle通知");
        NotificationCompat.Builder builder = mConfigBuilder.build().toRealBuilder(this)
                .addAction(0, "百度", firstPendingIntent)
                .addAction(0, "SuperRandy", secondPendingIntent)
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine("InboxStyle item 1")
                        .addLine("InboxStyle item 2")
                        .addLine("InboxStyle item 3")
                        .addLine("InboxStyle item 4")
                        .setSummaryText("InboxStyle Summary Text"));
        mNotificationManager.notify(notifyId, KdNotificationManager.getInstance(this)
                .makeNotification(builder));
    }

    /**
     * 显示MessagingStyle Notification
     */
    private void showMessagingStyleNotify() {
        mConfigBuilder = getNotifyConfigBuilder();
        mConfigBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .channelId(String.valueOf(notifyId))
                .channelName(String.valueOf(notifyId))
                .setContentTitle("MessagingStyle样式通知")
                .setContentText("MessagingStyle样式通知内容")
                .setContentInfo(String.valueOf(++messagingStyleCount));
        NotificationCompat.Builder builder = mConfigBuilder.build().toRealBuilder(this)
                .setStyle(new NotificationCompat.MessagingStyle("me")
                        .setConversationTitle("Team lunch")
                        .addMessage("Hi", SystemClock.elapsedRealtime(), "me") // Pass in null for user.
                        .addMessage("What's up?", SystemClock.elapsedRealtime(), "Coworker")
                        .addMessage("Not much", SystemClock.elapsedRealtime(), "me")
                        .addMessage("How about lunch?", SystemClock.elapsedRealtime(), "Coworker"))
                .setDefaults(Notification.DEFAULT_ALL);
        mNotificationManager.notify(notifyId, KdNotificationManager.getInstance(this)
                .makeNotification(builder));
    }

    /**
     * 显示自定义通知
     */
    private void showCustomNotify() {
        mConfigBuilder = getNotifyConfigBuilder();
        //先设定RemoteViews
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.view_custom);
        remoteViews.setImageViewResource(R.id.custom_icon, R.drawable.icon);
        remoteViews.setTextViewText(R.id.tv_custom_title, "今日头条");
        remoteViews.setTextViewText(R.id.tv_custom_content, "金州勇士官方宣布球队已经解雇了主帅马克-杰克逊，随后宣布了最后的结果。");
        mConfigBuilder.setContent(remoteViews)
                .channelId(String.valueOf(notifyId))
                .channelName(String.valueOf(notifyId))
                .setContentIntent(getDefaultPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT))
                .setContentInfo(String.valueOf(++customLayoutCount))
                .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                .setTicker("有新资讯")
                .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
                .setOngoing(false)//不是正在进行的   true为正在进行  效果和.flag一样
                .setSmallIcon(R.drawable.icon);
        Notification notify = KdNotificationManager.getInstance(this)
                .makeNotification(this, mConfigBuilder.build());
        mNotificationManager.notify(notifyId, notify);
    }

    /**
     * 显示自定义带按钮的通知栏
     */
    private void showCustomButtonNotify() {
        mConfigBuilder = getNotifyConfigBuilder();
        RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.view_custom_button);
        mRemoteViews.setImageViewResource(R.id.custom_song_icon, R.drawable.sing_icon);
        //API 3.0 以上的时候显示按钮，否则消失
        mRemoteViews.setTextViewText(R.id.tv_custom_song_singer, "周杰伦");
        mRemoteViews.setTextViewText(R.id.tv_custom_song_name, "七里香");
        //如果版本号低于（3.0），那么不显示按钮
        mRemoteViews.setViewVisibility(R.id.ll_custom_button, View.VISIBLE);
        if (isPlay) {
            mRemoteViews.setImageViewResource(R.id.btn_custom_play, R.drawable.btn_pause);
        } else {
            mRemoteViews.setImageViewResource(R.id.btn_custom_play, R.drawable.btn_play);
        }
        //点击的事件处理
        Intent buttonIntent = new Intent(ACTION_BUTTON);
        /* 上一首按钮 */
        buttonIntent.putExtra(INTENT_BUTTON_ID_TAG, BUTTON_PREV_ID);
        //这里加了广播，所及INTENT的必须用getBroadcast方法
        PendingIntent intent_prev = PendingIntent.getBroadcast(this, 1, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_custom_prev, intent_prev);
        /* 播放/暂停  按钮 */
        buttonIntent.putExtra(INTENT_BUTTON_ID_TAG, BUTTON_PLAY_ID);
        PendingIntent intent_play = PendingIntent.getBroadcast(this, 2, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_custom_play, intent_play);
        /* 下一首 按钮  */
        buttonIntent.putExtra(INTENT_BUTTON_ID_TAG, BUTTON_NEXT_ID);
        PendingIntent intent_next = PendingIntent.getBroadcast(this, 3, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_custom_next, intent_next);

        mConfigBuilder.setContent(mRemoteViews)
                .channelId(String.valueOf(notifyId))
                .channelName(String.valueOf(notifyId))
                .setContentIntent(getDefaultPendingIntent(0))
                .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                .setTicker("正在播放")
                .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
                .setOngoing(true)
                .setSmallIcon(R.drawable.sing_icon);
        Notification notify = KdNotificationManager.getInstance(this)
                .makeNotification(this, mConfigBuilder.build());
        notify.flags = Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(notifyId, notify);
    }

    /**
     * 显示悬浮通知
     */
    private void showFloatStyleNotify() {
        Intent hangIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.baidu.com"));
        PendingIntent hangPendingIntent = PendingIntent.getActivity(this, 0, hangIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mConfigBuilder = getNotifyConfigBuilder();
        mConfigBuilder.setSmallIcon(R.mipmap.icon)
                .channelId(String.valueOf(notifyId))
                .channelName(String.valueOf(notifyId))
                .groupName("showFloatStyleNotify")
                .setContentTitle("Float Notification")
                .setContentText("Float 样式通知")
                .setContentIntent(getDefaultPendingIntent(0))
                .setTicker("Float notification 来了")
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true);
        NotificationCompat.Builder builder = mConfigBuilder.build().toRealBuilder(this)
                .addAction(0, "百度", firstPendingIntent).addAction(0,
                        "SuperRandy", secondPendingIntent)
                .setFullScreenIntent(hangPendingIntent, true);
        mNotificationManager.notify(notifyId, KdNotificationManager.getInstance(this)
                .makeNotification(builder));

    }

    /**
     * 显示带progress的通知（包括确定进度的和不确定进度的
     */
    private void showProgressNotify() {
        progress = 0;
        mConfigBuilder = getNotifyConfigBuilder()
                .channelId(String.valueOf(notifyId))
                .channelName(String.valueOf(notifyId))
                .groupName("showFloatStyleNotify")
                .setContentTitle("等待下载")
                .setContentText("进度:")
                .setTicker("开始下载");// 通知首次出现在通知栏，带上升动画效果的
        if (isStyleConfirmed) {
            //确定进度的
            mConfigBuilder.setProgress(100, progress, false); // 这个方法是显示进度条  设置为true就是不确定的那种进度条效果
        } else {
            //不确定进度的
            mConfigBuilder.setProgress(0, 0, true);
        }
        Notification notification = KdNotificationManager.getInstance(this)
                .makeNotification(this, mConfigBuilder.build());
        mNotificationManager.notify(notifyId, notification);

    }

    /**
     * 显示自定义带Progress的通知
     */
    private void showCustomProgressNotify(String status) {
        mConfigBuilder = getNotifyConfigBuilder();
        RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.view_custom_progress);
        mRemoteViews.setImageViewResource(R.id.custom_progress_icon, R.drawable.icon);
        mRemoteViews.setTextViewText(R.id.tv_custom_progress_title, "今日头条");
        mRemoteViews.setTextViewText(R.id.tv_custom_progress_status, status);
        if (progress >= 100 || downloadThread == null) {
            mRemoteViews.setProgressBar(R.id.custom_progressbar, 0, 0, false);
            mRemoteViews.setViewVisibility(R.id.custom_progressbar, View.GONE);
        } else {
            mRemoteViews.setProgressBar(R.id.custom_progressbar, 100, progress, false);
            mRemoteViews.setViewVisibility(R.id.custom_progressbar, View.VISIBLE);
        }
        mConfigBuilder.channelId(String.valueOf(notifyId))
                .channelName(String.valueOf(notifyId))
                .groupName("showFloatStyleNotify")
                .setContent(mRemoteViews)
                .setContentIntent(getDefaultPendingIntent(0))
                .setTicker("头条更新");

        Notification notify = KdNotificationManager.getInstance(this)
                .makeNotification(this, mConfigBuilder.build());
        mNotificationManager.notify(notifyId, notify);
        startDownloadNotify();
    }

    /**
     * 开始下载通知
     */
    private void startDownloadNotify() {
        isPause = false;
        if (downloadThread != null && downloadThread.isAlive()) {
//			downloadThread.start();
            // do nothing
        } else {
            downloadThread = new DownloadThread();
            downloadThread.start();
        }
    }

    /**
     * 暂停下载通知
     */
    private void pauseDownloadNotify() {
        isPause = true;
        if (!isCustom) {
            mConfigBuilder.setContentTitle("已暂停");
            setNotify(progress);
        } else {
            showCustomProgressNotify("已暂停");
        }
    }

    /**
     * 设置下载进度
     */
    public void setNotify(int progress) {
        mConfigBuilder.setProgress(100, progress, false); // 这个方法是显示进度条
        mNotificationManager.notify(notifyId, KdNotificationManager.getInstance(this)
                .makeNotification(mConfigBuilder.build().toRealBuilder(this)));
    }

    /**
     * 取消下载通知
     */
    private void cancelDownloadNotify() {
        if (downloadThread != null) {
            downloadThread.interrupt();
        }
        downloadThread = null;
        if (!isCustom) {
            mConfigBuilder.setContentTitle("下载已取消").setProgress(0, 0, false);
            mNotificationManager.notify(notifyId, KdNotificationManager.getInstance(this)
                    .makeNotification(this, mConfigBuilder.build()));
        } else {
            showCustomProgressNotify("下载已取消");
        }
    }

    /**
     * 带按钮的通知栏点击广播接收
     */
    private void initButtonReceiver() {
        bReceiver = new ButtonBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_BUTTON);
        registerReceiver(bReceiver, intentFilter);
    }

    /**
     * 广播监听按钮点击事件
     */
    public class ButtonBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_BUTTON)) {
                //通过传递过来的ID判断按钮点击属性或者通过getResultCode()获得相应点击事件
                int buttonId = intent.getIntExtra(INTENT_BUTTON_ID_TAG, 0);
                switch (buttonId) {
                    case BUTTON_PREV_ID:
                        Log.d(TAG, "上一首");
                        Toast.makeText(getApplicationContext(), "上一首", Toast.LENGTH_SHORT).show();
                        break;
                    case BUTTON_PLAY_ID:
                        String play_status;
                        isPlay = !isPlay;
                        if (isPlay) {
                            play_status = "开始播放";
                        } else {
                            play_status = "已暂停";
                        }
                        showCustomButtonNotify();
                        Log.d(TAG, play_status);
                        Toast.makeText(getApplicationContext(), play_status, Toast.LENGTH_SHORT).show();
                        break;
                    case BUTTON_NEXT_ID:
                        Log.d(TAG, "下一首");
                        Toast.makeText(getApplicationContext(), "下一首", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 模拟下载线程
     */
    public class DownloadThread extends Thread {
        @Override
        public void run() {
            int progressNow = 0;
            while (progressNow <= 100) {
                if (downloadThread == null) {
                    break;
                }
                if (!isPause) {
                    progress = progressNow;
                    if (!isCustom) {
                        mConfigBuilder.setContentTitle("下载中");
                        if (isStyleConfirmed) {
                            setNotify(progress);
                        }
                    } else {
                        showCustomProgressNotify("下载中");
                    }
                    progressNow += 10;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // When the loop is finished, updates the notification
            if (downloadThread != null) {
                if (!isCustom) {
                    mConfigBuilder.setContentText("下载完成")
                            // Removes the progress bar
                            .setProgress(0, 0, false);
                    mNotificationManager.notify(notifyId, KdNotificationManager.getInstance(MainActivity.this)
                            .makeNotification(MainActivity.this, mConfigBuilder.build()));
                } else {
                    showCustomProgressNotify("下载完成");
                }
            }
        }
    }

}
