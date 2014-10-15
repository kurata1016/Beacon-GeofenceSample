package jp.co.strawbag.geofencesample;

import jp.co.strawbag.beaconsample.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class GeoFenceSampleActivity extends FragmentActivity {

	private static final String TAG = GeoFenceSampleActivity.class.getSimpleName();
	private static int flag = 1;

	/*
	 * (非 Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");

		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_geo_fence_sample);
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("ジオフェンス範囲設定");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final CharSequence[] items = { "50m", "100m", "200m", "400m" };

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("ジオフェンス範囲");

		switch (GoogleMapFragment.GEO_FENCE_RADIUS) {
		case 50:
			flag = 0;
			break;
		case 100:
			flag = 1;
			break;
		case 200:
			flag = 2;
			break;
		case 400:
			flag = 3;
			break;
		}

		dialog.setSingleChoiceItems(items, flag, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					changeValue(50);
					Toast.makeText(GeoFenceSampleActivity.this, "ジオフェンスの範囲を50mに変更しました", Toast.LENGTH_SHORT).show();
					break;
				case 1:
					changeValue(100);
					Toast.makeText(GeoFenceSampleActivity.this, "ジオフェンスの範囲を100mに変更しました", Toast.LENGTH_SHORT).show();
					break;
				case 2:
					changeValue(200);
					Toast.makeText(GeoFenceSampleActivity.this, "ジオフェンスの範囲を200mに変更しました", Toast.LENGTH_SHORT).show();
					break;
				case 3:
					changeValue(400);
					Toast.makeText(GeoFenceSampleActivity.this, "ジオフェンスの範囲を400mに変更しました", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		});
		dialog.create().show();
		return true;
	}

	private void changeValue(int value) {
		GoogleMapFragment.GEO_FENCE_RADIUS = value;
	}
}
