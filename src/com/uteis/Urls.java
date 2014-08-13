/**
 * @author Bruno Zeraik 
 */

package com.uteis;

public interface Urls {

  // String API = "http://10.0.2.2:8000";
  String API = "http://contcombapi.herokuapp.com";

  String USER_AUTHENTICATE = API + "/generate_token/";
  String USER_SAVE = API + "/user/save";
  String USER_GET = API + "/user/get/username/";
  String USER_UPDATE = API + "/user/update";

  String CONTACT_MESSAGE_SAVE = API + "/contact/save";
  String CONTACT_MESSAGE_LIST = API + "/contact/get/user";
  String CONTACT_MESSAGE_GET = API + "/contact/get/";
  String CONTACT_MESSAGE_DELETE = API + "/contact/delete/";

  String CAR_LIST = API + "/vehicle/get/user";
  String CAR_SAVE = API + "/vehicle/save";
  String CAR_UPDATE = API + "/vehicle/update";
  String CAR_GET = API + "/vehicle/get/";
  String CAR_DELETE = API + "/vehicle/delete/";
  String CAR_GET_MODELS = API + "/vehicle/get_models";
  String CAR_LIST_FUEL = API + "/vehicle/fuel/get/user";
  String CAR_RANKING = API + "/vehicle/ranking";

  String SUPPLY_LIST = API + "/supply/get/user/";
  String SUPPLY_SAVE = API + "/supply/save";
  String SUPPLY_UPDATE = API + "/supply/update";
  String SUPPLY_GET = API + "/supply/get/";
  String SUPPLY_DELETE = API + "/supply/delete/";
  String SUPPLY_SUMMARY = API + "/supply/get/summary/";
}
