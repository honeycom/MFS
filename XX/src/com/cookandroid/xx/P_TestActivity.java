package com.cookandroid.xx;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class P_TestActivity extends Activity {
	private TextView pp1,pp2,pp3;
	private TextView fp1,fp2,fp3;
	private TextView rp1,rp2,rp3;
	
	private TextView[] pp=new TextView[3];
	private TextView[] fp=new TextView[3];
	private TextView[] rp=new TextView[3];
	
	//확인 버튼
    private static Button front;
	private static Button back;
	private static Button left;
	
    SQLiteDatabase db, sqlDB;
	patternDBHelper myPattern;
	
	//통신
	BlueReceiver2 br2= new BlueReceiver2();
	
	static Handler h = new Handler();
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.p_test);
		
		pp1=(TextView)findViewById(R.id.pp1);
		pp2=(TextView)findViewById(R.id.pp2);
		pp3=(TextView)findViewById(R.id.pp3);
		rp1=(TextView)findViewById(R.id.rp1);
		rp2=(TextView)findViewById(R.id.rp2);
		rp3=(TextView)findViewById(R.id.rp3);
		fp1=(TextView)findViewById(R.id.fp1);
		fp2=(TextView)findViewById(R.id.fp2);
		fp3=(TextView)findViewById(R.id.fp3);
		
		front=(Button)findViewById(R.id.pfront);
		back=(Button)findViewById(R.id.pback);
		left=(Button)findViewById(R.id.pshake);
		
		pp[0]=pp1;
		pp[1]=pp2;
		pp[2]=pp3;
		
		fp[0]=fp1;
		fp[1]=fp2;
		fp[2]=fp3;
		
		rp[0]=rp1;
		rp[1]=rp2;
		rp[2]=rp3;
		
		
		myPattern = new patternDBHelper(this);
		Cursor cur;
		br2.setXValue("act");
		if(br2.getBlueValue()=="front"){
			front.setBackgroundColor(Color.RED);
			back.setBackgroundColor(Color.GRAY);
			left.setBackgroundColor(Color.GRAY);
		}else if(br2.getBlueValue()=="back"){
			front.setBackgroundColor(Color.GRAY);
			back.setBackgroundColor(Color.RED);
			left.setBackgroundColor(Color.GRAY);
		}else if(br2.getBlueValue()=="shake"){
			front.setBackgroundColor(Color.GRAY);
			back.setBackgroundColor(Color.GRAY);
			left.setBackgroundColor(Color.RED);
		}
		
		// 텍뷰의 내용을 db에서 얻어서 setText
		sqlDB = myPattern.getReadableDatabase();

		cur = sqlDB.rawQuery("select * from pt where name='police';", null);
		
		while (cur.moveToNext()) {
			//Toast.makeText(getApplicationContext(),cur.getString(0)+": "+cur.getString(1)+", "+cur.getString(2)+", "+cur.getString(3),4000).show();
			for (int i = 0; i < 3; i++) {
				if (cur.getString(i + 1).equals("f")) {
					pp[i].setText("압력(앞)");
				}
				if (cur.getString(i + 1).equals("b")) {
					pp[i].setText("압력(뒤)");
				}
				if (cur.getString(i + 1).equals("l")) {
					pp[i].setText("기울기(좌)");
				}
				if (cur.getString(i + 1).equals("r")) {
					pp[i].setText("기울기(우)");
				}
			}
		}
		sqlDB.close();
		
		sqlDB = myPattern.getReadableDatabase();
		cur = sqlDB.rawQuery("select * from pt where name='people';", null);
		while (cur.moveToNext()) {
			//Toast.makeText(getApplicationContext(),cur.getString(0)+": "+cur.getString(1)+", "+cur.getString(2)+", "+cur.getString(3),4000).show();
			for (int i = 0; i < 3; i++) {
				if (cur.getString(i + 1).equals("f")) {
					fp[i].setText("압력(앞)");
				}
				if (cur.getString(i + 1).equals("b")) {
					fp[i].setText("압력(뒤)");
				}
				if (cur.getString(i + 1).equals("l")) {
					fp[i].setText("기울기(좌)");
				}
				if (cur.getString(i + 1).equals("r")) {
					fp[i].setText("기울기(우)");
				}
			}
		}
		sqlDB.close();
		
		sqlDB = myPattern.getReadableDatabase();
		cur = sqlDB.rawQuery("select * from pt where name='record';", null);
		while (cur.moveToNext()) {
			//Toast.makeText(getApplicationContext(),cur.getString(0)+": "+cur.getString(1)+", "+cur.getString(2)+", "+cur.getString(3),4000).show();
			for (int i = 0; i < 3; i++) {
				if (cur.getString(i + 1).equals("f")) {
					rp[i].setText("압력(앞)");
				}
				if (cur.getString(i + 1).equals("b")) {
					rp[i].setText("압력(뒤)");
				}
				if (cur.getString(i + 1).equals("l")) {
					rp[i].setText("기울기(좌)");
				}
				if (cur.getString(i + 1).equals("r")) {
					rp[i].setText("기울기(우)");
				}
			}
		}

		cur.close();
		sqlDB.close();
	}
	// ---------- 패턴
	public class patternDBHelper extends SQLiteOpenHelper {
		public patternDBHelper(Context context) {
			super(context, "mypatternDB", null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase pdb) {
			// TODO Auto-generated method stub
			pdb.execSQL("create table pt(name CHAR(50), p1 CHAR(10), p2 CHAR(10), p3 CHAR(10))"); // police,
																									// people,record
		}

		@Override
		public void onUpgrade(SQLiteDatabase pdb, int arg1, int arg2) {
			// TODO Auto-generated method stub
			pdb.execSQL("drop table if exists pt");// 초기화때 onUpgrade사용
			onCreate(pdb); // 기존 테이블 없애고 새로운 테이블 만듦
		}
	}
	
	
	public static class splashhandler implements Runnable{
    	public void run()	{
    		front.setBackgroundColor(Color.GRAY);
			back.setBackgroundColor(Color.GRAY);
			left.setBackgroundColor(Color.GRAY);
	}
}
	
	
	public static class BlueReceiver2 extends BroadcastReceiver{
		String bluevalue="no";
		static String x="a";
		@Override
		public void onReceive(Context c, Intent intent) {
			// TODO Auto-generated method stub
			String name = intent.getAction();
			
			if(name.equals("com.cookandroid.xx.front")){
				if(x.equals("a"))
					bluevalue="front";
				else{
					front.setBackgroundColor(Color.RED);
					back.setBackgroundColor(Color.GRAY);
					left.setBackgroundColor(Color.GRAY);
					
					 
					 h.postDelayed(new splashhandler(), 1500);
					
				}
			}
			else if(name.equals("com.cookandroid.xx.back")){
				if(x.equals("a"))
				bluevalue="back";
				else{
					front.setBackgroundColor(Color.GRAY);
					back.setBackgroundColor(Color.RED);
					left.setBackgroundColor(Color.GRAY);
					
					 h.postDelayed(new splashhandler(), 1500);
				}
			}else if(name.equals("com.cookandroid.xx.shake")){
				Log.e("TEST","[TEST]"+x);
				if(x.equals("a"))
					bluevalue="shake";
				else{
					front.setBackgroundColor(Color.GRAY);
					back.setBackgroundColor(Color.GRAY);
					left.setBackgroundColor(Color.RED);
					
					 h.postDelayed(new splashhandler(), 1500);
				}
			}
		}
		public String getBlueValue(){
			return bluevalue;
		}
		public void setXValue(String x){
			this.x=x;
		}
	}
	
}
