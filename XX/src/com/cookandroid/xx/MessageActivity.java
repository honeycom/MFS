package com.cookandroid.xx;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MessageActivity extends Activity {
		private Button MAdd;
		private String name, number;
		private LinearLayout contents;
		
		SQLiteDatabase sqlDB;
		messageDBHelper myMessage;
		
		//gps
		LocationManager manager;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.messagelay);
		
		myMessage = new messageDBHelper(this);
		
		//처음 실행 했을 때, DB에서 읽어 오기
		sqlDB = myMessage.getReadableDatabase();

		Cursor cur;
		cur = sqlDB.rawQuery("select * from mt;", null);
		
		while (cur.moveToNext()) {
			AddList(cur.getString(0),cur.getString(1),cur.getString(2));
		}
		
		cur.close();
		sqlDB.close();
		
		MAdd=(Button)findViewById(R.id.madd);
		
		MAdd.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				//전화번호부불러오기
				Intent intent = new Intent(Intent.ACTION_PICK);
				intent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
				startActivityForResult(intent, 0);
			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {

			Cursor cursor = getContentResolver()
					.query(data.getData(),
							new String[] {
									ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
									ContactsContract.CommonDataKinds.Phone.NUMBER },
							null, null, null);
			cursor.moveToFirst();
			name = cursor.getString(0); // 0은 이름을 얻어옵니다.

			number = cursor.getString(1); // 1은 번호를 받아옵니다..
			cursor.close();
			
			//번호값을 비교한다.
			int check=0;
			sqlDB=myMessage.getReadableDatabase();
			Cursor cur = sqlDB.rawQuery("select phone from mt;", null);
			while(cur.moveToNext()){
				if(cur.getString(0).equals(number)){
					check=1;
					break;
				}
			}
			cur.close();
			sqlDB.close();
			if(check==1){
				Toast.makeText(getApplicationContext(), "번호가 중복됩니다.", 800).show();
			}else{
				//DB에 저장하기...
				sqlDB = myMessage.getWritableDatabase();
				
				ContentValues value = new ContentValues();
				value.put("name", name);
				value.put("phone", number);
				value.put("content", "도와주세요!");
				value.put("state", 0);
				sqlDB.insert("mt", null, value);

				sqlDB.close();

				AddList(name,number,"도와주세요!");
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	protected void AddList(String name,String number, final String text){
		contents = (LinearLayout)findViewById(R.id.inflatedLayout);
		
		final String ndb=name;
		final LinearLayout parentLL = new LinearLayout(MessageActivity.this);
		parentLL.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		parentLL.setOrientation(LinearLayout.HORIZONTAL);
		parentLL.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder dlg=new AlertDialog.Builder(MessageActivity.this);
				dlg.setTitle("삭제");
				dlg.setMessage("삭제하시겠습니까?");
				dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						((ViewManager) parentLL.getParent()).removeView(parentLL);
						
						//db에서도 삭제
						sqlDB = myMessage.getWritableDatabase();
						sqlDB.delete("mt", "name='"+ndb+"'", null); 
						sqlDB.close();
					}
				});
				dlg.setNegativeButton("취소", null);
				dlg.show();
			}
		});
		
		
		final CheckBox MCheck = new CheckBox(MessageActivity.this);
		
		sqlDB = myMessage.getReadableDatabase();

		Cursor cur;
		cur = sqlDB.rawQuery("select * from mt where name='"+ndb+"';", null);
		
		while (cur.moveToNext()) {
			if(cur.getInt(3)==1)
				MCheck.setChecked(true);
		}
		
		cur.close();
		sqlDB.close();
		
		MCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(MCheck.isChecked() == true) {
					
					sqlDB = myMessage.getWritableDatabase();

					ContentValues value = new ContentValues();
					value.put("state", 1); //check 되면 상태 1로 변환
					
					sqlDB.update("mt",value, "name='"+ndb+"'", null);
					
					sqlDB.close();

				}
				else {
					sqlDB = myMessage.getWritableDatabase();

					ContentValues value = new ContentValues();
					value.put("state", 0); //check 해제되면 상태 0으로 변환
					
					sqlDB.update("mt",value, "name='"+ndb+"'", null);
					
					sqlDB.close();
					
				}
			}
		});

		DisplayMetrics dm = getResources().getDisplayMetrics();
		int size = Math.round(20 * dm.density);		TextView MName = new TextView(MessageActivity.this);
		MName.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		MName.setBackgroundColor(Color.parseColor("#00FFFFFF"));
		MName.setPadding(20, 10, 10, 10);
		MName.setTextColor(Color.parseColor("#ffffff"));
		MName.setTextSize(20);
		MName.setText(""+name);
		
		MName.setPadding(size, 0, size, 0);
		
		TextView MPhoneNum=new Button(MessageActivity.this);
		MPhoneNum.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		MPhoneNum.setPadding(20, 10, 10, 10);
		MPhoneNum.setBackgroundColor(Color.parseColor("#00FFFFFF"));
		MPhoneNum.setTextColor(Color.parseColor("#ffffff"));
		MPhoneNum.setTextSize(20);
		MPhoneNum.setText(""+number);
		MPhoneNum.setPadding(0, 0, size, 0);
		Button MContent=new Button(MessageActivity.this);
		MContent.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		MContent.setPadding(20, 10, 10, 10);
		MContent.setTextColor(Color.parseColor("#ffffff"));
		MContent.setTextSize(20);
		MContent.setText("문자내용");
		MContent.setOnClickListener(new View.OnClickListener() {
	
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder dlg=new AlertDialog.Builder(MessageActivity.this);
				dlg.setTitle("문자내용");
				
				//DB에서 문자 내용 읽어 오기
				sqlDB = myMessage.getReadableDatabase();

				Cursor cur;
				cur = sqlDB.rawQuery("select * from mt where name='"+ndb+"';", null);
				
				//커서의 위치 처음으로 이동
				cur.moveToFirst(); 
				
				String t = cur.getString(2);
				
				
				cur.close();
				sqlDB.close();
				
				final EditText name = new EditText(MessageActivity.this);
				name.setText(t);
				dlg.setView(name);
				dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int whichButton) {
		                   String text = name.getText().toString();
		                   //Toast.makeText(getApplicationContext(), text, 1000).show();
		                   
								// DB에 문자 내용 text로 업데이트
								sqlDB = myMessage.getWritableDatabase();

								ContentValues value = new ContentValues();
								value.put("content", text);
								
								sqlDB.update("mt",value, "name='"+ndb+"'", null);
								
								sqlDB.close();

							}
		            });
		 
		 
				dlg.setNegativeButton("취소",new DialogInterface.OnClickListener() {
		                        public void onClick(DialogInterface dialog, int whichButton) {
		                   
		                        }
		                    });
		           
				
				dlg.show();
			}
		});
		parentLL.addView(MCheck);
		parentLL.addView(MName);
		parentLL.addView(MPhoneNum);
		parentLL.addView(MContent);
		
		contents.addView(parentLL);
	}
	
	// ---------- 문자
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
}
