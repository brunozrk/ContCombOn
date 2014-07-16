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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.client.CarService;
import com.client.exceptions.InvalidCredentialsException;
import com.uteis.StaticValues;
import com.uteis.Util;

public class CarActivity extends BaseActivity {

	Util util = new Util();
	EditText etModel, etMotor, etManufactured;
	TextView tvNoCars;
	LinearLayout header;
	int currentPage = 0;
	MenuItem menuNew, menuSave;
	
	// AUTO COMPLETE
	AutoCompleteTextView autoComplete;
	ArrayAdapter<String> adapterComplete;
	JSONObject models;

	LinearLayout progressbar;
	String errorMessage = "";
	ListView lvCars;
	ListAdapter adapter;
	ArrayList<HashMap<String, String>> list;
	JSONObject car;
	int car_id = 0;
	
	public static final int PAGE_CAR_LIST = 0;
	public static final int PAGE_CAR_NEW = 1;
	public static final int PAGE_CONTACT_EDIT = 2;
	
	public static final int CREATE_SUCCESS = 0;
	public static final int MSG_ERROR = 1;
	public static final int LIST = 2;
	public static final int EDIT_FORM = 3;
	public static final int DELETE_SUCCESS = 4;
	public static final int UPDATE_SUCCESS = 5;
	public static final int AUTOCOMPLETE= 6;
	
	
	/**
	 * CALLBACKS
	 */
    private Handler handler = new Handler() {
        @Override
         public void handleMessage(Message msg) {
        	try {
        		finishProgress(CarActivity.this);
			} catch (Exception e) {
				// TODO: handle exception
			}
        	switch (msg.what) {
        		case CREATE_SUCCESS:    util.showToast(getResources().getString(R.string.create_success), 
    									    		   CarActivity.this);
						        	    loadCarListPage();
						        		enableDrawer();
 						 				break;
        		case UPDATE_SUCCESS:    util.showToast(getResources().getString(R.string.update_success), 
											    		   CarActivity.this);
								 	    loadCarListPage();
								 		enableDrawer();
										break;
        		case MSG_ERROR: 		util.showMessage(getResources().getString(R.string.error), 
						 				  			     errorMessage, 
						 				 				 CarActivity.this);
        								break;
        		case LIST:				progressbar.setVisibility(View.GONE);
        								if (adapter.isEmpty()) {
        									tvNoCars.setText(getResources().getString(R.string.no_cars));
        									tvNoCars.setVisibility(View.VISIBLE);
        									header.setVisibility(View.GONE);
        								}else{
        									tvNoCars.setVisibility(View.GONE);
        									header.setVisibility(View.VISIBLE);
        									lvCars.setVisibility(View.VISIBLE);
        									lvCars.setAdapter(adapter);
        								}
        								break;
        		case EDIT_FORM:         loadCarFormEdit(car);
        								break;
        		case DELETE_SUCCESS:    util.showToast(getResources().getString(R.string.delete_success), 
									    		   	   CarActivity.this);
						        		loadCarListPage();
						        		enableDrawer();
        								break;
        		case AUTOCOMPLETE:     	prepareAutoComplete(models);
        								break;
        	}
        }
    };
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar = getSupportActionBar();
		actionBar.setTitle(getDrawerListViewItems()[StaticValues.ACTION_CAR]);
		loadCarListPage();
		leftMenu();
	}
	
	public void loadCarListPage(){
		upateMenu(PAGE_CAR_LIST);
		clearCarId();
		setContentView(R.layout.activity_car_list);
		tvNoCars = (TextView) findViewById(R.id.tvNoCars);
		tvNoCars.setVisibility(View.GONE);
		header = (LinearLayout) findViewById(R.id.header);
		header.setVisibility(View.GONE);
		progressbar = (LinearLayout) findViewById(R.id.progressbar);
		progressbar.setVisibility(View.VISIBLE);
        new Thread() {
            @Override
            public void run() {
                super.run();
				 CarService carService = new CarService(CarActivity.this);
				 try {
					 JSONObject cars = carService.getCars();
					 prepareListView(cars);
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
	}
	
	public void prepareListView(JSONObject messages) throws JSONException{
		 JSONArray items = messages.getJSONArray("cars");
		 list = new ArrayList<HashMap<String, String>>();
		 for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            String id = item.getString("id");
            String model = item.getString("model__name");
            String motor = item.getString("motor");
            String manufactured = item.getString("manufactured");
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("id", id);
            map.put("model", util.ellipsize(model, 15));
            map.put("motor", motor);
            map.put("manufactured", manufactured);
            list.add(map);
	     }
		 String[] from = new String[] { "model", "motor", "manufactured"};
		 int[] to = new int[] { R.id.tvModel, R.id.tvMotor, R.id.tvManufactured};
		 adapter = new SimpleAdapter(CarActivity.this, list, R.layout.item_list_car, from, to);
		 lvCars = (ListView) findViewById(R.id.listViewCars);
	}
	
	public void onClickItem(){
		lvCars.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    		final int id_car = util.convertStringToInt(list.get(position).get("id"));
		    		car_id = id_car;
		        	startProgress(CarActivity.this);
		            new Thread() {
		                @Override
		                public void run() {
		                    super.run();
		    				 CarService carService = new CarService(CarActivity.this);
		    				 try {
	    			    		 car = carService.get(id_car);
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
		lvCars.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, final View view, int position, long id) {
            	final int id_car = util.convertStringToInt(list.get(position).get("id"));
				util.confirm(CarActivity.this, 
						 getResources().getString(R.string.confirmation),
						 getResources().getString(R.string.delete_confirmation), 
						 getResources().getString(R.string.yes), 
						 getResources().getString(R.string.no),
						 new Runnable() {
							public void run() {
					        	startProgress(CarActivity.this);
					            new Thread() {
					                @Override
					                public void run() {
					                    super.run();
					    				 CarService carService = new CarService(CarActivity.this);
					    				 try {
					    					 carService.delete(id_car);
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
	
	public void loadCarFormNew(){
		try {
			upateMenu(PAGE_CAR_NEW);
			setContentView(R.layout.activity_car_form);
			disableDrawer();
	
			startProgress(CarActivity.this);
            new Thread() {
                @Override
                public void run() {
                    super.run();
    				 CarService carService = new CarService(CarActivity.this);
    				 try {
						models = carService.getModels();
			    		handler.sendEmptyMessage(AUTOCOMPLETE);
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
	
	public void prepareAutoComplete(JSONObject models){
		String[] items;
		try {
			items = util.convertJSONArrayToStringArray(models.getJSONArray("models"));
			String[] models_list = items;
			adapterComplete = new ArrayAdapter<String>(CarActivity.this,android.R.layout.simple_list_item_1,models_list);
			autoComplete = (AutoCompleteTextView) findViewById(R.id.etModel);
			autoComplete.setAdapter(adapterComplete);
			autoComplete.setThreshold(1);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadCarFormEdit(JSONObject car){
		
		loadCarFormNew();
		upateMenu(PAGE_CONTACT_EDIT);
		etModel= (EditText) findViewById(R.id.etModel);
		etMotor= (EditText) findViewById(R.id.etMotor);
		etManufactured = (EditText) findViewById(R.id.etManufactured);
		
		try {
			etModel.setText(car.getString("model"));
			etMotor.setText(car.getString("motor"));
			etManufactured.setText(car.getString("manufactured"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveCar(){
    	startProgress(CarActivity.this);
        new Thread() {
            @Override
            public void run() {
                super.run();
                 etModel= (EditText) findViewById(R.id.etModel);
                 etMotor= (EditText) findViewById(R.id.etMotor);
                 etManufactured= (EditText) findViewById(R.id.etManufactured);
				 String model = etModel.getText().toString();
				 String motor = etMotor.getText().toString();
				 String manufactured = etManufactured.getText().toString();
				 CarService carService = new CarService(CarActivity.this);
				 try {
					 if (isUpdate()){
						 carService.update(model, motor, manufactured, car_id);
						 handler.sendEmptyMessage(UPDATE_SUCCESS);
					 }
					 else{
						 carService.save(model, motor, manufactured);
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
			case PAGE_CAR_LIST:
				menuNew.setVisible(true);
				menuSave.setVisible(false);
				break;
			case PAGE_CAR_NEW:
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
	        case android.R.id.home: if (currentPage == PAGE_CAR_LIST){
						    			return(super.onOptionsItemSelected(item));
						    		}
	        						redirectBack();
	        						return true;

	        case R.id.menu_new: loadCarFormNew();
	        					return true;

	        case R.id.menu_save: saveCar();
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
			case PAGE_CAR_LIST:
				returnToIndex();
				break;
			case PAGE_CAR_NEW :
			case PAGE_CONTACT_EDIT :
				loadCarListPage();
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
	
	
	private void clearCarId(){
		car_id = 0;
	}
	
	private boolean isUpdate(){
		return car_id != 0;
	}

}
