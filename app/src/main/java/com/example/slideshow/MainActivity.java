package com.example.slideshow;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.tulv.slidemsg.SlideMsgView;


public class MainActivity extends AppCompatActivity {
	SlideMsgView slideMsgView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		slideMsgView = findViewById(R.id.slidemsg);
	}

	public void onClickSend(View view) {
		slideMsgView.addMsg("!!!!!!!1111111111111111111111111111111111111111" + System.currentTimeMillis());
	}

	public void onClickClear(View view) {
		slideMsgView.clearMsg();
	}


}
