/**
 * @author Bruno Zeraik 
 */

package com.contcombon;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.client.CarService;
import com.client.exceptions.InvalidCredentialsException;
import com.uteis.StaticValues;

public class RankingActivity extends BaseActivity {

  JSONObject ranking;

  TextView tvNoCars;
  LinearLayout progressbar, summaryBody;
  String errorMessage = "";
  ListAdapter adapter;
  ListView lvRanking;
  ArrayList<HashMap<String, String>> list;

  public static final int MSG_ERROR = 1;
  public static final int LIST = 2;

  /**
   * CALLBACKS
   */
  private Handler handler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      progressbar.setVisibility(View.GONE);
      switch (msg.what) {
        case MSG_ERROR:
          util.showMessage(getResources().getString(R.string.error), errorMessage, RankingActivity.this);
          break;
        case LIST:
          if (adapter.isEmpty()) {
            tvNoCars.setText(getResources().getString(R.string.no_cars));
            tvNoCars.setVisibility(View.VISIBLE);
          }
          else {
            tvNoCars.setVisibility(View.GONE);
            lvRanking.setVisibility(View.VISIBLE);
            lvRanking.setAdapter(adapter);
          }
          break;
      }
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ranking);
    actionBar = getSupportActionBar();
    actionBar.setTitle(getDrawerListViewItems()[StaticValues.ACTION_RANKING]);
    leftMenu();

    tvNoCars = (TextView) findViewById(R.id.tvNoCars);
    tvNoCars.setVisibility(View.GONE);
    progressbar = (LinearLayout) findViewById(R.id.progressbar);
    progressbar.setVisibility(View.VISIBLE);

    new Thread() {

      @Override
      public void run() {
        super.run();
        CarService carService = new CarService(RankingActivity.this);
        try {
          ranking = carService.getRanking();
          prepareListView(ranking);
          handler.sendEmptyMessage(LIST);
        }
        catch (InvalidCredentialsException e) {
          logout();
        }
        catch (Exception e) {
          errorMessage = e.getMessage();
          handler.sendEmptyMessage(MSG_ERROR);
        }
      }
    }.start();

  }

  public void prepareListView(JSONObject ranking) throws JSONException {
    JSONArray items = ranking.getJSONArray("ranking");
    list = new ArrayList<HashMap<String, String>>();
    for (int i = 0; i < items.length(); i++) {
      JSONObject item = items.getJSONObject(i);
      String vehicle = item.getString("vehicle");
      String count = item.getJSONObject("details").getString("count");
      String average = item.getJSONObject("details").getString("total_average");
      HashMap<String, String> map = new HashMap<String, String>();
      map.put("position", i + 1 + "º");
      map.put("vehicle", vehicle);
      map.put("count", count + " Veículo(s)");
      map.put("average", average + " Km/L");
      list.add(map);
    }
    String[] from = new String[] { "position", "vehicle", "count", "average" };
    int[] to = new int[] { R.id.tvPosition, R.id.tvVehicle, R.id.tvCount, R.id.tvAverage };
    adapter = new SimpleAdapter(RankingActivity.this, list, R.layout.item_list_ranking, from, to);
    lvRanking = (ListView) findViewById(R.id.listViewRanking);
  }

}
