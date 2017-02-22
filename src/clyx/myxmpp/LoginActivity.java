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
		//设置监听器
		loginButton.setOnClickListener(new MyClick());
		registerButton.setOnClickListener(new MyClick());
	}

	class MyClick implements OnClickListener{
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			//登陆按钮的处理
			case R.id.login_login:
				final String account=accountEditText.getText().toString();  
				final String password=passwordEditText.getText().toString();  
				if(account.equals("") || password.equals("")){  
					Toast.makeText(LoginActivity.this, "账号或密码不能为空！", Toast.LENGTH_SHORT).show();  
				}else{
					//android4.0之后网络服务必须在子线程中
					Thread t=new Thread(new Runnable(){
						@Override
						public void run() {
							XmppTool.getConnection();//获取服务器连接
							boolean flag=XmppTool.login(account,password);
							if(flag){
								//获取离线消息
								handler.sendEmptyMessage(1);
								offlineMsgs=XmppTool.receiveOfflineMessage();
								handler.sendEmptyMessage(2);
								//登陆成功,跳转到FriendListActivity
								Intent intent=new Intent();
								intent.setClass(LoginActivity.this,FriendListActivity.class);//jump to FriendListActivity
								startActivity(intent);  
							}else{  
								//登陆失败
								XmppTool.closeConnection();//关闭服务器连接
								handler.sendEmptyMessage(3);//向主线程发送失败信息
							}  
						}});
					t.start();
				}
				break;
				//注册按钮处理
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
				progressDialog = ProgressDialog.show(LoginActivity.this,  "请稍等...", "正在登陆",true);
				break;
			case 2:
				progressDialog.dismiss();//取消进度条
				break;
			case 3:
				Toast.makeText(LoginActivity.this, "登录失败,用户不存在或帐号密码不匹配！",Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
	};
}

