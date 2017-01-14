package ir.actfun.toofan.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.ToxicBakery.viewpager.transforms.RotateUpTransformer;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.mikepenz.crossfadedrawerlayout.view.CrossfadeDrawerLayout;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.MiniDrawer;
import com.mikepenz.materialdrawer.interfaces.ICrossfader;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;

import net.grobas.view.MovingImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import gapchenko.llttz.Converter;
import gapchenko.llttz.IConverter;
import gapchenko.llttz.stores.TimeZoneListStore;
import ir.actfun.toofan.Adapters.ViewPagerAdapter;
import ir.actfun.toofan.Adapters.WeatherRecyclerAdapter;
import ir.actfun.toofan.AlarmReceiver;
import ir.actfun.toofan.Constants;
import ir.actfun.toofan.Fragments.RecyclerViewFragment;
import ir.actfun.toofan.activities.NowWeatherPage;

import ir.actfun.toofan.R;
import ir.actfun.toofan.ScalableVideoView;
import ir.actfun.toofan.models.Weather;
import ir.actfun.toofan.tasks.GenericRequestTask2;
import ir.actfun.toofan.tasks.ParseResult;
import ir.actfun.toofan.tasks.TaskOutput;
import ir.actfun.toofan.widgets.AbstractWidgetProvider;
import ir.actfun.toofan.widgets.DashClockWeatherExtension;

/**
 * Created by Ali Gholami on 8/10/2016.
 */
public class NowWeatherPage  extends AppCompatActivity implements LocationListener {

    public static String lat1 = "35.66666";
    public static String lon1 = "51.43333";

    private static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;

    // Time in milliseconds; only reload weather if last update is longer ago than this value
    private static final int NO_UPDATE_REQUIRED_THRESHOLD = 300000;

    private static Map<String, Integer> speedUnits = new HashMap<>(3);
    private static Map<String, Integer> pressUnits = new HashMap<>(3);
    private static boolean mappingsInitialised = false;


    boolean universal_flag = true;
    TextView cityField;
    Typeface weatherFont;
    Weather todayWeather = new Weather();
    TextView todayTemperature;
    TextView todayDescription;
    TextView todayWind;
    TextView todayPressure;
    TextView todayHumidity;
    TextView todaySunrise;
    TextView todaySunset;
    TextView lastUpdate;
    TextView todayIcon;
    ViewPager viewPager;
    TabLayout tabLayout;
    RelativeLayout relativeLayout;
    View appView;
    LocationManager locationManager;
    ProgressDialog progressDialog;
    TextView quote_text;
    TextView quote_text2;
    TextView quote_writer;
    MovingImageView movingImageView;
    boolean darkTheme;
    boolean destroyed = false;

    private List<Weather> longTermWeather;
    private List<Weather> longTermTodayWeather;
    private List<Weather> longTermTomorrowWeather;

    public String recentCity = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize the associated SharedPreferences file with default values
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

        darkTheme = false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getString("theme", "fresh").equals("dark")) {
            setTheme(R.style.AppTheme_NoActionBar_Dark);
            darkTheme = true;
        } else if (prefs.getString("theme", "fresh").equals("transparent")) {
            setTheme(R.style.AppTheme_NoActionBar_transparent);
        }

        // Initiate activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        appView = findViewById(R.id.viewApp);

        progressDialog = new ProgressDialog(NowWeatherPage.this);

        // Load toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (darkTheme) {
            toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay_Dark);
        }

        // Initialize textboxes
        setContentView(R.layout.nowweather_layout);

        weatherFont = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/weathericons-regular-webfont.ttf");

        YoYo.with(Techniques.ZoomIn)
                .duration(2000)
                .playOn(findViewById(R.id.weather_icon));
        YoYo.with(Techniques.ZoomIn)
                .duration(2000)
                .playOn(findViewById(R.id.city_field));
        YoYo.with(Techniques.ZoomIn)
                .duration(2000)
                .playOn(findViewById(R.id.current_temperature_field));
        YoYo.with(Techniques.ZoomIn)
                .duration(2200)
                .playOn(findViewById(R.id.details_field));
        YoYo.with(Techniques.ZoomIn)
                .duration(2400)
                .playOn(findViewById(R.id.humidity_field));
        YoYo.with(Techniques.ZoomIn)
                .duration(2800)
                .playOn(findViewById(R.id.pressure_field));
        YoYo.with(Techniques.ZoomIn)
                .duration(2600)
                .playOn(findViewById(R.id.wind_field));
        YoYo.with(Techniques.ZoomIn)
                .duration(3000)
                .playOn(findViewById(R.id.updated_field));


        quote_writer = (TextView)findViewById(R.id.quote_writer);
        quote_text2 = (TextView)findViewById(R.id.quote_text2);
        quote_text = (TextView)findViewById(R.id.quote_text);
        cityField = (TextView) findViewById(R.id.city_field);
        lastUpdate = (TextView) findViewById(R.id.updated_field);
        todayDescription = (TextView) findViewById(R.id.details_field);
        todayTemperature = (TextView) findViewById(R.id.current_temperature_field);
        todayHumidity = (TextView) findViewById(R.id.humidity_field);
        todayPressure = (TextView) findViewById(R.id.pressure_field);
        todayWind =  (TextView) findViewById(R.id.wind_field);
        todayIcon = (TextView) findViewById(R.id.weather_icon);
        todayIcon.setTypeface(weatherFont);

        // Preload data from cache
        preloadWeather();
        updateLastUpdateTime();

        // Set autoupdater
        AlarmReceiver.setRecurringAlarm(this);


        //Getting the quote from Ganjoor

        QuoteGenerator.placeIdTask asyncTask =new QuoteGenerator.placeIdTask(new QuoteGenerator.AsyncResponse() {
            public void processFinish(String m1, String m2, String author) {

                quote_text.setText(m1);
                quote_text2.setText(m2);
                quote_writer.setText(author);



            }
        });
        asyncTask.execute("25.180000", "89.530000"); //  asyncTask.execute("Latitude", "Longitude")

        YoYo.with(Techniques.FadeIn)
                .duration(10000)
                .playOn(findViewById(R.id.quote_text));
        YoYo.with(Techniques.FadeIn)
                .duration(10000)
                .playOn(findViewById(R.id.quote_text2));
        YoYo.with(Techniques.FadeOut)
                .duration(5000)
                .playOn(findViewById(R.id.quote_writer));
        //End of OnCreate Method.

    }

    public WeatherRecyclerAdapter getAdapter(int id) {
        WeatherRecyclerAdapter weatherRecyclerAdapter;
        if (id == 0) {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermTodayWeather);
        } else if (id == 1) {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermTomorrowWeather);
        } else {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermWeather);
        }
        return weatherRecyclerAdapter;
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean darkTheme =
                PreferenceManager.getDefaultSharedPreferences(this).getString("theme", "fresh").equals("dark");
        if (darkTheme != this.darkTheme) {
            // Restart activity to apply theme
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
        } else if (shouldUpdate() && isNetworkAvailable()) {
            getTodayWeather();
            getLongTermWeather();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyed = true;

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.removeUpdates(NowWeatherPage.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void preloadWeather() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(NowWeatherPage.this);

        String lastToday = sp.getString("lastToday", "");
        if (!lastToday.isEmpty()) {
            new TodayWeatherTask(this, this, progressDialog).execute("cachedResponse", lastToday);
        }
        String lastLongterm = sp.getString("lastLongterm", "");
        if (!lastLongterm.isEmpty()) {
            new LongTermWeatherTask(this, this, progressDialog).execute("cachedResponse", lastLongterm);
        }
    }

    private void getTodayWeather() {
        new TodayWeatherTask(this, this, progressDialog).execute();
    }

    private void getLongTermWeather() {
        new LongTermWeatherTask(this, this, progressDialog).execute();
    }

    private void searchCities() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(this.getString(R.string.search_title));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setMaxLines(1);
        input.setSingleLine(true);
        alert.setView(input, 32, 0, 32, 0);
        alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String result = input.getText().toString();
                if (!result.isEmpty()) {
                    saveLocation(result);
                }
            }
        });
        alert.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Cancelled
            }
        });
        alert.show();


    }

    private void saveLocation(String result) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(NowWeatherPage.this);
        recentCity = preferences.getString("city", Constants.DEFAULT_CITY);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("city", result);
        editor.commit();

        if (!recentCity.equals(result)) {
            // New location, update weather
            getTodayWeather();
            getLongTermWeather();
        }
    }

    private void aboutDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Toofan");
        final WebView webView = new WebView(this);
        String about = "<p>A lightweight, opensource weather app.</p>" +
                "<p>Developed by <a href='mailto:t.martykan@gmail.com'>Tomas Martykan</a></p>" +
                "<p>Data provided by <a href='http://openweathermap.org/'>OpenWeatherMap</a>, under the <a href='http://creativecommons.org/licenses/by-sa/2.0/'>Creative Commons license</a>" +
                "<p>Icons are <a href='https://erikflowers.github.io/weather-icons/'>Weather Icons</a>, by <a href='http://www.twitter.com/artill'>Lukas Bischoff</a> and <a href='http://www.twitter.com/Erik_UX'>Erik Flowers</a>, under the <a href='http://scripts.sil.org/OFL'>SIL OFL 1.1</a> licence.";
        if (darkTheme) {
            // Style text color for dark theme
            about = "<style media=\"screen\" type=\"text/css\">" +
                    "body {\n" +
                    "    color:white;\n" +
                    "}\n" +
                    "a:link {color:cyan}\n" +
                    "</style>" +
                    about;
        }
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.loadData(about, "text/html", "UTF-8");
        alert.setView(webView, 32, 0, 32, 0);
        alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        alert.show();
    }

    private String setWeatherIcon(int actualId, int hourOfDay) {

        movingImageView = (MovingImageView)findViewById(R.id.movingimageview);



        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            if (hourOfDay >= 7 && hourOfDay < 20) {
                if(universal_flag)
                {
                    final TypedArray images = getResources().obtainTypedArray(R.array.sunny_images);
                    final int choice = (int) (Math.random() * images.length());
                    NowWeatherPage.this.runOnUiThread(new Runnable() {
                        public void run() {
                            movingImageView.setImageResource(images.getResourceId(choice, R.drawable.sun1));
                            todayTemperature.setTextColor(getResources().getColor(R.color.sunny_icon));
                            todayIcon.setTextColor(getResources().getColor(R.color.sunny_icon));

                        }
                    });
                    universal_flag = false;
                }

                icon = this.getString(R.string.weather_sunny);
            } else {
                if(universal_flag)
                {
                    final TypedArray images = getResources().obtainTypedArray(R.array.night_images);
                    final int choice = (int) (Math.random() * images.length());
                    NowWeatherPage.this.runOnUiThread(new Runnable() {
                        public void run() {
                            movingImageView.setImageResource(images.getResourceId(choice, R.drawable.night1));
                            todayTemperature.setTextColor(getResources().getColor(R.color.night_icon));
                            todayIcon.setTextColor(getResources().getColor(R.color.night_icon));
                        }
                    });
                    universal_flag = false;
                }

                icon = this.getString(R.string.weather_clear_night);
            }
        } else {
            switch (id) {
                case 2: {
                    if (universal_flag) {
                        final TypedArray images = getResources().obtainTypedArray(R.array.thunder_images);
                        final int choice = (int) (Math.random() * images.length());
                        NowWeatherPage.this.runOnUiThread(new Runnable() {
                            public void run() {
                                movingImageView.setImageResource(images.getResourceId(choice, R.drawable.thunder1));
                                todayTemperature.setTextColor(getResources().getColor(R.color.rainy_icon));
                                todayIcon.setTextColor(getResources().getColor(R.color.rainy_icon));
                            }
                        });
                        universal_flag = false;
                    }
                    icon = this.getString(R.string.weather_thunder);
                    break;
                }

                case 3: {
                    if (universal_flag) {
                        final TypedArray images = getResources().obtainTypedArray(R.array.rainy_images);
                        final int choice = (int) (Math.random() * images.length());
                        NowWeatherPage.this.runOnUiThread(new Runnable() {
                            public void run() {
                                movingImageView.setImageResource(images.getResourceId(choice, R.drawable.rain1));
                                todayTemperature.setTextColor(getResources().getColor(R.color.rainy_icon));
                                todayIcon.setTextColor(getResources().getColor(R.color.rainy_icon));
                            }
                        });
                        universal_flag = false;
                    }
                    icon = this.getString(R.string.weather_drizzle);
                    break;
                }

                case 7: {
                    if (universal_flag) {
                        final TypedArray images = getResources().obtainTypedArray(R.array.fog_images);
                        final int choice = (int) (Math.random() * images.length());
                        NowWeatherPage.this.runOnUiThread(new Runnable() {
                            public void run() {
                                movingImageView.setImageResource(images.getResourceId(choice, R.drawable.fog1));
                                todayTemperature.setTextColor(getResources().getColor(R.color.cloudy_icon));
                                todayIcon.setTextColor(getResources().getColor(R.color.cloudy_icon));
                            }
                        });
                        universal_flag = false;
                    }
                    icon = this.getString(R.string.weather_foggy);
                    break;
                }

                case 8: {
                    if (universal_flag) {
                        final TypedArray images = getResources().obtainTypedArray(R.array.cloudy_images);
                        final int choice = (int) (Math.random() * images.length());
                        NowWeatherPage.this.runOnUiThread(new Runnable() {
                            public void run() {
                                movingImageView.setImageResource(images.getResourceId(choice, R.drawable.cloud2));
                                todayTemperature.setTextColor(getResources().getColor(R.color.cloudy_icon));
                                todayIcon.setTextColor(getResources().getColor(R.color.cloudy_icon));
                            }
                        });
                        universal_flag = false;
                    }
                    icon = this.getString(R.string.weather_cloudy);
                    break;
                }

                case 6: {
                    if (universal_flag) {
                        final TypedArray images = getResources().obtainTypedArray(R.array.snowy_images);
                        final int choice = (int) (Math.random() * images.length());
                        NowWeatherPage.this.runOnUiThread(new Runnable() {
                            public void run() {
                                movingImageView.setImageResource(images.getResourceId(choice, R.drawable.snow1));
                                todayTemperature.setTextColor(getResources().getColor(R.color.snowy_icon));
                                todayIcon.setTextColor(getResources().getColor(R.color.snowy_icon));
                            }
                        });
                        universal_flag = false;
                    }
                    icon = this.getString(R.string.weather_snowy);
                    break;
                }

                case 5: {
                    if (universal_flag) {
                        final TypedArray images = getResources().obtainTypedArray(R.array.rainy_images);
                        final int choice = (int) (Math.random() * images.length());
                        NowWeatherPage.this.runOnUiThread(new Runnable() {
                            public void run() {
                                movingImageView.setImageResource(images.getResourceId(choice, R.drawable.rain1));
                                todayTemperature.setTextColor(getResources().getColor(R.color.rainy_icon));
                                todayIcon.setTextColor(getResources().getColor(R.color.rainy_icon));
                            }
                        });
                        universal_flag = false;
                    }
                    icon = this.getString(R.string.weather_rainy);
                    break;
                }


            }
        }

        return icon;
    }

    private String getRainString(JSONObject rainObj) {
        String rain = "0";
        if (rainObj != null) {
            rain = rainObj.optString("3h", "fail");
            if ("fail".equals(rain)) {
                rain = rainObj.optString("1h", "0");
            }
        }
        return rain;
    }

    private ParseResult parseTodayJson(String result) {
        try {
            JSONObject reader = new JSONObject(result);
            /************************************************/

            String latitude;
            String longitude;

            JSONObject area_coordinations = reader.optJSONObject("coord");
            if(area_coordinations!=null)
            {
                latitude = area_coordinations.getString("lat");
                longitude = area_coordinations.getString("lon");
                set_correct_time(latitude,longitude);
            }


            /************************************************/
            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                return ParseResult.CITY_NOT_FOUND;
            }


            String city = reader.getString("name");
            String country = "";
            JSONObject countryObj = reader.optJSONObject("sys");
            if (countryObj != null) {
                country = countryObj.getString("country");
                todayWeather.setSunrise(countryObj.getString("sunrise"));
                todayWeather.setSunset(countryObj.getString("sunset"));
            }
            todayWeather.setCity(city);
            todayWeather.setCountry(country);

            JSONObject main = reader.getJSONObject("main");

            todayWeather.setTemperature(main.getString("temp"));
            todayWeather.setDescription(reader.getJSONArray("weather").getJSONObject(0).getString("description"));
            JSONObject windObj = reader.getJSONObject("wind");
            todayWeather.setWind(windObj.getString("speed"));
            if (windObj.has("deg")) {
                todayWeather.setWindDirectionDegree(windObj.getDouble("deg"));
            } else {
                Log.e("parseTodayJson", "No wind direction available");
                todayWeather.setWindDirectionDegree(null);
            }
            todayWeather.setPressure(main.getString("pressure"));
            todayWeather.setHumidity(main.getString("humidity"));

            JSONObject rainObj = reader.optJSONObject("rain");
            String rain;
            if (rainObj != null) {
                rain = getRainString(rainObj);
            } else {
                JSONObject snowObj = reader.optJSONObject("snow");
                if (snowObj != null) {
                    rain = getRainString(snowObj);
                } else {
                    rain = "0";
                }
            }
            todayWeather.setRain(rain);

            IConverter iconv = Converter.getInstance(TimeZoneListStore.class);
            TimeZone tz = iconv.getTimeZone(Double.parseDouble(lat1),Double.parseDouble(lon1));

            final String idString = reader.getJSONArray("weather").getJSONObject(0).getString("id");
            todayWeather.setId(idString);
            todayWeather.setIcon(setWeatherIcon(Integer.parseInt(idString), Calendar.getInstance(tz).get(Calendar.HOUR_OF_DAY)));

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(NowWeatherPage.this).edit();
            editor.putString("lastToday", result);
            editor.commit();

        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            return ParseResult.JSON_EXCEPTION;
        }

        return ParseResult.OK;
    }
    /*********************************************/
    public void set_correct_time(String latitude,String longitude)
    {
        lon1 = longitude;
        lat1 = latitude;

    }


    private void updateTodayWeatherUI() {
        String city = todayWeather.getCity();
        String country = todayWeather.getCountry();
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());
        getSupportActionBar().setTitle(city + (country.isEmpty() ? "" : ", " + country));

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(NowWeatherPage.this);

        float temperature = Float.parseFloat(todayWeather.getTemperature());
        if (sp.getString("unit", "C").equals("C")) {
            temperature = temperature - 273.15f;
        }

        if (sp.getString("unit", "C").equals("F")) {
            temperature = (((9 * (temperature - 273.15f)) / 5) + 32);
        }

        double rain = Double.parseDouble(todayWeather.getRain());
        String rainString = "";
        if (rain > 0) {
            if (sp.getString("lengthUnit", "mm").equals("mm")) {
                if (rain < 0.1) {
                    rainString = " (<0.1 mm)";
                } else {
                    rainString = String.format(Locale.ENGLISH, " (%.1f %s)", rain, sp.getString("lengthUnit", "mm"));
                }
            } else {
                rain = rain / 25.4;
                if (rain < 0.01) {
                    rainString = " (<0.01 in)";
                } else {
                    rainString = String.format(Locale.ENGLISH, " (%.2f %s)", rain, sp.getString("lengthUnit", "mm"));
                }
            }

        }

        double wind = Double.parseDouble(todayWeather.getWind());
        if (sp.getString("speedUnit", "m/s").equals("kph")) {
            wind = wind * 3.59999999712;
        }

        if (sp.getString("speedUnit", "m/s").equals("mph")) {
            wind = wind * 2.23693629205;
        }

        double pressure = Double.parseDouble(todayWeather.getPressure());
        if (sp.getString("pressureUnit", "hPa").equals("kPa")) {
            pressure = pressure / 10;
        }
        if (sp.getString("pressureUnit", "hPa").equals("mm Hg")) {
            pressure = pressure * 0.750061561303;
        }
        /***************************************************/

        /**************************************************/
        cityField.setText(todayWeather.getCity().toUpperCase(Locale.US) + ", " + todayWeather.getCountry());
        todayTemperature.setText(new DecimalFormat("#.#").format(temperature) + " °" + sp.getString("unit", "C"));
        todayDescription.setText(todayWeather.getDescription().substring(0, 1).toUpperCase() +
                todayWeather.getDescription().substring(1) + rainString);
        todayWind.setText(getString(R.string.wind) + ": " + new DecimalFormat("#.0").format(wind) + " " +
               localize(sp, "speedUnit", "m/s") +
                (todayWeather.isWindDirectionAvailable() ? " " + getWindDirectionString(sp, this, todayWeather) : ""));
        todayPressure.setText(getString(R.string.pressure) + ": " + new DecimalFormat("#.0").format(pressure) + " " +
                localize(sp, "pressureUnit", "hPa"));
        todayHumidity.setText(getString(R.string.humidity) + ": " + todayWeather.getHumidity() + " %");
        todayIcon.setText(todayWeather.getIcon());
    }

    public ParseResult parseLongTermJson(String result) {
        int i;
        try {
            JSONObject reader = new JSONObject(result);

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                if (longTermWeather == null) {
                    longTermWeather = new ArrayList<>();
                    longTermTodayWeather = new ArrayList<>();
                    longTermTomorrowWeather = new ArrayList<>();
                }
                return ParseResult.CITY_NOT_FOUND;
            }

            longTermWeather = new ArrayList<>();
            longTermTodayWeather = new ArrayList<>();
            longTermTomorrowWeather = new ArrayList<>();

            JSONArray list = reader.getJSONArray("list");
            for (i = 0; i < list.length(); i++) {
                Weather weather = new Weather();

                JSONObject listItem = list.getJSONObject(i);
                JSONObject main = listItem.getJSONObject("main");

                weather.setDate(listItem.getString("dt"));
                weather.setTemperature(main.getString("temp"));
                weather.setDescription(listItem.optJSONArray("weather").getJSONObject(0).getString("description"));
                JSONObject windObj = listItem.optJSONObject("wind");
                if (windObj != null) {
                    weather.setWind(windObj.getString("speed"));
                    weather.setWindDirectionDegree(windObj.getDouble("deg"));
                }
                weather.setPressure(main.getString("pressure"));
                weather.setHumidity(main.getString("humidity"));

                JSONObject rainObj = listItem.optJSONObject("rain");
                String rain = "";
                if (rainObj != null) {
                    rain = getRainString(rainObj);
                } else {
                    JSONObject snowObj = listItem.optJSONObject("snow");
                    if (snowObj != null) {
                        rain = getRainString(snowObj);
                    } else {
                        rain = "0";
                    }
                }
                weather.setRain(rain);

                final String idString = listItem.optJSONArray("weather").getJSONObject(0).getString("id");
                weather.setId(idString);

                /**********************************************/
                IConverter iconv = Converter.getInstance(TimeZoneListStore.class);
                TimeZone tz = iconv.getTimeZone(Double.parseDouble(lat1),Double.parseDouble(lon1));

                final String dateMsString = listItem.getString("dt") + "000";
                Calendar cal = Calendar.getInstance(tz);
                cal.setTimeInMillis(Long.parseLong(dateMsString));
                weather.setIcon(setWeatherIcon(Integer.parseInt(idString), cal.get(Calendar.HOUR_OF_DAY)));


                Calendar today = Calendar.getInstance(tz);
                if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                    longTermTodayWeather.add(weather);
                } else if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) + 1) {
                    longTermTomorrowWeather.add(weather);
                } else {
                    longTermWeather.add(weather);
                }
            }
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(NowWeatherPage.this).edit();
            editor.putString("lastLongterm", result);
            editor.commit();
        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            return ParseResult.JSON_EXCEPTION;
        }

        return ParseResult.OK;
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean shouldUpdate() {
        /**********************************************/

        IConverter iconv = Converter.getInstance(TimeZoneListStore.class);
        TimeZone tz = iconv.getTimeZone(Double.parseDouble(lat1),Double.parseDouble(lon1));

        long lastUpdate = PreferenceManager.getDefaultSharedPreferences(this).getLong("lastUpdate", -1);
        // Update if never checked or last update is longer ago than specified threshold
        return lastUpdate < 0 || (Calendar.getInstance(tz).getTimeInMillis() - lastUpdate) > NO_UPDATE_REQUIRED_THRESHOLD;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            if (isNetworkAvailable()) {
                getTodayWeather();
                getLongTermWeather();
            } else {
                Snackbar.make(appView, getString(R.string.msg_connection_not_available), Snackbar.LENGTH_LONG).show();
            }
            return true;
        }
        /*if (id == R.id.action_search) {
            searchCities();
            return true;
        }
        if (id == R.id.action_location) {
            getCityByLocation();
            return true;
        }
        if (id == R.id.action_settings) {
            Intent intent = new Intent(NowWeatherPage.this, SettingsActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_about) {
            aboutDialog();
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    public static void initMappings() {
        if (mappingsInitialised)
            return;
        mappingsInitialised = true;
        speedUnits.put("m/s", R.string.speed_unit_mps);
        speedUnits.put("kph", R.string.speed_unit_kph);
        speedUnits.put("mph", R.string.speed_unit_mph);

        pressUnits.put("hPa", R.string.pressure_unit_hpa);
        pressUnits.put("kPa", R.string.pressure_unit_kpa);
        pressUnits.put("mm Hg", R.string.pressure_unit_mmhg);
    }

    private String localize(SharedPreferences sp, String preferenceKey, String defaultValueKey) {
        return localize(sp, this, preferenceKey, defaultValueKey);
    }

    public static String localize(SharedPreferences sp, Context context, String preferenceKey, String defaultValueKey) {
        String preferenceValue = sp.getString(preferenceKey, defaultValueKey);
        String result = preferenceValue;
        if ("speedUnit".equals(preferenceKey)) {
            if (speedUnits.containsKey(preferenceValue)) {
                result = context.getString(speedUnits.get(preferenceValue));
            }
        } else if ("pressureUnit".equals(preferenceKey)) {
            if (pressUnits.containsKey(preferenceValue)) {
                result = context.getString(pressUnits.get(preferenceValue));
            }
        }
        return result;
    }

    public static String getWindDirectionString(SharedPreferences sp, Context context, Weather weather) {
        try {
            if (Double.parseDouble(weather.getWind()) != 0) {
                String pref = sp.getString("windDirectionFormat", null);
                if ("arrow".equals(pref)) {
                    return weather.getWindDirection(8).getArrow(context);
                } else if ("abbr".equals(pref)) {
                    return weather.getWindDirection().getLocalizedString(context);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    void getCityByLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                // Explanation not needed, since user requests this himself

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_ACCESS_FINE_LOCATION);
            }

        } else {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.getting_location));
            progressDialog.setCancelable(false);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        locationManager.removeUpdates(NowWeatherPage.this);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            progressDialog.show();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCityByLocation();
                }
                return;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        progressDialog.hide();
        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();
        Log.i("GPS LOCATION", location.getLatitude() + ", " + location.getLongitude());
        new ProvideCityNameTask(this, this, progressDialog).execute("coords", Double.toString(latitude), Double.toString(longitude));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }



    class TodayWeatherTask extends GenericRequestTask2 {
        public TodayWeatherTask(Context context, NowWeatherPage activity, ProgressDialog progressDialog) {
            super(context, activity, progressDialog);
        }

        @Override
        protected void onPreExecute() {
            loading = 0;
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(TaskOutput output) {
            super.onPostExecute(output);
            // Update ir.actfun.toofan.widgets
        }

        @Override
        protected ParseResult parseResponse(String response) {
            return parseTodayJson(response);
        }

        @Override
        protected String getAPIName() {
            return "weather";
        }

        @Override
        protected void updateMainUI() {
            updateTodayWeatherUI();
            updateLastUpdateTime();
        }
    }

    class LongTermWeatherTask extends GenericRequestTask2 {
        public LongTermWeatherTask(Context context, NowWeatherPage activity, ProgressDialog progressDialog) {
            super(context, activity, progressDialog);
        }

        @Override
        protected ParseResult parseResponse(String response) {
            return parseLongTermJson(response);
        }

        @Override
        protected String getAPIName() {
            return "forecast";
        }

        @Override
        protected void updateMainUI() {
        }
    }

    class ProvideCityNameTask extends GenericRequestTask2 {

        public ProvideCityNameTask(Context context, NowWeatherPage activity, ProgressDialog progressDialog) {
            super(context,activity, progressDialog);
        }

        @Override
        protected void onPreExecute() { /*Nothing*/ }

        @Override
        protected String getAPIName() {
            return "weather";
        }

        @Override
        protected ParseResult parseResponse(String response) {
            Log.i("RESULT", response.toString());
            try {
                JSONObject reader = new JSONObject(response);

                final String code = reader.optString("cod");
                if ("404".equals(code)) {
                    Log.e("Geolocation", "No city found");
                    return ParseResult.CITY_NOT_FOUND;
                }

                String city = reader.getString("name");
                String country = "";
                JSONObject countryObj = reader.optJSONObject("sys");
                if (countryObj != null) {
                    country = ", " + countryObj.getString("country");
                }

                saveLocation(city + country);

            } catch (JSONException e) {
                Log.e("JSONException Data", response);
                e.printStackTrace();
                return ParseResult.JSON_EXCEPTION;
            }

            return ParseResult.OK;
        }

        @Override
        protected void onPostExecute(TaskOutput output) {
            /* Handle possible errors only */
            handleTaskOutput(output);
        }
    }

    public static long saveLastUpdateTime(SharedPreferences sp) {
        /**********************************************/
        IConverter iconv = Converter.getInstance(TimeZoneListStore.class);
        TimeZone tz = iconv.getTimeZone(Double.parseDouble(lat1),Double.parseDouble(lon1));
        Calendar now = Calendar.getInstance(tz);
        sp.edit().putLong("lastUpdate", now.getTimeInMillis()).apply();
        return now.getTimeInMillis();
    }

    private void updateLastUpdateTime() {
        updateLastUpdateTime(
                PreferenceManager.getDefaultSharedPreferences(this).getLong("lastUpdate", -1)
        );
    }

    private void updateLastUpdateTime(long timeInMillis) {
        if (timeInMillis < 0) {
            // No time
            lastUpdate.setText("");
        } else {
            lastUpdate.setText(getString(R.string.last_update, formatTimeWithDayIfNotToday(this, timeInMillis)));
        }
    }

    public static  String formatTimeWithDayIfNotToday(Context context, long timeInMillis) {
        /**********************************************************/
        IConverter iconv = Converter.getInstance(TimeZoneListStore.class);
        TimeZone tz = iconv.getTimeZone(Double.parseDouble(lat1),Double.parseDouble(lon1));
        Calendar now = Calendar.getInstance(tz);
        Calendar lastCheckedCal = new GregorianCalendar(tz);
        lastCheckedCal.setTimeInMillis(timeInMillis);
        Date lastCheckedDate = new Date(timeInMillis);
        String timeFormat = android.text.format.DateFormat.getTimeFormat(context).format(lastCheckedDate);
        if (now.get(Calendar.YEAR) == lastCheckedCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == lastCheckedCal.get(Calendar.DAY_OF_YEAR)) {
            // Same day, only show time
            return timeFormat;
        } else {
            return android.text.format.DateFormat.getDateFormat(context).format(lastCheckedDate) + " " + timeFormat;
        }
    }


}
