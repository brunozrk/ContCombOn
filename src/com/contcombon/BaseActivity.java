/**
 * @author Bruno Zeraik 
 */

package com.contcombon;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.uteis.StaticValues;
import com.uteis.Util;

public abstract class BaseActivity extends ActionBarActivity {

  private ProgressDialog loadingdialog;
  private String[] drawerListViewItems;
  private DrawerLayout drawerLayout;
  private ListView drawerListView;
  private ActionBarDrawerToggle actionBarDrawerToggle;
  ActionBar actionBar;
  Util util = new Util();

  SharedPreferences sharedpreferences;
  Editor editor;

  public String[] getDrawerListViewItems() {
    return drawerListViewItems;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setSharedPreferences();
    // get list items from strings.xml
    drawerListViewItems = getResources().getStringArray(R.array.items);
  }

  private class MyArrayAdapter extends ArrayAdapter<String> {

    public MyArrayAdapter(Context context, int textViewResourceId, String[] objects) {
      super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      LayoutInflater inflater = getLayoutInflater();
      View row = inflater.inflate(R.layout.drawer_listview_item, parent, false);
      TextView text = (TextView) row.findViewById(R.id.text1);

      switch (position) {
        case 0:
          mountMenuItem(text, drawerListViewItems[StaticValues.ACTION_HOME], R.drawable.ic_action_home);
          break;
        case 1:
          mountMenuItem(text, drawerListViewItems[StaticValues.ACTION_CAR], R.drawable.ic_action_car);
          break;
        case 2:
          mountMenuItem(text, drawerListViewItems[StaticValues.ACTION_SUPPLY], R.drawable.ic_action_refuelling);
          break;
        case 3:
          mountMenuItem(text, drawerListViewItems[StaticValues.ACTION_SUMMARY], R.drawable.ic_action_view_as_list);
          break;
        case 4:
          mountMenuItem(text, drawerListViewItems[StaticValues.ACTION_CONTACT], R.drawable.ic_action_email);
          break;
        case 5:
          mountMenuItem(text, drawerListViewItems[StaticValues.ACTION_RANKING], R.drawable.ic_action_important);
          break;
        case 6:
          mountMenuItem(text, drawerListViewItems[StaticValues.ACTION_PROFILE], R.drawable.ic_action_person);
          break;
        case 7:
          mountMenuItem(text, drawerListViewItems[StaticValues.ACTION_CLOSE], R.drawable.ic_action_cancel);
          break;
        case 8:
          mountMenuItem(text, drawerListViewItems[StaticValues.ACTION_LOGOUT], R.drawable.ic_action_reply);
          break;
        default:
          break;
      }
      return row;
    }
  }

  public void mountMenuItem(TextView text, String title, int drawable) {
    text.setText(title);
    text.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);
  }

  public void leftMenu() {
    // get ListView defined in activity_main.xml
    drawerListView = (ListView) findViewById(R.id.left_drawer);

    // Set the adapter for the list view
    drawerListView.setAdapter(new MyArrayAdapter(this, R.layout.drawer_listview_item, drawerListViewItems));

    // 2. App Icon
    drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

    // 2.1 create ActionBarDrawerToggle
    actionBarDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
    drawerLayout, /* DrawerLayout object */
    R.drawable.ic_drawer, /* nav drawer icon to replace 'Up' caret */
    R.string.drawer_open, /* "open drawer" description */
    R.string.drawer_close /* "close drawer" description */
    );
    // 2.2 Set actionBarDrawerToggle as the DrawerListener
    drawerLayout.setDrawerListener(actionBarDrawerToggle);

    // 2.3 enable and show "up" arrow
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    // just styling option
    drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

    drawerListView.setOnItemClickListener(new DrawerItemClickListener());
  }

  public void disableDrawer() {
    actionBarDrawerToggle.setDrawerIndicatorEnabled(false);
    actionBar.setDisplayHomeAsUpEnabled(true);
  }

  public void enableDrawer() {
    actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
    leftMenu();
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    actionBarDrawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    actionBarDrawerToggle.onConfigurationChanged(newConfig);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    // call ActionBarDrawerToggle.onOptionsItemSelected(), if it returns true
    // then it has handled the app icon touch event

    if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private class DrawerItemClickListener implements ListView.OnItemClickListener {

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {

      switch (position) {
        case StaticValues.ACTION_HOME:
          Intent iIndex = new Intent(getBaseContext(), IndexActivity.class);
          startActivity(iIndex);
          finish();
          break;
        case StaticValues.ACTION_CAR:
          Intent iCar = new Intent(getBaseContext(), CarActivity.class);
          startActivity(iCar);
          finish();
          break;
        case StaticValues.ACTION_SUPPLY:
          Intent iSupply = new Intent(getBaseContext(), SupplyActivity.class);
          startActivity(iSupply);
          finish();
          break;
        case StaticValues.ACTION_SUMMARY:
          Intent iSummary = new Intent(getBaseContext(), SummaryActivity.class);
          startActivity(iSummary);
          finish();
          break;
        case StaticValues.ACTION_CONTACT:
          Intent iContact = new Intent(getBaseContext(), ContactActivity.class);
          startActivity(iContact);
          finish();
          break;
        case StaticValues.ACTION_RANKING:
          Intent iRanking = new Intent(getBaseContext(), RankingActivity.class);
          startActivity(iRanking);
          finish();
          break;
        case StaticValues.ACTION_PROFILE:
          Intent iProfile = new Intent(getBaseContext(), ProfileActivity.class);
          startActivity(iProfile);
          finish();
          break;
        case StaticValues.ACTION_CLOSE:
          finish();
          break;
        case StaticValues.ACTION_LOGOUT:
          logout();
          break;
        default:
          finish();
          break;
      }
      drawerLayout.closeDrawer(drawerListView);
    }
  }

  public void logout() {
    editor.clear();
    editor.commit();
    Intent iMain = new Intent(getBaseContext(), MainActivity.class);
    startActivity(iMain);
    finish();
  }

  public void startProgress(Context context) {
    loadingdialog = ProgressDialog.show(context, "", getResources().getString(R.string.loading), true);
    loadingdialog.setCanceledOnTouchOutside(true);
  }

  public void finishProgress(Context context) {
    loadingdialog.dismiss();
  }

  public void updateSharedPreferences(String login) {
    editor = sharedpreferences.edit();
    editor.putString("login", login);
    editor.commit();
  }

  public void returnToIndex() {
    Intent i = new Intent(getBaseContext(), MainActivity.class);
    startActivity(i);
    this.finish();
  }

  private void setSharedPreferences() {
    sharedpreferences = getSharedPreferences("userdetails", Context.MODE_PRIVATE);
    editor = sharedpreferences.edit();
  }
}
