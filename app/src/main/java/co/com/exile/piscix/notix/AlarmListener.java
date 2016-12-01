package co.com.exile.piscix.notix;

import org.json.JSONArray;
import org.json.JSONObject;


public interface AlarmListener {

    void onAlarm(JSONObject alarm);

    void onShowAlarm(JSONArray alarms);
}
