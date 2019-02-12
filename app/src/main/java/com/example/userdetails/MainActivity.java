package com.example.userdetails;

import android.app.SearchManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.example.userdetails.model.Name;
import com.example.userdetails.model.Users;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity implements CustomAdapter.CustomAdapterListener {

    Gson gson;
    String jsonResults;
    Users users;
    CustomAdapter adapter;
    RecyclerView recyclerView;
    ArrayList<Name> names;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        users = new Users();
        names = new ArrayList<>();

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().name("usersrealm.realm").build();
        Realm.setDefaultConfiguration(config);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setTitle("Random Friends");

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        recyclerView.hasFixedSize();
        new GsonDeserializer().execute();

        //Pull to refresh
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GsonDeserializer().execute();
                pullToRefresh.setRefreshing(false);
            }
        });
    }

    @Override
    public void onSelected(String item) {

    }

    class GsonDeserializer extends AsyncTask<String, Void, Users>{

        @Override
        protected void onPreExecute() {
            gson = new Gson();
        }

        @Override
        protected Users doInBackground(String... strings) {

            Users results = new Users();

           try {
                URL url = new URL("https://randomuser.me/api/?results=100");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    byte[] contents = new byte[1024];
                    int bytesRead = 0;
                    jsonResults = "";
                    while((bytesRead = in.read(contents)) != -1) {
                        jsonResults += new String(contents, 0, bytesRead);
                    }
                    Type resultType = new TypeToken<Users>(){}.getType();
                    results = gson.fromJson(jsonResults, resultType);
                } finally {
                    urlConnection.disconnect();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onPostExecute(Users results) {
            users = results;
            adapter = new CustomAdapter(getBaseContext(),
                    R.layout.list_item, users.getResults());
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
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
        int id = item.getItemId();
        if (id == R.id.action_search) {
            return true;
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
}