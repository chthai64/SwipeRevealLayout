package com.chauthai.swipereveallayoutdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_recycler_view:
                startActivity(new Intent(this, RecyclerDemoActivity.class));
                return true;

            case R.id.action_list_view:
                startActivity(new Intent(this, ListDemoActivity.class));
                return true;

            case R.id.action_grid_view:
                startActivity(new Intent(this, GridActivity.class));
                return true;
        }

        return false;
    }

    public void layoutOneOnClick(View v) {
        Toast.makeText(MainActivity.this, "Layout 1 clicked", Toast.LENGTH_SHORT).show();
    }

    public void layoutTwoOnClick(View v) {
        Toast.makeText(MainActivity.this, "Layout 2 clicked", Toast.LENGTH_SHORT).show();
    }

    public void layoutThreeOnClick(View v) {
        Toast.makeText(MainActivity.this, "Layout 3 clicked", Toast.LENGTH_SHORT).show();
    }

    public void layoutFourOnClick(View v) {
        Toast.makeText(MainActivity.this, "Layout 4 clicked", Toast.LENGTH_SHORT).show();
    }

    public void moreOnClick(View v) {
        Toast.makeText(MainActivity.this, "More clicked", Toast.LENGTH_SHORT).show();
    }

    public void deleteOnClick(View v) {
        Toast.makeText(MainActivity.this, "Delete clicked", Toast.LENGTH_SHORT).show();
    }

    public void archiveOnClick(View v) {
        Toast.makeText(MainActivity.this, "Archive clicked", Toast.LENGTH_SHORT).show();
    }

    public void helpOnClick(View v) {
        Toast.makeText(MainActivity.this, "Help clicked", Toast.LENGTH_SHORT).show();
    }

    public void searchOnClick(View v) {
        Toast.makeText(MainActivity.this, "Search clicked", Toast.LENGTH_SHORT).show();
    }

    public void starOnClick(View v) {
        Toast.makeText(MainActivity.this, "Star clicked", Toast.LENGTH_SHORT).show();
    }
}
