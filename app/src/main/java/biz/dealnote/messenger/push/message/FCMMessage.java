package biz.dealnote.messenger.push.message;

import android.content.Context;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Map;

import biz.dealnote.messenger.api.util.VKStringUtils;
import biz.dealnote.messenger.longpoll.NotificationHelper;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.push.NotificationUtils;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Logger;

import static biz.dealnote.messenger.util.Utils.hasFlag;

public class FCMMessage {

    //04-14 11:55:34.387 30081-30114/ru.ezorrio.phoenix D/MyFcmListenerService: onMessage,
    // from: 652332232777,
    // collapseKey: null,
    // data: {image_type=user, from_id=280186075, id=msg_280186075_828342,
    // url=https://vk.com/im?sel=280186075&msgid=828342, body=Test, icon=message_24,
    // time=1523696134, type=msg, badge=1,
    // image=[{"width":200,"url":"https:\/\/pp.userapi.com\/c837424\/v837424529\/5c2cb\/OkkyraBZJCY.jpg","height":200},
    // {"width":100,"url":"https:\/\/pp.userapi.com\/c837424\/v837424529\/5c2cc\/dRPyhRW_dvU.jpg","height":100},
    // {"width":50,"url":"https:\/\/pp.userapi.com\/c837424\/v837424529\/5c2cd\/BB6tk_bcJ3U.jpg","height":50}],
    // sound=1, title=Yevgeni Polkin, to_id=216143660, group_id=msg_280186075, context={"msg_id":828342,"sender_id":280186075}}

    public long from;
    public String collapse_key;
    public String image_type;
    public int from_id;
    public String body;
    public long vk_time;
    public String type;
    public int badge;
    public String image;
    public boolean sound;
    public String title;
    public int to_id;
    public String group_id;
    public int message_id;
    public int sender_id;
    public long google_sent_time;
    public String photo_200_url;

    class MessageContext {
        @SerializedName("msg_id")
        int msg_id;

        @SerializedName("sender_id")
        int sender_id;
    }

    public static FCMMessage fromRemoteMessage(RemoteMessage remote) {
        FCMMessage message = new FCMMessage();
        Map<String, String> data = remote.getData();

        message.from = Long.parseLong(remote.getFrom());
        message.collapse_key = data.get("collapse_key");
        message.image_type = data.get("image_type");
        message.from_id = Integer.parseInt(data.get("from_id"));
        message.body = VKStringUtils.unescape(data.get("body"));
        message.vk_time = Long.parseLong(data.get("time"));
        message.type = data.get("type");
        message.badge = NotificationUtils.optInt(remote.getData().get("badge"), -1);
        //message.image = data.get("image"); //todo
        message.sound = Integer.parseInt(data.get("sound")) == 1;
        message.title = data.get("title");
        message.to_id = Integer.parseInt(data.get("to_id"));
        message.group_id = data.get("group_id");

        MessageContext context = new Gson().fromJson(data.get("context"), MessageContext.class);
        message.message_id = context.msg_id;
        message.sender_id = context.sender_id;

        FCMPhotoDto[] photo_array = new Gson().fromJson(data.get("image"), FCMPhotoDto[].class);
        message.photo_200_url = photo_array[0].url;

        Logger.d("PUSH", "id:" + message.message_id + " " + "sender:" + message.sender_id);

        message.google_sent_time = remote.getSentTime();
        return message;
    }

    public void notify(Context context, int accountId) {
        int mask = Settings.get().notifications().getNotifPref(accountId, from_id);
        if (!hasFlag(mask, ISettings.INotificationSettings.FLAG_SHOW_NOTIF)) {
            return;
        }

        if (Settings.get().accounts().getCurrent() != accountId) {
            Logger.d("FCMMessage", "notify, Attempting to send a notification does not in the current account!!!");
            return;
        }

        NotificationHelper.showNotification(context, accountId, title, body, message_id,
                sender_id, vk_time, photo_200_url);

    }
}