/**
 * @author Bruno Zeraik 
 */

package com.client;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;

import com.client.exceptions.InvalidCredentialsException;
import com.client.exceptions.ObjectDoesNotExistException;
import com.client.exceptions.ValidationException;
import com.uteis.StaticValues;
import com.uteis.Util;

public class HttpConnection{
 
    public static final int DID_START = 0;
    public static final int DID_ERROR = 1;
    public static final int DID_SUCCEED = 2;
 
    private static final int GET = 0;
    private static final int POST = 1;
    private static final int PUT = 2;
    private static final int DELETE = 3;
    private static final int AUTHENTICATE = 4;
    private static final int CREATE_ACCOUNT = 5;
    
    String login, password, authentication;
    SharedPreferences sharedpreferences;
    Context context;
    Util util = new Util();
    
    public HttpConnection(Context context) { 
    	this.context = context;
    }

    public String createAccount(String url, HashMap<String, String> data) throws IllegalStateException, IOException, JSONException, ValidationException, Exception {
		return executeHTTPConnection(CREATE_ACCOUNT, url, data);
    }
    
    public String authenticate(String url, HashMap<String, String> data) throws IllegalStateException, IOException, JSONException, ValidationException, Exception {
		return executeHTTPConnection(AUTHENTICATE, url, data);
    }
    
    public String get(String url) throws IllegalStateException, IOException, JSONException, ValidationException, Exception {
		return executeHTTPConnection(GET, url, null);
    }
 
    public String post(String url, HashMap<String, String> data) throws IllegalStateException, IOException, JSONException, ValidationException, Exception {
        return executeHTTPConnection(POST, url, data);
    }
 
    public String put(String url, HashMap<String, String> data) throws IllegalStateException, IOException, JSONException, ValidationException, Exception {
        return executeHTTPConnection(PUT, url, data);
    }
 
    public String delete(String url) throws IllegalStateException, IOException, JSONException, ValidationException, Exception {
        return executeHTTPConnection(DELETE, url, null);
    }
 
    public Bitmap bitmap(String url) throws IllegalStateException, IOException {
        return executeHTTPConnectionBitmap(url);
    }
 
    private boolean isConnected(){
        ConnectivityManager connectivity = (ConnectivityManager)this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
          if (connectivity != null) 
          {
              NetworkInfo netInfo = connectivity.getActiveNetworkInfo();
              
              if (netInfo == null) {
                return false;
              }
              
              int netType = netInfo.getType();

              if (netType == ConnectivityManager.TYPE_WIFI || 
                    netType == ConnectivityManager.TYPE_MOBILE) {
                  return netInfo.isConnected();

              } else {
                  return false;
              }
          }else{
            return false;
          }
    }
    
    private String executeHTTPConnection(int method, String url, HashMap<String, String> data) throws IllegalStateException, IOException, JSONException, ValidationException, Exception {
    	try {
    		
    		if (!isConnected()){
    			throw new Exception("Sem conexão.");
    		}
    		
    		HttpClient httpClient = new DefaultHttpClient();
            HttpConnectionParams.setSoTimeout(httpClient.getParams(), 25000);
            HttpResponse response = null;
            switch (method) {
            case GET:
            	HttpGet httpGet = new HttpGet(url);
            	httpGet = (HttpGet) setHeaders(httpGet);
                response = httpClient.execute(httpGet);
                break;
            case POST:
                HttpPost httpPost = new HttpPost(url);
                httpPost = setPostParams(httpPost, data);
                httpPost = (HttpPost) setHeaders(httpPost);
                response = httpClient.execute(httpPost);
                break;
            case PUT:
                HttpPut httpPut = new HttpPut(url);
//                httpPut.setEntity(new StringEntity(data));
                response = httpClient.execute(httpPut);
                break;
            case DELETE:
            	HttpDelete httpDelete = new HttpDelete(url);
            	httpDelete = (HttpDelete) setHeaders(httpDelete);
                response = httpClient.execute(httpDelete);
                break;
            case AUTHENTICATE:
    			login = data.get("login");
    			password = data.get("password");
            	HttpGet get = new HttpGet(url);
    			authentication = login+":"+password;
    			authentication = Base64.encodeToString(authentication.getBytes("UTF-8"), Base64.DEFAULT);
    			get.addHeader(new BasicHeader("Accept", "application/json"));
    			get.setHeader("Authorization", "Basic "+ authentication);
                response = httpClient.execute(get);
                break;
            case CREATE_ACCOUNT:
                HttpPost httpCreateAccount = new HttpPost(url);
                httpCreateAccount = setPostParams(httpCreateAccount, data);
                response = httpClient.execute(httpCreateAccount);
                break;             
            default:
                throw new IllegalArgumentException("Unknown Request.");
            }  
            
            return validResponse(response);

		} catch (HttpHostConnectException e) {
			Log.i("Error: ", e.getMessage());
			throw new Exception("Houve um erro na comunicação. Tente novamente mais tarde.");
		}
    	
    }
 
    private String validResponse(HttpResponse response) throws JSONException, ValidationException, Exception {
    	
    	String responseString;
    	
		if (response.getStatusLine().getStatusCode() == 401){
			throw new InvalidCredentialsException();
    	}else{
    		responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
    		Log.i("Response: ", responseString);
    		JSONObject responseJson = util.convertStringToJson(responseString);
    		
    		if (responseJson != null && responseJson.has("exception") && responseJson.getBoolean("exception")){
    			
    			if(responseJson.get("name").equals("ValidationException")){
    				throw new ValidationException(getErrors(responseJson.getJSONObject("error_list")));
    			}
    			if(responseJson.get("name").equals("ObjectDoesNotExistException")){
    				throw new ObjectDoesNotExistException();
    			}
    			else{
    				throw new Exception("Ocorreu um erro não esperado.");
    			}
    		}
    	}
		return responseString;
	}

    private HttpRequestBase setHeaders(HttpRequestBase http) throws Exception{
    	sharedpreferences = this.context.getSharedPreferences(StaticValues.PREFERENCES, Context.MODE_PRIVATE);
    	login = sharedpreferences.getString("login", null);
		password = sharedpreferences.getString("password", null);
		authentication = login+":"+password;
		authentication = Base64.encodeToString(authentication.getBytes("UTF-8"), Base64.URL_SAFE|Base64.NO_WRAP);
		http.addHeader(new BasicHeader("Accept", "application/json"));
		http.setHeader("Authorization", "Basic "+ authentication);   
		return http;
    }
    
    private HttpPost setPostParams(HttpPost httpPost, HashMap<String, String> data) throws UnsupportedEncodingException{
        ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters);
        httpPost.setEntity(entity);
        return httpPost;
    }

    private String getErrors(JSONObject json) throws JSONException{
    	
    	Map<String,String> keys_errors = new HashMap<String,String>();
    	keys_errors.put("model", "Modelo");
    	keys_errors.put("motor", "Motor");
    	keys_errors.put("liters", "Litros");
    	keys_errors.put("odometer", "Odômetro");
    	keys_errors.put("fuel_price", "Preço");
    	keys_errors.put("station", "Posto");
    	keys_errors.put("message", "Mensagem");
    	keys_errors.put("username", "Usuário");
    	keys_errors.put("name", "Nome");
    	keys_errors.put("password", "Senha");
    	keys_errors.put("confirm_password", "Confirmação de Senha");
    	keys_errors.put("manufactured", "Ano de Fabricação");
    	
    	String response = "";
        Iterator<String> keys = json.keys();
        JSONArray jsonArray = null;
        while(keys.hasNext()){
            String key = keys.next();
            String val = null;
            String messages = "";
            try{
                 JSONObject value = json.getJSONObject(key);
                 getErrors(value);
            }catch(Exception e){
                val = json.getString(key);
            	jsonArray = json.getJSONArray(key);
            }
            
            if(val != null){
                int i;
                for(i = 0; i < jsonArray.length(); i++){
                	messages += jsonArray.getString(i);
                }
                String key_var = keys_errors.get(key);
                key = key_var != null ? key_var : key; 
                response +=  key + ": " +  messages + "\n";
            }
        }
        return response;
    }
    
    private Bitmap executeHTTPConnectionBitmap(String url) throws IllegalStateException, IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setSoTimeout(httpClient.getParams(), 25000);
        HttpResponse response = httpClient.execute(new HttpGet(url));       
        return processBitmapEntity(response.getEntity());
    }
 
    private Bitmap processBitmapEntity(HttpEntity entity) throws IOException {
        BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
        Bitmap bm = BitmapFactory.decodeStream(bufHttpEntity.getContent());
        return bm;
    }
 
}