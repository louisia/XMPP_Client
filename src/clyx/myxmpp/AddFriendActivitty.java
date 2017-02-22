package clyx.myxmpp;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import clyx.xmpphelp.XmppTool;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class AddFriendActivitty extends Activity{
	private EditText userAccountTextView;
	private Button searchButton;
	private ListView friendaddListView;
	private ArrayList<Map<String, String>> listdata;
	private SimpleAdapter listAdapter;
	private HashMap<String, String> listmap;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addfriend);
		userAccountTextView=(EditText)findViewById(R.id.addfriendid);
		searchButton=(Button)findViewById(R.id.addfirendsearchbtn);
		friendaddListView=(ListView)findViewById(R.id.friendaddlistview);
		listdata=new ArrayList<Map<String,String>>();
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String str=XmppTool.searchFriend(userAccountTextView.getText().toString());
				listdata=new ArrayList<Map<String,String>>();
				for(int i=0;i<str.split("\n").length;i++)
				{
					listmap=new HashMap<String, String>();
					listmap.put("account", str.split("\n")[i].split(":")[0].toString());
					listmap.put("name", str.split("\n")[i].split(":")[1].toString());
					listdata.add(listmap);
				}
				listAdapter=new SimpleAdapter(AddFriendActivitty.this, listdata, R.layout.friendadditem, new String[]{"account","name"}, new int[]{R.id.friendaddaccount,R.id.friendaddname});
				friendaddListView.setAdapter(listAdapter);
			}
		});
		friendaddListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String account=(String)listdata.get(position).get("account");
				String name=(String)listdata.get(position).get("name");
				XmppTool.addUser(account,name,"ÎÒµÄºÃÓÑ");
				AddFriendActivitty.this.finish();
			}
		});
	}
}
