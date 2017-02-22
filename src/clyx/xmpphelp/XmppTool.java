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
	private final static  int PORT=5222;//�ͻ������ӷ������˿ں�
	private final static String IP="192.168.191.1";//�ͻ������ӷ�����IP��ַ
	/*
	 * �����ݿ�����
	 */
	private static void openConnection() {
		try {
			//���÷��������ӵ�IP��ַ�Ͷ˿ں� 
			ConnectionConfiguration connConfig = new ConnectionConfiguration(IP, PORT);
			connConfig.setSecurityMode(SecurityMode.disabled);
			connConfig.setSendPresence(false);//�������ڵ�����״̬���͸�������
			con = new XMPPConnection(connConfig);
			con.connect();//���ӷ�����
		}
		catch (XMPPException e) 
		{
			e.printStackTrace();
		}
	}
	/**
	 * ��ȡ����
	 * @return XMPPConnection
	 */
	public static XMPPConnection getConnection() {
		if (con == null) {
			openConnection();
		}
		return con;
	}
	/**
	 * �ر�����
	 */
	public static void closeConnection() {
		con.disconnect();
		con = null;
	}
	/**
	 * �û���½
	 * @param a:�û�JID
	 * @param p:�û�����
	 * @return:true(��½�ɹ�),false(��½ʧ��)
	 */
	public static boolean login(String a,String p){  
		try {
			con.login(a, p);//�����û������룬��½��������
			return true;
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * �û�ע��
	 * @param a:�û�JID
	 * @param p:�û�����
	 * @param n:�û���
	 * @return:true(�����ɹ�),false:(����ʧ��)
	 */
	public static boolean register(String a,String p,String n){		
		AccountManager amgr = con.getAccountManager();//��ȡ�ٷ������������û�����
		HashMap<String,String> attr=new HashMap<String,String>();//�����½����������
		attr.put("name", n);		
		try {
			amgr.createAccount(a, p,attr);//����ָ�����û�������������Դ����û�
			return true;
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * ������Ϣ
	 * @param ���ѵ�JID
	 * @param ���͵���Ϣ����
	 */
	public static void sendMessage(String friendaccount,String sendmsg){
		try {
			ChatManager cm=con.getChatManager();
			Chat chat=cm.createChat(friendaccount,null);//��������
			Message m=new Message();
			m.setBody(sendmsg);//������Ϣ����
			chat.sendMessage(m);//������Ϣ
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}
	/**
	 * ����������Ϣ
	 * @return ������Ϣ
	 */
	public static Map<String,ArrayList<Message>> receiveOfflineMessage(){
		//��ŵ�ǰ�û�������������Ϣ����һ��������ʾ���ѵ�JID
        Map<String,ArrayList<Message>> offlineMsgs = new HashMap<String,ArrayList<Message>>();  
		OfflineMessageManager offlineManager = new OfflineMessageManager(con);  
        try {  
            Iterator<org.jivesoftware.smack.packet.Message> it = offlineManager.getMessages();  
            while (it.hasNext()) {  
                org.jivesoftware.smack.packet.Message message = it.next(); 
                //��ȡ���ѵ�JID
                String fromUser = message.getFrom().split("/")[0];  
                if(offlineMsgs.containsKey(fromUser)){ //����Ϣ������Ӧ��JID��
                    offlineMsgs.get(fromUser).add(message);  
                }else{  
                    ArrayList<Message> temp = new ArrayList<Message>();//�½�һ��JID��
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
	 * ��������
	 * @param account���ѵ�JID
	 * @return���ѵ�����
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
     * ��Ӻ��� �з��� 
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
	 * ɾ������
	 * @param roster�����б�
	 * @param account�����ʺ�
	 * @return true���ɹ���false��ʧ��
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