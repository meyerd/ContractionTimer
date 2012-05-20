package com.ianhanniballake.contractiontimer.ui;

import java.util.Date;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ianhanniballake.contractiontimer.BuildConfig;
import com.ianhanniballake.contractiontimer.R;
import com.ianhanniballake.contractiontimer.analytics.AnalyticsManagerService;
import com.ianhanniballake.contractiontimer.appwidget.AppWidgetUpdateHandler;
import com.ianhanniballake.contractiontimer.provider.ContractionContract;

/**
 * Fragment showing the details of an individual contraction
 */
public class ViewFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor>
{
	/**
	 * Helper class used to store temporary references to list item views
	 */
	static class ViewHolder
	{
		/**
		 * TextView representing the duration of the contraction
		 */
		TextView duration;
		/**
		 * TextView representing the formatted end date of the contraction
		 */
		TextView endDate;
		/**
		 * TextView representing the formatted end time of the contraction
		 */
		TextView endTime;
		/**
		 * TextView representing the note attached to the contraction
		 */
		TextView note;
		/**
		 * TextView representing the formatted start date of the contraction
		 */
		TextView startDate;
		/**
		 * TextView representing the formatted start time of the contraction
		 */
		TextView startTime;
	}

	/**
	 * Creates a new Fragment to display the given contraction
	 * 
	 * @param contractionId
	 *            Id of the Contraction to display
	 * @return ViewFragment associated with the given id
	 */
	public static ViewFragment createInstance(final long contractionId)
	{
		final ViewFragment viewFragment = new ViewFragment();
		final Bundle args = new Bundle();
		args.putLong(BaseColumns._ID, contractionId);
		viewFragment.setArguments(args);
		return viewFragment;
	}

	/**
	 * Adapter to display the detailed data
	 */
	private CursorAdapter adapter;
	/**
	 * Id of the current contraction to show
	 */
	private long contractionId = 0;
	/**
	 * Handler for asynchronous deletes of contractions
	 */
	private AsyncQueryHandler contractionQueryHandler;
	/**
	 * Whether the current contraction is ongoing (i.e., not yet ended). Null
	 * indicates that we haven't checked yet, while true or false indicates
	 * whether the contraction is ongoing
	 */
	private Boolean isContractionOngoing = null;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		final Context applicationContext = getActivity()
				.getApplicationContext();
		contractionQueryHandler = new AsyncQueryHandler(getActivity()
				.getContentResolver())
		{
			@Override
			protected void onDeleteComplete(final int token,
					final Object cookie, final int result)
			{
				AppWidgetUpdateHandler.createInstance().updateAllWidgets(
						applicationContext);
				final Activity activity = getActivity();
				if (activity != null)
					activity.finish();
			}
		};
		adapter = new CursorAdapter(getActivity(), null, 0)
		{
			@Override
			public void bindView(final View view, final Context context,
					final Cursor cursor)
			{
				final Object viewTag = view.getTag();
				ViewHolder holder;
				if (viewTag == null)
				{
					holder = new ViewHolder();
					holder.startTime = (TextView) view
							.findViewById(R.id.start_time);
					holder.startDate = (TextView) view
							.findViewById(R.id.start_date);
					holder.endTime = (TextView) view
							.findViewById(R.id.end_time);
					holder.endDate = (TextView) view
							.findViewById(R.id.end_date);
					holder.duration = (TextView) view
							.findViewById(R.id.duration);
					holder.note = (TextView) view.findViewById(R.id.note);
					view.setTag(holder);
				}
				else
					holder = (ViewHolder) viewTag;
				String timeFormat = "hh:mm:ssaa";
				if (DateFormat.is24HourFormat(context))
					timeFormat = "kk:mm:ss";
				final int startTimeColumnIndex = cursor
						.getColumnIndex(ContractionContract.Contractions.COLUMN_NAME_START_TIME);
				final long startTime = cursor.getLong(startTimeColumnIndex);
				holder.startTime.setText(DateFormat.format(timeFormat,
						startTime));
				final Date startDate = new Date(startTime);
				holder.startDate.setText(DateFormat
						.getDateFormat(getActivity()).format(startDate));
				final int endTimeColumnIndex = cursor
						.getColumnIndex(ContractionContract.Contractions.COLUMN_NAME_END_TIME);
				isContractionOngoing = cursor.isNull(endTimeColumnIndex);
				if (isContractionOngoing)
				{
					holder.endTime.setText(" ");
					holder.endDate.setText(" ");
					holder.duration
							.setText(getString(R.string.duration_ongoing));
				}
				else
				{
					final long endTime = cursor.getLong(endTimeColumnIndex);
					holder.endTime.setText(DateFormat.format(timeFormat,
							endTime));
					final Date endDate = new Date(endTime);
					holder.endDate.setText(DateFormat.getDateFormat(
							getActivity()).format(endDate));
					final long durationInSeconds = (endTime - startTime) / 1000;
					holder.duration.setText(DateUtils
							.formatElapsedTime(durationInSeconds));
				}
				getActivity().supportInvalidateOptionsMenu();
				final int noteColumnIndex = cursor
						.getColumnIndex(ContractionContract.Contractions.COLUMN_NAME_NOTE);
				final String note = cursor.getString(noteColumnIndex);
				holder.note.setText(note);
			}

			@Override
			public View newView(final Context context, final Cursor cursor,
					final ViewGroup parent)
			{
				// View is already inflated in onCreateView
				return null;
			}
		};
		if (getArguments() != null)
		{
			contractionId = getArguments().getLong(BaseColumns._ID, 0);
			if (contractionId != 0)
				getLoaderManager().initLoader(0, null, this);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args)
	{
		final Uri contractionUri = ContentUris.withAppendedId(
				ContractionContract.Contractions.CONTENT_ID_URI_PATTERN,
				contractionId);
		return new CursorLoader(getActivity(), contractionUri, null, null,
				null, null);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		// Only allow editing contractions that have already finished
		final boolean showEdit = isContractionOngoing != null
				&& !isContractionOngoing;
		final MenuItem editItem = menu.findItem(R.id.menu_edit);
		editItem.setEnabled(showEdit);
		editItem.setVisible(showEdit);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_view, container, false);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> data)
	{
		adapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data)
	{
		adapter.swapCursor(data);
		if (data.moveToFirst())
			adapter.bindView(getView(), getActivity(), data);
		getActivity().supportInvalidateOptionsMenu();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		final Uri uri = ContentUris.withAppendedId(
				ContractionContract.Contractions.CONTENT_ID_URI_BASE,
				contractionId);
		switch (item.getItemId())
		{
			case R.id.menu_edit:
				// isContractionOngoing should be non-null at this point, but
				// just in case
				if (isContractionOngoing == null)
					return true;
				if (BuildConfig.DEBUG)
					Log.d(getClass().getSimpleName(), "View selected edit");
				AnalyticsManagerService.trackEvent(getActivity(), "View",
						"Edit", Boolean.toString(isContractionOngoing));
				if (isContractionOngoing)
					Toast.makeText(getActivity(), R.string.edit_ongoing_error,
							Toast.LENGTH_SHORT).show();
				else
					startActivity(new Intent(Intent.ACTION_EDIT, uri));
				return true;
			case R.id.menu_delete:
				if (BuildConfig.DEBUG)
					Log.d(getClass().getSimpleName(), "View selected delete");
				AnalyticsManagerService.trackEvent(getActivity(), "View",
						"Delete");
				contractionQueryHandler.startDelete(0, 0, uri, null, null);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
