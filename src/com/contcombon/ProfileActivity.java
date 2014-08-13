/**
 * @author Bruno Zeraik 
 */

package com.contcombon;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.client.UserService;
import com.client.exceptions.InvalidCredentialsException;
import com.uteis.StaticValues;
import com.uteis.Util;

public class ProfileActivity extends BaseActivity {

  Util util = new Util();
  EditText etLogin, etName, etEmail;
  int currentPage = 0;
  MenuItem menuEdit, menuSave;
  String errorMessage = "";

  JSONObject profile;

  public static final int PAGE_PROFILE_LOCKED = 0;
  public static final int PAGE_PROFILE_UNLOCKED = 1;

  public static final int MSG_SUCCESS_UPDATE = 0;
  public static final int MSG_ERROR = 2;
  public static final int PROFILE = 3;

  private Handler handler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      finishProgress(ProfileActivity.this);
      switch (msg.what) {
        case MSG_SUCCESS_UPDATE:
          util.showToast(getResources().getString(R.string.update_success), ProfileActivity.this);
          loadProfilePage();
          currentPage = PAGE_PROFILE_LOCKED;
          supportInvalidateOptionsMenu();
          break;
        case MSG_ERROR:
          util.showMessage(getResources().getString(R.string.error), errorMessage, ProfileActivity.this);
          break;
        case PROFILE:
          loadProfileForm();
        default:
          break;
      }
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setTitle(getDrawerListViewItems()[StaticValues.ACTION_PROFILE]);
    loadProfilePage();
  }

  public void loadProfilePage() {
    setContentView(R.layout.activity_profile);
    leftMenu();
    startProgress(ProfileActivity.this);
    new Thread() {

      @Override
      public void run() {
        super.run();
        UserService userService = new UserService(ProfileActivity.this);
        try {
          etLogin = (EditText) findViewById(R.id.etLogin);
          etName = (EditText) findViewById(R.id.etName);
          etEmail = (EditText) findViewById(R.id.etEmail);

          profile = userService.getUser(sharedpreferences.getString("login", null));

          handler.sendEmptyMessage(PROFILE);

        }
        catch (InvalidCredentialsException e) {
          logout();
        }
        catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }.start();

  }

  public void saveUser() {
    startProgress(ProfileActivity.this);
    new Thread() {

      @Override
      public void run() {
        super.run();
        String login = etLogin.getText().toString();
        String name = etName.getText().toString();
        UserService userService = new UserService(ProfileActivity.this);
        try {
          userService.updateProfile(name, login);
          updateSharedPreferences(login);
          handler.sendEmptyMessage(MSG_SUCCESS_UPDATE);
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

  public void loadProfileForm() {
    try {
      etLogin.setText(profile.getString("username"));
      etLogin.setEnabled(false);
      etName.setText(profile.getString("name"));
      etName.setEnabled(false);
      etEmail.setText(profile.getString("email"));
      etEmail.setEnabled(false);
    }
    catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.profile_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menuEdit = menu.findItem(R.id.menu_edit);
    menuSave = menu.findItem(R.id.menu_save);

    switch (currentPage) {
      case PAGE_PROFILE_LOCKED:
        menuEdit.setVisible(true);
        menuSave.setVisible(false);
        break;
      case PAGE_PROFILE_UNLOCKED:
        menuEdit.setVisible(false);
        menuSave.setVisible(true);
        break;
      default:
        menuEdit.setVisible(true);
        menuSave.setVisible(false);
        break;
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_edit:
        etLogin.setEnabled(true);
        etName.setEnabled(true);
        currentPage = PAGE_PROFILE_UNLOCKED;
        supportInvalidateOptionsMenu();
        return true;
      case R.id.menu_save:
        saveUser();
        return true;

    }
    return (super.onOptionsItemSelected(item));
  }

  @Override
  public void onBackPressed() {
    Intent iMain = new Intent(getBaseContext(), MainActivity.class);
    startActivity(iMain);
    finish();
  }
}
