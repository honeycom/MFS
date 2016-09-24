package com.cookandroid.xx;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class PatternActivity extends Activity {
	// 패턴 : 신고, 지인, 녹음, 여분
		private LinearLayout PTreport, PTFriend, PTRec, PTEtc;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.patternlay);
		// 패턴
		PTreport = (LinearLayout) findViewById(R.id.PReport);
		PTFriend = (LinearLayout) findViewById(R.id.PFriend);
		PTRec = (LinearLayout) findViewById(R.id.PRec);
		PTEtc = (LinearLayout) findViewById(R.id.PEtc);
		
		PTreport.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PatternActivity.this, P_PoliceActivity.class);
				startActivity(intent);
			}
		});
		PTFriend.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PatternActivity.this, P_FriendActivity.class);
				startActivity(intent);
			}
		});
		PTRec.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PatternActivity.this, P_RecActivity.class);
				startActivity(intent);
			}
		});
		PTEtc.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PatternActivity.this, P_TestActivity.class);
				startActivity(intent);
			}
		});
	}

}
