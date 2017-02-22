package clyx.myxmpp;

import clyx.xmpphelp.XmppTool;
import android.annotation.SuppressLint;
import android.app.Activity;
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
public class RegisterActivity extends Activity{
	private EditText accountEditText;
	private EditText usernameEditText;
	private EditText passwordEditText;
	private EditText confirmpasswordEditText;
	private Button registerButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);	
		//�ؼ���ʼ��
		accountEditText=(EditText) findViewById(R.id.register_account); 
		usernameEditText=(EditText)findViewById(R.id.register_username);
		passwordEditText=(EditText) findViewById(R.id.register_password);  
		confirmpasswordEditText=(EditText) findViewById(R.id.register_confirmpassword);  
		registerButton=(Button)findViewById(R.id.register_register);
		//����ע�ᰴť������
		registerButton.setOnClickListener(new MyClick());	
		}
	class MyClick implements OnClickListener{
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.register_register:
				final String account=accountEditText.getText().toString(); 
				final String username=usernameEditText.getText().toString();
				final String password=passwordEditText.getText().toString(); 
				final String confirmpassword=confirmpasswordEditText.getText().toString();
				if(account.equals("")||username.equals("")||password.equals("")||confirmpassword.equals("")){
					Toast.makeText(RegisterActivity.this, "ע����Ϣ����Ϊ�գ�", Toast.LENGTH_SHORT).show();  			
				}else if(!password.equals(confirmpassword)){
					Toast.makeText(RegisterActivity.this, "�������벻һ�£�", Toast.LENGTH_SHORT).show();  			
				}else{
					Thread t=new Thread(new Runnable(){
						@Override
						public void run() {
							XmppTool.getConnection();
							boolean flag=XmppTool.register(account,password,username);
							if(flag){
								startActivity(new Intent(RegisterActivity.this,LoginActivity.class));  
							}else{  
								XmppTool.closeConnection();
								handler.sendEmptyMessage(0);//���̷߳���ע��ʧ����Ϣ
							}  
						}});
					t.start();
				}
				break;
			}
		}
	}
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Toast.makeText(RegisterActivity.this, "ע��ʧ��,�ʺ��Ѵ��ڣ�",Toast.LENGTH_SHORT).show();
		}
	};
}
