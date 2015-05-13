package com.sharemee.app.sharemee.activity;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.sharemee.app.sharemee.R;

/**
 * Created by user-laptop on 12/02/2015.
 */
public class BaseActivity extends ActionBarActivity {
    private DrawerLayout mDrawerLayout = null;
    private ListView mDrawerList = null;
    private String[] mDrawerItems;
    private ActionBarDrawerToggle mDrawerToggle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_layout);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerItems = getResources().getStringArray(R.array.left_drawer_array);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerList.setAdapter(new ArrayAdapter<String>(
                this, R.layout.drawer_list_item, mDrawerItems));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, R.drawable.ic_drawer,
                R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerOpened(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        handleIntent(getIntent());
    }


    @Override
    protected void onNewIntent(Intent intent) {

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //doMySearch(query);
            //use the query to search your data somehow
        }
    }
    public void doMySearch(String query){

        Log.d("Je fais un test :", query);
        Log.d("Je fais un test :", "CA MARCHE");
    }


    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_searchable, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);

        for (int index = 0; index < menu.size(); index++) {
            MenuItem menuItem = menu.getItem(index);
            if (menuItem != null) {
                // hide the menu items if the drawer is open
                menuItem.setVisible(!drawerOpen);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            switch (position) {
                case 0: {
                    Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                    startActivity(intent);
                    break;
                }
                case 1: {
                    Intent intent = new Intent(BaseActivity.this, MyProfileActivity.class);
                    startActivity(intent);
                    break;
                }
                case 2: {
                    Intent intent = new Intent(BaseActivity.this, MyObjectsActivity.class);
                    startActivity(intent);
                    break;
                }
                case 3: {
                    Intent intent = new Intent(BaseActivity.this, SearchableActivity.class);
                    startActivity(intent);
                    break;
                }
                case 4: {
                    Intent intent = new Intent(BaseActivity.this, AboutActivity.class);
                    startActivity(intent);
                    break;
                }
                default:
                    break;
            }
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }
}
