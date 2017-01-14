package ir.actfun.toofan.activities;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
/**
 * Created by Ali Gholami on 8/16/2016.
 */


public class QuoteGenerator {

    private static final String OPEN_WEATHER_MAP_URL =
            "http://c.ganjoor.net/beyt-json.php?a=1#poet";

    private static final String OPEN_WEATHER_MAP_API = "";

    public interface AsyncResponse {

        void processFinish(String output1, String output2, String output3);
    }





    public static class placeIdTask extends AsyncTask<String, Void, String> {

        public AsyncResponse delegate = null;//Call back interface

        public placeIdTask(AsyncResponse asyncResponse) {
            delegate = asyncResponse;//Assigning call back interfacethrough constructor
        }

        @Override
        protected String doInBackground(String... params) {

            String jsonWeather = null;
            try {
                jsonWeather = getWeatherJSON();
            } catch (Exception e) {
                Log.d("Error", "Cannot process JSON results", e);
            }


            return jsonWeather;
        }

        @Override
        protected void onPostExecute(String json) {
            try {
                if(json != null){
                    JSONObject reader = new JSONObject(json);
                    String poetry1 = reader.getString("m1");
                    String poetry2 = reader.getString("m2");
                    String poet = reader.getString("poet");

                    delegate.processFinish(poetry1,poetry2,poet);

                }
            } catch (JSONException e) {
                //Log.e(LOG_TAG, "Cannot process JSON results", e);
            }



        }
    }






    public static String getWeatherJSON(){
        try {
            URL url = new URL(String.format(OPEN_WEATHER_MAP_URL));
            HttpURLConnection connection =
                    (HttpURLConnection)url.openConnection();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();
            return json.toString();
        }catch(Exception e){
            return null;
        }
    }




}