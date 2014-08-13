/**
 * @author Bruno Zeraik 
 */

package com.client;

import java.util.HashMap;

import org.json.JSONObject;

import android.content.Context;

import com.uteis.Urls;
import com.uteis.Util;

public class SupplyService {

  Util util = new Util();
  HttpConnection httpConnection;

  public SupplyService(Context context) {
    super();
    httpConnection = new HttpConnection(context);
  }

  public void save(String odometer, String liters, String date, String isFull, String vehicle, String fuel, String station, String fuel_price, String obs) throws Exception {
    HashMap<String, String> data = new HashMap<String, String>();

    data.put("odometer", odometer);
    data.put("liters", liters);
    data.put("date", date);
    data.put("is_full", isFull);
    data.put("vehicle", vehicle);
    data.put("fuel", fuel);
    data.put("station", station);
    data.put("fuel_price", fuel_price);
    data.put("obs", obs);

    httpConnection.post(Urls.SUPPLY_SAVE, data);
  }

  public void update(String odometer, String liters, String date, String isFull, String vehicle, String fuel, String station, String fuel_price, String obs, int supply_id) throws Exception {
    HashMap<String, String> data = new HashMap<String, String>();

    data.put("odometer", odometer);
    data.put("liters", liters);
    data.put("date", date);
    data.put("is_full", isFull);
    data.put("vehicle", vehicle);
    data.put("fuel", fuel);
    data.put("station", station);
    data.put("fuel_price", fuel_price);
    data.put("obs", obs);
    data.put("supply_id", util.convertIntToString(supply_id));

    httpConnection.post(Urls.SUPPLY_UPDATE, data);
  }

  public JSONObject getByVehicle(String vehicle_id) throws Exception {

    String result = httpConnection.get(Urls.SUPPLY_LIST + vehicle_id);
    return util.convertStringToJson(result);
  }

  public JSONObject get(int id) throws Exception {

    String result = httpConnection.get(Urls.SUPPLY_GET + id);
    return util.convertStringToJson(result);
  }

  public void delete(int id) throws Exception {

    httpConnection.delete(Urls.SUPPLY_DELETE + id);
  }

  public JSONObject getSummary(String vehicle_id) throws Exception {

    String result = httpConnection.get(Urls.SUPPLY_SUMMARY + vehicle_id);
    return util.convertStringToJson(result);
  }

}
