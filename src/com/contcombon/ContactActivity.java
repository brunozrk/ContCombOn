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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.client.ContactService;
import com.client.exceptions.InvalidCredentialsException;
import com.uteis.StaticValues;
import com.uteis.Util;

public class ContactActivity extends BaseActivity {

  Util util = new Util();
  EditText etMessage, etReply;
  TextView tvNoMessages;
  int currentPage = 0;
  MenuItem menuNew, menuSave;

  LinearLayout progressbar;
  String errorMessage = "";
  ListView lvMessages;
  ListAdapter adapter;
  ArrayList<HashMap<String, String>> list;
  JSONObject message;

  public static final int PAGE_CONTACT_LIST = 0;
  public static final int PAGE_CONTACT_NEW = 1;
  public static final int PAGE_CONTACT_EDIT = 2;

  public static final int CREATE_SUCCESS = 0;
  public static final int MSG_ERROR = 1;
  public static final int LIST = 2;
  public static final int EDIT_FORM = 3;
  public static final int DELETE_SUCCESS = 4;

  /**
   * CALLBACKS
   */
  private Handler handler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      try {
        finishProgress(ContactActivity.this);
      }
      catch (Exception e) {
        // TODO: handle exception
      }
      switch (msg.what) {
        case CREATE_SUCCESS:
          util.showToast(getResources().getString(R.string.send_success), ContactActivity.this);
          loadContactListPage();
          enableDrawer();
          break;
        case MSG_ERROR:
          util.showMessage(getResources().getString(R.string.error), errorMessage, ContactActivity.this);
          break;
        case LIST:
          progressbar.setVisibility(View.GONE);
          if (adapter.isEmpty()) {
            tvNoMessages.setText(getResources().getString(R.string.no_messages));
            tvNoMessages.setVisibility(View.VISIBLE);
          }
          else {
            tvNoMessages.setVisibility(View.GONE);
            lvMessages.setVisibility(View.VISIBLE);
            lvMessages.setAdapter(adapter);
          }
          break;
        case EDIT_FORM:
          loadContactFormEdit(message);
          break;
        case DELETE_SUCCESS:
          util.showToast(getResources().getString(R.string.delete_success), ContactActivity.this);
          loadContactListPage();
          enableDrawer();
          break;
      }
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    actionBar = getSupportActionBar();
    actionBar.setTitle(getDrawerListViewItems()[StaticValues.ACTION_CONTACT]);
    loadContactListPage();
    leftMenu();
  }

  public void loadContactListPage() {
    upateMenu(PAGE_CONTACT_LIST);
    setContentView(R.layout.activity_contact_list);
    tvNoMessages = (TextView) findViewById(R.id.tvNoMessages);
    tvNoMessages.setVisibility(View.GONE);
    progressbar = (LinearLayout) findViewById(R.id.progressbar);
    progressbar.setVisibility(View.VISIBLE);

    new Thread() {

      @Override
      public void run() {
        super.run();
        ContactService contactService = new ContactService(ContactActivity.this);
        try {
          JSONObject messages = contactService.getMessages();
          prepareListView(messages);
          onClickItem();
          onLongClickItem();
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

  public void prepareListView(JSONObject messages) throws JSONException {
    JSONArray items = messages.getJSONArray("messages");
    list = new ArrayList<HashMap<String, String>>();
    for (int i = 0; i < items.length(); i++) {
      JSONObject item = items.getJSONObject(i);
      String id = item.getString("id");
      String message = item.getString("message");
      String reply = item.getString("reply");
      HashMap<String, String> map = new HashMap<String, String>();
      map.put("id", id);
      map.put("message", util.ellipsize(message, 15));
      map.put("reply", util.ellipsize(reply, 15));
      list.add(map);
    }
    String[] from = new String[] { "message", "reply" };
    int[] to = new int[] { R.id.tvMessage, R.id.tvReply };
    adapter = new SimpleAdapter(ContactActivity.this, list, R.layout.item_list_message, from, to);
    lvMessages = (ListView) findViewById(R.id.listViewMessages);
  }

  public void onClickItem() {
    lvMessages.setOnItemClickListener(new OnItemClickListener() {

      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final int id_message = util.convertStringToInt(list.get(position).get("id"));
        startProgress(ContactActivity.this);
        new Thread() {

          @Override
          public void run() {
            super.run();
            ContactService contactService = new ContactService(ContactActivity.this);
            try {
              message = contactService.getMessage(id_message);
              handler.sendEmptyMessage(EDIT_FORM);
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
    });
  }

  public void onLongClickItem() {
    lvMessages.setOnItemLongClickListener(new OnItemLongClickListener() {

      public boolean onItemLongClick(AdapterView<?> arg0, final View view, int position, long id) {
        final int id_message = util.convertStringToInt(list.get(position).get("id"));
        util.confirm(ContactActivity.this, getResources().getString(R.string.confirmation), getResources().getString(R.string.delete_confirmation), getResources().getString(R.string.yes),
            getResources().getString(R.string.no), new Runnable() {

              public void run() {
                startProgress(ContactActivity.this);
                new Thread() {

                  @Override
                  public void run() {
                    super.run();
                    ContactService contactService = new ContactService(ContactActivity.this);
                    try {
                      contactService.delete(id_message);
                      handler.sendEmptyMessage(DELETE_SUCCESS);
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
            }, null);

        return true;
      }
    });
  }

  public void loadContactFormNew() {
    upateMenu(PAGE_CONTACT_NEW);
    setContentView(R.layout.activity_contact_form);
    disableReplyEditText();
    disableDrawer();
  }

  public void loadContactFormEdit(JSONObject message) {

    loadContactFormNew();
    upateMenu(PAGE_CONTACT_EDIT);
    etMessage = (EditText) findViewById(R.id.etMessage);
    etReply = (EditText) findViewById(R.id.etReply);

    try {
      etMessage.setText(message.getString("message"));
      etMessage.setEnabled(false);
      etReply.setText(message.getString("reply"));
    }
    catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void saveContactMessage() {
    startProgress(ContactActivity.this);
    new Thread() {

      @Override
      public void run() {
        super.run();
        etMessage = (EditText) findViewById(R.id.etMessage);
        String message = etMessage.getText().toString();
        ContactService contactService = new ContactService(ContactActivity.this);
        try {
          contactService.sendMessage(message);
          handler.sendEmptyMessage(CREATE_SUCCESS);
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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.default_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menuNew = menu.findItem(R.id.menu_new);
    menuSave = menu.findItem(R.id.menu_save);

    switch (currentPage) {
      case PAGE_CONTACT_LIST:
        menuNew.setVisible(true);
        menuSave.setVisible(false);
        break;
      case PAGE_CONTACT_NEW:
        menuNew.setVisible(false);
        menuSave.setVisible(true);
        break;
      case PAGE_CONTACT_EDIT:
        menuNew.setVisible(false);
        menuSave.setVisible(false);
        break;
      default:
        break;
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        if (currentPage == PAGE_CONTACT_LIST) {
          return (super.onOptionsItemSelected(item));
        }
        redirectBack();
        return true;

      case R.id.menu_new:
        loadContactFormNew();
        return true;

      case R.id.menu_save:
        saveContactMessage();
        return true;

    }
    return (super.onOptionsItemSelected(item));
  }

  @Override
  public void onBackPressed() {
    redirectBack();
  }

  public void redirectBack() {
    switch (currentPage) {
      case PAGE_CONTACT_LIST:
        returnToIndex();
        break;
      case PAGE_CONTACT_NEW:
      case PAGE_CONTACT_EDIT:
        loadContactListPage();
        enableDrawer();
        break;
      default:
        finish();
        break;
    }
  }

  private void upateMenu(int page) {
    currentPage = page;
    supportInvalidateOptionsMenu();
  }

  private void disableReplyEditText() {
    etReply = (EditText) findViewById(R.id.etReply);
    etReply.setEnabled(false);
  }
}
