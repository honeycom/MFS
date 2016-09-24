package com.cookandroid.xx;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Service {
	// Debugging
	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	protected static final int REQUEST_CONNECT_DEVICE = 1;
	protected static final int REQUEST_ENABLE_BT = 2;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	//private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;//얘가 일을 하는거지
	
	//gps
    LocationManager locationManager;
    String locationProvider;
    Location lastKnownLocation;
    private double longitude=0;//경도
    private double latitude=0;//위도

	// motion->String으로!
	private static ArrayList<String> Motions;
	private static String[] Pattern = new String[3];

	SQLiteDatabase db, sqlDB;
	messageDBHelper myMessage;

	//알림
	private NotificationManager notificationMgr;
	
	//구글계정
	String Gaccount="null";
	
	//진동
	private Vibrator vide;
	@SuppressLint({ "NewApi", "NewApi", "NewApi" })
	public void onCreate() {
		
		super.onCreate();
		vide = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		//효율적으로..
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		notificationMgr =(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		displayNotificationMessage("MFS 실행중입니다.");
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");

		setupChat();
		myMessage = new messageDBHelper(this);
			
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
				
			Log.e(TAG, "[onCreate][end]cur.moveToNext");
			Log.e(TAG, "[police]"+Constants.police[0]+Constants.police[1]+Constants.police[2]+Constants.police[3]);
			Log.e(TAG, "[people]"+Constants.people[0]+Constants.people[1]+Constants.people[2]+Constants.people[3]);
			Log.e(TAG, "[record]"+Constants.record[0]+Constants.record[1]+Constants.record[2]+Constants.record[3]);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		Log.e(TAG, "[create]mBluetoothAdapter");
		
		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			return;
		}
		
        //계정정보 가져오기
        AccountManager manager = AccountManager.get(this);
        Account[] accounts =  manager.getAccounts();
        final int count = accounts.length;
        Account account = null;
              
        for(int i=0;i<count;i++) {
        	account = accounts[i];
        	Log.d("ACCOUNT", "Account - name: " + account.name + ", type :" + account.type);

        	if(account.type.equals("com.google")){		//이러면 구글 계정 구분 가능
        		Gaccount=account.name;
        	} 
        }      
        //onSendServer("Server"); 
	}

	private void displayNotificationMessage(String message) {
		// TODO Auto-generated method stub
		 Notification notification = new Notification(R.drawable.app,message,System.currentTimeMillis());
		 notification.flags = notification.FLAG_NO_CLEAR;
	        PendingIntent contentIntent =
	            PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
	        notification.setLatestEventInfo(this, "Motion For Safe",message,contentIntent);
	        notificationMgr.notify(123, notification);
	}

	public void onStart() {
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			//startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			setupChat();
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e(TAG, "### onStartCommand ###");
		
		String result=intent.getStringExtra("make");
		if(result==null)
			result=intent.getStringExtra("connect");

		if(result!=null){	//make나 connect중 하나를 통해 들어옴
			if(result.equals("run")){//make discoverable
				ensureDiscoverable();
				Log.e(TAG, "### ensureDiscoverable() ###");
			}
				
			else{	//makeDiscoverable이 아니라  connect
				// Get the device MAC address
				Log.e(TAG, "### connect() ###");
				String address = result;
				
				// Get the BLuetoothDevice object
				Log.e(TAG, "--->"+address);
				BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
				
				if(device==null){
					Toast.makeText(getApplicationContext(), "연결 실패", 1000).show();
					stopSelf();
				}
				// Attempt to connect to the device
				else
					mChatService.connect(device);
			}
		}else{
			//Toast.makeText(getApplicationContext(), "MFS 서비스 종료", 800).show();
			//stopSelf();
		}
		
		return START_REDELIVER_INTENT;
	}
	
	public void onStartCommand(){
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		Motions = new ArrayList<String>();

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);
		Log.d(TAG, "[create]mChatService");
		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
		//stopSelf();
		notificationMgr.cancelAll();
		Toast.makeText(getApplicationContext(), "MFS 서비스 종료", 700).show();
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		
		if(mBluetoothAdapter==null){
			Log.d(TAG, "[null]mBluetoothAdapter");
			stopSelf();
		}
			
		
		else if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			discoverableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(discoverableIntent);
			Log.d(TAG, "[ensureDiscoverable]startActivity");
		}
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);//setText(mOutStringBuffer)
		}
	}

	// The action listener for the EditText widget, to listen for the return key
	private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
		public boolean onEditorAction(TextView view, int actionId,
				KeyEvent event) {
			// If the action is a key-up event on the return key, send the
			// message
			if (actionId == EditorInfo.IME_NULL
					&& event.getAction() == KeyEvent.ACTION_UP) {
				String message = view.getText().toString();
				sendMessage(message);
			}
			if (D)
				Log.i(TAG, "END onEditorAction");
			return true;
		}
	};

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					// mTitle.setText(R.string.title_connected_to);
					// mTitle.append(mConnectedDeviceName);
					//mConversationArrayAdapter.clear();
					break;
				case BluetoothChatService.STATE_CONNECTING:
					// mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					//Toast.makeText(getApplicationContext(), "MFS 서비스 종료", 700).show();
					stopSelf();
					// mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				//mConversationArrayAdapter.add("Me:  " + writeMessage);

				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				String readMessage = new String(readBuf, 0, msg.arg1);

				Log.d(TAG, readMessage);

				String values = readMessage;

				if (Motions.size() >= 3)
					Motions.remove(0);

				if (values.charAt(0) == 'q'){
					values=values.replaceAll("q", "");
				}
				
				if (values.charAt(0) == 'f') {
					Motions.add("f");
					// 앞에 압력이 가해졌다고 앱에 알린다.
					Log.d(TAG,"[Motion] Front");
					sendBroadcast(new Intent("com.cookandroid.xx.front"));
				} else if (values.charAt(0) == 'b') {
					Motions.add("b");
					// 뒤에 압력이 가해졌다고 앱에 알린다.
					Log.d(TAG,"[Motion] Back");
					sendBroadcast(new Intent("com.cookandroid.xx.back"));
				} else if (values.charAt(0) == 'd') { //긴급 상황
					Motions.add("d");
					// 흔들기 알리기
					Log.d(TAG,"[Motion] Dangerous");
					sendBroadcast(new Intent("com.cookandroid.xx.shake"));
					
				}
				
				//위급상황 지인 문자
				if(Motions.get(0).equals("d")) {
						vide.vibrate(1000);
						Motions.clear();
						Toast.makeText(getApplicationContext(),"위급상황!!!!",800).show();
						//gps
						getGPS();
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
						onSendServer("dangerous");
						// 메소드를 호출하자...
						//sendSMS(null, PHONE_NUMBER, MESSAGE, SIM_STATE);
						for(int i=0;i<c;i++) {
							sendSMS(null, PHONE_NUMBER.elementAt(i), MESSAGE.elementAt(i), SIM_STATE);		
						}
				}
				
				if (Motions.size() == 3) {// 3개가 들어왔다면
					Log.e(TAG, "[Motion.size()==3][start]");
					
					Log.e(TAG, "[Motion.size()==3][start]"+Motions.get(0)+Motions.get(1)+Motions.get(2));
					
					if (Constants.police[0].equals(Motions.get(0))
							&& Constants.police[1].equals(Motions.get(1))
							&& Constants.police[2].equals(Motions.get(2))) {// 모두 비교
						
						if(Constants.police[3].equals("1")) {
							// check=1;
							Motions.clear();
							//getGPS()해서 문자 보내기 해야됨...
							
							//임시로 지정한 동작
							//Intent i=new Intent(BluetoothChat.this,PatternActivity.class);
							//i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							//startActivity(i);
							//실제로 구현할 시 아래의 지인 문자 코드에서 
							//DB로 읽는 부분 제외하고 다이렉트로 경찰 번호 집어 넣으면 됨.
							//하지만 역시 그냥 토스트나 창 띄우는 선에서 끝내는 게 안전하지 않을까...
							//진짜 신고되면...끄앙
							//long[] pattern = {1000,500};
							//vide.vibrate(pattern,2);
							vide.vibrate(2000);
							//Toast.makeText(getApplicationContext(),"112신고!!",800).show();
							onSendServer("police");
							
						}
						else {
							Toast.makeText(getApplicationContext(),"경찰 문자 패턴 off중",500).show();
						}
					}

					else if ((Constants.people[0].equals(Motions.get(0))
							&& Constants.people[1].equals(Motions.get(1))
							&& Constants.people[2].equals(Motions.get(2)))) {// 모두 비교
						Log.e(TAG, "[Motion.size()==3][MESSAGE]");
						if(Constants.people[3].equals("1")) {
							vide.vibrate(1000);
							Motions.clear();
							getGPS();
							Log.e(TAG, "[Motion.size()==3][MESSAGE][on]");
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
									MESSAGE.addElement(cur.getString(2));
									c++;
								}
							}
							
							cur.close();
							sqlDB.close();
							
							//Toast.makeText(getBaseContext(), "SMS sent",Toast.LENGTH_LONG).show();
							
							// 메소드를 호출하자...
							//sendSMS(null, PHONE_NUMBER, MESSAGE, SIM_STATE);
							for(int i=0;i<c;i++) {
								/*Toast.makeText(getApplicationContext(),PHONE_NUMBER.elementAt(i)+
										": "+MESSAGE.elementAt(i),4000).show();*/
								
								sendSMS(null, PHONE_NUMBER.elementAt(i), MESSAGE.elementAt(i), SIM_STATE);
								sendSMS(null, PHONE_NUMBER.elementAt(i), " http://maps.google.com/?q="+latitude+","+longitude, SIM_STATE);
										
							}

							// startActivity(new Intent(BluetoothChat.this,
							// MessageActivity.class));
							vide.vibrate(2000);
							onSendServer("message");
						}
						else {
							Toast.makeText(getApplicationContext(),"지인 문자 패턴 off중",500).show();
						}
					}

					else if (Constants.record[0].equals(Motions.get(0))
							&& Constants.record[1].equals(Motions.get(1))
							&& Constants.record[2].equals(Motions.get(2))) {// 모두 비교
						Log.e(TAG, "[Motion.size()==3][record]");
						if(Constants.record[3].equals("1")) {
							vide.vibrate(2500);
							Log.e(TAG, "[Motion.size()==3][record][on]");
							// check=3;										
							// 메소드 실행
							// ((RecordActivity)RecordActivity.RecordCont).onBtnRecord();
							onSendServer("record");
							vide.vibrate(2000);
							Intent i=new Intent(BluetoothChat.this,
									RecordActivity.class);
							i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							
							startActivity(i);
							Motions.clear();
							Intent intent = new Intent(getApplicationContext(),
									RecordActivity.class);
							intent.putExtra("act", "act");
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
						}
						else {
							Toast.makeText(getApplicationContext(),"녹음 패턴 off중",500).show();
						}
					}

					else
						break;
					Log.e(TAG, "[Motion.size()==3][end]");
				}

				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

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
							//Toast.makeText(getBaseContext(), "SMS sent",Toast.LENGTH_LONG).show();
							// When sms sent successfully, start service to
							// insert sent message

							Intent intent = new Intent(BluetoothChat.this,SentSmsLogger.class);

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

	/**
	 * @return true if SIM card exists false if SIM card is locked or doesn't
	 *         exists <br/>
	 * <br/>
	 *         <b>Note:</b> This method requires permissions <b>
	 *         "android.permission.READ_PHONE_STATE" </b>
	 */
	private boolean isSimExists(int SIM_STATE) {
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		SIM_STATE = telephonyManager.getSimState();

		if (SIM_STATE == TelephonyManager.SIM_STATE_READY)
			return true;

		return false;
	}

	/**
	 * Get simcard state
	 * 
	 * @return
	 */
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
	//

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	public void onSendServer(String pattern) {
		// TODO Auto-generated method stub
		Log.e("[Server]", "[Server]onSendServer---");
		//String url = "http://203.252.218.118:8080/Test0728/MFSServer.jsp";   //http://localhost:8080/Test0728/JSONServer.jsp
		String url = "http://14.63.197.184:8080/MFS.jsp";
		HttpClient http = new DefaultHttpClient();
		String arg1=Gaccount;
		String arg2=pattern;
		Log.e("[Server]", "[Server]String--"+arg1+": "+arg2);
		
		//gps
		getGPS();
		
		Calendar mCalendar= Calendar.getInstance();;
		String date = mCalendar.getTime().toString();
		
		try { 
			if(pattern==null)
				arg2="null";
			if(date==null)
				date="null";

			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("motion", arg1+"/"+arg2+"/"+latitude+"/"+longitude+"/"+date));

			HttpParams params = http.getParams();
			HttpConnectionParams.setConnectionTimeout(params, 5000);
			HttpConnectionParams.setSoTimeout(params, 5000);

			HttpPost httpPost = new HttpPost(url);
			UrlEncodedFormEntity entityRequest = 
					new UrlEncodedFormEntity(nameValuePairs, "EUC-KR");
			
			httpPost.setEntity(entityRequest);
			
			HttpResponse responsePost = http.execute(httpPost);
			HttpEntity resEntity = responsePost.getEntity();
			
			Toast.makeText(getBaseContext(), ""+pattern,Toast.LENGTH_LONG).show();
			
			

		}catch(Exception e){
			Log.e("[Server]", "[Server]onSendServer--catch");
			e.printStackTrace();
			}
	}

	public void getGPS(){//gps위치를 얻는 메소드
		//♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣     
				//gps
		        // Acquire a reference to the system Location Manager
		        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		        LocationListener locationListener = new LocationListener() {
		            public void onLocationChanged(Location location) {
		            	latitude = location.getLatitude();
		            	longitude = location.getLongitude();

		            	
		            }

		            public void onStatusChanged(String provider, int status, Bundle extras) {///이게 뭘까...
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
		            longitude = lastKnownLocation.getLongitude();
		            latitude = lastKnownLocation.getLatitude();
		        	}

		        
		        Log.e("[어떤게위도 경도]",latitude+"/"+longitude);
		       
		      //♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣♣  
	}
}