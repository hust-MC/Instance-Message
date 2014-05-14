package com.example.audio_clientrev;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MagnifyActivity extends Activity
{
	private int imageWidth = 1000;
	int i = 0;
	ImageView imageView;

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		Log.d("MC","touch");
		if (event.getPointerCount() == 2)
		{
			if (event.getAction() == MotionEvent.ACTION_MOVE)
			{
				int historySize = event.getHistorySize();
				if (historySize == 0)
					return true;
				float currentY1 = event.getY(0);
				float historyY1 = event.getHistoricalY(0, historySize - 1);

				float currentY2 = event.getY(1);
				float historyY2 = event.getHistoricalY(1, historySize - 1);
				float distance = Math.abs(currentY1 - currentY2);
				float historyDistance = Math.abs(historyY1 - historyY2);

				if (distance > historyDistance)                      // magnify
				{
					int newWidth = imageWidth + (i += 100);
					int newHeight = (int) (newWidth * 10 / 7);
					imageView.setLayoutParams(new LinearLayout.LayoutParams(
							newWidth, newHeight));
				}
				else if (distance < historyDistance)                  // shrink
				{
					if (i > -700)
					{
						int newWidth = imageWidth + (i -= 100);
						int newHeight = (int) (newWidth * 10 / 7);
						imageView
								.setLayoutParams(new LinearLayout.LayoutParams(
										newWidth, newHeight));
					}
				}
				else
				{
					Log.d("status", "�ƶ�");
				}
			}
		}
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.d("MC", String.valueOf(i));
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);                    // no title
		setContentView(R.layout.magnify);

		 Intent intent = getIntent();
//		
		int position= intent.getExtras().getInt("position");

		File file = new File(Environment.getExternalStorageDirectory()
				+ "/mc/image/" + String.valueOf(position) + ".png");
		Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
				
		imageView = (ImageView) this.findViewById(R.id.mag_pic);
		imageView.setImageBitmap(bitmap);
//		imageView.setOnClickListener(new View.OnClickListener()
//		{ // �������
//					public void onClick(View paramView)
//					{
//						finish();
//					}
//				});
	}

	// image.setImageResource(R.drawable.shutdown);

	@Override
	public void onDestroy()
	{
		Log.d("MC", "destroy");
		super.onDestroy();
	}
}
