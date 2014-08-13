/**
 * @author Bruno Zeraik 
 */

package com.client;

import java.util.HashMap;

import org.json.JSONObject;

import android.content.Context;

import com.uteis.Urls;
import com.uteis.Util;

public class UserService {

  Util util = new Util();
  HttpConnection httpConnection;

  public UserService(Context context) {
    super();
    httpConnection = new HttpConnection(context);
  }

  public boolean authenticate(String login, String password) throws Exception {

    HashMap<String, String> data = new HashMap<String, String>();
    data.put("login", login);
    data.put("password", password);

    String response = httpConnection.authenticate(Urls.USER_AUTHENTICATE, data);

    JSONObject responseJson = util.convertStringToJson(response);

    return responseJson.has("token");
  }

  public void createAccount(String login, String password, String confirmPassword, String name, String email) throws Exception {

    HashMap<String, String> data = new HashMap<String, String>();

    data.put("username", login);
    data.put("password", password);
    data.put("confirm_password", confirmPassword);
    data.put("name", name);
    data.put("email", email);

    httpConnection.createAccount(Urls.USER_SAVE, data);
  }

  public JSONObject getUser(String username) throws Exception {

    String response = httpConnection.get(Urls.USER_GET + username);

    return util.convertStringToJson(response).getJSONObject("user");
  }

  public void updateProfile(String name, String username) throws Exception {
    HashMap<String, String> data = new HashMap<String, String>();

    data.put("username", username);
    data.put("name", name);

    httpConnection.post(Urls.USER_UPDATE, data);
  }
}
