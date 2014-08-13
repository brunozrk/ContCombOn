/**
 * @author Bruno Zeraik 
 */

package com.contcombon;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.uteis.StaticValues;
import com.uteis.Util;

public class IndexActivity extends BaseActivity {

  ImageButton btnCalc;

  EditText etEthanol, etGasoline;
  Util util = new Util();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_index);
    ActionBar actionBar;
    actionBar = getSupportActionBar();
    actionBar.setTitle(getDrawerListViewItems()[StaticValues.ACTION_HOME]);
    leftMenu();
    onClickCalculate();
  }

  public void onClickCalculate() {
    btnCalc = (ImageButton) findViewById(R.id.btnCalc);
    etEthanol = (EditText) findViewById(R.id.etEthanol);
    etGasoline = (EditText) findViewById(R.id.etGasoline);

    btnCalc.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View arg0) {

        String ethanol = etEthanol.getText().toString().trim();
        String gasoline = etGasoline.getText().toString().trim();
        if (ethanol.equals("") || gasoline.equals("")) {
          util.showMessage(getResources().getString(R.string.error), getResources().getString(R.string.field_required), IndexActivity.this);
        }
        else {
          if (util.convertStringToDouble(ethanol) == 0 || util.convertStringToDouble(gasoline) == 0) {
            util.showMessage(getResources().getString(R.string.invalid_values), getResources().getString(R.string.field_not_zero), IndexActivity.this);
          }
          else {
            Double calc = util.convertStringToDouble(ethanol) / util.convertStringToDouble(gasoline);
            Double calc_percent = calc * 100;
            String percent = util.toThreeDecimal(calc_percent);
            String ethanol_or_gasoline = getResources().getString(R.string.ethanol_or_gasoline);
            if (calc <= 0.7) {
              util.showMessage(ethanol_or_gasoline, getResources().getString(R.string.ethanol) + "(" + percent + "%)", IndexActivity.this);
            }
            else {
              util.showMessage(ethanol_or_gasoline, getResources().getString(R.string.gasoline) + "(" + percent + "%)", IndexActivity.this);
            }
          }
        }
      }
    });
  }
}
