package com.cookandroid.xx;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class P_RecActivity extends Activity {

	TextView tvmotion[];
	Spinner motion1, motion2, motion3;
	Button ok, no; //확인되면 저장, 아니면 뒤로가기 또는 원상태로 복구
	Switch sw;
	int first=0;
	
	SQLiteDatabase sqlDB;
	patternDBHelper myPattern;
	
	ImageView ivmotion;
	private AnimationDrawable frameAnimation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.p_rec);

		tvmotion = new TextView[3];
		tvmotion[0] =(TextView)findViewById(R.id.rtvmotion1);
		tvmotion[1] =(TextView)findViewById(R.id.rtvmotion2);
		tvmotion[2] =(TextView)findViewById(R.id.rtvmotion3);
		
		ivmotion =(ImageView)findViewById(R.id.rivmotion);
		
		motion1 = (Spinner) findViewById(R.id.rmotion1);
		motion2 = (Spinner) findViewById(R.id.rmotion2);
		motion3 = (Spinner) findViewById(R.id.rmotion3);

		ok=(Button)findViewById(R.id.Rsave);
		no=(Button)findViewById(R.id.Rcan);
		
		sw=(Switch)findViewById(R.id.rswitch);
		
		sw.setChecked(false);
		ok.setEnabled(false);
		motion1.setEnabled(false);
		motion2.setEnabled(false);
		motion3.setEnabled(false);
		
		myPattern = new patternDBHelper(this);
		
		int n[] = new int[3];

		Cursor cur;

		// 읽기
		sqlDB = myPattern.getReadableDatabase();
		
		cur = sqlDB.rawQuery("select * from pt where name='record';", null);

		while (cur.moveToNext()) {
			first=1; //투플 존재함 => update
			//Toast.makeText(getApplicationContext(),cur.getString(0)+": "+cur.getString(1)+", "+cur.getString(2)+", "+cur.getString(3),500).show();
			for (int i = 0; i < 3; i++) {	
				if (cur.getString(i + 1).equals("f")) {
					n[i] = 0;
				}
				if (cur.getString(i + 1).equals("b")) {
					n[i] = 1;
				}
			
			}
			if(cur.getInt(4)==1) {
				sw.setChecked(true);
				ok.setEnabled(true);
				motion1.setEnabled(true);
				motion2.setEnabled(true);
				motion3.setEnabled(true);
			}
			else {
				sw.setChecked(false);
				ok.setEnabled(false);
				motion1.setEnabled(false);
				motion2.setEnabled(false);
				motion3.setEnabled(false);
			}
		}

		cur.close();
		sqlDB.close();
		
		
		ArrayAdapter adapter = ArrayAdapter.createFromResource(this,
				R.array.motion, R.layout.spin);
		adapter.setDropDownViewResource(R.layout.spin_dropdown);
		motion1.setAdapter(adapter);
		motion2.setAdapter(adapter);
		motion3.setAdapter(adapter);
		
		motion1.setSelection(n[0]);
		motion2.setSelection(n[1]);
		motion3.setSelection(n[2]);
		
		motion1.setOnItemSelectedListener(new MyOnItemSelectedListener());
		motion2.setOnItemSelectedListener(new MyOnItemSelectedListener());
		motion3.setOnItemSelectedListener(new MyOnItemSelectedListener());
		
		no.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		sw.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked) { //On
		            //on일때 상태 1로
					sqlDB = myPattern.getWritableDatabase();

					ContentValues value = new ContentValues();
					value.put("state", 1);
					Constants.record[3]="1";
					
					sqlDB.update("pt", value, "name='record'", null);

					sqlDB.close();
					
					ok.setEnabled(true);
					motion1.setEnabled(true);
					motion2.setEnabled(true);
					motion3.setEnabled(true);
		        }
		        else { //Off
		        	//off일 때 상태 0으로
		        	sqlDB = myPattern.getWritableDatabase();

					ContentValues value = new ContentValues();
					value.put("state", 0);
					Constants.record[3]="0";
					
					sqlDB.update("pt", value, "name='record'", null);

					sqlDB.close();
					
					ok.setEnabled(false);
					motion1.setEnabled(false);
					motion2.setEnabled(false);
					motion3.setEnabled(false);
		        }
			}
			
		});
		
		//확인 버튼 눌렀을 경우 바뀐 정보 DB에 저장
		ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//myPattern.onUpgrade(sqlDB, 1, 2);
				
				String p[] = new String[3];
				
				for(int i=0;i<3;i++) {
	        		if( ((String)tvmotion[i].getText()).equals("압력-앞") ) {
	            		p[i]="f";
	            	}
	        		if(((String)tvmotion[i].getText()).equals("압력-뒤")) {
	            		p[i]="b";
	            	}
	        		
	        	}
				
				//패턴 중복되는지  확인
				sqlDB = myPattern.getReadableDatabase();

				Cursor cur = sqlDB.rawQuery("select * from pt;", null);

				int duplication=0;
				
				while (cur.moveToNext()) {
					if(cur.getString(1).equals(p[0])&&cur.getString(2).equals(p[1])&&cur.getString(3).equals(p[2])) {
						if(cur.getString(0).equals("record")) 
							duplication = 2;
						
						else 
							duplication = 1; //중복
					}
				}

				cur.close();
				sqlDB.close();
				
				if(duplication==0) { //중복되지 않을 경우 DB에 저장
					//패턴 저장
					sqlDB = myPattern.getWritableDatabase();

					ContentValues value = new ContentValues();
					value.put("name", "record");
					value.put("p1", p[0]);
					value.put("p2", p[1]);
					value.put("p3", p[2]);
					
					Constants.record[0]=p[0];
					Constants.record[1]=p[1];
					Constants.record[2]=p[2];
					
					if(first==0) {
						value.put("state", 1);
						Constants.record[3]=p[3];
						sqlDB.insert("pt", null, value);
					}
					
					else if(first==1) //이미 존재하는 경우 
						sqlDB.update("pt",value,"name='record'",null);

					sqlDB.close();
					
					finish();
				}
				else if(duplication==1) {
					Toast.makeText(getApplicationContext(),"경고:다른 패턴과 중복됩니다.",500).show();
				}
				else if(duplication==2) {
					Toast.makeText(getApplicationContext(),"경고:이전 패턴과 중복됩니다.",500).show();
				}

			}
		});
	}
	
	public class MyOnItemSelectedListener implements OnItemSelectedListener {

		   

	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	    	
	    	if(parent.getId()==R.id.rmotion1){
	    		tvmotion[0].setText(""+parent.getItemAtPosition(pos));
	    		switch((int) parent.getItemIdAtPosition(pos)) {
				case 0 :
					 
					ivmotion.setBackgroundResource(R.drawable.front_animation_list);
			        
			        frameAnimation = (AnimationDrawable) ivmotion.getBackground();
					frameAnimation.start();
					break;
				case 1 :
					 
					ivmotion.setBackgroundResource(R.drawable.back_animation_list);
			        
			        frameAnimation = (AnimationDrawable) ivmotion.getBackground();
					frameAnimation.start();
					break;
	    		}
	    	}
	    	else if(parent.getId()==R.id.rmotion2) {
	    		tvmotion[1].setText(""+parent.getItemAtPosition(pos));
	    		switch((int) parent.getItemIdAtPosition(pos)) {
				case 0 :
					 
					ivmotion.setBackgroundResource(R.drawable.front_animation_list);
			        
			        frameAnimation = (AnimationDrawable) ivmotion.getBackground();
					frameAnimation.start();
					break;
				case 1 :
					 
					ivmotion.setBackgroundResource(R.drawable.back_animation_list);
			        
			        frameAnimation = (AnimationDrawable) ivmotion.getBackground();
					frameAnimation.start();
					break;
			
				}
	    	}
	    	else if(parent.getId()==R.id.rmotion3){
	    		tvmotion[2].setText(""+parent.getItemAtPosition(pos));
	    		switch((int) parent.getItemIdAtPosition(pos)) {
				case 0 :
					 
					ivmotion.setBackgroundResource(R.drawable.front_animation_list);
			        
			        frameAnimation = (AnimationDrawable) ivmotion.getBackground();
					frameAnimation.start();
					break;
				case 1 :
					 
					ivmotion.setBackgroundResource(R.drawable.back_animation_list);
			        
			        frameAnimation = (AnimationDrawable) ivmotion.getBackground();
					frameAnimation.start();
					break;
			
				}
	    	}
	    }

	    public void onNothingSelected(AdapterView parent) {
	      // Do nothing.
	    }

	}
	// ---------- 패턴
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
		//
}
