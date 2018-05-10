package biz.dealnote.messenger.push.message;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import android.text.Spannable;

import com.google.firebase.messaging.RemoteMessage;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.MainActivity;
import biz.dealnote.messenger.link.LinkHelper;
import biz.dealnote.messenger.link.internal.OwnerLinkSpanFactory;
import biz.dealnote.messenger.longpoll.AppNotificationChannels;
import biz.dealnote.messenger.longpoll.NotificationHelper;
import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.push.NotificationScheduler;
import biz.dealnote.messenger.push.NotificationUtils;
import biz.dealnote.messenger.push.OwnerInfo;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Utils;

import static biz.dealnote.messenger.push.NotificationUtils.configOtherPushNotification;

public class ReplyFCMMessage {

    private static final String TAG = ReplyFCMMessage.class.getSimpleName();

    //04-14 13:02:31.114 1784-2485/ru.ezorrio.phoenix D/MyFcmListenerService: onMessage,
    // from: 652332232777, collapseKey: null, data: {image_type=user, from_id=280186075,
    // id=reply_280186075_60, url=https://vk.com/wall280186075_56?reply=60, body=Yevgeni Polkin: Emin, test,
    // icon=reply_24, time=1523700152, type=comment, badge=1,
    // image=[{"width":200,"url":"https:\/\/pp.userapi.com\/c837424\/v837424529\/5c2cb\/OkkyraBZJCY.jpg","height":200},
    // {"width":100,"url":"https:\/\/pp.userapi.com\/c837424\/v837424529\/5c2cc\/dRPyhRW_dvU.jpg","height":100},
    // {"width":50,"url":"https:\/\/pp.userapi.com\/c837424\/v837424529\/5c2cd\/BB6tk_bcJ3U.jpg","height":50}],
    // sound=1, title=Reply to your comment, to_id=216143660, group_id=reply,
    // context={"feedback":true,"reply_id":60,"user_id":280186075,"item_id":56,"owner_id":"280186075","type":"comment"}}


        private int from_id;
    private int reply_id;
    //public String firstName;
    //private int sex;
    //public long from;
    private String text;
    private String place;
    //public String lastName;
    //private String type;

    public static ReplyFCMMessage fromRemoteMessage(@NonNull RemoteMessage remote){
        ReplyFCMMessage message = new ReplyFCMMessage();
        message.from_id = NotificationUtils.optInt(remote, "from_id");
        message.reply_id = NotificationUtils.optInt(remote, "reply_id");
        //message.sex = optInt(bundle, "sex");
        //message.firstName = bundle.getString("first_name");
        //message.lastName = bundle.getString("last_name");
        //message.from = optLong(bundle, "from");
        message.text = remote.getData().get("text");
        //message.type = bundle.getString("type");
        message.place = remote.getData().get("place");
        return message;
    }

    public void notify(final Context context, int accountId){
        if (!Settings.get()
                .notifications()
                .isReplyNotifEnabled()) {
            return;
        }

        Context app = context.getApplicationContext();
        OwnerInfo.getRx(app, accountId, from_id)
                .subscribeOn(NotificationScheduler.INSTANCE)
                .subscribe(ownerInfo -> notifyImpl(app, ownerInfo.getOwner(), ownerInfo.getAvatar()), throwable -> {/*ignore*/});
    }

    private void notifyImpl(Context context, @NonNull Owner owner, Bitmap bitmap){
        String url = "vk.com/" + place;
        Commented commented = LinkHelper.findCommentedFrom(url);

        if(commented == null){
            Logger.e(TAG, "Unknown place: " + place);
            return;
        }

        Spannable snannedText = OwnerLinkSpanFactory.withSpans(text, true, false, null);
        String targetText = snannedText == null ? null : snannedText.toString();

        final NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Utils.hasOreo()){
            nManager.createNotificationChannel(AppNotificationChannels.getCommentsChannel(context));
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppNotificationChannels.COMMENTS_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notify_statusbar)
                .setLargeIcon(bitmap)
                .setContentTitle(owner.getFullName())
                .setContentText(targetText)
                .setSubText(context.getString(R.string.in_reply_to_your_comment))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(targetText))
                .setAutoCancel(true);

        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        int aid = Settings.get()
                .accounts()
                .getCurrent();

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Extra.PLACE, PlaceFactory.getCommentsPlace(aid, commented, reply_id));
        intent.setAction(MainActivity.ACTION_OPEN_PLACE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, reply_id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();

        configOtherPushNotification(notification);
        nManager.notify(place, NotificationHelper.NOTIFICATION_REPLY_ID, notification);
    }
}
