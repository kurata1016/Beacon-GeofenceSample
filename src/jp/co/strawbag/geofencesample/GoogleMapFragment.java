package jp.co.strawbag.geofencesample;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.co.strawbag.beaconsample.R;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Google map表示クラス
 *
 */
public class GoogleMapFragment extends Fragment implements GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationClient.OnAddGeofencesResultListener, LocationListener,
		GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

	private static final String TAG = GoogleMapFragment.class.getSimpleName();
	private static boolean LocationCenteringFlag = false;

	/** google map */
	private GoogleMap mGoogleMap;

	/** プリファレンス */
	private SharedPreferences mPrefs;

	/** LocationClient */
	private LocationClient mLocationClient;

	/** 地図タップ位置の緯度経度 */
	private LatLng mTappedLatlng;

	/** タップされたマーカーのインスタンス保持 */
	private Marker mTappedMarker;

	/** ジオフェンス情報を保持するオブジェクト */
	private SimpleGeofenceStore mGeofenceStore;

	/** サークル保持するマップ */
	private Map<LatLng, Circle> circles = new HashMap<LatLng, Circle>();

	/** CirclesのKey用List */
	private List<LatLng> keys = new ArrayList<LatLng>();

	/** マップズームサイズ */
	private static final int MAP_ZOOM_SIZE = 16;

	/** フェンスの範囲(メートル) */
	public static int GEO_FENCE_RADIUS = 100;

	private int Id = 0;
	private String UId = "ID:" + Id;

	/**
	 *
	 */
	public GoogleMapFragment() {
		super();
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_google_maps, container);
		return view;
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// マップフラグメント取得
		SupportMapFragment fragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
				.findFragmentById(R.id.fragment_map);
		mGoogleMap = fragment.getMap();
		mGoogleMap.setMyLocationEnabled(true);

		// インスタンス取得
		mGeofenceStore = new SimpleGeofenceStore(getActivity());
		mLocationClient = new LocationClient(getActivity(), this, this);
		mPrefs = mGeofenceStore.getPref();

		// リスナー設置
		mGoogleMap.setOnMapClickListener(this);
		mGoogleMap.setOnMarkerClickListener(this);
		mGoogleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

			@Override
			public void onMyLocationChange(Location location) {
				if (LocationCenteringFlag == false) {
					LocationCenteringFlag = true;
					// 現在地表示
					CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), MAP_ZOOM_SIZE);
					mGoogleMap.animateCamera(cameraUpdate);
				}
			}
		});

		// ピンを立てる
		MarkerOptions options = new MarkerOptions();
		options.position(new LatLng(35.641794, 139.70114));
		options.title("株式会社ストローバッグ");
		mGoogleMap.addMarker(options);

		// 登録済みジオフェンスのサークル描画
		for (int i = 0; i < keys.size(); i++) {
			Circle circle = mGoogleMap.addCircle(new CircleOptions().center(keys.get(i)).radius(GEO_FENCE_RADIUS).strokeColor(Color.parseColor("#FF0000"))
					.fillColor(Color.parseColor("#11FF0000")));
			circle.setVisible(true);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mLocationClient.disconnect();
		LocationCenteringFlag = false;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		LocationCenteringFlag = false;
		mPrefs.edit().clear().commit();
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see
	 * com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener
	 * #onAddGeofencesResult(int, java.lang.String[])
	 */
	@Override
	public void onAddGeofencesResult(int statusCode, String[] arg1) {
		// If adding the geocodes was successful
		if (LocationStatusCodes.SUCCESS == statusCode) {
			Log.d(TAG, "SUCCESS");
		}

		for (String string : arg1) {
			Log.d(TAG, string);
		}
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see com.google.android.gms.common.GooglePlayServicesClient.
	 * OnConnectionFailedListener
	 * #onConnectionFailed(com.google.android.gms.common.ConnectionResult)
	 */
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see
	 * com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks
	 * #onConnected(android.os.Bundle)
	 */
	@Override
	public void onConnected(Bundle bundle) {
		Log.d(TAG, "onConnected");

		// GPSの位置情報リスナー登録
		LocationRequest request = LocationRequest.create();
		// 1秒後毎に計測
		request.setFastestInterval(1000);
		request.setInterval(1000);
		request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		mLocationClient.requestLocationUpdates(request, this);

		// ジオフェンス登録
		addGeoFences(mTappedLatlng);
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see
	 * com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks
	 * #onDisconnected()
	 */
	@Override
	public void onDisconnected() {
		Log.d(TAG, "onDisconnected");
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see
	 * com.google.android.gms.location.LocationListener#onLocationChanged(android
	 * .location.Location)
	 */
	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "onLocationChanged");

		//		LatLng latlng = new LatLng(location.getLatitude(),
		//				location.getLongitude());
		//		CameraUpdate camera = CameraUpdateFactory.newCameraPosition(new
		//				CameraPosition.Builder().target(latlng).zoom(16).build());
		//		//		 地図移動
		//		mGoogleMap.animateCamera(camera);
	}

	@Override
	public void onMapClick(final LatLng latlng) {
		// ダイアログ表示
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("ジオフェンス登録");
		builder.setMessage("この場所をジオフェンスに登録しますか？");
		builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				mTappedLatlng = latlng;

				String address = getAddress(latlng);

				// ピンを立てる
				MarkerOptions options = new MarkerOptions();
				options.position(latlng);
				options.title(address);

				mGoogleMap.addMarker(options);

				// 地図移動
				CameraUpdate camera = CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(latlng).zoom(MAP_ZOOM_SIZE).build());
				mGoogleMap.animateCamera(camera);

				// サークル描画
				Circle circle = mGoogleMap.addCircle(new CircleOptions().center(latlng).radius(GEO_FENCE_RADIUS).strokeColor(Color.parseColor("#FF0000"))
						.fillColor(Color.parseColor("#11FF0000")));
				keys.add(latlng);
				circles.put(latlng, circle);

				// Connects the client to Google Play services.
				mLocationClient.connect();
			}
		});
		builder.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		builder.setCancelable(true);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void addGeoFences(LatLng latlng) {
		String address;
		if (mTappedMarker == null) {
			// 緯度経度から住所を取得
			address = getAddress(latlng);
			if (TextUtils.isEmpty(address) == true) {
				return;
			}
		} else {
			address = mTappedMarker.getTitle();
			mTappedMarker = null;
		}

		// ジオフェンスリスト
		ArrayList<Geofence> fenceList = new ArrayList<Geofence>();

		// ジオフェンス情報をプリファレンスに保存
		SimpleGeofence geofence = new SimpleGeofence(UId, latlng.latitude, latlng.longitude, GEO_FENCE_RADIUS, Geofence.NEVER_EXPIRE,
				(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT), address);
		mGeofenceStore.setGeofence(UId, geofence);
		Id++;
		UId = "ID:" + Id;

		fenceList.add(geofence.toGeofence());

		// ジオフェンスイベント受信インテント
		Intent intent = new Intent(getActivity(), GeofenceService.class);
		intent.putExtra(geofence.getId(), "hogehoge");
		PendingIntent pendingIntent = PendingIntent.getService(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// ジオフェンス登録
		mLocationClient.addGeofences(fenceList, pendingIntent, this);

		Locale locale = Locale.getDefault();
		String strLatitude = String.format(locale, "%.2f", latlng.latitude);
		String strLongtitude = String.format(locale, "%.2f", latlng.longitude);
		StringBuilder sb = new StringBuilder();
		sb.append(address).append("(").append(strLatitude).append(",").append(strLongtitude).append(")をジオフェンスに登録しました");
		Toast.makeText(getActivity(), sb.toString(), Toast.LENGTH_SHORT).show();
	}

	/**
	 * 緯度経度から住所を取得する
	 *
	 * @param latlng
	 * @return
	 */
	private String getAddress(LatLng latlng) {
		String address = null;

		try {
			Geocoder geoCorder = new Geocoder(getActivity());
			List<Address> addressList = geoCorder.getFromLocation(latlng.latitude, latlng.longitude, 1);
			if (addressList != null && addressList.size() != 0) {
				Address addr = addressList.get(0);
				address = addr.getAdminArea() + addr.getLocality() + addr.getSubLocality() + addr.getThoroughfare()
						+ addr.getSubThoroughfare();
			}
		} catch (IOException e) {
			Log.d(TAG, e.getMessage());
		}

		return address;
	}

	@Override
	public boolean onMarkerClick(final Marker marker) {
		mTappedLatlng = marker.getPosition();
		double tapLat = MyRound(mTappedLatlng.latitude);
		double tapLon = MyRound(mTappedLatlng.longitude);
		SimpleGeofence PrefGeoFence;

		if (circles.containsKey(mTappedLatlng)) {
			for (int i = 0; i < Id; i++) {
				final String geofenceKey = "ID:" + i;
				PrefGeoFence = mGeofenceStore.getGeofence(geofenceKey);
				if (PrefGeoFence != null) {
					double prefLat = MyRound(PrefGeoFence.getLatitude());
					double prefLon = MyRound(PrefGeoFence.getLongitude());

					if (prefLat == tapLat && prefLon == tapLon) {
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setTitle("ジオフェンス削除");
						builder.setMessage(marker.getTitle() + "をジオフェンスから削除しますか？");
						builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// プリファレンスから該当ジオフェンス削除
								mGeofenceStore.clearGeofence(geofenceKey);

								// サークル削除
								Circle tappedCircle = circles.get(mTappedLatlng);
								tappedCircle.remove();
								circles.remove(mTappedLatlng);

								// ストローバッグのマーカーは削除しない
								if(!marker.getTitle().equals("株式会社ストローバッグ"))
								// マーカー削除
								marker.remove();

								Toast.makeText(getActivity(), marker.getTitle() + "のジオフェンスを削除しました", Toast.LENGTH_SHORT).show();
							}
						});
						builder.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						});

						builder.setCancelable(true);
						AlertDialog dialog = builder.create();
						dialog.show();
					}
				}
			}
		} else {

			// ダイアログ表示
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("ジオフェンス登録");
			builder.setMessage(marker.getTitle() + "をジオフェンスに登録しますか？");
			builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Circle circle = mGoogleMap.addCircle(new CircleOptions().center(mTappedLatlng).radius(GEO_FENCE_RADIUS).strokeColor(Color.parseColor("#FF0000"))
							.fillColor(Color.parseColor("#11FF0000")));
					keys.add(mTappedLatlng);
					circles.put(mTappedLatlng, circle);
					mTappedMarker = marker;

					mLocationClient.connect();
				}
			});
			builder.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});

			builder.setCancelable(true);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		return false;
	}

	public double MyRound(double val) {
		BigDecimal bd = new BigDecimal(val);
		return bd.setScale(3, BigDecimal.ROUND_DOWN).doubleValue();
	}
}
