package com.example.userdetails;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.example.userdetails.model.Results;
import com.example.userdetails.model.Users;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity implements CustomAdapter.CustomAdapterListener {

    Gson gson;
    String jsonResults;
    Users users;
    List<Results> unmanagedResults;
    CustomRealmAdapter adapter;
    RecyclerView recyclerView;
    SearchView searchView;
    File dir;
    Snackbar snackbar;
    Realm realm;
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        users = new Users();
        dir = getExternalFilesDir("userdetails/db");
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().name("usersrealm.realm").
                directory(dir).build();
        Realm.setDefaultConfiguration(config);

        setupToolbar();
        setupNavigationDrawer();
        setupRecyclerView();

        //Pull to refresh
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ConnectivityManager cm =
                        (ConnectivityManager) getBaseContext()
                                .getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();

                if (isConnected == false) {
                    snackbar = Snackbar.make(findViewById(android.R.id.content),
                            R.string.offline, Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else
                    new GsonDeserializer().execute();
                pullToRefresh.setRefreshing(false);
            }
        });
    }

    private void setupToolbar() {
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setTitle(R.string.app_name);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
    }

    private void setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.refresh_list_nav:
                                ConnectivityManager cm =
                                        (ConnectivityManager) getBaseContext()
                                                .getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                                boolean isConnected = activeNetwork != null &&
                                        activeNetwork.isConnectedOrConnecting();
                                if (isConnected == false) {
                                    snackbar = Snackbar.make(findViewById(android.R.id.content),
                                            R.string.offline, Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                } else
                                    new GsonDeserializer().execute();
                                break;

                            case R.id.delete_list_nav:
                                deleteAlertDialog();
                                break;
                            case R.id.app_info:
                                appInfoActivity();
                        }
                        drawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @Override
    public void onSelected(String item) {

    }

    class GsonDeserializer extends AsyncTask<String, Void, Users> {

        @Override
        protected void onPreExecute() {
            gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        }

        @Override
        protected Users doInBackground(String... strings) {

            Realm realm = Realm.getDefaultInstance();
            Users results = new Users();

            try {
                URL url = new URL("https://randomuser.me/api/?results=10");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    byte[] contents = new byte[1024];
                    int bytesRead = 0;
                    jsonResults = "";
                    while ((bytesRead = in.read(contents)) != -1) {
                        jsonResults += new String(contents, 0, bytesRead);
                    }
                    Type resultType = new TypeToken<Users>() {
                    }.getType();
                    results = gson.fromJson(jsonResults, resultType);
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            final Users finalResults = results;
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for(Results result : finalResults.getResults()) {
                        Number currentIdNum = realm.where(Results.class)
                                .max("dbId");
                        int nextId;
                        if(currentIdNum == null) {
                            nextId = 1;
                        } else {
                            nextId = currentIdNum.intValue() + 1;
                        }
                            result.setDbId(nextId);
                            realm.insert(result);
                    }
                }
            });
            return results;
        }

        @Override
        protected void onPostExecute(Users results) {
            users = new Users();
            users.setResults(new ArrayList<>());
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<Results> queryResults;
                    queryResults = realm
                            .where(Results.class)
                            .findAll()
                            .sort("dbId", Sort.DESCENDING);
                    adapter = new CustomRealmAdapter
                            (queryResults, true);
                }
            });
            realm.close();
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                adapter.getFilter().filter(query);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.action_search:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recyclerView.setAdapter(null);
        realm.close();
    }

    private void setupRecyclerView() {
        realm = Realm.getDefaultInstance();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomRealmAdapter(realm.where(Results.class).findAll(), true);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }

    public void deleteAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(R.string.cancel_alert_msg)
                .setTitle(R.string.cancel_alert_title);

        // Add the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.deleteAll();
                    }
                });
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void appInfoActivity(){
        Intent intent = new Intent(this, AppInfo.class);
        startActivity(intent);
    }

}