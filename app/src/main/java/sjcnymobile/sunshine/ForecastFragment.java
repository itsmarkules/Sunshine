package sjcnymobile.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.text.format.Time;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Mack on 2/23/2016.
 */
public class ForecastFragment extends Fragment {
    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            FetchWeatherTask weatherTask = new FetchWeatherTask();
            weatherTask.execute("11772");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    ArrayAdapter<String> mForecastAdapter;

//   String[] humidity = {"h1","h2","h3","h4","h5","h6","h7"};
//    String[] pressure = {"p1","p2","p3","p4","p5","p6","p7"};
//    String[] wind = {"w1","w2","w3","w4","w5","w6","w7"};

    String[] humidity = new String[100];
    String[] pressure = new String[100];
    String[] wind = new String[100];



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        final String[] forecastArray = {
                "Sunday - Sunny - 88/63",
                "Monday - Sunny - 88/63",
                "Tuesday - Sunny - 88/63",
                "Wednesday - Sunny - 88/63",
                "Thursday - Sunny - 88/63",
                "Friday - Sunny - 88/63",
                "Saturday - Sunny - 88/63",

        };

        List<String> weekForecast = new ArrayList<String>();

        mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        new FetchWeatherTask().execute("11778");
       /* try {
            new FetchWeatherTask().showDetails("11778");
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l){

                String forecastDay = mForecastAdapter.getItem(position); //get the array number of item clicked

                //Toast.makeText(getActivity(),forecastDay , Toast.LENGTH_SHORT).show(); //pop up shows
                //Create exp(licit intent for DetailActivity
                Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, forecastDay);
                intent.putExtra("humidity",humidity[position]);
                intent.putExtra("pressure",pressure[position]);
                intent.putExtra("wind",wind[position]);
                startActivity(intent);
            }
        });

        return rootView;
    }


    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            if(strings==null)
                return;
            mForecastAdapter.clear();
            for(String s: strings)
            {
                mForecastAdapter.add(s);
            }
            mForecastAdapter.notifyDataSetChanged();
        }

        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }


        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */


        private String[] getWeatherDataFromJson(String forecastJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "temp_max";
            final String OWM_MIN = "temp_min";
            final String OWM_DESCRIPTION = "main";
            final String OWM_HUMIDITY = "humidity";
            final String OWM_WIND = "speed";
            final String OWM_PRESSURE = "pressure";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[weatherArray.length()];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);


                //humidity
             //   int n;
              //  String h;
                //get the Json object
              // JSONObject humidityObject = dayForecast.getJSONObject(OWM_HUMIDITY);
              // h = dayForecast.getJSONObject(OWM_HUMIDITY).getString("humidity"); //get humidity frm description and parse
               // h =String.valueOf(n);

                //wind data
               // String w;
               //JSONObject windObject = dayForecast.getJSONObject(OWM_WIND);
               // w = windObject.getString(OWM_WIND);

                //pressure data
                //String p;
                //JSONObject pressureObject = dayForecast.getJSONObject(OWM_PRESSURE);
               // p = pressureObject.getString(OWM_PRESSURE);

              //  humidity[i] = "Humidity: ";
                //wind[i] = "Wind speed: " + dayForecast.getJSONObject(OWM_WIND);

               // pressure[i] = "Pressure: " +  p;

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return resultStrs;

        }

        @Override
        protected String[] doInBackground(String... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
                       if (params.length == 0) {
                               return null;
                           }

            // These two need to be declared outside the try/catch
// so that they can be closed in the finally block.


            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

// Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            String format = "json";
            String units = "imperial";
            int numDays = 7;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
              //  URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=patchogue&APPID=f6b1f499d10db8d6e38b0642d05e4cf5&cnt=7&units=imperial");

               // final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?q=patchogue&APPID=f6b1f499d10db8d6e38b0642d05e4cf5&cnt=7&units=imperial";
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/";
                                final String QUERY_PARAM = "q";
                                final String FORMAT_PARAM = "mode";
                                final String UNITS_PARAM = "units";
                                final String DAYS_PARAM = "cnt";
                                final String APPID_PARAM = "APPID";

                                        Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                                                .appendQueryParameter(QUERY_PARAM, params[0])
                                                .appendQueryParameter(FORMAT_PARAM, format)
                                                .appendQueryParameter(UNITS_PARAM, units)
                                                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                                                .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                                                .build();

                                        URL url = new URL(builtUri.toString());

                                        Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Forecast Json String: " +forecastJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

            String[] ret;

            try
            {
                ret = getWeatherDataFromJson(forecastJsonStr);
            }
            catch(Exception e)
            {
                ret = null;
            }

            return ret;


        }
    }

}




