package android.example.com.squawker.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.example.com.squawker.MainActivity;
import android.example.com.squawker.R;
import android.example.com.squawker.provider.SquawkContract;
import android.example.com.squawker.provider.SquawkProvider;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Dan on 29/10/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.i(TAG, String.format("Squawk from %s", remoteMessage.getFrom()));
        Map<String, String> data = remoteMessage.getData();

        if(data.size() > 0){
            Observable.just(data)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Observer<Map<String, String>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Map<String, String> squawkMap) {
                            ContentValues squawk = new ContentValues();
                            squawk.put(SquawkContract.COLUMN_AUTHOR, squawkMap.get(SquawkContract.COLUMN_AUTHOR));
                            squawk.put(SquawkContract.COLUMN_ID, squawkMap.get(SquawkContract.COLUMN_ID));
                            squawk.put(SquawkContract.COLUMN_AUTHOR_KEY, squawkMap.get(SquawkContract.COLUMN_AUTHOR_KEY));
                            squawk.put(SquawkContract.COLUMN_MESSAGE, squawkMap.get(SquawkContract.COLUMN_MESSAGE));
                            squawk.put(SquawkContract.COLUMN_DATE, squawkMap.get(SquawkContract.COLUMN_DATE));
                            getContentResolver().insert(SquawkProvider.SquawkMessages.CONTENT_URI, squawk);


                            Intent intent = new Intent(MyFirebaseMessagingService.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                            PendingIntent pendingIntent = PendingIntent.getActivity(MyFirebaseMessagingService.this,
                                    0 /* request code */, intent, PendingIntent.FLAG_ONE_SHOT);

                            String author = squawkMap.get(SquawkContract.COLUMN_AUTHOR);
                            String message = squawkMap.get(SquawkContract.COLUMN_MESSAGE);
                            if(message.length() > 30){
                                message = message.substring(0, 30) + "\u2026";
                            }

                            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(MyFirebaseMessagingService.this)
                                    .setSmallIcon(R.drawable.ic_duck)
                                    .setContentTitle(String.format(getString(R.string.notification_message), author))
                                    .setContentText(message)
                                    .setAutoCancel(true)
                                    .setSound(defaultSoundUri)
                                    .setContentIntent(pendingIntent);

                            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                            manager.notify(0 /* Id of notification */, builder.build());
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }


}
