package com.cookandroid.xx;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class RecordActivity extends Activity implements MediaRecorder.OnInfoListener{
	
	// 녹음 : 녹음목록, 삭제버튼
		private ListView RecList;
		private Button RecDelete, RecStart, RecEnd,RecPlay;
		
		MediaPlayer mPlayer = null;
	    MediaRecorder mRecorder = null;
	    String mFilePath;
	    ArrayAdapter<String> RecAdapter;
	    String sdRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
	    String valueAct="a";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reclay);
		
		// 호출하여 실행
		Intent intent = getIntent();
		
		if(intent!=null)
			valueAct = intent.getExtras().getString("act");

		// 녹음
		RecList = (ListView) findViewById(R.id.reclist);
		RecDelete = (Button) findViewById(R.id.recdelete);
		RecStart = (Button) findViewById(R.id.recstart);
		RecEnd = (Button) findViewById(R.id.recend);
		RecPlay = (Button) findViewById(R.id.recplay);
		
		RecEnd.setEnabled(false);
		
		//String sdRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		//mFilePath = sdRootPath + "/MFS/"+new Date(System.currentTimeMillis())+".mp3";
		String str = Environment.getExternalStorageState();
		String dirPath = sdRootPath + "/MFS";
		if ( str.equals(Environment.MEDIA_MOUNTED)) {
          File file = new File(dirPath); 
          if( !file.exists() )  // 원하는 경로에 폴더가 있는지 확인
            file.mkdirs();
        }
        else
          Toast.makeText(RecordActivity.this, "SD Card 인식 실패", Toast.LENGTH_SHORT).show();
        
        File[] listFiles =(new File(dirPath).listFiles());
        
        List<String> list = new ArrayList<String>();//넣어준 스트링을 
        for( File file : listFiles )//리스트에 
         list.add(file.getName());//추가 해 줍니다.
        
        RecAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,list);
        //리스트에 뿌려주고요 위에건 기본 리스트고 아래건 선택 버튼이 있는 리스트겠죠..
           //android.R.layout.simple_list_item_multiple_choice
        RecList.setAdapter(RecAdapter); //리스트를 아답터에 셋팅하여 화면에 뿌려 줍니다.
        
        
        RecDelete.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				int count, checked ;
                count = RecAdapter.getCount() ;
                if (count > 0) {
                    // 현재 선택된 아이템의 position 획득.
                    checked = RecList.getCheckedItemPosition();

                    if (checked > -1) {
                       String filenameis = RecAdapter.getItem(checked);
                    	
                    	String fileChk = sdRootPath + "/MFS/" + filenameis;
                    	File file = new File(fileChk);
                    	Log.e("1","File Check:"+file.exists());
                    	file.delete();
                    	
                    	RecAdapter.clear();
                        File[] listFiles =(new File(sdRootPath+"/MFS").listFiles());
                        List<String> list = new ArrayList<String>();//넣어준 스트링을 
                        for( File file2 : listFiles )//리스트에 
                         list.add(file2.getName());//추가 해 줍니다.
                        //ArrayAdapter<String> RecAdapter2; 
                        //RecAdapter2=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_single_choice,list);
                        RecAdapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_single_choice,list);
                        RecList.setAdapter(RecAdapter);
                        RecAdapter.notifyDataSetChanged();
                    }
                }
		
			}
		});
        RecPlay.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int count, checked ;
                count = RecAdapter.getCount() ;

                if (count > 0) {
                    // 현재 선택된 아이템의 position 획득.
                    checked = RecList.getCheckedItemPosition();

                    if (checked > -1) {
                       String filenameis = RecAdapter.getItem(checked);
                       if( mPlayer != null ) {
                           mPlayer.stop();
                           mPlayer.release();
                           mPlayer = null;
                       }
                       mPlayer = new MediaPlayer();
                    
                       try {
                           mPlayer.setDataSource(sdRootPath + "/MFS/"+filenameis);
                           mPlayer.prepare();
                       } catch(IOException e) {
                           Log.d("tag", "Audio Play error");
                           return;
                       }
                       mPlayer.start();
                    }
                }
		
			}
		});
        
        //패턴으로 실행시켰을 때
        if(valueAct!=null){
        	if(valueAct.equals("act")){
            	Toast.makeText(getApplicationContext(), "act", 500).show();
            	onBtnRecord();
            }
        }
	}
	
	public void onBtnRecord() {
        if( mRecorder != null ) {
            mRecorder.release();
            mRecorder = null;
        }
        mRecorder = new MediaRecorder();
        
        
        mFilePath = sdRootPath + "/MFS/"+new Date(System.currentTimeMillis())+".mp3";
        mRecorder.setOutputFile(mFilePath);
        
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 
        mRecorder.setMaxDuration(600 * 1000);
        mRecorder.setMaxFileSize(600 * 1000 * 1000);
        
        mRecorder.setOnInfoListener(this);
        
 
        try {
            mRecorder.prepare();
        } catch(IOException e) {
            Log.d("tag", "Record Prepare error");
        }
        mRecorder.start();
 
        // 버튼 활성/비활성 설정
        RecStart.setEnabled(false);
        RecEnd.setEnabled(true);
        RecPlay.setEnabled(false);
    }
 
    public void onBtnStop() {
        mRecorder.stop();
        mRecorder.release();
        
        RecAdapter.clear();
        File[] listFiles =(new File(sdRootPath+"/MFS").listFiles());
        List<String> list = new ArrayList<String>();//넣어준 스트링을 
        for( File file : listFiles )//리스트에 
         list.add(file.getName());//추가 해 줍니다.
        RecAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,list);
        RecList.setAdapter(RecAdapter);
        RecAdapter.notifyDataSetChanged();
        // 버튼 활성/비활성 설정
        RecStart.setEnabled(true);
        RecEnd.setEnabled(false);
        RecPlay.setEnabled(true);
    }
 
    public void onBtnPlay() {
        if( mPlayer != null ) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        mPlayer = new MediaPlayer();
     
        try {
            mPlayer.setDataSource(mFilePath);
            mPlayer.prepare();
        } catch(IOException e) {
            Log.d("tag", "Audio Play error");
            return;
        }
        mPlayer.start();
    }
 
    public void onClick(View v) {
        switch( v.getId() ) {
        case R.id.recstart :
        	Toast.makeText(getApplication(), "start", 700).show();
            onBtnRecord();
            break;
        case R.id.recend :
        	Toast.makeText(getApplication(), "end", 700).show();
            onBtnStop();
            break;
        case R.id.recplay :
        	Toast.makeText(getApplication(), "play", 700).show();
            onBtnPlay();
            break;
        }
    }

	@Override
	public void onInfo(MediaRecorder mr, int what, int extra) {
		// TODO Auto-generated method stub
		 switch( what ) {
	        case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED :
	        case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED :
	            onBtnStop();
	            break;
	        }
	}
	@Override
	public void onBackPressed() {
		finish();
	}
}
