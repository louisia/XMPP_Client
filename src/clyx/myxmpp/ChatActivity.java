package clyx.myxmpp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import clyx.xmpphelp.XmppTool;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

@SuppressLint("HandlerLeak")
public class ChatActivity extends Activity{
	private TextView friendnameTextView;
	private ListView recordListView;
	private ArrayList<Map<String, String>> listdata=new ArrayList<Map<String,String>>();
	private SimpleAdapter listAdapter;
	private HashMap<String, String> listmap;

	private EditText msgEditText;
	private Button sendButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		friendnameTextView=(TextView)findViewById(R.id.chatfriendname);
		friendnameTextView.setText(getIntent().getStringExtra("friendname"));

		recordListView=(ListView)findViewById(R.id.chatrecord);
		msgEditText=(EditText)findViewById(R.id.chatmsg);
		sendButton=(Button)findViewById(R.id.chatsendbtn);

		listAdapter=new SimpleAdapter(ChatActivity.this, listdata, R.layout.chatitem, new String[]{"sendmessage","receivemessage"}, new int[]{R.id.sendmessage,R.id.receivermessage});
		recordListView.setAdapter(listAdapter);

		//receive offlineMsg
		Thread t1=new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(LoginActivity.offlineMsgs.containsKey(getIntent().getStringExtra("friendaccount"))){
					for(int i=0;i<LoginActivity.offlineMsgs.get(getIntent().getStringExtra("friendaccount")).size();i++){
						listmap=new HashMap<String, String>();
						listmap.put("receivemessage", LoginActivity.offlineMsgs.get(getIntent().getStringExtra("friendaccount")).get(i).getBody());
						listdata.add(listmap);
					}
				}
				handler.sendEmptyMessage(0);
			}
		});
		t1.start();//wait thread t1 to finish
		try{
			t1.join();
		}catch(InterruptedException e){
			e.printStackTrace();
		}

		//receive message
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				ChatManager cm=XmppTool.getConnection().getChatManager();
				cm.addChatListener(new ChatManagerListener() {  
					@Override  
					public void chatCreated(Chat chat, boolean createdLocally) {  
						chat.addMessageListener(new MessageListener() {  
							@Override
							public void processMessage(Chat arg0, Message message) {
								// TODO Auto-generated method stub
								listmap=new HashMap<String, String>();
								listmap.put("receivemessage", message.getBody());
								listdata.add(listmap);
								handler.sendEmptyMessage(2);
							}  
						});  
					}  
				});  
			}
		}).start();

		//send message
		sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Thread t=new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						XmppTool.getConnection();
						XmppTool.sendMessage(getIntent().getStringExtra("friendaccount"),msgEditText.getText().toString());
						listmap=new HashMap<String, String>();
						listmap.put("sendmessage", msgEditText.getText().toString());
						listdata.add(listmap);
						handler.sendEmptyMessage(1);//send meaasge to parent thread
					}
				});
				t.start();
			}
		});
	}
	//main thread process
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
			case 0:
				listAdapter=new SimpleAdapter(ChatActivity.this, listdata, R.layout.chatitem, new String[]{"sendmessage","receivemessage"}, new int[]{R.id.sendmessage,R.id.receivermessage});
				recordListView.setAdapter(listAdapter);
				break;
			case 1:
				listAdapter=new SimpleAdapter(ChatActivity.this, listdata, R.layout.chatitem, new String[]{"sendmessage","receivemessage"}, new int[]{R.id.sendmessage,R.id.receivermessage});
				recordListView.setAdapter(listAdapter);
				msgEditText.setText(null);
				break;
			case 2:
				listAdapter=new SimpleAdapter(ChatActivity.this, listdata, R.layout.chatitem, new String[]{"sendmessage","receivemessage"}, new int[]{R.id.sendmessage,R.id.receivermessage});
				recordListView.setAdapter(listAdapter);
				break;
			}
		}
	};

}
