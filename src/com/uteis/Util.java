/**
 * @author Bruno Zeraik 
 */

package com.uteis;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

public class Util {

  private Pattern pattern;
  private Matcher matcher;
  private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

  private final static String NON_THIN = "[^iIl1\\.,']";

  private static int textWidth(String str) {
    return (int) (str.length() - str.replaceAll(NON_THIN, "").length() / 2);
  }

  public String ellipsize(String text, int max) {

    if (textWidth(text) <= max)
      return text;

    // Start by chopping off at the word before max
    // This is an over-approximation due to thin-characters...
    int end = text.lastIndexOf(' ', max - 3);

    // Just one long word. Chop it off.
    if (end == -1)
      return text.substring(0, max - 3) + "...";

    // Step forward as long as textWidth allows.
    int newEnd = end;
    do {
      end = newEnd;
      newEnd = text.indexOf(' ', end + 1);

      // No more spaces.
      if (newEnd == -1)
        newEnd = text.length();

    } while (textWidth(text.substring(0, newEnd) + "...") < max);

    return text.substring(0, end) + "...";
  }

  public boolean isValidEmail(final String email) {
    pattern = Pattern.compile(EMAIL_PATTERN);

    matcher = pattern.matcher(email);
    return matcher.matches();
  }

  public boolean convertStringToBoolean(String value) {
    return value == "true";
  }

  public String[] convertJSONArrayToStringArray(JSONArray jsonArray) throws JSONException {
    int i;
    String[] list = new String[jsonArray.length()];
    for (i = 0; i < jsonArray.length(); i++) {
      list[i] = jsonArray.get(i).toString();
    }

    return list;
  }

  public JSONObject convertStringToJson(String value) {
    try {
      return new JSONObject(value);
    }
    catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  public String toThreeDecimal(Double value) {
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(3);
    String formated = nf.format(value);

    return formated;
  }

  public Double convertStringToDouble(String value) {
    return Double.parseDouble(value);
  }

  public int convertStringToInt(String value) {
    return Integer.parseInt(value);
  }

  public String convertDoubleToString(Double value) {
    return String.valueOf(value);
  }

  public String convertIntToString(int value) {
    return String.valueOf(value);
  }

  public void showMessage(String title, String msg, Context context) {
    AlertDialog.Builder mensagem = new AlertDialog.Builder(context);
    mensagem.setTitle(title);
    mensagem.setMessage(msg);
    mensagem.setNeutralButton("OK", null);
    mensagem.show();
  }

  public void showToast(String msg, Context context) {

    Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
    toast.show();
  }

  /**
   * Display a confirm dialog.
   * 
   * @param activity
   * @param title
   * @param message
   * @param positiveLabel
   * @param negativeLabel
   * @param onPositiveClick
   *          runnable to call (in UI thread) if positive button pressed. Can be
   *          null
   * @param onNegativeClick
   *          runnable to call (in UI thread) if negative button pressed. Can be
   *          null
   */
  public void confirm(final Activity activity, final String title, final String message, final String positiveLabel, final String negativeLabel, final Runnable onPositiveClick,
      final Runnable onNegativeClick) {

    AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
    dialog.setTitle(title);
    dialog.setMessage(message);
    dialog.setCancelable(false);
    dialog.setPositiveButton(positiveLabel, new DialogInterface.OnClickListener() {

      public void onClick(DialogInterface dialog, int buttonId) {
        if (onPositiveClick != null)
          onPositiveClick.run();
      }
    });
    dialog.setNegativeButton(negativeLabel, new DialogInterface.OnClickListener() {

      public void onClick(DialogInterface dialog, int buttonId) {
        if (onNegativeClick != null)
          onNegativeClick.run();
      }
    });
    dialog.setIcon(android.R.drawable.ic_dialog_alert);
    dialog.show();

  }

  public void animation_slide_out_right(Context context, View view, final Runnable func) {
    final Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right);
    view.startAnimation(animation);
    Handler handle = new Handler();
    handle.postDelayed(new Runnable() {

      @Override
      public void run() {
        try {
          func.run();
        }
        catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }, animation.getDuration());
  }
}
