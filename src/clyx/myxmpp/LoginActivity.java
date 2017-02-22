package clyx.myxmpp;

import java.util.ArrayList;
import java.util.Map;

import clyx.xmpphelp.XmppTool;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class LoginActivity extends Activity {
	private EditText accountEditText;  
	private EditText passwordEditText;
	private Button loginButton;
	private Button registerButton;
	public  static Map<String, ArrayList<org.jivesoftware.smack.packet.Message>> offlineMsgs;  
	private ProgressDialog progressDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		accountEditText=(EditText) findViewById(R.id.login_account);  
		passwordEditText=(EditText) findViewById(R.id.login_password);  
		loginButton=(Button)findViewById(R.id.login_login);
		registerButton=(Button)findViewById(R.id.login_register);
		//���ü�����
		loginButton.setOnClickListener(new MyClick());
		registerButton.setOnClickListener(new MyClick());
	}

	class MyClick implements OnClickListener{
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			//��½��ť�Ĵ���
			case R.id.login_login:
				final String account=accountEditText.getText().toString();  
				final String password=passwordEditText.getText().toString();  
				if(account.equals("") || password.equals("")){  
					Toast.makeText(LoginActivity.this, "�˺Ż����벻��Ϊ�գ�", Toast.LENGTH_SHORT).show();  
				}else{
					//android4.0֮�����������������߳���
					Thread t=new Thread(new Runnable(){
						@Override
						public void run() {
							XmppTool.getConnection();//��ȡ����������
							boolean flag=XmppTool.login(account,password);
							if(flag){
								//��ȡ������Ϣ
								handler.sendEmptyMessage(1);
								offlineMsgs=XmppTool.receiveOfflineMessage();
								handler.sendEmptyMessage(2);
								//��½�ɹ�,��ת��FriendListActivity
								Intent intent=new Intent();
								intent.setClass(LoginActivity.this,FriendListActivity.class);//jump to FriendListActivity
								startActivity(intent);  
							}else{  
								//��½ʧ��
								XmppTool.closeConnection();//�رշ���������
								handler.sendEmptyMessage(3);//�����̷߳���ʧ����Ϣ
							}  
						}});
					t.start();
				}
				break;
				//ע�ᰴť����
			case R.id.login_register:
				startActivity(new Intent(LoginActivity.this,RegisterActivity.class));  
				break;
			}  
		}
	}

	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				progressDialog = ProgressDialog.show(LoginActivity.this,  "���Ե�...", "���ڵ�½",true);
				break;
			case 2:
				progressDialog.dismiss();//ȡ��������
				break;
			case 3:
				Toast.makeText(LoginActivity.this, "��¼ʧ��,�û������ڻ��ʺ����벻ƥ�䣡",Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
	};
}

