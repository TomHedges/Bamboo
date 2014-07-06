package com.tomhedges.bamboo;

import com.tomhedges.bamboo.R;
import com.tomhedges.bamboo.fragments.DummySectionFragment;
import com.tomhedges.bamboo.fragments.LaunchFragment;
import com.tomhedges.bamboo.fragments.LoginFragment;
import com.tomhedges.bamboo.fragments.RetrieveOnlineFileFragment;
import com.tomhedges.bamboo.fragments.TestDatabaseFragment;
import com.tomhedges.bamboo.fragments.WeatherFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;

public class MainMenu extends FragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
	 * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
	 * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
	 * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
		// Create the adapter that will return a fragment for each of the three primary sections
		// of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main_menu, menu);
		return true;
	}


	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
	 * sections of the app.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			Fragment fragment = null;
			Bundle args = new Bundle();
			switch (i) {
			case 0:
				fragment = new DummySectionFragment();
				args.putString(DummySectionFragment.ARG_SECTION_NUMBER, "hello");
				fragment.setArguments(args);
				break;
			case 1:
				fragment = new LaunchFragment();
				break;
			case 2:
				fragment = new RetrieveOnlineFileFragment();
				break;
			case 3:
				fragment = new WeatherFragment();
				break;
			case 4:
				fragment = new TestDatabaseFragment();
				break;
			case 5:
				fragment = new LoginFragment();
				break;
			}
			//REPLACED
			//args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
			return fragment;
		}

		@Override
		public int getCount() {
			return 6;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0: return getString(R.string.title_section1).toUpperCase();
			case 1: return getString(R.string.title_section2).toUpperCase();
			case 2: return getString(R.string.title_section3).toUpperCase();
			case 3: return getString(R.string.title_section4).toUpperCase();
			case 4: return getString(R.string.title_section5).toUpperCase();
			case 5: return getString(R.string.title_section6).toUpperCase();
			}
			return null;
		}
	}
}
