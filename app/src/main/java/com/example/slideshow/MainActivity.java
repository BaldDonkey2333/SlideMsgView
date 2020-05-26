package com.example.slideshow;

import android.os.Bundle;
import android.view.View;

import com.tulv.slidemsg.SlideMsgView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
	SlideMsgView slideMsgView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		slideMsgView = findViewById(R.id.slidemsg);
	}

	public void onClickSend(View view) {
		slideMsgView.addMsg("!!!!!!!" + System.currentTimeMillis());
	}

	public void onClickClear(View view) {
		slideMsgView.clearMsg();
	}


}
