package com.cput.datacollection.reporting;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.cput.datacollection.reporting.databinding.ActivityMainBinding;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private DataCollectionDBHelper dbDataCollection;
    String jsonFileString;
    private List<LinksToDownload> links;
    Gson gson = new Gson();
    private EditText requestUrlsEditor = null;
    private Button requestUrlButton = null;
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        jsonFileString = Utils.getJsonFromAssets(getApplicationContext(), "1000_links_to_download.json");

        if(requestUrlsEditor == null)
        {
            requestUrlsEditor = (EditText)findViewById(R.id.get_urls_input);
        }

        dbDataCollection = new DataCollectionDBHelper(MainActivity.this);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date dateStart = new Date();
                Log.v("Process Start *******: ", formatter.format(dateStart));
                String reqUrl =  requestUrlsEditor.getText().toString();

                if (TextUtils.isEmpty(reqUrl)) {
                    Snackbar.make(view, "Input one of the comma separated values", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }

                Type listLinks = new TypeToken<List<LinksToDownload>>() { }.getType();
                List<LinksToDownload> data = gson.fromJson(jsonFileString, listLinks);

                try {
                    data = data.stream().filter(f -> f.id == Integer.parseInt(reqUrl)).collect(Collectors.toList());
                }
                catch(Exception ex) {
                    Snackbar.make(view, "Input invalid. Enter one of the numbers shown", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }

                for (int i = 0; i < data.size(); i++) {
                    Log.v("Process Data: ", String.valueOf(data.get(i).data.size()));
                    for (int j = 0; j < data.get(i).data.size(); j++) {
                        String urlToGet = "https://" + data.get(i).data.get(j).website;
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlToGet));
                        // make explicit
                        intent.setPackage("com.android.chrome");
                        intent.setComponent(new ComponentName("com.android.chrome","com.google.android.apps.chrome.IntentDispatcher"));
                        // end make explicit

                        startActivity(intent);
                    }
                }

                Snackbar.make(view, "Busy Processing", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                Date dateEnd = new Date();

                long difference_In_Time = dateEnd.getTime() - dateStart.getTime();
                Long seconds = TimeUnit.MILLISECONDS.toMillis(difference_In_Time);

                Log.v("Process Difference: ", seconds.toString());
                Log.v("Process End *******: ", formatter.format(dateEnd));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}