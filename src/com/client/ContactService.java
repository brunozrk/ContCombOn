/**
 * @author Bruno Zeraik 
 */

package com.client;

import java.util.HashMap;

import org.json.JSONObject;

import android.content.Context;

import com.uteis.Urls;
import com.uteis.Util;

public class ContactService {

	Util util = new Util();
	HttpConnection httpConnection;
	
	public ContactService(Context context) {
		super();
		httpConnection = new HttpConnection(context);
	}
	
	public void sendMessage(String message) throws Exception{
		HashMap<String, String> data = new HashMap<String, String>();
		
		data.put("message", message);
		
		httpConnection.post(Urls.CONTACT_MESSAGE_SAVE, data);
	}
	
	
	public JSONObject getMessages() throws Exception{
		
		String result = httpConnection.get(Urls.CONTACT_MESSAGE_LIST);
		return util.convertStringToJson(result);
	}
	
	
	
	public JSONObject getMessage(int id) throws Exception{
		
		String result = httpConnection.get(Urls.CONTACT_MESSAGE_GET + id);
		return util.convertStringToJson(result);
	}	
	
	
	public void delete(int id) throws Exception{
		
		httpConnection.delete(Urls.CONTACT_MESSAGE_DELETE + id);
	}
}
