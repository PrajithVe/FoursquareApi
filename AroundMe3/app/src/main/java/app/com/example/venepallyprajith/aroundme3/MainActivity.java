package app.com.example.venepallyprajith.aroundme3;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends ListActivity {
    ArrayList<FoursquareVenue> venuesList;


    ArrayAdapter<String> myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        new fourquare().execute();
    }

    private class fourquare extends AsyncTask<String, Void, String> {

        String temp;

        @Override
        protected String doInBackground(String... urls) {


            temp = makeCall();
            //System.out.print(temp);
            return "";

        }


        @Override
        protected void onPostExecute(String result) {
            if (temp == null) {

            } else {

                venuesList = parseFoursquare(temp);

                List<String> listTitle = new ArrayList<String>();

                for (int i = 0; i < venuesList.size(); i++) {

                    listTitle.add(i, venuesList.get(i).getName() + ", " + venuesList.get(i).getCategory() + "" + venuesList.get(i).getCity());
                }


                myAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.row_layout, R.id.listText, listTitle);
                setListAdapter(myAdapter);
                ListView list=(ListView)findViewById(android.R.id.list);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String place=myAdapter.getItem(position);
                        Intent intent=new Intent(MainActivity.this,DetailActivity.class);
                        intent.putExtra("EXTRA",place);
                        startActivity(intent);
                    }
                });

            }
        }
    }

    public  String makeCall() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        //String coordinates = sharedPref.getString(getString(R.string.pref_coordinates),"12.9915,80.2336");
            final String FORECAST_BASE_URL =
                    "https://api.foursquare.com/v2/venues/search?";
            final String CLIENT1_PARAM = "client_id";
            final String CLIENT2_PARAM = "client_secret";
            final String UNITS_PARAM = "v";
            final String L1_PARAM = "ll";
            final String coordinates="12.9915,80.2336";
            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(CLIENT1_PARAM, "ZKPZSBQKXY3SPLU2VDL2IEPL2ESOSUZNTX4N03KAZVLDULW2")
                    .appendQueryParameter(CLIENT2_PARAM,"LPKO4OQYJI1QWTIHFSSW420BXJFF0OZC4HTTYJYJBGGRHJ0C")
                    .appendQueryParameter(UNITS_PARAM, "20130815")
                    .appendQueryParameter(L1_PARAM, coordinates)
                    .build();
        InputStream is = null;

        try {
            URL url = new URL(builtUri.toString());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            int response = urlConnection.getResponseCode();

            is = urlConnection.getInputStream();
            String contentAsString = readIt(is);
            return contentAsString;

       }catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            return null;}

        finally {

            if (is != null) {
                try {
                    is.close();
                } catch (final IOException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
            }
    }
    public  String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    private static ArrayList<FoursquareVenue> parseFoursquare(final String response) {

        ArrayList<FoursquareVenue> temp = new ArrayList<FoursquareVenue>();
        try {

            // make an jsonObject in order to parse the response
            JSONObject jsonObject = new JSONObject(response);

            // make an jsonObject in order to parse the response
            if (jsonObject.has("response")) {
                if (jsonObject.getJSONObject("response").has("venues")) {
                    JSONArray jsonArray = jsonObject.getJSONObject("response").getJSONArray("venues");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        FoursquareVenue poi = new FoursquareVenue();
                        if (jsonArray.getJSONObject(i).has("name")) {
                            poi.setName(jsonArray.getJSONObject(i).getString("name"));

                            if (jsonArray.getJSONObject(i).has("location")) {
                                if (jsonArray.getJSONObject(i).getJSONObject("location").has("address")) {
                                    if (jsonArray.getJSONObject(i).getJSONObject("location").has("city")) {
                                        poi.setCity(jsonArray.getJSONObject(i).getJSONObject("location").getString("city"));
                                    }
                                    if (jsonArray.getJSONObject(i).has("categories")) {
                                        if (jsonArray.getJSONObject(i).getJSONArray("categories").length() > 0) {
                                            if (jsonArray.getJSONObject(i).getJSONArray("categories").getJSONObject(0).has("icon")) {
                                                poi.setCategory(jsonArray.getJSONObject(i).getJSONArray("categories").getJSONObject(0).getString("name"));
                                            }
                                        }
                                    }

                                }
                            }
                        }
                        temp.add(poi);
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<FoursquareVenue>();
        }
        return temp;

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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
            Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
