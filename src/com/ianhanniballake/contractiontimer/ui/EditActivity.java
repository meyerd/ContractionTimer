package com.ianhanniballake.contractiontimer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.analytics.tracking.android.EasyTracker;
import com.ianhanniballake.contractiontimer.BuildConfig;
import com.ianhanniballake.contractiontimer.R;
import com.ianhanniballake.contractiontimer.actionbar.ActionBarFragmentActivity;

/**
 * Stand alone activity used to view the details of an individual contraction
 */
public class EditActivity extends ActionBarFragmentActivity
{
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);
		if (findViewById(R.id.edit) == null)
		{
			// A null details view means we no longer need this activity
			finish();
			return;
		}
		if (savedInstanceState == null)
			showFragment();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		if (Intent.ACTION_INSERT.equals(getIntent().getAction()))
			getMenuInflater().inflate(R.menu.activity_add, menu);
		else
			getMenuInflater().inflate(R.menu.activity_edit, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				Intent intent;
				if (Intent.ACTION_INSERT.equals(getIntent().getAction()))
				{
					if (BuildConfig.DEBUG)
						Log.d(getClass().getSimpleName(), "Add selected home");
					EasyTracker.getTracker().trackEvent("Add", "Home", "", 0L);
					intent = new Intent(this, MainActivity.class);
				}
				else
				{
					if (BuildConfig.DEBUG)
						Log.d(getClass().getSimpleName(), "Edit selected home");
					EasyTracker.getTracker().trackEvent("Edit", "Home", "", 0L);
					intent = new Intent(Intent.ACTION_VIEW, getIntent()
							.getData()).setClass(this, ViewActivity.class);
				}
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		getActionBarHelper().setDisplayHomeAsUpEnabled(true);
		EasyTracker.getInstance().activityStart(this);
		if (Intent.ACTION_INSERT.equals(getIntent().getAction()))
			EasyTracker.getTracker().trackView("Add");
		else
			EasyTracker.getTracker().trackView("Edit");
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	/**
	 * Creates and shows the fragment associated with the current contraction
	 */
	private void showFragment()
	{
		final EditFragment viewFragment = new EditFragment();
		// Execute a transaction, replacing any existing fragment
		// with this one inside the frame.
		final FragmentTransaction ft = getSupportFragmentManager()
				.beginTransaction();
		ft.replace(R.id.edit, viewFragment);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}
}
