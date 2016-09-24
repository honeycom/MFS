package com.cookandroid.xx;
 

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class SettingActivity extends Activity {

	private Button  Inquiry, backhome;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settinglay);
		// 설정
		Inquiry = (Button) findViewById(R.id.inquiry);
		Inquiry.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent it = new Intent(Intent.ACTION_SEND);
				String[] mailaddr = {"dshoneycom@gmail.com"};

				it.setType("plaine/text");
				it.putExtra(Intent.EXTRA_EMAIL, mailaddr); // 받는사람
				it.putExtra(Intent.EXTRA_SUBJECT, "[MFS문의사항]"); // 제목

				startActivity(it);
				
				
			}
		});
		
		backhome = (Button) findViewById(R.id.backhome);
		backhome.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(SettingActivity.this, BackhomeActivity.class);
				startActivity(intent);
			}
		});
	
	}

}
