package clyx.myxmpp;
import clyx.xmpphelp.XmppTool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class FriendListActivity extends Activity{
	private ListView listView;	
	private Button addFriendBtn;
	private ArrayList<Map<String, Object>> listdata=new ArrayList<Map<String,Object>>();
	private SimpleAdapter listAdapter;
	private HashMap<String, Object> listmap;
	public static Roster roster;
	private String receivermsgFrom;
	private String presencermsgFrom;	
	private Object status;
	private String deleteAccount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friendlist);
		listView=(ListView)findViewById(R.id.friendlist_listView);
		addFriendBtn=(Button)findViewById(R.id.friendlist_addfriendbtn);
		addFriendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent();
				intent.setClass(FriendListActivity.this, AddFriendActivitty.class);
				startActivity(intent);
			}
		});
		roster=XmppTool.getConnection().getRoster();//返回好友列表
		Collection<RosterEntry> it=roster.getEntries(); //获取好友列表中的还有记录
		for (RosterEntry rosterEntry:it) {
			listmap=new HashMap<String, Object>();
			listmap.put("account", rosterEntry.getUser());//返回用户JID
			listmap.put("name", rosterEntry.getName());//返回用户姓名 
			Presence presence = roster.getPresence(rosterEntry.getUser());//Returns the presence  of a particular user
			boolean isOnline=presence.isAvailable();
			if(isOnline){
				listmap.put("status", R.drawable.online);
			}else{
				listmap.put("status", R.drawable.offline);
			}
			if(LoginActivity.offlineMsgs.containsKey(rosterEntry.getUser())){
				listmap.put("num", LoginActivity.offlineMsgs.get(rosterEntry.getUser()).size());
			}
			else 
				listmap.put("num","");
			listdata.add(listmap);
		}
		listAdapter=new SimpleAdapter(FriendListActivity.this, listdata, R.layout.frienditem, new String[]{"account","name","status","num"}, new int[]{R.id.account,R.id.name,R.id.status,R.id.num});
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(new MyListItemClick());	
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				deleteAccount=(String)listdata.get(position).get("account").toString();
				listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
					@Override
					public void onCreateContextMenu(ContextMenu menu, View v,
							ContextMenuInfo menuInfo) {
						// TODO Auto-generated method stub
						menu.add("删除好友");
					}

				});
				return false;
			}
		});
		//添加会话监听器
		ChatManager cm=XmppTool.getConnection().getChatManager();
		cm.addChatListener(new ChatManagerListener() {  
			@Override  
			public void chatCreated(Chat chat, boolean createdLocally) {  
				chat.addMessageListener(new MessageListener() {  
					@Override
					public void processMessage(Chat arg0, Message message) {
						receivermsgFrom=message.getFrom().split("/")[0];
						if(LoginActivity.offlineMsgs.containsKey(receivermsgFrom)){ //将消息加入相应的JID中
							LoginActivity.offlineMsgs.get(receivermsgFrom).add(message);  
						}else{  
							ArrayList<Message> temp = new ArrayList<Message>();//新建一个JID项
							temp.add(message);  
							LoginActivity.offlineMsgs.put(receivermsgFrom, temp);  
						}
						handler.sendEmptyMessage(1);
					}  
				});  
			}  
		});
		//添加好友状态变化监听器
		roster.addRosterListener(new RosterListener() {
			//监听好友状态变化
			@Override
			public void presenceChanged(Presence arg0) {
				if (arg0.isAvailable()) {
					status=R.drawable.online;
				}
				else{
					status=R.drawable.offline;
				}
				presencermsgFrom=arg0.getFrom().split("/")[0];
				handler.sendEmptyMessage(2);
			}
			@Override
			public void entriesUpdated(Collection<String> arg0) {
				Log.v("XXMPClient"," entriesUpdated");
			}
			@Override
			public void entriesDeleted(Collection<String> arg0) {
				// TODO Auto-generated method stub
				Log.v("XXMPClient"," entriesDeleted");

			}
			@Override
			public void entriesAdded(Collection<String> arg0) {
				// TODO Auto-generated method stub
				Log.v("XXMPClient","entriesAdded");

			}
		});
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {  
			moveTaskToBack(true);  
			return true;  
		}  
		return super.onKeyDown(keyCode, event); 
	}

	class MyListItemClick implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			listmap=new HashMap<String, Object>();
			listmap.put("account", listdata.get(position).get("account"));
			listmap.put("name", listdata.get(position).get("name"));
			listmap.put("status", listdata.get(position).get("status"));
			listmap.put("num", "");
			listdata.remove(position);
			listdata.add(listmap);
			listAdapter=new SimpleAdapter(FriendListActivity.this, listdata, R.layout.frienditem, new String[]{"account","name","status","num"}, new int[]{R.id.account,R.id.name,R.id.status,R.id.num});
			listView.setAdapter(listAdapter);
			Intent intent=new Intent();
			intent.setClass(FriendListActivity.this, ChatActivity.class);
			intent.putExtra("friendname",(String)listdata.get(position).get("name"));
			intent.putExtra("friendaccount", (String)listdata.get(position).get("account"));
			startActivity(intent);
		}
	}
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			case 1:
				for(int i=0;i<listdata.size();i++)
				{
					if(listdata.get(i).get("account").toString().equals(receivermsgFrom)){
						listmap=new HashMap<String, Object>();
						listmap.put("account", listdata.get(i).get("account"));
						listmap.put("name", listdata.get(i).get("name"));
						listmap.put("status", listdata.get(i).get("status"));
						int num;
						if(listdata.get(i).get("num").toString().equals(""))
							num=0;
						else
							num=(Integer)listdata.get(i).get("num");
						num++;
						listmap.put("num", num);
						listdata.remove(i);
						listdata.add(listmap);
						listAdapter=new SimpleAdapter(FriendListActivity.this, listdata, R.layout.frienditem, new String[]{"account","name","status","num"}, new int[]{R.id.account,R.id.name,R.id.status,R.id.num});
						listView.setAdapter(listAdapter);
						break;
					}
				}
				break;
			case 2:
				for(int i=0;i<listdata.size();i++)
				{
					if(listdata.get(i).get("account").toString().equals(presencermsgFrom)){
						listmap=new HashMap<String, Object>();
						listmap.put("account", listdata.get(i).get("account"));
						listmap.put("name", listdata.get(i).get("name"));
						listmap.put("num", listdata.get(i).get("num"));
						listmap.put("status", status);
						listdata.remove(i);
						listdata.add(listmap);
						listAdapter=new SimpleAdapter(FriendListActivity.this, listdata, R.layout.frienditem, new String[]{"account","name","status","num"}, new int[]{R.id.account,R.id.name,R.id.status,R.id.num});
						listView.setAdapter(listAdapter);
						break;
					}
				}
				break;
			}
		}
	};
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) { 
		case 0: 
			// 添加操作 
			boolean flag=XmppTool.deleteUser(roster,deleteAccount);
			if(flag)
				Toast.makeText(FriendListActivity.this,"删除成功", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(FriendListActivity.this,"删除失败", Toast.LENGTH_SHORT).show();
			break;
		}
		return super.onContextItemSelected(item);
	}
}
