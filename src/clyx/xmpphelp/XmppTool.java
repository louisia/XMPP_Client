package clyx.xmpphelp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.OfflineMessageManager;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;

import android.util.Log;
/**
 * class XMPPTool is used to called asmack api based on XMPP
 * @author clyx
 *
 */
public class XmppTool {
	private static XMPPConnection con = null;//XMPPConnection
	private final static  int PORT=5222;//客户端连接服务器端口号
	private final static String IP="192.168.191.1";//客户端连接服务器IP地址
	/*
	 * 打开数据库连接
	 */
	private static void openConnection() {
		try {
			//配置服务器连接的IP地址和端口号 
			ConnectionConfiguration connConfig = new ConnectionConfiguration(IP, PORT);
			connConfig.setSecurityMode(SecurityMode.disabled);
			connConfig.setSendPresence(false);//不将现在的在线状态发送给服务器
			con = new XMPPConnection(connConfig);
			con.connect();//连接服务器
		}
		catch (XMPPException e) 
		{
			e.printStackTrace();
		}
	}
	/**
	 * 获取连接
	 * @return XMPPConnection
	 */
	public static XMPPConnection getConnection() {
		if (con == null) {
			openConnection();
		}
		return con;
	}
	/**
	 * 关闭连接
	 */
	public static void closeConnection() {
		con.disconnect();
		con = null;
	}
	/**
	 * 用户登陆
	 * @param a:用户JID
	 * @param p:用户密码
	 * @return:true(登陆成功),false(登陆失败)
	 */
	public static boolean login(String a,String p){  
		try {
			con.login(a, p);//根据用户名密码，登陆到服务器
			return true;
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * 用户注册
	 * @param a:用户JID
	 * @param p:用户密码
	 * @param n:用户名
	 * @return:true(创建成功),false:(创建失败)
	 */
	public static boolean register(String a,String p,String n){		
		AccountManager amgr = con.getAccountManager();//获取再服务器创建新用户对象
		HashMap<String,String> attr=new HashMap<String,String>();//设置新建对象的属性
		attr.put("name", n);		
		try {
			amgr.createAccount(a, p,attr);//利用指定的用户名，密码和属性创建用户
			return true;
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * 发送消息
	 * @param 好友的JID
	 * @param 发送的消息内容
	 */
	public static void sendMessage(String friendaccount,String sendmsg){
		try {
			ChatManager cm=con.getChatManager();
			Chat chat=cm.createChat(friendaccount,null);//创建聊天
			Message m=new Message();
			m.setBody(sendmsg);//设置消息内容
			chat.sendMessage(m);//发送消息
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 接收离线消息
	 * @return 离线消息
	 */
	public static Map<String,ArrayList<Message>> receiveOfflineMessage(){
		//存放当前用户的所有离线消息，第一个参数表示好友的JID
        Map<String,ArrayList<Message>> offlineMsgs = new HashMap<String,ArrayList<Message>>();  
		OfflineMessageManager offlineManager = new OfflineMessageManager(con);  
        try {  
            Iterator<org.jivesoftware.smack.packet.Message> it = offlineManager.getMessages();  
            while (it.hasNext()) {  
                org.jivesoftware.smack.packet.Message message = it.next(); 
                //获取好友的JID
                String fromUser = message.getFrom().split("/")[0];  
                if(offlineMsgs.containsKey(fromUser)){ //将消息加入相应的JID中
                    offlineMsgs.get(fromUser).add(message);  
                }else{  
                    ArrayList<Message> temp = new ArrayList<Message>();//新建一个JID项
                    temp.add(message);  
                    offlineMsgs.put(fromUser, temp);  
                }  
            }  
            offlineManager.deleteMessages();  
        } catch (Exception e) {  
            e.printStackTrace();  
        } 
        Presence presence = new Presence(Presence.Type.available);
        XmppTool.getConnection().sendPacket(presence);
        return offlineMsgs;
	}
	/**
	 * 搜索好友
	 * @param account好友的JID
	 * @return好友的名称
	 */
	public static String searchFriend(String account){
		String ansS = "";
		try {
			UserSearchManager search = new UserSearchManager(XmppTool.getConnection());
			Form searchForm = search.getSearchForm("search.clyx");
			Form answerForm = searchForm.createAnswerForm();
			answerForm.setAnswer("Username", true);
			answerForm.setAnswer("search", account);
			org.jivesoftware.smackx.ReportedData data = search.getSearchResults(answerForm, "search.clyx");
			Iterator<Row> it = data.getRows();
			Row row = null;
			while (it.hasNext()) {
				row = it.next();
				ansS += row.getValues("username").next().toString()+":"+row.getValues("name").next().toString() + "\n";
				}
		} catch (Exception e) {
			Log.v("XMPPClient", e.toString());
		}
		return ansS;
	}
	
	/** 
     * 添加好友 有分组 
     * @param userName 
     * @param name 
     * @param groupName 
     * @return 
     */ 
    public static boolean addUser(String userName, String name, String groupName) {  
        try {  
            Presence subscription = new Presence(Presence.Type.subscribed);  
            subscription.setTo(userName);  
            userName += "@" + getConnection().getServiceName(); 
            getConnection().sendPacket(subscription);  
            getConnection().getRoster().createEntry(userName, name,new String[] { groupName });  
            return true;  
        } catch (Exception e) {  
            e.printStackTrace(); 
            Log.v("XMPPClient", e.toString());
            return false;  
        }  
    }  
	/**
	 * 删除好友
	 * @param roster好友列表
	 * @param account好友帐号
	 * @return true：成功，false：失败
	 */
	public static boolean deleteUser(Roster roster,String account){
		 try {  
		        RosterEntry entry = roster.getEntry(account);  
		        roster.removeEntry(entry);  
		        return true;  
		    } catch (XMPPException e) {  
		        e.printStackTrace(); 
		        Log.v("XMPPClient", e.toString());
		        return false;  
		    }  
	}
}