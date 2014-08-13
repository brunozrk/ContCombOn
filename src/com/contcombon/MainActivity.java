/**
 * @author Bruno Zeraik 
 */

package com.contcombon;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.client.UserService;
import com.client.exceptions.ValidationException;
import com.uteis.StaticValues;
import com.uteis.Util;

public class MainActivity extends ActionBarActivity {

  ProgressDialog loadingdialog;
  Button btnLogin, btnCreate;
  EditText etLogin, etPassword, etName, etEmail, etConfirmPassword;
  TextView tvCreateAccount;
  Util util = new Util();
  SharedPreferences sharedpreferences;
  int currentPage = 0;
  String errorMessage = "";
  ActionBar actionBar;

  public static final int PAGE_LOGIN = 0;
  public static final int PAGE_CREATE_ACCOUNT = 1;

  public static final int MSG_SUCCESS_AUTHENTICATE = 1;
  public static final int MSG_FIELD_REQUIRED = 2;
  public static final int MSG_ERROR_AUTHENTICATE = 3;
  public static final int MSG_WRONG_COFIRM_PASSWORD = 4;
  public static final int MSG_ERROR = 5;
  public static final int MSG_CREATE_SUCCESS = 6;
  public static final int MSG_INVALID_EMAIL = 7;

  private Handler handler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      loadingdialog.dismiss();
      switch (msg.what) {
        case MSG_SUCCESS_AUTHENTICATE:
          startIndexActivity();
          break;
        case MSG_FIELD_REQUIRED:
          util.showMessage(getResources().getString(R.string.error), getResources().getString(R.string.field_required), MainActivity.this);
          break;
        case MSG_ERROR_AUTHENTICATE:
          util.showMessage(getResources().getString(R.string.invalid_values), getResources().getString(R.string.wrong_credentials), MainActivity.this);
          break;
        case MSG_WRONG_COFIRM_PASSWORD:
          util.showMessage(getResources().getString(R.string.invalid_values), getResources().getString(R.string.wrong_confirm_password), MainActivity.this);
          break;
        case MSG_ERROR:
          util.showMessage(getResources().getString(R.string.error), errorMessage, MainActivity.this);
          break;
        case MSG_CREATE_SUCCESS:
          util.showMessage(getResources().getString(R.string.congrats), getResources().getString(R.string.create_success), MainActivity.this);
          loadMainPage();
          break;
        case MSG_INVALID_EMAIL:
          util.showMessage(getResources().getString(R.string.invalid_values), getResources().getString(R.string.invalid_email), MainActivity.this);
          break;
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (hasSession()) {
      startIndexActivity();
    }
    actionBar = getSupportActionBar();
    loadMainPage();

  }

  /*****************/
  /** LOGIN CLICK **/
  /*****************/
  public void onClickLogin() {

    btnLogin = (Button) findViewById(R.id.btnLogin);
    btnLogin.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        loadingdialog = ProgressDialog.show(MainActivity.this, "", getResources().getString(R.string.loading), true);
        new Thread() {

          @Override
          public void run() {
            super.run();
            etLogin = (EditText) findViewById(R.id.etLogin);
            etPassword = (EditText) findViewById(R.id.etPassword);
            String login = etLogin.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (login.equals("") || password.equals("")) {
              handler.sendEmptyMessage(MSG_FIELD_REQUIRED);
            }
            else {
              UserService userService = new UserService(MainActivity.this);
              try {
                if (userService.authenticate(login, password)) {
                  setSession(login, password);
                  handler.sendEmptyMessage(MSG_SUCCESS_AUTHENTICATE);
                }
                else {
                  handler.sendEmptyMessage(MSG_ERROR_AUTHENTICATE);
                }
              }
              catch (Exception e) {
                errorMessage = e.getMessage();
                handler.sendEmptyMessage(MSG_ERROR);
              }
            }

          }
        }.start();
      }
    });
  }

  /**************************/
  /** CREATE ACCOUNT CLICK **/
  /**************************/
  public void onClickCreateAccount() {

    tvCreateAccount = (TextView) findViewById(R.id.tvCreateAccount);
    tvCreateAccount.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        currentPage = PAGE_CREATE_ACCOUNT;
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.create_account);
        onClickCreate();
      }
    });
  }

  /******************/
  /** CREATE CLICK **/
  /******************/
  public void onClickCreate() {

    btnCreate = (Button) findViewById(R.id.btnCreate);
    btnCreate.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        loadingdialog = ProgressDialog.show(MainActivity.this, "", getResources().getString(R.string.loading), true);
        new Thread() {

          @Override
          public void run() {
            super.run();

            etLogin = (EditText) findViewById(R.id.etLogin);
            etPassword = (EditText) findViewById(R.id.etPassword);
            etConfirmPassword = (EditText) findViewById(R.id.etConfirmPassword);
            etName = (EditText) findViewById(R.id.etName);
            etEmail = (EditText) findViewById(R.id.etEmail);

            String login = etLogin.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (login.equals("") || password.equals("") || confirmPassword.equals("") || name.equals("") || email.equals("")) {
              handler.sendEmptyMessage(MSG_FIELD_REQUIRED);
            }
            else if (!password.equals(confirmPassword)) {
              handler.sendEmptyMessage(MSG_WRONG_COFIRM_PASSWORD);
            }
            else if (!util.isValidEmail(email)) {
              handler.sendEmptyMessage(MSG_INVALID_EMAIL);
            }
            else {
              UserService userService = new UserService(MainActivity.this);
              try {
                userService.createAccount(login, password, confirmPassword, name, email);
                handler.sendEmptyMessage(MSG_CREATE_SUCCESS);
              }
              catch (ValidationException e) {
                errorMessage = e.getMessage();
                handler.sendEmptyMessage(MSG_ERROR);
              }
              catch (Exception e) {
                errorMessage = e.getMessage();
                handler.sendEmptyMessage(MSG_ERROR);
              }

            }

          }
        }.start();
      }
    });
  }

  public void loadMainPage() {
    currentPage = PAGE_LOGIN;
    actionBar.setDisplayHomeAsUpEnabled(false);
    setContentView(R.layout.login);
    onClickLogin();
    onClickCreateAccount();

  }

  public boolean hasSession() {
    sharedpreferences = getSharedPreferences(StaticValues.PREFERENCES, Context.MODE_PRIVATE);
    if (sharedpreferences.contains("login") && sharedpreferences.contains("password")) {
      return true;
    }
    else {
      return false;
    }
  }

  public void setSession(String login, String password) {
    Editor editor = sharedpreferences.edit();
    editor.putString("login", login);
    editor.putString("password", password);
    editor.commit();
  }

  public void startIndexActivity() {
    Intent iIndex = new Intent(getBaseContext(), IndexActivity.class);
    startActivity(iIndex);
    finish();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        loadMainPage();
        return (true);
    }
    return (super.onOptionsItemSelected(item));
  }

  @Override
  public void onBackPressed() {
    switch (currentPage) {
      case PAGE_LOGIN:
        finish();
        break;
      case PAGE_CREATE_ACCOUNT:
        loadMainPage();
        break;
      default:
        finish();
        break;
    }
  }
}
