package com.cookandroid.xx;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.cookandroid.xx.BackhomeActivity.AlarmDBHelper;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	// back
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	// 메인 화면 : 패턴, 문자, 녹음, 설정
	private Button PTBtn, MSGBtn, RECBtn, SETBtn;

	// 블루투스
	private BluetoothAdapter mBluetoothAdapter = null;

	// 알람
	private AlarmManager timeManager;
	// private NotificationManager timeNotification;
	private GregorianCalendar mCalendar;
	private Intent intent;
	private PendingIntent ServicePending;
	SQLiteDatabase sqlDB;
	AlarmDBHelper myAlarm;
	patternDBHelper myPattern;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 알람 객체 얻기
		timeManager = (AlarmManager) this
				.getSystemService(Context.ALARM_SERVICE);
		// timeNotification =
		// (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		/*
		 * //기존 DB 비우기용 코드 File f = new
		 * File("/data/data/com.cookandroid.xx/databases/mypatternDB");
		 * 
		 * if(f.exists()) { //만약 파일 존재하면 f.delete(); //삭제 }
		 */

		// 메인
		PTBtn = (Button) findViewById(R.id.PTBtn);
		// PTBtn
		Typeface face = Typeface.createFromAsset(getAssets(),
				"fonts/BMJUA_ttf.ttf");

		PTBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// setContentView(R.layout.patternlay);
				startActivity(new Intent(MainActivity.this,
						PatternActivity.class));
			}
		});
		// 메시지
		MSGBtn = (Button) findViewById(R.id.MSGBtn);
		MSGBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(MainActivity.this,
						MessageActivity.class));
			}
		});
		RECBtn = (Button) findViewById(R.id.RECBtn);
		RECBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getApplicationContext(),
						RecordActivity.class);
				intent.putExtra("act", "no");
				startActivity(intent);
			}
		});
		SETBtn = (Button) findViewById(R.id.SETBtn);
		SETBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(MainActivity.this,
						SettingActivity.class));
			}
		});

		// 블루투스 승인 요청
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", 800).show();
			finish();
			return;
		}
		
		//Constants에 DB내용 넣기
		myPattern = new patternDBHelper(this);
		sqlDB = myPattern.getReadableDatabase();
		
		Cursor cur;
		sqlDB = myPattern.getReadableDatabase();

		cur = sqlDB.rawQuery("select * from pt;", null);

		String name = "";

		while (cur.moveToNext()) {
			name = cur.getString(0);
			if (name.equals("police")) {
				for (int i = 0; i < 4; i++) {
					Constants.police[i] = cur.getString(i + 1);
				}
			}
			if (name.equals("people")) {
				for (int i = 0; i < 4; i++) {
					Constants.people[i] = cur.getString(i + 1);
				}
			}
			if (name.equals("record")) {
				for (int i = 0; i < 4; i++) {
					Constants.record[i] = cur.getString(i + 1);
				}
			}
			
			if(Constants.police[0]==null){
				Constants.police[0]="";
				Constants.police[1]="";
				Constants.police[2]="";
			}
			
			if(Constants.people[0]==null){
				Constants.people[0]="";
				Constants.people[1]="";
				Constants.people[2]="";
			}
			
			if(Constants.record[0]==null){
				Constants.record[0]="";
				Constants.record[1]="";
				Constants.record[2]="";
			}
			Log.e("Main", "[police]"+Constants.police[0]+Constants.police[1]+Constants.police[2]+Constants.police[3]);
			Log.e("Main", "[people]"+Constants.people[0]+Constants.people[1]+Constants.people[2]+Constants.people[3]);
			Log.e("Main", "[record]"+Constants.record[0]+Constants.record[1]+Constants.record[2]+Constants.record[3]);
		}

		cur.close();
		sqlDB.close();
	}

	@Override
	public void onStart() {
		super.onStart();
		// GPS승인 요청
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			createGpsDisabledAlert();
		}
		// 블루투스
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_OK) {
				Toast.makeText(getApplicationContext(), "블루투스를 활성화 하였습니다", 800).show();
			} else {
				Toast.makeText(getApplicationContext(), "블루투스를 활성화하지 못했습니다.",
						800).show();
			}
		}
	}

	private void createGpsDisabledAlert() {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
		builder.setMessage(
				"애플리케이션에서 GPS를 켜는 권한을 요청하고 있습니다. 허용할까요?")
				.setCancelable(false)
				.setPositiveButton("네", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Intent gpsOptionIntent = new Intent(
								android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(gpsOptionIntent);
					}
				})
				.setNegativeButton("아니요",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan:// Connect
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(MainActivity.this,
					DeviceListActivity.class);
			startActivityForResult(serverIntent,
					BluetoothChat.REQUEST_CONNECT_DEVICE);// 안되면
															// BluetoothChat클래스
															// 없이 해보자
			return true;

		case R.id.back_start:// Connect
			// Launch the DeviceListActivity to see devices and do scan
			// ★
			// db읽기
			Toast.makeText(getApplicationContext(), "귀가알림시작", 800).show();
			myAlarm = new AlarmDBHelper(this);
			sqlDB = myAlarm.getReadableDatabase();
			Cursor cur = sqlDB.rawQuery("select * from at", null);// noon, hour,
																	// min
			int noon = 3;
			int hour = 0;
			int min = 0;

			while (cur.moveToNext()) {
				noon = Integer.parseInt(cur.getString(0));
				hour = Integer.parseInt(cur.getString(1));
				min = Integer.parseInt(cur.getString(2));
			}
			cur.close();
			sqlDB.close();
			// noon을 인자로 사용하여 시작.

			// 현재 시간 가져오기
			mCalendar = new GregorianCalendar();

			// date객체
			Date date = new Date();
			Date tt = new Date();

			// 오후 1시 30분
			// 1시간 30분 뒤

			if (noon == 2) {// 상대적 시간
				mCalendar.set((date.getYear() + 1900), (date.getMonth()),
						date.getDate(), date.getHours() + hour,
						date.getMinutes() + min);
				Log.e("*[time]", mCalendar.getTime().toString());
				Log.e("*[time]",
						(date.getYear() + 1900) + "/" + (date.getMonth() + 1)
								+ "/" + date.getDate() + "/" + hour + "/" + min);

			} else if (noon == 3) {
				/*
				 * mCalendar.set((date.getYear() + 1900), (date.getMonth()),
				 * date.getDate(), date.getHours(), date.getMinutes());
				 */
				// do nothing
			} else {// 0,1
				hour = hour + (noon * 12);
				mCalendar.set((date.getYear() + 1900), (date.getMonth()),
						date.getDate(), hour, min);
				Log.e("*[time]", mCalendar.getTime().toString());
				Log.e("*[time]",
						(date.getYear() + 1900) + "/" + (date.getMonth() + 1)
								+ "/" + date.getDate() + "/" + hour + "/" + min);

			}

			// Receiver로 보내기 위한 인텐트
			intent = new Intent(getApplicationContext(), AlarmReceiver.class);
			ServicePending = PendingIntent.getBroadcast(MainActivity.this, 0,
					intent, 0);
			Date t = new Date();
			t.setTime(mCalendar.getTimeInMillis());
			timeManager.set(AlarmManager.RTC_WAKEUP, t.getTime(),
					ServicePending);

			return true;
		}
		return false;
	}

	public class AlarmDBHelper extends SQLiteOpenHelper {
		public AlarmDBHelper(Context context) {
			super(context, "AlarmDB", null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase pdb) {
			// TODO Auto-generated method stub
			pdb.execSQL("create table at(noon INTEGER, hour INTEGER, min INTEGER)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase pdb, int arg1, int arg2) {
			// TODO Auto-generated method stub
			pdb.execSQL("drop table if exists at");// 초기화때 onUpgrade사용
			onCreate(pdb); // 기존 테이블 없애고 새로운 테이블 만듦
		}
	}
	
	public class patternDBHelper extends SQLiteOpenHelper {
		public patternDBHelper(Context context) {
			super(context, "mypatternDB", null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase pdb) {
			// TODO Auto-generated method stub
			pdb.execSQL("create table pt(name CHAR(50), p1 CHAR(10), p2 CHAR(10), p3 CHAR(10), state INTEGER)"); // police,
																									// people,record
		}

		@Override
		public void onUpgrade(SQLiteDatabase pdb, int arg1, int arg2) {
			// TODO Auto-generated method stub
			pdb.execSQL("drop table if exists pt");// 초기화때 onUpgrade사용
			onCreate(pdb); // 기존 테이블 없애고 새로운 테이블 만듦
		}
	}
	
}