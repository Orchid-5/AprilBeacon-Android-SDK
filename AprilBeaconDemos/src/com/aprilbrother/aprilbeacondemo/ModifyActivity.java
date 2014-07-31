package com.aprilbrother.aprilbeacondemo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aprilbrother.aprilbrothersdk.Beacon;
import com.aprilbrother.aprilbrothersdk.BeaconManager;
import com.aprilbrother.aprilbrothersdk.BeaconManager.MonitoringListener;
import com.aprilbrother.aprilbrothersdk.Region;
import com.aprilbrother.aprilbrothersdk.connection.AprilBeaconCharacteristics;
import com.aprilbrother.aprilbrothersdk.connection.AprilBeaconCharacteristics.MyReadCallBack;
import com.aprilbrother.aprilbrothersdk.connection.AprilBeaconConnection;
import com.aprilbrother.aprilbrothersdk.connection.AprilBeaconConnection.MyWriteCallback;

public class ModifyActivity extends Activity implements OnClickListener {

	protected static final String TAG = "ModifyActivity";
	private EditText uuid;
	private EditText major;
	private EditText minor;
	private EditText password;
	private Beacon beacon;

	private AprilBeaconConnection conn;
	private TextView tv_battery;
	private TextView tv_txpower;
	private TextView tv_advinterval;
	private TextView tv_firmwareRevision;
	private TextView tv_manufacturerName;
	private String oldPassword;

	private Button btn_modify, btn_battery, btn_txpower, btn_advinterval,
			btn_firmwareRevision, btn_manufacturerName;

	private AprilBeaconCharacteristics read;

	private BeaconManager beaconManager;

	private static final Region ALL_BEACONS_REGION = new Region("apr", null,
			null, null);

	final String URL_Post = "http://bbs.aprbrother.com";
	final String URL = "http://bbs.aprbrother.com";

	public final static Uri URI = Uri.parse("http://aprbrother.com");

	private String resultData = "";
	private HttpURLConnection urlConn = null;

	private boolean isPost = true;

	public static final int READ_BATTERY = 0;
	public static final int READ_TXPOWER = 1;
	public static final int READ_ADVINTERVAL = 2;
	public static final int READ_FW_REVISON = 3;
	public static final int READ_MANUFACTURER = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_modify);
		super.onCreate(savedInstanceState);
		initView();
	}

	private void initView() {

		uuid = (EditText) findViewById(R.id.uuid);
		major = (EditText) findViewById(R.id.major);
		minor = (EditText) findViewById(R.id.minor);
		password = (EditText) findViewById(R.id.password);

		tv_battery = (TextView) findViewById(R.id.battery);
		tv_txpower = (TextView) findViewById(R.id.txpower);
		tv_advinterval = (TextView) findViewById(R.id.advinterval);
		tv_firmwareRevision = (TextView) findViewById(R.id.firmwareRevision);
		tv_manufacturerName = (TextView) findViewById(R.id.manufacturerName);

		btn_modify = (Button) findViewById(R.id.btn_modify);
		btn_battery = (Button) findViewById(R.id.btn_battery);
		btn_txpower = (Button) findViewById(R.id.btn_txpower);
		btn_advinterval = (Button) findViewById(R.id.btn_advinterval);
		btn_firmwareRevision = (Button) findViewById(R.id.btn_firmwareRevision);
		btn_manufacturerName = (Button) findViewById(R.id.btn_manufacturerName);

		btn_modify.setOnClickListener(this);
		btn_battery.setOnClickListener(this);
		btn_txpower.setOnClickListener(this);
		btn_advinterval.setOnClickListener(this);
		btn_firmwareRevision.setOnClickListener(this);
		btn_manufacturerName.setOnClickListener(this);

		Bundle bundle = getIntent().getExtras();
		beacon = bundle.getParcelable("beacon");

		Log.i(TAG, beacon.getMacAddress());
		Log.i(TAG, beacon.getMajor() + "");
		Log.i(TAG, beacon.getMinor() + "");

		String proximityUUID = beacon.getProximityUUID();
		uuid.setHint(proximityUUID);
		major.setHint(beacon.getMajor() + "");
		minor.setHint(beacon.getMinor() + "");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_modify:
			showEnterDialog();
			break;
		case R.id.btn_battery:
			setBattery();
			break;
		case R.id.btn_txpower:
			setTxPower();
			break;
		case R.id.btn_advinterval:
			setAdvinterval();
			break;
		case R.id.btn_firmwareRevision:
			setFWRevision();
			break;
		case R.id.btn_manufacturerName:
			setManufacturer();
			break;

		default:
			break;
		}

	}

	private void setAdvinterval() {
		read = new AprilBeaconCharacteristics(this, beacon);
		read.connectGattToRead(new MyReadCallBack() {
			@Override
			public void readyToGetAdvinterval() {
				final Integer advinterval;
				try {
					advinterval = read.getAdvinterval();
					ModifyActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							tv_advinterval.setText(advinterval + "00ms");
							read.close();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, READ_ADVINTERVAL);
	}

	private void setTxPower() {
		read = new AprilBeaconCharacteristics(this, beacon);
		read.connectGattToRead(new MyReadCallBack() {
			@Override
			public void readyToGetTxPower() {
				final Integer txPower;
				try {
					txPower = read.getTxPower();
					ModifyActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							switch (txPower) {
							case 0:
								tv_txpower.setText("0dbm");
								read.close();
								break;
							case 1:
								tv_txpower.setText("4dbm");
								read.close();
								break;
							case 2:
								tv_txpower.setText("-6dbm");
								read.close();
								break;
							case 3:
								tv_txpower.setText("-23dbm");
								read.close();
								break;
							default:
								break;
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, READ_TXPOWER);
	}

	private void setBattery() {
		read = new AprilBeaconCharacteristics(this, beacon);
		read.connectGattToRead(new MyReadCallBack() {
			@Override
			public void readyToGetBattery() {
				final Integer battery;
				try {
					battery = read.getBattery();
					ModifyActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							tv_battery.setText(battery + "%");
							read.close();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, READ_BATTERY);
	}

	private void setFWRevision() {
		read = new AprilBeaconCharacteristics(this, beacon);
		read.connectGattToRead(new MyReadCallBack() {
			@Override
			public void readyToGetFWRevision() {

				final String firmwareRevision;
				try {
					firmwareRevision = read.getFWRevision();
					ModifyActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							tv_firmwareRevision.setText(firmwareRevision);
							read.close();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, READ_FW_REVISON);
	}

	private void setManufacturer() {
		read = new AprilBeaconCharacteristics(this, beacon);
		read.connectGattToRead(new MyReadCallBack() {
			@Override
			public void readyToGetManufacturer() {
				final String manufacturerName;
				try {
					manufacturerName = read.getManufacturer();
					ModifyActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							tv_manufacturerName.setText(manufacturerName);
							read.close();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, READ_MANUFACTURER);
	}

	/**
	 * 输入密码的对话框
	 */
	private void showEnterDialog() {
		TextView textView = (TextView) LayoutInflater.from(this).inflate(
				R.layout.dialog_text, null);
		new AlertDialog.Builder(ModifyActivity.this).setTitle("输入密码")
				.setView(textView)
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

					}
				})
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						aprilWrite();
						dialog.dismiss();

					}
				}).create().show();
	}

	private void aprilWrite() {
		conn = new AprilBeaconConnection(this, beacon);

		if (!TextUtils.isEmpty(major.getText().toString())) {
			int newMajor = Integer.parseInt(major.getText().toString());
			conn.writeMajor(newMajor);
		}
		if (!TextUtils.isEmpty(minor.getText().toString())) {
			int newMinor = Integer.parseInt(minor.getText().toString());
			conn.writeMinor(newMinor);
		}
		if (!TextUtils.isEmpty(uuid.getText().toString())) {
			String newUuid = uuid.getText().toString();
			conn.writeUUID(newUuid);
		}
		if (!TextUtils.isEmpty(password.getText().toString())) {
			String newPassword = password.getText().toString();
			conn.writePassword(newPassword);
		}

		conn.connectGattToWrite(new MyWriteCallback() {

			@Override
			public void onWriteNewPasswordSuccess(final String oldPassword,
					final String newPassword) {
				super.onWriteNewPasswordSuccess(oldPassword, newPassword);
				ModifyActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(
								ModifyActivity.this,
								"oldPassword is " + oldPassword
										+ "newPassword is" + newPassword,
								Toast.LENGTH_SHORT).show();
					}
				});
			}

			@Override
			public void onBeaconError() {
				ModifyActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(
								ModifyActivity.this,
								"please make sure the beacon is AprilBeacon and the FW should above 2.0",
								Toast.LENGTH_SHORT).show();
					}
				});
			}

			@Override
			public void onPasswordWrong(final String password) {
				ModifyActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(
								ModifyActivity.this,
								"password is wrong what you input is ："
										+ password, Toast.LENGTH_SHORT).show();
					}
				});
			}

			@Override
			public void onWriteMajorSuccess(final int oldMajor,
					final int newMajor) {
				ModifyActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(
								ModifyActivity.this,
								"oldMajor is " + oldMajor + "newMajor is "
										+ newMajor, Toast.LENGTH_SHORT).show();
					}
				});
			}

			@Override
			public void onErrorOfPassword() {
				Toast.makeText(ModifyActivity.this, "onErrorOfPassword",
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onErrorOfConnection() {
				Toast.makeText(ModifyActivity.this, "onErrorOfConnection",
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onErrorOfDiscoveredServices() {
				Toast.makeText(ModifyActivity.this,
						"onErrorOfDiscoveredServices", Toast.LENGTH_SHORT)
						.show();
			}

			@Override
			public void onWriteMinorSuccess(final int oldMionr,
					final int newMinor) {
				ModifyActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(
								ModifyActivity.this,
								"oldMionr is " + oldMionr + "newMinor is "
										+ newMinor, Toast.LENGTH_SHORT).show();
					}
				});
			}
		}, oldPassword);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (conn != null && !conn.isConnected()) {
			conn = new AprilBeaconConnection(this, beacon);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		beacon = null;
		if (conn != null && conn.isConnected()) {
			conn.close();
		}
	}

	public void notify(View v) {
		beaconManager = new BeaconManager(this);
		beaconManager.setMonitoringListener(new MonitoringListener() {

			@Override
			public void onExitedRegion(Region region) {
				Toast.makeText(getApplicationContext(), "你离开beacon范围", 0)
						.show();
			}

			@Override
			public void onEnteredRegion(Region region, List<Beacon> beacons) {

				// try {
				// beaconManager.stopMonitoring(ALL_BEACONS_REGION);
				// } catch (RemoteException e) {
				// e.printStackTrace();
				// }

				Toast.makeText(getApplicationContext(),
						"你进入beacon范围 beacons.size =" + beacons.size(), 0)
						.show();
				// HttpURL();
				Intent it = new Intent(Intent.ACTION_VIEW, URI);
				startActivity(it);
			}
		});
		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
			@Override
			public void onServiceReady() {
				try {
					beaconManager.startMonitoring(ALL_BEACONS_REGION);
				} catch (RemoteException e) {
				}
			}
		});
	}

	private void HttpURLConnection_Get() {
		try {
			isPost = false;
			// 通过openConnection 连接
			URL url = new java.net.URL(URL);
			urlConn = (HttpURLConnection) url.openConnection();
			// 设置输入和输出流
			urlConn.setDoOutput(true);
			urlConn.setDoInput(true);
			// 关闭连接
			urlConn.disconnect();
		} catch (Exception e) {
			resultData = "连接超时";
			Message mg = Message.obtain();
			mg.obj = resultData;
			handler.sendMessage(mg);
			e.printStackTrace();
		}
	}

	private void HttpURLConnection_Post() {
		try {
			// 通过openConnection 连接
			URL url = new java.net.URL(URL_Post);
			urlConn = (HttpURLConnection) url.openConnection();
			// 设置输入和输出流
			urlConn.setDoOutput(true);
			urlConn.setDoInput(true);

			urlConn.setRequestMethod("POST");
			urlConn.setUseCaches(false);
			// 配置本次连接的Content-type，配置为application/x-www-form-urlencoded的
			urlConn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			// 连接，从postUrl.openConnection()至此的配置必须要在connect之前完成，
			// 要注意的是connection.getOutputStream会隐含的进行connect。
			urlConn.connect();
			// DataOutputStream流
			DataOutputStream out = new DataOutputStream(
					urlConn.getOutputStream());
			// 要上传的参数
			String content = "par=" + URLEncoder.encode("ylx_Post+中正", "UTF_8");
			// 将要上传的内容写入流中
			out.writeBytes(content);
			// 刷新、关闭
			out.flush();
			out.close();

		} catch (Exception e) {
			resultData = "连接超时";
			Message mg = Message.obtain();
			mg.obj = resultData;
			handler.sendMessage(mg);
			e.printStackTrace();
		}
	}

	private void HttpURL() {
		new Thread() {
			public void run() {

				try {
					// Get方式
					// HttpURLConnection_Get();
					// Post方式
					HttpURLConnection_Post();

					InputStreamReader in = new InputStreamReader(
							urlConn.getInputStream());
					BufferedReader buffer = new BufferedReader(in);
					String inputLine = null;
					while (((inputLine = buffer.readLine()) != null)) {
						resultData += inputLine + "\n";
					}
					System.out.println(resultData);
					in.close();

				} catch (Exception e) {
					resultData = "连接超时";
					e.printStackTrace();
				} finally {
					try {
						// 关闭连接
						if (isPost)
							urlConn.disconnect();

						Message mg = Message.obtain();
						mg.obj = resultData;
						handler.sendMessage(mg);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String m = (String) msg.obj;

			Intent intent = new Intent(ModifyActivity.this,
					RequestActivity.class);

			intent.putExtra("string", m);

			startActivity(intent);

			Log.i(TAG, m);
		}
	};

	@Override
	protected void onStop() {
		try {
			if (beaconManager != null)
				beaconManager.disconnect();
			// beaconManager.stopRanging(ALL_BEACONS_REGION);
			// beaconManager.stopMonitoring(ALL_BEACONS_REGION);
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onStop();
	}

}
