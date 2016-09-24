package com.cookandroid.xx;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

//imports

public class SentSmsLogger extends Service {

	private static final String TELEPHON_NUMBER_FIELD_NAME = "address";
	private static final String MESSAGE_BODY_FIELD_NAME = "body";
	private static final Uri SENT_MSGS_CONTET_PROVIDER = Uri
			.parse("content://sms/sent");

	@Override
	public void onStart(Intent intent, int startId) {
		addMessageToSentIfPossible(intent);
		stopSelf();
	}

	private void addMessageToSentIfPossible(Intent intent) {
		if (intent != null) {
			String telNumber = intent
					.getStringExtra(Constants.KEY_PHONE_NUMBER);
			String messageBody = intent.getStringExtra(Constants.KEY_MESSAGE);
			if (telNumber != null && messageBody != null) {
				addMessageToSent(telNumber, messageBody);
			}
		}
	}

	private void addMessageToSent(String telNumber, String messageBody) {
		ContentValues sentSms = new ContentValues();
		sentSms.put(TELEPHON_NUMBER_FIELD_NAME, telNumber);
		sentSms.put(MESSAGE_BODY_FIELD_NAME, messageBody);

		ContentResolver contentResolver = getContentResolver();
		contentResolver.insert(SENT_MSGS_CONTET_PROVIDER, sentSms);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
