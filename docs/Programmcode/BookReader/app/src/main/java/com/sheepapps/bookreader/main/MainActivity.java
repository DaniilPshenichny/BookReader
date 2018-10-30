package com.sheepapps.bookreader.main;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;

import com.sheepapps.bookreader.R;
import com.sheepapps.bookreader.about.AboutFragment;
import com.sheepapps.bookreader.app.BookReaderApp;
import com.sheepapps.bookreader.blank.BlankBookFragment;
import com.sheepapps.bookreader.data.CurrentBook;
import com.sheepapps.bookreader.library.Fb2EpubReaderFragment;
import com.sheepapps.bookreader.library.LibraryFragment;
import com.sheepapps.bookreader.pdf.PdfFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Fragment mCurrentFragment;
    private MainActivityViewModel mViewModel;
    private NavigationView mNavigationView;
    private final String SAVED_FRAGMENT = "saved_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        CurrentBook.currentBook = new MutableLiveData<>();
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        mViewModel.getItemId().observe(this, this::selectMenuItem);
        setUpMenus();
        setUpNightModeApp();
        checkSavedState(savedInstanceState);
        subscribeCurrentBookChanged();
    }

    private void setUpMenus() {
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        Menu menu = mNavigationView.getMenu();
        MenuItem menuItem = menu.findItem(R.id.nav_toggle);
        View actionView = menuItem.getActionView();
        Switch switcher = actionView.findViewById(R.id.nightSwitch);
        switcher.setChecked(BookReaderApp.getInstance().getSharedPreferences().getBoolean("isDark", false));
        switcher.setOnClickListener(v -> {
            BookReaderApp.getInstance().getSharedPreferences().edit().putBoolean("isDark", ((Switch)v).isChecked()).apply();
            recreate();
        });
    }

    private void setUpNightModeApp() {
        if (BookReaderApp.getInstance().getSharedPreferences().getBoolean("isDark", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void checkSavedState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String fragmentClassName = savedInstanceState.getString(SAVED_FRAGMENT);
            if (fragmentClassName.equals(LibraryFragment.class.getName())) {
                selectFragment(LibraryFragment.newInstance());
            } else if (fragmentClassName.equals(AboutFragment.class.getName())) {
                selectFragment(AboutFragment.newInstance());
            } else if (fragmentClassName.equals(Fb2EpubReaderFragment.class.getName()) ||
                    fragmentClassName.equals(PdfFragment.class.getName())) {
                selectBookFragment();
            } else if (fragmentClassName.equals(BlankBookFragment.class.getName())) {
                selectFragment(BlankBookFragment.newInstance());
            }
        } else {
            selectBookFragment();
        }
    }

    private void selectMenuItem(int id) {
        switch (id) {
            case R.id.nav_current:
                selectBookFragment();
                break;

            case R.id.nav_library:
                selectFragment(LibraryFragment.newInstance());
                break;

            case R.id.nav_about:
                selectFragment(AboutFragment.newInstance());
                break;
            case R.id.nav_share:
                startShareIntent();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_about) {
            selectFragment(AboutFragment.newInstance());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        mViewModel.setItemId(item.getItemId());
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void startShareIntent() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = getResources().getString(R.string.share_intent);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Поделиться с помощью"));
    }

    public void selectFragment(Fragment fragment) {
        mCurrentFragment = fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    private void subscribeCurrentBookChanged() {
        CurrentBook.currentBook.observe(this, filePath -> {
            if (filePath != null) {
                mViewModel.saveLastBook();
                selectBookFragment();
            }
        });
    }

    private void selectBookFragment() {
        String filePath = mViewModel.getLastBook();
        if (filePath != null) {
            mNavigationView.setCheckedItem(R.id.nav_current);
            if (filePath.endsWith(".pdf")) {
                selectFragment(PdfFragment.newInstance(filePath));
            } else if (filePath.endsWith(".epub") || filePath.endsWith(".fb2")
                    || filePath.endsWith(".fb2.zip") || filePath.endsWith(".xhtml") ||
                    filePath.endsWith(".html") || filePath.endsWith(".htm")) {
                selectFragment(Fb2EpubReaderFragment.newInstance());
            }
        } else {
            selectFragment(BlankBookFragment.newInstance());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_FRAGMENT, mCurrentFragment.getClass().getName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CurrentBook.currentBook.removeObservers(this);
        CurrentBook.currentBook = null;
    }
}
