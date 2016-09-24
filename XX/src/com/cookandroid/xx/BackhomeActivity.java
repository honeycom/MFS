package com.cookandroid.xx;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

public class BackhomeActivity extends Activity {

	TimePicker tp1;
	TimePicker tp2;
	RadioGroup rg;
	RadioButton ab_timebtn, re_timebtn, no_timebtn;
	Button time_savebtn;
	
	SQLiteDatabase sqlDB;
	AlarmDBHelper myAlarm;
	
	int first=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.backhome);
		
		tp1=(TimePicker)findViewById(R.id.ab_timePicker);
		tp2=(TimePicker)findViewById(R.id.re_timePicker);
		
		rg=(RadioGroup)findViewById(R.id.time_rg);
		ab_timebtn=(RadioButton)findViewById(R.id.absolute_time);
		re_timebtn=(RadioButton)findViewById(R.id.relative_time);
		no_timebtn=(RadioButton)findViewById(R.id.none_time);
		
		time_savebtn=(Button)findViewById(R.id.time_save);
		
		tp2.setIs24HourView(true);
		
		//onTimeSet(tp2,0,0);
		tp2.setCurrentHour(0);
		tp2.setCurrentMinute(0);
		
		//db읽기
		myAlarm = new AlarmDBHelper(this);
		sqlDB = myAlarm.getReadableDatabase();
		Cursor cur = sqlDB.rawQuery("select * from at", null);//noon, hour, min
		
		int noon=3;
		int hour=0;
		int min=0;
		
		while(cur.moveToNext()){
			first=1;
			noon=Integer.parseInt(cur.getString(0));
			hour=Integer.parseInt(cur.getString(1));
			min=Integer.parseInt(cur.getString(2));
			
			Log.e("[DB]", noon+":"+hour+":"+min);
		}
		
		switch(noon){
		case 0:
		case 1:
			ab_timebtn.setChecked(true);
			tp1.setEnabled(true);
			tp2.setEnabled(false);
			tp1.setCurrentHour(hour);
			tp1.setCurrentMinute(min);
			break;
		case 2:
			re_timebtn.setChecked(true);
			tp1.setEnabled(false);
			tp2.setEnabled(true);
			tp2.setCurrentHour(hour);
			tp2.setCurrentMinute(min);
			break;
		case 3:
			no_timebtn.setChecked(true);
			tp1.setEnabled(false);
			tp2.setEnabled(false);
			break;
		}
		
		cur.close();
		sqlDB.close();
		
		//라디오 활성화/비활성화
		rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup rg, int checkedId) {
				// TODO Auto-generated method stub
				
				switch(checkedId){
				case R.id.absolute_time:
					tp1.setEnabled(true);
					tp2.setEnabled(false);
					break;
				case R.id.relative_time:
					tp1.setEnabled(false);
					tp2.setEnabled(true);
					break;
				case R.id.none_time:
					tp1.setEnabled(false);
					tp2.setEnabled(false);
					break;
				}
			}
		});
		
		//저장버튼 누르면
		time_savebtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				//라디오버튼 선택된 것 찾기
				int checkedId=rg.getCheckedRadioButtonId();
				int noon=3;
				int hour=0;
				int min=0;
				//선택된 라디오버튼의 타임피커의 값을 가져오기(경우)
				switch(checkedId){
				case R.id.absolute_time:
					hour=tp1.getCurrentHour();
					min=tp1.getCurrentMinute();
					
					if(hour>12)
						noon=1;//AM
					else
						noon=0;//PM
					//Toast.makeText(getApplicationContext(), ""+noon+" : "+hour+" : "+min, 10000).show();
					break;
					
				case R.id.relative_time:
					hour=tp2.getCurrentHour();
					min=tp2.getCurrentMinute();
					noon=2;
					//Toast.makeText(getApplicationContext(), ""+noon+" : "+hour+" : "+min, 10000).show();
					break;
					
				case R.id.none_time:
					noon=3;
					//없음은 아무것도 안한다.
					break;
				default:
					Toast.makeText(getApplicationContext(), "시간설정 오류", 800).show();
					return;//죽지 않을까.....
				}
				//저장하기
				
				sqlDB = myAlarm.getWritableDatabase();
				
				ContentValues value = new ContentValues();
				value.put("noon", noon);
				value.put("hour", hour);
				value.put("min", min);
				
				if(first==0){
					sqlDB.insert("at", null, value);
				}

				else if(first==1) //이미 존재하는 경우 
					sqlDB.execSQL("update at set noon='"+noon+"', "+"hour='"+hour+"', min='"+min+"';");

				sqlDB.close();
				
				
				finish();
				//알람설정하기-시간이 되면 알람을 띄운다.
				
			}
		});
		

		
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
	
}
