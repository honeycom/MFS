package com.cookandroid.xx;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
	 
    @Override
    public void onReceive(Context context, Intent intent) {
         
        try {
            intent = new Intent(context, AlarmNotificationActivity.class);
            PendingIntent pi = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);
 
            pi.send();
             
        } catch (CanceledException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}