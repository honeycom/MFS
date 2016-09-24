package com.cookandroid.xx;

import java.io.IOException;
import java.util.Vector;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.Toast;

import com.cookandroid.xx.BluetoothChat.messageDBHelper;

public class AlarmNotificationActivity extends Activity{
	private Button alarmcancel;
	private Chronometer chro;
	private MediaPlayer mPlayer;
	private Vibrator vide;
	private AudioManager  mAudioManager,cAudioManager;
	private int mode;
	//gps
    LocationManager locationManager;
    String locationProvider;
    Location lastKnownLocation;
    private double longitude=0;//경도
    private double latitude=0;//위도
	
	SQLiteDatabase db, sqlDB;
	messageDBHelper myMessage;
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarmnotification);

		mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		mode = mAudioManager.getRingerMode();
		mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		mPlayer = new MediaPlayer();         // 객체생성
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);  

		try {
		    // 이렇게 URI 객체를 그대로 삽입해줘야한다. 
		    //인터넷에서 url.toString() 으로 하는것이 보이는데 해보니까 안된다 -_-;
		    mPlayer.setDataSource(this, alert);        


		    // 출력방식(재생시 사용할 방식)을 설정한다. STREAM_RING 은 외장 스피커로,
		    // STREAM_VOICE_CALL 은 전화-수신 스피커를 사용한다. 
		    mPlayer.setAudioStreamType(AudioManager.STREAM_RING);

		    mPlayer.setLooping(true);  // 반복여부 지정
		    mPlayer.prepare();    // 실행전 준비
		} catch (IOException e) {
		    e.printStackTrace();
		}
		mPlayer.start();   // 실행 시작 
		
		
		vide= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		myMessage = new messageDBHelper(this);
		//gps
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
            	latitude = location.getLatitude();
            	longitude = location.getLongitude();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            	Toast.makeText(getApplicationContext(), "onStatusChanged", 800).show();
            }

            public void onProviderEnabled(String provider) {
            	Toast.makeText(getApplicationContext(), "onProviderEnabled", 800).show();
            }

            public void onProviderDisabled(String provider) {
            	Toast.makeText(getApplicationContext(), "onProviderDisabled", 800).show();
            }
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        
        // 수동으로 위치 구하기
        String locationProvider = LocationManager.GPS_PROVIDER;
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        if (lastKnownLocation != null) {
            longitude = lastKnownLocation.getLatitude();
            latitude = lastKnownLocation.getLatitude();
        }
		
		alarmcancel = (Button) findViewById(R.id.alarmcancel);
		chro = (Chronometer) findViewById(R.id.chronometer);
		alarmcancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				chro.stop();
				mPlayer.stop();         
				mPlayer.release();  
				if(mode == 0)
					mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT); //무음
				else if (mode == 1)
					mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);  //진동
				else if(mode == 2)
					mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);  //벨
				finish(); 
			}
		});
		vide.vibrate(2000);

		chro.setBase(SystemClock.elapsedRealtime());
		chro.start();
		chro.setOnChronometerTickListener(new OnChronometerTickListener() {
			public void onChronometerTick(Chronometer chronometer) {
				//매 초마다 비프음을 발생시키기 위한 부분
				long t = ((SystemClock.elapsedRealtime() - chro.getBase()) / 1000);
				if(t == 25){
					chro.stop();
					
					//문자를 보내렴 림림아
					//gps
    				//DB에서 전화번호, 내용 (+좌표)해서 문자 보내기
					Vector<String> PHONE_NUMBER = new Vector<String>();
					Vector<String> MESSAGE = new Vector<String>();
					int SIM_STATE;
					int c=0;
					
    				TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    				TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    				SIM_STATE = telephonyManager.getSimState();

					//DB에서 읽어 오기
					sqlDB = myMessage.getReadableDatabase();
					
					Cursor cur;
					cur = sqlDB.rawQuery("select * from mt;", null);
					
					while (cur.moveToNext()) {
						if(cur.getInt(3)==1) { //state가 1인 경우에만 
							PHONE_NUMBER.addElement(cur.getString(1));
							MESSAGE.addElement(cur.getString(2)+" http://maps.google.com/?q="+latitude+","+longitude);
							c++;
						}
					}

					cur.close();
					sqlDB.close();
					
					// 메소드를 호출하자...
					//sendSMS(null, PHONE_NUMBER, MESSAGE, SIM_STATE);
					for(int i=0;i<c;i++) {
						/*Toast.makeText(getApplicationContext(),PHONE_NUMBER.elementAt(i)+
								": "+MESSAGE.elementAt(i),4000).show();*/
						
						sendSMS(null, PHONE_NUMBER.elementAt(i), MESSAGE.elementAt(i), SIM_STATE);
								
					}
					
					mPlayer.stop();         // 이 방식은 미디어를 멈추는것이고
					mPlayer.release();  
					
					if(mode == 0)
						mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT); //무음
					else if (mode == 1)
						mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);  //진동
					else if(mode == 2)
						mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);  //벨
					finish();
				}
			 }	
			});
		
	}
	// gps문자 보내기 메소드
		public void sendSMS(View v, final String PHONE_NUMBER,
				final String MESSAGE, int SIM_STATE) {
			if (isSimExists(SIM_STATE)) {

				try {

					String SENT = "SMS_SENT";

					PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
							new Intent(SENT), 0);

					registerReceiver(new BroadcastReceiver() {
						public void onReceive(Context arg0, Intent arg1) {
							int resultCode = getResultCode();
							switch (resultCode) {
							case Activity.RESULT_OK:
								Toast.makeText(getBaseContext(), "SMS sent",
										Toast.LENGTH_LONG).show();
								// When sms sent successfully, start service to
								// insert sent message

								Intent intent = new Intent(AlarmNotificationActivity.this,SentSmsLogger.class);

								intent.putExtra(Constants.KEY_PHONE_NUMBER,
										PHONE_NUMBER);
								intent.putExtra(Constants.KEY_MESSAGE, MESSAGE);
								startService(intent);

								break;
							case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
								Toast.makeText(getBaseContext(), "Generic failure",
										Toast.LENGTH_LONG).show();
								break;
							case SmsManager.RESULT_ERROR_NO_SERVICE:
								Toast.makeText(getBaseContext(), "No service",
										Toast.LENGTH_LONG).show();
								break;
							case SmsManager.RESULT_ERROR_NULL_PDU:
								Toast.makeText(getBaseContext(), "Null PDU",
										Toast.LENGTH_LONG).show();
								break;
							case SmsManager.RESULT_ERROR_RADIO_OFF:
								Toast.makeText(getBaseContext(), "Radio off",
										Toast.LENGTH_LONG).show();
								break;
							}
						}
					}, new IntentFilter(SENT));

					SmsManager smsMgr = SmsManager.getDefault();
					smsMgr.sendTextMessage(PHONE_NUMBER, null, MESSAGE, sentPI,
							null);

				} catch (Exception e) {
					Toast.makeText(this,
							e.getMessage() + "!\n" + "Failed to send SMS",
							Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
			} else {
				Toast.makeText(this,
						getSimState(SIM_STATE) + " " + "Cannot send SMS",
						Toast.LENGTH_LONG).show();
			}
		}
		private boolean isSimExists(int SIM_STATE) {
			TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			SIM_STATE = telephonyManager.getSimState();

			if (SIM_STATE == TelephonyManager.SIM_STATE_READY)
				return true;

			return false;
		}
		private String getSimState(int SIM_STATE) {
			switch (SIM_STATE) {
			case TelephonyManager.SIM_STATE_ABSENT: // SimState =
				return "No Sim Found!"; // "No Sim Found!";
			case TelephonyManager.SIM_STATE_NETWORK_LOCKED: // SimState =
															// "Network Locked!";
				return "Network Locked!";
			case TelephonyManager.SIM_STATE_PIN_REQUIRED: // SimState =
															// "PIN Required to access SIM!";
				return "PIN Required to access SIM!";
			case TelephonyManager.SIM_STATE_PUK_REQUIRED: // SimState =
															// "PUK Required to access SIM!";
															// // Personal
															// Unblocking Code
				return "PUK Required to access SIM!";
			case TelephonyManager.SIM_STATE_UNKNOWN: // SimState =
														// "Unknown SIM State!";
				return "Unknown SIM State!";
			}
			return null;
		}
		
		// ---------- 문자 DB
		public class messageDBHelper extends SQLiteOpenHelper {
			public messageDBHelper(Context context) {
				super(context, "mymessageDB", null, 1);
			}

			@Override
			public void onCreate(SQLiteDatabase pdb) {
				// TODO Auto-generated method stub
				pdb.execSQL("create table mt(name CHAR(30), phone CHAR(20), content CHAR(250), state INTEGER)");
			}

			@Override
			public void onUpgrade(SQLiteDatabase pdb, int arg1, int arg2) {
				// TODO Auto-generated method stub
				pdb.execSQL("drop table if exists mt");// 초기화때 onUpgrade사용
				onCreate(pdb); // 기존 테이블 없애고 새로운 테이블 만듦
			}
		}
		
		@Override
		public void onBackPressed() {
		    //super.onBackPressed();
		}
}
