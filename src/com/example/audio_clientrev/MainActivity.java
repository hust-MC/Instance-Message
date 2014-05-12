package com.example.audio_clientrev;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity
{
	static int i = 0;

	final int STOP_RECORDING = 800;

	ClientThread clientThread;                        	 // Create a new thread to process client service
	EditText inputMessage;
	Button send_bt;
	ListView lv;
	Button sound_bt;
	Handler handler;                                     // Create a handler object to process UI update
	String mContent;                                     // MContent means message content
	Camera camera = new Camera();                        // Process camera service

	static ChatAdapter chatAdapter;
	DataTransmission dataTransmission = new DataTransmission();

	List<short[]> revSound = new ArrayList<short[]>();     // Store Sound

	public void wiget_init()
	{
		lv = (ListView) findViewById(R.id.lv);
		inputMessage = (EditText) findViewById(R.id.inputMessage);
		send_bt = (Button) findViewById(R.id.send_bt);
		sound_bt = (Button) findViewById(R.id.more_bt);
	}

	public void setListener()
	{
		lv.setOnItemClickListener(new OnItemClickListener()                   // Set the
		// listview's click
		// event
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				if (ChatAdapter.chatList.get(position).Content instanceof Bitmap)
				{
					Intent intent = new Intent(MainActivity.this,
							MagnifyActivity.class);
					intent.putExtra("position", position);
					startActivity(intent);
				}
			}
		});

		sound_bt.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					Intent intent = new Intent(MainActivity.this,
							RecordActivity.class);
					startActivity(intent);
				}
				else if (event.getAction() == MotionEvent.ACTION_UP)
				{
					RecordActivity.isRecording = false;

					// new Handler().postDelayed(new Runnable()
					// {
					// @Override
					// public void run() //create a thread to send sound message
					// {
					// new Thread(new Runnable()
					// {
					// @Override
					// public void run()
					// {
					// try
					// {
					// dataTransmission.send(Audio.TxBuffer);
					// }
					// catch (IOException e)
					// {
					// e.printStackTrace();
					// }
					// }
					// }).start();

					chatAdapter.addList("123", true);
				}
				// }, STOP_RECORDING);
				// }
				return true;
			}
		});
	}

	public void showToast(String content)
	{
		Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
	}

	public void onClick_send(View view)                                     // Words message event
	{
		mContent = inputMessage.getText().toString();
		chatAdapter.addList(mContent, true);

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					dataTransmission.send(mContent);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}).start();
		inputMessage.setText("");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			Log.d("MC", "finish");
			System.exit(0);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onClick_more(View view)
	{
		// AlertDialog dialog = new AlertDialog();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		wiget_init();
		setListener();                                                    // initial sound button
		chatAdapter = new ChatAdapter(this);
		lv.setAdapter(chatAdapter);

		handler = new Handler()
		{
			Uri notification = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),
					notification);

			@Override
			public void handleMessage(Message msg)                            // process UI
			{
				if (msg.arg1 == ClientThread.CONNECT_FAILED)
				{
					showToast("����ʧ��");
				}
				else if (msg.arg1 == ClientThread.CONNECT_SUCCESS)
				{
					showToast("���ӳɹ�");
				}
				else
				{
					switch (msg.arg1)
					{
					case ClientThread.WORD:
						
						chatAdapter.addList(msg.obj, false);
						r.play();
						break;
						
					case ClientThread.PIC:

						camera.handlePhoto((byte[]) msg.obj);
						break;
						
					case ClientThread.SOUND:

						revSound = (List<short[]>) msg.obj;
						final int SIZE = revSound.get(0).length;

						for (int i = 0; i < revSound.size(); i++)
						{
							System.arraycopy(RecordActivity.audioData, i * SIZE,
									revSound.get(i), 0, SIZE);
						}
						break;
					}
				}

			}
		};
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == 1)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				camera.savePhoto();
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case 1:
			clientThread = new ClientThread(handler);
			new Thread(clientThread).start();
			break;

		case 2:
			String status = Environment.getExternalStorageState();
			if (status.equals(Environment.MEDIA_MOUNTED))
			{
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, camera.openCamera());
				startActivityForResult(intent, 1);
			}
			else
			{
				showToast("û�д洢��");
			}
			break;

		case 3:
			new AlertDialog.Builder(this).setTitle("����")
					.setMessage("�汾: ��ʱͨ��(V1.4)").setNegativeButton("ȷ��", null)
					.show();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0, 1, 1, "��¼");
		menu.add(0, 2, 2, "����");
		menu.add(0, 3, 3, "����");
		return super.onCreateOptionsMenu(menu);
	}
}
