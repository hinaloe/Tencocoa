package org.kb10uy.tencocoa;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;

import org.kb10uy.tencocoa.model.TwitterAccountInformation;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.RequestToken;


public class TencocoaReadPermissionService extends Service {

    private static final int TENCOCOA_WRITE_PERMISSION_NOTIFICATION_ID = 0xC0C0A3;

    private TwitterAccountInformation currentUser;
    private Twitter mTwitter;
    private NotificationManager mNotificationManager;
    private TencocoaReadPermissionServiceBinder mBinder = new TencocoaReadPermissionServiceBinder();
    private RequestToken storedToken;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String ck = pref.getString(getString(R.string.preference_twitter_consumer_key), "");
        String cs = pref.getString(getString(R.string.preference_twitter_consumer_secret), "");
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void showNotification(int tickerStringId, int descriptionStringId) {
        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.tencocoa_notify)
                .setTicker(getString(tickerStringId))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(descriptionStringId));
        mNotificationManager.cancelAll();
        mNotificationManager.notify(TENCOCOA_WRITE_PERMISSION_NOTIFICATION_ID, builder.build());
    }

    private void showNotification(String tickerString, String descriptionText) {
        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.tencocoa_notify)
                .setTicker(tickerString)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(descriptionText);
        mNotificationManager.cancelAll();
        mNotificationManager.notify(TENCOCOA_WRITE_PERMISSION_NOTIFICATION_ID, builder.build());
    }

    public void setTarget(Twitter twitter, TwitterAccountInformation info) {
        currentUser = info;
        mTwitter = twitter;
    }

    public User getTargetUser() {
        if (mTwitter == null) return null;
        try {
            return mTwitter.users().showUser(currentUser.getUserId());
        } catch (TwitterException te) {
            //Log.d(getString(R.string.app_name), te.getErrorMessage());
        }
        return null;
    }

    public List<Status> getLatestHomeTimeline(int count) {
        try {
            return mTwitter.timelines().getHomeTimeline(new Paging(1, count));
        } catch (TwitterException e) {
            //Log.d(getString(R.string.app_name), e.getErrorMessage());
        }
        return new ArrayList<>();
    }

    public void storeOAuthRequestToken(RequestToken token) {
        storedToken = token;
    }

    public RequestToken recallOAuthRequestToken() {
        return storedToken;
    }

    public class TencocoaReadPermissionServiceBinder extends Binder {
        public TencocoaReadPermissionService getService() {
            return TencocoaReadPermissionService.this;
        }
    }
}
