package jp.co.strawbag.geofencesample;

import java.util.List;

import jp.co.strawbag.beaconsample.R;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

/**
 * ジオフェンスのENTER/EXITの通知を受け取るサービスクラス
 *
 */
public class GeofenceService extends IntentService {

	public static final String TAG = GeofenceService.class.getSimpleName();

	/**
	 *  コンストラクタ
	 */
	public GeofenceService() {
		super("GeofenceService");
	}

	/*
	 * (非 Javadoc)
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent start");

		try {
			// First check for errors
			if (LocationClient.hasError(intent)) {

				// Get the error code with a static method
				int errorCode = LocationClient.getErrorCode(intent);

				// Log the error
				Log.e("ReceiveTransitionsIntentService", "Location Services error: " + Integer.toString(errorCode));
				/*
				 * You can also send the error code to an Activity or Fragment
				 * with a broadcast Intent
				 */
				/*
				 * If there's no error, get the transition type and the IDs of
				 * the geofence or geofences that triggered the transition
				 */
			} else {
				// ジオフェンスターゲット取得
				List<Geofence> triggerList = LocationClient.getTriggeringGeofences(intent);
				if (triggerList == null) {
					Log.d(TAG, "onHandleIntent end. trigger is null");
					return;
				}

				for (Geofence geofence : triggerList) {
					// 通知用インテント作成
//					Intent notifyIntent = new Intent(getApplicationContext(), GeoFenceSampleActivity.class);
					Intent notifyIntent = new Intent(getApplicationContext(),getClass());
					PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notifyIntent,
							PendingIntent.FLAG_UPDATE_CURRENT);

					// このジオフェンス通知の情報をプリファレンスから取得
					SimpleGeofenceStore store = new SimpleGeofenceStore(getApplicationContext());
					SimpleGeofence simpleGeofence = store.getGeofence(geofence.getRequestId());

					// 通知作成
					Notification notification;
					Notification.Builder builder = new Notification.Builder(getApplicationContext());
					builder.setContentIntent(pendingIntent)
							.setSmallIcon(R.drawable.ic_launcher)
							.setWhen(System.currentTimeMillis())
							.setAutoCancel(true);
					notification = builder.build();
					notification.vibrate = new long[] { 0, 200, 100, 200, 100, 200 };

					// Get the type of transition (entry or exit)
					int transitionType = LocationClient.getGeofenceTransition(intent);

					// 領域に入った場合
					if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
						Log.d(TAG, "GEOFENCE_TRANSITION_ENTER");

						notification.tickerText = simpleGeofence.getPointName() + "に入りました";
						notification.setLatestEventInfo(getApplicationContext(), "ジオフェンス通知", simpleGeofence.getPointName() + "に入りました",
								pendingIntent);

						// 領域から出た場合
					} else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
						Log.d(TAG, "GEOFENCE_TRANSITION_EXIT");

						notification.tickerText = simpleGeofence.getPointName() + "から出ました";
						notification.setLatestEventInfo(getApplicationContext(), "ジオフェンス通知", simpleGeofence.getPointName() + "から出ました",
								pendingIntent);

						// An invalid transition was reported
					} else {
						/*
						 * At this point, you can store the IDs for further use
						 * display them, or display the details associated with
						 * them.
						 */
						Log.e("ReceiveTransitionsIntentService", "Geofence transition error: " + Integer.toString(transitionType));
						continue;
					}

					// 通知
					NotificationManager notificationManager = (NotificationManager) getApplication().getSystemService(
							Context.NOTIFICATION_SERVICE);
					notificationManager.notify(R.string.app_name, notification);
				}
			}
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
		}

		Log.d(TAG, "onHandleIntent end");
	}
}
