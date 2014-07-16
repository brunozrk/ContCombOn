/**
 * @author Bruno Zeraik 
 */

package com.contcombon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.client.CarService;
import com.client.SupplyService;
import com.client.exceptions.InvalidCredentialsException;
import com.uteis.StaticValues;

public class SummaryActivity extends BaseActivity {

	TextView tvMotor, tvManufactured, tvAverage, tvSummaryAverage, tvCountVehicles, tvAverageVehicles;
	int currentPage = 0;
	
	Spinner spVehicles;
	String idVehicle = "-1";
	
	ImageButton btnCriterion;
	
	JSONObject summary;
	JSONObject vehicles_fuels;

	LinearLayout progressbar, summaryBody;
	String errorMessage = "";
	ListView lvSupplies, lvEqualVehiclesSupplies;
	ListAdapter adapter;
	ArrayList<HashMap<String, Object>> list;
	
	public static final int PAGE_SUMMARY = 0;
	
	public static final int MSG_ERROR = 1;
	public static final int LIST = 2;
	public static final int SPINNER_LIST = 3;
	
	
	/**
	 * CALLBACKS
	 */
    private Handler handler = new Handler() {
        @Override
         public void handleMessage(Message msg) {
        	finishProgress(SummaryActivity.this);
        	switch (msg.what) {
        		case MSG_ERROR: 		util.showMessage(getResources().getString(R.string.error), 
						 				  			     errorMessage, 
						 				 				 SummaryActivity.this);
        								break;
        		case LIST:				progressbar.setVisibility(View.GONE);
        								summaryBody.setVisibility(View.VISIBLE);
        								loadSummary();
        								break;
        		case SPINNER_LIST:		prepareSpinnerVehicles();
        								setOnItemSelectedSpinnerList();
										break;
        	}
        }
    };
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar = getSupportActionBar();
		actionBar.setTitle(getDrawerListViewItems()[StaticValues.ACTION_SUMMARY]);
		loadSummaryPage();
		leftMenu();
		onClickCriterion();
	}
	

	public void onClickCriterion(){
		btnCriterion = (ImageButton) findViewById(R.id.btnCriterion);
		
		btnCriterion.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				util.showMessage(getResources().getString(R.string.criterion),
								 getResources().getString(R.string.criterion_description),
								 SummaryActivity.this);
			}
		});
	}
	
	public void loadSummaryPage(){
		upateMenu(PAGE_SUMMARY);
		setContentView(R.layout.activity_summary);
		
    	startProgress(SummaryActivity.this);
        new Thread() {
            @Override
            public void run() {
                super.run();
				 CarService carService = new CarService(SummaryActivity.this);
				 try {
					 vehicles_fuels = carService.getVehiclesAndFuel();
					 handler.sendEmptyMessage(SPINNER_LIST);
				 }catch (InvalidCredentialsException e) {
	        			logout();
        		 }catch (Exception e){
					 errorMessage = e.getMessage();
					 handler.sendEmptyMessage(MSG_ERROR);
				 }
            }
        }.start();
	}
	
	
	/**
	 * Spinner Functions
	 */
	
	private void prepareSpinnerVehicles(){
		JSONArray items;
		List<String> names = new ArrayList<String>();
		List<String> ids = new ArrayList<String>();
		try {
			spVehicles = (Spinner) findViewById(R.id.spVehicles);
			items = vehicles_fuels.getJSONArray("vehicles");
			for (int i=0; i<items.length(); i++){
				names.add(items.getJSONObject(i).getString("model__name"));
				ids.add(items.getJSONObject(i).getString("id"));
			}
			ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, names);
			ArrayAdapter<String> spinnerArrayAdapter = arrayAdapter;
			spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spVehicles.setAdapter(spinnerArrayAdapter);
			if (util.convertStringToInt(idVehicle) != -1){
				spVehicles.setSelection(ids.indexOf(idVehicle));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setOnItemSelectedSpinnerList(){
		spVehicles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 
			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
				try {
					idVehicle = vehicles_fuels.getJSONArray("vehicles").getJSONObject(position).getString("id");
					summaryBody = (LinearLayout) findViewById(R.id.summaryBody);
					summaryBody.setVisibility(View.GONE);
					progressbar = (LinearLayout) findViewById(R.id.progressbar);
					progressbar.setVisibility(View.VISIBLE);
			        new Thread() {
			            @Override
			            public void run() {
			                super.run();
							 SupplyService supplyService = new SupplyService(SummaryActivity.this);
							 try {
								 summary = supplyService.getSummary(idVehicle);
								 handler.sendEmptyMessage(LIST);
							 }catch (InvalidCredentialsException e) {
				        			logout();
			        		 }catch (Exception e){
								 errorMessage = e.getMessage();
								 handler.sendEmptyMessage(MSG_ERROR);
							 }
			            }
			        }.start();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
 
			}
		});
	}
	
	
	public void loadSummary(){
		try {
			
			// ---------- Vehicle ---------- 
			JSONObject vehicleObject = summary.getJSONObject("vehicle");
			
			tvMotor = (TextView) findViewById(R.id.tvMotor);
			tvMotor.setText(vehicleObject.getString("motor"));

			tvManufactured = (TextView) findViewById(R.id.tvManufactured);
			tvManufactured.setText(vehicleObject.getString("manufactured"));
			
			// ---------- Equal Vehicles ---------- 
			JSONObject equalVehiclesObject = summary.getJSONObject("equal_vehicles");
			tvCountVehicles = (TextView) findViewById(R.id.tvCountVehicles);
			int countSameVehicles = equalVehiclesObject.getInt("count");
			String sameVehicle = "";
			
			if (countSameVehicles > 1)
				sameVehicle += countSameVehicles + " " + getResources().getString(R.string.vehicles_found);
			else
				sameVehicle += countSameVehicles + " " + getResources().getString(R.string.vehicle_found);
			tvCountVehicles.setText(sameVehicle);

			tvAverageVehicles  = (TextView) findViewById(R.id.tvAverageVehicles);
			tvAverageVehicles.setText(equalVehiclesObject.getString("total_average") + " Km/L");

			JSONObject equalVehiclesFuelsDetailsObject = equalVehiclesObject.getJSONObject("fuels_details");
			Iterator<?> equalVehiclesFuelsKeys = equalVehiclesFuelsDetailsObject.keys();
			
			list = new ArrayList<HashMap<String, Object>>();
	        while( equalVehiclesFuelsKeys.hasNext() ){
	            String fuel = (String)equalVehiclesFuelsKeys.next();
	            
	            HashMap<String, Object> map = new HashMap<String, Object>();
	            map.put("fuel", fuel);
	            map.put("average", equalVehiclesFuelsDetailsObject.getString(fuel) + " Km/L");
	            list.add(map);
	        }
			String[] from2 = new String[] { "fuel", "average"};
			int[] to2 = new int[] { R.id.tvFuel, R.id.tvAverage};
			adapter = new SimpleAdapter(SummaryActivity.this, list, R.layout.item_list_summary_supply, from2, to2);
			lvEqualVehiclesSupplies = (ListView) findViewById(R.id.listViewSuppliesGeneral);
			lvEqualVehiclesSupplies.setAdapter(adapter);

			
			// ---------- Supplies ---------- 
			JSONObject suppliesObject = summary.getJSONObject("supplies");

			tvAverage = (TextView) findViewById(R.id.tvAverage);
			tvAverage.setText(suppliesObject.getString("total_average") + " Km/L");
			
			JSONObject fuelsDetailsObject = suppliesObject.getJSONObject("fuels_details");
			Iterator<?> fuelsKeys = fuelsDetailsObject.keys();
			
			list = new ArrayList<HashMap<String, Object>>();
	        while( fuelsKeys.hasNext() ){
	            String fuel = (String)fuelsKeys.next();
	            
	            HashMap<String, Object> map = new HashMap<String, Object>();
	            map.put("fuel", fuel);
	            map.put("average", fuelsDetailsObject.getString(fuel) + " Km/L");
	            list.add(map);
	        }
			String[] from = new String[] { "fuel", "average"};
			int[] to = new int[] { R.id.tvFuel, R.id.tvAverage};
			adapter = new SimpleAdapter(SummaryActivity.this, list, R.layout.item_list_summary_supply, from, to);
			lvSupplies = (ListView) findViewById(R.id.listViewSupplies);
			lvSupplies.setAdapter(adapter);
			
			tvSummaryAverage = (TextView) findViewById(R.id.tvSummaryAverage);
			
			if (isBelowAverage(suppliesObject.getDouble("total_average"), equalVehiclesObject.getDouble("total_average"))){
				tvAverage.setTextColor(Color.parseColor(StaticValues.COLOR_RED));
				tvSummaryAverage.setText(getResources().getString(R.string.below_average));
				tvSummaryAverage.setTextColor(Color.parseColor(StaticValues.COLOR_RED));
			}else if (isSameAverage(suppliesObject.getDouble("total_average"), equalVehiclesObject.getDouble("total_average"))) {
				tvAverage.setTextColor(Color.parseColor(StaticValues.COLOR_BLUE));
				tvSummaryAverage.setText(getResources().getString(R.string.same_average));
				tvSummaryAverage.setTextColor(Color.parseColor(StaticValues.COLOR_BLUE));
			}else{
				tvAverage.setTextColor(Color.parseColor(StaticValues.COLOR_GREEN));
				tvSummaryAverage.setText(getResources().getString(R.string.above_average));
				tvSummaryAverage.setTextColor(Color.parseColor(StaticValues.COLOR_GREEN));
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean isBelowAverage(Double vehicleAverage, Double totalAverage){
		Double percentage = (vehicleAverage * 100) / totalAverage;
		return percentage < 80.0;
	}

	private boolean isSameAverage(Double vehicleAverage, Double totalAverage){
		Double percentage = (vehicleAverage * 100) / totalAverage;
		return percentage > 80.0 && percentage < 120.0;
	}

//	private boolean isAboveAverage(Double vehicleAverage, Double totalAverage){
//		Double percentage = (vehicleAverage * 100) / totalAverage;
//		return percentage < 120.0;
//	}
	
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	        case android.R.id.home: if (currentPage == PAGE_SUMMARY){
						    			return(super.onOptionsItemSelected(item));
						    		}
	        						redirectBack();
	        						return true;
		}
	    return(super.onOptionsItemSelected(item));
	}
    
    
	@Override
    public void onBackPressed() {
		redirectBack();
    }
	
	public void redirectBack(){
		switch (currentPage) {
			case PAGE_SUMMARY:
				returnToIndex();
				break;
			default:
				finish();
				break;
		}
	}
	
	private void upateMenu(int page){
		currentPage = page;
		supportInvalidateOptionsMenu();
	}

}
