/**
 * @author Bruno Zeraik 
 */

package com.client;

import java.util.HashMap;

import org.json.JSONObject;

import android.content.Context;

import com.uteis.Urls;
import com.uteis.Util;

public class CarService {

	Util util = new Util();
	HttpConnection httpConnection;
	
	public CarService(Context context) {
		super();
		httpConnection = new HttpConnection(context);
	}
	
	public void save(String model, String motor, String manufactured) throws Exception{
		HashMap<String, String> data = new HashMap<String, String>();
		
		data.put("model", model);
		data.put("motor", motor);
		data.put("manufactured", manufactured);
		
		httpConnection.post(Urls.CAR_SAVE, data);
	}
	
	public void update(String model, String motor, String manufactured, int car_id) throws Exception{
		HashMap<String, String> data = new HashMap<String, String>();
		
		data.put("model", model);
		data.put("motor", motor);
		data.put("manufactured", manufactured);
		data.put("car_id", util.convertIntToString(car_id));
		
		httpConnection.post(Urls.CAR_UPDATE, data);
	}
	
	public JSONObject getCars() throws Exception{
		
		String result = httpConnection.get(Urls.CAR_LIST);
		return util.convertStringToJson(result);
	}
	
	public JSONObject getVehiclesAndFuel() throws Exception{
		
		String result = httpConnection.get(Urls.CAR_LIST_FUEL);
		return util.convertStringToJson(result);
	}
	
	public JSONObject get(int id) throws Exception{
		
		String result = httpConnection.get(Urls.CAR_GET + id);
		return util.convertStringToJson(result);
	}
	
	public void delete(int id) throws Exception{
		
		httpConnection.delete(Urls.CAR_DELETE + id);
	}
	
	
	public JSONObject getModels() throws Exception{
		
		String result = httpConnection.get(Urls.CAR_GET_MODELS);
		return util.convertStringToJson(result);
	}
	
	public JSONObject getRanking() throws Exception{
		
		String result = httpConnection.get(Urls.CAR_RANKING);
		return util.convertStringToJson(result);
	}
	
	
}
