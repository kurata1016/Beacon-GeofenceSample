package jp.co.strawbag.beaconsample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.estimote.sdk.Utils.Proximity;
import com.estimote.sdk.utils.L;

public class ListBeaconsActivity extends Activity {

	private static final String TAG = ListBeaconsActivity.class.getSimpleName();

	public static final String EXTRAS_TARGET_ACTIVITY = "extrasTargetActivity";
	public static final String EXTRAS_BEACON = "extrasBeacon";

	private static final int NOTIFICATION_ID = 123;
	private static final int REQUEST_ENABLE_BT = 1234;
	private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);

	private BeaconManager beaconManager;
	private NotificationManager notificationManager;
	private LeDeviceListAdapter adapter;

	final static String IMMEDIATE = "IMMEDIATE";
	final static String NEAR = "NEAR";
	final static String FAR = "FAR";
	final static String KEY = "key";
	String recentMsg = "初期設定";
	boolean dialogFlagImmediat = false;
	boolean dialogFlagNear = false;
	boolean dialogFlagFar = false;
	Beacon setBeacon;
	SharedPreferences pref;

	Utils.Proximity proximity;
	Utils.Proximity setting;
	List<Beacon> setBeacons;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		dialogFlagImmediat = false;
		dialogFlagNear = false;
		dialogFlagFar = false;
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		setBeacons = new ArrayList<Beacon>();

		adapter = new LeDeviceListAdapter(this);
		ListView list = (ListView) findViewById(R.id.device_list);
		list.setAdapter(adapter);
		list.setOnItemClickListener(createOnItemClickListener());

		L.enableDebugLogging(true);

		if (pref.contains(KEY)) {
			setting = Utils.Proximity.valueOf(pref.getString(KEY, FAR));
		} else {
			setting = Proximity.FAR;
		}

		beaconManager = new BeaconManager(this);
		beaconManager.setRangingListener(new BeaconManager.RangingListener() {

			@Override
			public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
				setBeacons.clear();

				switch (setting) {
				case IMMEDIATE:
					for (int i = 0; i < beacons.size(); i++) {
						setBeacon = beacons.get(i);
						proximity = Utils.proximityFromAccuracy(Utils.computeAccuracy(setBeacon));
						if (proximity == Proximity.IMMEDIATE) {
							setBeacons.add(setBeacon);
							postNotification(setBeacon.getName() + "のImmediate領域に入りました");
							if (!dialogFlagImmediat) {
								showDialog("受付完了しました\n\n現在の待ち人数は10人です\n\n前回の通院日は2014年9月2日です");
								dialogFlagImmediat = true;
							}
						} else {
							postNotification(setBeacon.getName() + "のImmediate領域から出ました");
						}
					}
					break;
				case NEAR:
					for (int i = 0; i < beacons.size(); i++) {
						setBeacon = beacons.get(i);
						proximity = Utils.proximityFromAccuracy(Utils.computeAccuracy(setBeacon));
						if (proximity == Proximity.NEAR) {
							setBeacons.add(setBeacon);
							postNotification(setBeacon.getName() + "のNear領域に入りました");
							if (!dialogFlagNear) {
								showDialog("STB病院の現在の待ち人数は10人です\n\n前回の通院日は2014年9月2日です");
								dialogFlagNear = true;
							}
						} else if (proximity == Proximity.IMMEDIATE) {
							setBeacons.add(setBeacon);
							postNotification(setBeacon.getName() + "のImmediate領域に入りました");
							if (!dialogFlagImmediat) {
								showDialog("受付完了しました\n\n現在の待ち人数は10人です\n\n前回の通院日は2014年9月2日です");
								dialogFlagImmediat = true;
							}
						} else {
							postNotification(setBeacon.getName() + "のNear領域から出ました");
						}
					}
					break;
				case FAR:
					for (int i = 0; i < beacons.size(); i++) {
						setBeacon = beacons.get(i);
						proximity = Utils.proximityFromAccuracy(Utils.computeAccuracy(setBeacon));
						if (proximity == Proximity.FAR) {
							setBeacons.add(setBeacon);
							postNotification(setBeacon.getName() + "のFar領域に入りました");
							if (!dialogFlagFar) {
								showDialog("STB病院の近くです\n\n現在の待ち人数は10人です\n\n前回の通院日は2014年9月2日です");
								dialogFlagFar = true;
							}
						} else if (proximity == Proximity.NEAR) {
							setBeacons.add(setBeacon);
							postNotification(setBeacon.getName() + "のNear領域に入りました");
							if (!dialogFlagNear) {
								showDialog("STB病院の現在の待ち人数は10人です\n\n前回の通院日は2014年9月2日です");
								dialogFlagNear = true;
							}
						} else if (proximity == Proximity.IMMEDIATE) {
							setBeacons.add(setBeacon);
							postNotification(setBeacon.getName() + "のImmediate領域に入りました");
							if (!dialogFlagImmediat) {
								showDialog("受付完了しました\n\n現在の待ち人数は10人です\n\n前回の通院日は2014年9月2日です");
								dialogFlagImmediat = true;
							}
						} else {
							postNotification(setBeacon.getName() + "のFar領域から出ました");
						}
					}
					break;
				case UNKNOWN:
					break;
				}

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						getActionBar().setSubtitle("Found beacons: " + setBeacons.size());
						adapter.replaceWith(setBeacons);
					}
				});
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.scan_menu, menu);
		MenuItem refreshItem = menu.findItem(R.id.refresh);
		refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		notificationManager.cancel(NOTIFICATION_ID);
		beaconManager.disconnect();
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (!beaconManager.hasBluetooth()) {
			Toast.makeText(this, "このデバイスはBLEに対応してません", Toast.LENGTH_SHORT).show();
			return;
		}

		if (!beaconManager.isBluetoothEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			connectToService();
		}
	}

	@Override
	protected void onStop() {
		try {
			beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
		} catch (RemoteException e) {
			Log.d(TAG, "Error while stopping ranging", e);
		}
		super.onStop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_OK) {
				connectToService();
			} else {
				Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_LONG).show();
				getActionBar().setSubtitle("Bluetooth not enabled");
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void connectToService() {
		getActionBar().setSubtitle("スキャンしています・・・");
		adapter.replaceWith(Collections.<Beacon> emptyList());
		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {

			@Override
			public void onServiceReady() {
				try {
					beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
				} catch (RemoteException e) {
					Toast.makeText(ListBeaconsActivity.this, "Cannot start ranging, something terrible happened", Toast.LENGTH_SHORT).show();
					Log.e(TAG, "Cannot start ranging", e);
				}
			}
		});
	}

	private AdapterView.OnItemClickListener createOnItemClickListener() {
		return new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (getIntent().getStringExtra(EXTRAS_TARGET_ACTIVITY) != null) {
					try {
						Class<?> clazz = Class.forName(getIntent().getStringExtra(EXTRAS_TARGET_ACTIVITY));
						Intent intent = new Intent(ListBeaconsActivity.this, clazz);
						intent.putExtra(EXTRAS_BEACON, adapter.getItem(position));
						startActivity(intent);
					} catch (ClassNotFoundException e) {
						Log.e(TAG, "Finding class by name failed", e);
					}
				}
			}
		};
	}

	private void postNotification(String msg) {
		if (!(recentMsg.equals(msg))) {
			Intent notifyIntent = new Intent(ListBeaconsActivity.this, ListBeaconsActivity.class);
			notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent pendingIntent = PendingIntent.getActivities(
					ListBeaconsActivity.this,
					0,
					new Intent[] { notifyIntent },
					PendingIntent.FLAG_UPDATE_CURRENT);
			Notification notification = new Notification.Builder(ListBeaconsActivity.this)
					.setSmallIcon(R.drawable.beacon_gray)
					.setTicker(msg)
					.setContentTitle("Beacon通知")
					.setContentText(msg)
					.setAutoCancel(true)
					.setContentIntent(pendingIntent)
					.build();
			notification.defaults |= Notification.DEFAULT_SOUND;
			notification.defaults |= Notification.DEFAULT_LIGHTS;
			notificationManager.notify(NOTIFICATION_ID, notification);
			recentMsg = msg;
		}
	}

	private void showDialog(String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("STB病院からのお知らせ");
		builder.setMessage(msg);
		builder.setPositiveButton("確認", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.setCancelable(true);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

}
