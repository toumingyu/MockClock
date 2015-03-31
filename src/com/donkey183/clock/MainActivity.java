package com.donkey183.clock;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.genius.col.R;

import android.app.Activity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	private MockClockView myClockView;
	private Button beijing;
	private Button lundun;
	private TextView tvShow,tvShow2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		myClockView = (MockClockView) findViewById(R.id.clock_view);
		tvShow = (TextView) findViewById(R.id.tv_show);
		tvShow2 = (TextView) findViewById(R.id.tv_show2);
		beijing = (Button) findViewById(R.id.beijing);
		beijing.setOnClickListener(this);
		lundun = (Button) findViewById(R.id.lundun);
		lundun.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d = new Date();
		if (v.getId() == R.id.lundun) {
			myClockView.setTime(d, "America/New_York");
			d = myClockView.getTimezoneDate(d,"America/New_York");
			tvShow.setText("纽约时间:"+sdf.format(d).toString());
		} else if (v.getId() == R.id.beijing) {
			myClockView.setTime(d, "GMT+8");
			d = myClockView.getTimezoneDate(d, "GMT+8");
			tvShow2.setText("北京时间:"+sdf.format(d).toString());
		}
	}
}