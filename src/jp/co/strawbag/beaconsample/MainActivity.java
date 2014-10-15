package jp.co.strawbag.beaconsample;

import jp.co.strawbag.geofencesample.GeoFenceSampleActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends Activity {
	static int flag = 2;
	final static String IMMEDIATE = "IMMEDIATE";
	final static String NEAR = "NEAR";
	final static String FAR = "FAR";
	final static String KEY = "key";
	SharedPreferences pref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.all_demos);

		pref = PreferenceManager.getDefaultSharedPreferences(this);
		findViewById(R.id.distance_demo_button).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, ListBeaconsActivity.class);
				intent.putExtra(ListBeaconsActivity.EXTRAS_TARGET_ACTIVITY, DistanceBeaconActivity.class.getName());
				startActivity(intent);
			}
		});
		findViewById(R.id.geofence_demo_button).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,GeoFenceSampleActivity.class);
				startActivity(intent);
			}
		});
		//		findViewById(R.id.notify_demo_button).setOnClickListener(new OnClickListener() {
		//
		//			@Override
		//			public void onClick(View v) {
		//				Intent intent = new Intent(MainActivity.this, ListBeaconsActivity.class);
		//				intent.putExtra(ListBeaconsActivity.EXTRAS_TARGET_ACTIVITY, NotifyDemoActivity.class.getName());
		//				startActivity(intent);
		//			}
		//		});
		//		findViewById(R.id.characteristics_demo_button).setOnClickListener(new OnClickListener() {
		//
		//			@Override
		//			public void onClick(View v) {
		//				Intent intent = new Intent(MainActivity.this, ListBeaconsActivity.class);
		//				intent.putExtra(ListBeaconsActivity.EXTRAS_TARGET_ACTIVITY, CharacteristicsDemoActivity.class.getName());
		//				startActivity(intent);
		//			}
		//		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Beacon設定");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final CharSequence[] items = { IMMEDIATE, NEAR, FAR };
		final Editor editor = pref.edit();

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("Proximity Setting");
		if (pref.getString(KEY, FAR).equals(IMMEDIATE)) {
			flag = 0;
		} else if (pref.getString(KEY, FAR).equals(NEAR)) {
			flag = 1;
		} else {
			flag = 2;
		}
		dialog.setSingleChoiceItems(items, flag, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					editor.putString(KEY, IMMEDIATE);
					Toast.makeText(MainActivity.this, "設定をImmediateに変更しました", Toast.LENGTH_SHORT).show();
					flag = 0;
					break;
				case 1:
					editor.putString(KEY, NEAR);
					Toast.makeText(MainActivity.this, "設定をNearに変更しました", Toast.LENGTH_SHORT).show();
					flag = 1;
					break;
				case 2:
					editor.putString(KEY, FAR);
					Toast.makeText(MainActivity.this, "設定をFarに変更しました", Toast.LENGTH_SHORT).show();
					flag = 2;
					break;
				}
				editor.commit();
				dialog.dismiss();
			}
		});
		dialog.create().show();
		return true;
	}
}
