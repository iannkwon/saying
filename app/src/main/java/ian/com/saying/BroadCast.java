package ian.com.saying;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BroadCast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //NotificationManager 안드로이드 상태바에 메세지를 던지기위한 서비스 불러오고
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.ic_title).setTicker("HETT").setWhen(System.currentTimeMillis())
                .setNumber(1).setContentTitle("나도 명언가, 명언소!").setContentText("인생의 느낀 것을 명언으로 남겨보세요.")
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE).setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }
}
