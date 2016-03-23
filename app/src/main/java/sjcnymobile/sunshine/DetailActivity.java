/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sjcnymobile.sunshine;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */

    public static class PlaceholderFragment extends Fragment {
        String iconString = "http://openweathermap.org/img/w/";
        Bitmap bitImage;
        String h = "<font color='#000000'>Humidity: </font>";
        String p = "<font color='#000000'>Pressure: </font>";
        String w = "<font color='#000000'>Wind: </font>";
        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
           // String iconString ="http://openweathermap.org/img/w/03d.png";
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            Intent intent = getActivity().getIntent();
            if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT))
            {
                String forecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
                String humidity = intent.getStringExtra("humidity");
                String pressure = intent.getStringExtra("pressure");
                String wind = intent.getStringExtra("wind");

                //SpannableString ss1=  new SpannableString(forecastStr);
               // ss1.setSpan(new RelativeSizeSpan(2f), 0,11, 0); // set size
               // ss1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 11, 0);// set color
                ((TextView) rootView.findViewById(R.id.detail_text1)).setText(forecastStr + "°F");
                ((TextView) rootView.findViewById(R.id.detail_text2)).setText(Html.fromHtml(h + humidity + "%"));
                ((TextView) rootView.findViewById(R.id.detail_text3)).setText(Html.fromHtml(p + pressure + " hpa"));
                ((TextView) rootView.findViewById(R.id.detail_text4)).setText(Html.fromHtml(w + wind + " mph"));


            }

            Button btn = (Button) rootView.findViewById(R.id.backButton);

            btn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    System.out.println("in");
                    getActivity().finish();
                }
            });

            return rootView;
        }

    }
}