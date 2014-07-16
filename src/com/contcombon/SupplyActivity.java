/**
 * @author Bruno Zeraik 
 */

package com.contcombon;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.uteis.Util;

public class SupplyActivity extends BaseActivity {

	// Date Dialog
	int dpYear;
	int dpMonth;
	int dpDay;
	TextView tvDate;
	ImageButton btnDatePicker;
	StringBuilder date;
	final int DATE_DIALOG_ID = 0;

	Util util = new Util();
	EditText etOdometer, etLiters, etPrice, etStation, etObs;
	CheckBox chIsFull;
	TextView tvNoSupplies;
	int currentPage = 0;
	MenuItem menuNew, menuSave;
	
	Spinner spVehicles;
	String idVehicle = "-1";

	Spinner spFuels;
	String idFuel = "-1";
	
	JSONObject vehicles_fuels;

	LinearLayout progressbar;
	String errorMessage = "";
	ListView lvSupplies;
	ListAdapter adapter;
	ArrayList<HashMap<String, Object>> list;
	JSONObject supply;
	int supply_id = 0;
	
	public static final int PAGE_SUPPLY_LIST = 0;
	public static final int PAGE_SUPPLY_NEW = 1;
	public static final int PAGE_CONTACT_EDIT = 2;
	
	public static final int CREATE_SUCCESS = 0;
	public static final int MSG_ERROR = 1;
	public static final int LIST = 2;
	public static final int EDIT_FORM = 3;
	public static final int DELETE_SUCCESS = 4;
	public static final int UPDATE_SUCCESS = 5;
	public static final int SPINNER_FORM = 6;
	public static final int SPINNER_LIST = 7;
	
	
	/**
	 * CALLBACKS
	 */
    private Handler handler = new Handler() {
        @Override
         public void handleMessage(Message msg) {
        	finishProgress(SupplyActivity.this);
        	switch (msg.what) {
        		case CREATE_SUCCESS:    util.showToast(getResources().getString(R.string.create_success), 
    									    		   SupplyActivity.this);
						        	    loadSupplyListPage();
						        		enableDrawer();
 						 				break;
        		case UPDATE_SUCCESS:    util.showToast(getResources().getString(R.string.update_success), 
											    		   SupplyActivity.this);
								 	    loadSupplyListPage();
								 		enableDrawer();
										break;
        		case MSG_ERROR: 		util.showMessage(getResources().getString(R.string.error), 
						 				  			     errorMessage, 
						 				 				 SupplyActivity.this);
        								break;
        		case LIST:				progressbar.setVisibility(View.GONE);
        								if (adapter.isEmpty()) {
        									tvNoSupplies.setText(getResources().getString(R.string.no_supplies));
        									tvNoSupplies.setVisibility(View.VISIBLE);
        									lvSupplies.setAdapter(null);
        								}else{
        									tvNoSupplies.setVisibility(View.GONE);
        									lvSupplies.setVisibility(View.VISIBLE);
        									lvSupplies.setAdapter(adapter);
        								}
        								break;
        		case EDIT_FORM:         loadSupplyFormEdit(supply);
        								break;
        		case DELETE_SUCCESS:    util.showToast(getResources().getString(R.string.delete_success), 
									    		   	   SupplyActivity.this);
						        		loadSupplyListPage();
						        		enableDrawer();
        								break;
        		case SPINNER_LIST:		prepareSpinnerVehicles();
        								setOnItemSelectedSpinnerList();
										break;
        		case SPINNER_FORM: 		prepareSpinnerVehicles();
        								prepareSpinnerFuels();
        								setOnItemSelectedSpinnerForm();
        								break;
        	}
        }
    };
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar = getSupportActionBar();
		actionBar.setTitle(getDrawerListViewItems()[StaticValues.ACTION_SUPPLY]);
		loadSupplyListPage();
		leftMenu();
	}
	
	public void loadSupplyListPage(){
		upateMenu(PAGE_SUPPLY_LIST);
		clearSupplyId();
		setContentView(R.layout.activity_supply_list);
		
    	startProgress(SupplyActivity.this);
        new Thread() {
            @Override
            public void run() {
                super.run();
				 CarService carService = new CarService(SupplyActivity.this);
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
	
	public void prepareListView(JSONObject messages) throws JSONException{
		 JSONArray items = messages.getJSONArray("supplies");
		 list = new ArrayList<HashMap<String, Object>>();
		 for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            String id = item.getString("id");
            String date = item.getString("date");
            String fuel = item.getString("fuel");
            String liters = item.getString("liters");
            String average = item.getString("average");
            String odometer = item.getString("odometer");
            String is_full = item.getString("is_full");
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("id", id);
            map.put("date", date);
            map.put("fuel", fuel);
            map.put("liters", liters + " L");
            average = !average.equals("") ? average + " Km/L" : "";
            map.put("average", average);
            map.put("odometer", odometer + " Km");
            if (util.convertStringToBoolean(is_full))
            	map.put("is_full", R.drawable.ic_action_accept);
            else
            	map.put("is_full", R.drawable.ic_action_cancel_dark);
            list.add(map);
	     }
		 String[] from = new String[] { "date", "odometer", "fuel", "liters", "average", "is_full"};
		 int[] to = new int[] { R.id.tvDate, R.id.tvOdometer, R.id.tvFuel, R.id.tvLiters, R.id.tvAverage, R.id.tvIsFull};
		 adapter = new SimpleAdapter(SupplyActivity.this, list, R.layout.item_list_supply, from, to);
		 lvSupplies = (ListView) findViewById(R.id.listViewSupplies);
	}
	
	public void onClickItem(){
		lvSupplies.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    		final int id_supply = util.convertStringToInt(list.get(position).get("id").toString());
		    		supply_id = id_supply;
		        	startProgress(SupplyActivity.this);
		            new Thread() {
		                @Override
		                public void run() {
		                    super.run();
		    				 SupplyService supplyService = new SupplyService(SupplyActivity.this);
		    				 try {
	    			    		 supply = supplyService.get(id_supply);
	    			    		 handler.sendEmptyMessage(EDIT_FORM);
		    				 }catch (InvalidCredentialsException e) {
		    	        			logout();
		            		 }catch (Exception e){
		    					 errorMessage = e.getMessage();
		    					 handler.sendEmptyMessage(MSG_ERROR);
		    				 }
		                }
		            }.start();
			    }
		});
	}
	
	public void onLongClickItem(){
		lvSupplies.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, final View view, int position, long id) {
            	final int id_supply = util.convertStringToInt(list.get(position).get("id").toString());
				util.confirm(SupplyActivity.this, 
						 getResources().getString(R.string.confirmation),
						 getResources().getString(R.string.delete_confirmation), 
						 getResources().getString(R.string.yes), 
						 getResources().getString(R.string.no),
						 new Runnable() {
							public void run() {
					        	startProgress(SupplyActivity.this);
					            new Thread() {
					                @Override
					                public void run() {
					                    super.run();
					    				 SupplyService supplyService = new SupplyService(SupplyActivity.this);
					    				 try {
					    					 supplyService.delete(id_supply);
				    			    		 handler.sendEmptyMessage(DELETE_SUCCESS);
					    				 }catch (InvalidCredentialsException e) {
					    	        			logout();
					            		 }catch (Exception e){
					    					 errorMessage = e.getMessage();
					    					 handler.sendEmptyMessage(MSG_ERROR);
					    				 }
					                }
					            }.start();
							}
						}, null);
    
                return true;
            }
        });
	}
	
	public void loadSupplyFormNew(){
		try {
			upateMenu(PAGE_SUPPLY_NEW);
			setContentView(R.layout.activity_supply_form);
			prepareDatePicker();
			disableDrawer();
			clearFuelId();
			startProgress(SupplyActivity.this);
            new Thread() {
                @Override
                public void run() {
                    super.run();
    				 CarService carService = new CarService(SupplyActivity.this);
    				 try {
						vehicles_fuels = carService.getVehiclesAndFuel();
			    		handler.sendEmptyMessage(SPINNER_FORM);
    				 }catch (InvalidCredentialsException e) {
    	        			logout();
            		 }catch (Exception e){
    					 errorMessage = e.getMessage();
    					 handler.sendEmptyMessage(MSG_ERROR);
    				 }
                }
            }.start();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void loadSupplyFormEdit(JSONObject supply){
		
		loadSupplyFormNew();
		upateMenu(PAGE_CONTACT_EDIT);
        etOdometer = (EditText) findViewById(R.id.etOdometer);
        etLiters = (EditText) findViewById(R.id.etLiters);
        chIsFull = (CheckBox) findViewById(R.id.chIsFull);
        etPrice = (EditText) findViewById(R.id.etPrice);
        etStation = (EditText) findViewById(R.id.etStation);
        etObs = (EditText) findViewById(R.id.etObs);
        
		// Atualiza data para a data do registro a ser editado
//		String dataColumn = cursor.getString(cursor.getColumnIndex("strftime('%d/%m/%Y',date)"));
//		atualizaValoresData(util.convertStringParaInt(dataColumn.split("/")[0]), util.convertStringParaInt(dataColumn.split("/")[1]) - 1, util.convertStringParaInt(dataColumn.split("/")[2]));
//		Calendar c = Calendar.getInstance();
//		c.set(dpAno, dpMes, dpDia);

		try {
			etOdometer.setText(supply.getString("odometer"));
			etLiters.setText(supply.getString("liters"));
			chIsFull.setChecked(util.convertStringToBoolean(supply.getString("is_full")));
			etPrice.setText(supply.getString("fuel_price"));
			etStation.setText(supply.getString("station"));
			etObs.setText(supply.getString("obs"));

			String[] date = supply.getString("date").split("-");
			updateDateValues(util.convertStringToInt(date[2]), (util.convertStringToInt(date[1]) - 1), util.convertStringToInt(date[0]));
			updateTvDate();

			idVehicle = supply.getString("vehicle");
			idFuel = supply.getString("fuel");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveSupply(){
    	startProgress(SupplyActivity.this);
        new Thread() {
            @Override
            public void run() {
                super.run();
                 etOdometer = (EditText) findViewById(R.id.etOdometer);
                 etLiters = (EditText) findViewById(R.id.etLiters);
                 chIsFull = (CheckBox) findViewById(R.id.chIsFull);
                 etPrice = (EditText) findViewById(R.id.etPrice);
                 etStation = (EditText) findViewById(R.id.etStation);
                 etObs = (EditText) findViewById(R.id.etObs);
				 String odometer = etOdometer.getText().toString();
				 String liters = etLiters.getText().toString();
				 String isFull = chIsFull.isChecked() ? "True" : "False";
				 String price = etPrice.getText().toString();
				 String station = etStation.getText().toString();
				 String obs = etObs.getText().toString();
				 SupplyService supplyService = new SupplyService(SupplyActivity.this);
				 try {
					 if (isUpdate()){
						 supplyService.update(odometer, liters, date.toString(), isFull, idVehicle, idFuel, station, price, obs, supply_id);
						 handler.sendEmptyMessage(UPDATE_SUCCESS);
					 }
					 else{
						 supplyService.save(odometer, liters, date.toString(), isFull, idVehicle, idFuel, station, price, obs);
						 handler.sendEmptyMessage(CREATE_SUCCESS);
						 
					 }
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
	
	private void prepareSpinnerFuels(){
		JSONArray items;
		List<String> names = new ArrayList<String>();
		List<String> ids = new ArrayList<String>();
		try {
			spFuels= (Spinner) findViewById(R.id.spFuel);
			items = vehicles_fuels.getJSONArray("fuels");
			for (int i=0; i<items.length(); i++){
				names.add(items.getJSONObject(i).getString("name"));
				ids.add(items.getJSONObject(i).getString("id"));
			}
			ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, names);
			ArrayAdapter<String> spinnerArrayAdapter = arrayAdapter;
			spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spFuels.setAdapter(spinnerArrayAdapter);
			if (util.convertStringToInt(idFuel) != -1){
				spFuels.setSelection(ids.indexOf(idFuel));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void setOnItemSelectedSpinnerForm(){
		spVehicles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 
			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
				try {
					idVehicle = vehicles_fuels.getJSONArray("vehicles").getJSONObject(position).getString("id");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
 
			}
		});
	
		spFuels.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			 
			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
				try {
					idFuel = vehicles_fuels.getJSONArray("fuels").getJSONObject(position).getString("id");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
 
			}
		});
	}
	
	private void setOnItemSelectedSpinnerList(){
		spVehicles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 
			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
				try {
					idVehicle = vehicles_fuels.getJSONArray("vehicles").getJSONObject(position).getString("id");
					tvNoSupplies = (TextView) findViewById(R.id.tvNoSupplies);
					tvNoSupplies.setVisibility(View.GONE);
					progressbar = (LinearLayout) findViewById(R.id.progressbar);
					progressbar.setVisibility(View.VISIBLE);
			        new Thread() {
			            @Override
			            public void run() {
			                super.run();
							 SupplyService supplyService = new SupplyService(SupplyActivity.this);
							 try {
								 JSONObject supplies = supplyService.getByVehicle(idVehicle);
								 prepareListView(supplies);
								 onClickItem();
								 onLongClickItem();
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
	
	/**
	 * Date Picker Functions
	 */
	
	private void prepareDatePicker() {
		Calendar c = Calendar.getInstance();
		updateDateValues(c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH), c.get(Calendar.YEAR));
		btnDatePicker = (ImageButton) findViewById(R.id.btnDatePicker);
		btnDatePicker.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		        showDialog(DATE_DIALOG_ID);
		    }
		});
		updateTvDate();
	}
	
    @Override
    protected Dialog onCreateDialog(int id) {
	    switch (id) {
		    case DATE_DIALOG_ID:
		    	return new DatePickerDialog(this,
						                     mDateSetListener,
						                     dpYear, dpMonth, dpDay);
	    }
	    return null;
	}
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
    	switch (id) {
	    	case DATE_DIALOG_ID:
	    		((DatePickerDialog) dialog).updateDate(dpYear,
									    	           dpMonth,
									    	           dpDay);
    	} 	
    }
	
    private void updateTvDate() {
    	tvDate = (TextView) findViewById(R.id.tvDate);
    	date =  new StringBuilder().append(dpDay).append("/")
							       .append(dpMonth + 1).append("/") // Month is 0 based so add 1
							       .append(dpYear);
    	
        tvDate.setText(date);
    }
    
	private void updateDateValues(int dia, int mes, int ano){
	    dpDay = dia;
	    dpMonth = mes;
	    dpYear = ano;
	}
	
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int month, int day) {
                	updateDateValues(day, month, year);
                	updateTvDate();
                }
            };
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.default_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
		menuNew = menu.findItem(R.id.menu_new); 
		menuSave = menu.findItem(R.id.menu_save); 
		
		switch (currentPage) {
			case PAGE_SUPPLY_LIST:
				menuNew.setVisible(true);
				menuSave.setVisible(false);
				break;
			case PAGE_SUPPLY_NEW:
				menuNew.setVisible(false);
				menuSave.setVisible(true);
				break;
			case PAGE_CONTACT_EDIT:
				menuNew.setVisible(false);
				menuSave.setVisible(true);
				break;			
			default:
				break;
		}
		return true;
    }
    
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	        case android.R.id.home: if (currentPage == PAGE_SUPPLY_LIST){
						    			return(super.onOptionsItemSelected(item));
						    		}
	        						redirectBack();
	        						return true;

	        case R.id.menu_new: loadSupplyFormNew();
	        					return true;

	        case R.id.menu_save: saveSupply();
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
			case PAGE_SUPPLY_LIST:
				returnToIndex();
				break;
			case PAGE_SUPPLY_NEW :
			case PAGE_CONTACT_EDIT :
				loadSupplyListPage();
				enableDrawer();
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
	
	
	private void clearSupplyId(){
		supply_id = 0;
	}
	
	
	private void clearVehicleId(){
		idVehicle = "-1";
	}
	
	
	private void clearFuelId(){
		idFuel = "-1";
	}
	
	private boolean isUpdate(){
		return supply_id != 0;
	}

}
