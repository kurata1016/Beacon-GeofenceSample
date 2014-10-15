package jp.co.strawbag.beaconsample;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

public class DistanceBeaconActivity extends Activity {

	private static final String TAG = DistanceBeaconActivity.class.getSimpleName();

	private static final double RELATIVE_START_POS = 320.0 / 1110.0;
	private static final double RELATIVE_STOP_POS = 885.0 / 1110.0;

	private BeaconManager beaconManager;
	private Beacon beacon;
	private Region region;

	private View dotView;
	private int startY = -1;
	private int segmentLength = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.distance_view);
		dotView = findViewById(R.id.dot);

		beacon = getIntent().getParcelableExtra(ListBeaconsActivity.EXTRAS_BEACON);
		region = new Region("regionid", beacon.getProximityUUID(), beacon.getMajor(), beacon.getMinor());
		if (beacon == null) {
			Toast.makeText(this, "Beacon not found in intent extras", Toast.LENGTH_LONG).show();
			finish();
		}

		beaconManager = new BeaconManager(this);
		beaconManager.setRangingListener(new BeaconManager.RangingListener() {

			@Override
			public void onBeaconsDiscovered(Region region, final List<Beacon> rangedBeacons) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Beacon foundBeacon = null;
						for (Beacon rangedBeacon : rangedBeacons) {
							if (rangedBeacon.getMacAddress().equals(beacon.getMacAddress())) {
								foundBeacon = rangedBeacon;
							}
						}
						if (foundBeacon != null) {
							updateDistanceView(foundBeacon);
						}
					}
				});
			}
		});

		final View view = findViewById(R.id.sonar);
		view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

				startY = (int) (RELATIVE_START_POS * view.getMeasuredHeight());
				int stopY = (int) (RELATIVE_STOP_POS * view.getMeasuredHeight());
				segmentLength = stopY - startY;

				dotView.setVisibility(View.VISIBLE);
				dotView.setTranslationY(computeDotPosY(beacon));
			}
		});
	}

	private void updateDistanceView(Beacon foundBeacon) {
		if (segmentLength == -1) {
			return;
		}

		dotView.animate().translationY(computeDotPosY(foundBeacon)).start();
	}

	private int computeDotPosY(Beacon beacon) {
		double distance = Math.min(Utils.computeAccuracy(beacon), 6.0);
		return startY + (int) (segmentLength * (distance / 6.0));
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
	protected void onStart() {
		super.onStart();

		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {

			@Override
			public void onServiceReady() {
				try {
					beaconManager.startRanging(region);
				} catch (RemoteException e) {
					Toast.makeText(DistanceBeaconActivity.this, "Cannot start ranging, something terrible happened",
							Toast.LENGTH_LONG).show();
					Log.e(TAG, "Cannot start ranging", e);
				}
			}
		});
	}

	@Override
	protected void onStop() {
		beaconManager.disconnect();
		super.onStop();
	}
}
