package com.tinywebgears.relayme.view;

import java.text.DateFormat;
import java.util.Date;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.common.Constants;
import com.tinywebgears.relayme.contentprovider.LogEntriesContentProvider;
import com.tinywebgears.relayme.dao.DataBaseHelper;
import com.tinywebgears.relayme.service.LogStoreHelper;

@EFragment
public class ErrorsTabFragment extends TabFragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String TAG = ErrorsTabFragment.class.getName();

    // Transient state

    @ViewById(R.id.errorslist)
    ListView errorsListView;

    private SimpleCursorAdapter listViewAdapter;

    private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "ErrorsTabFragment.onCreate");
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.errors_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.errors, container, false);
        return view;
    }

    @Override
    public void onAttach(Activity activity)
    {
        Log.d(TAG, "ErrorsTabFragment.onAttach");
        super.onAttach(activity);

        listViewAdapter = new MyCursorAdapter(activity, R.layout.errorslistitem, null, new String[] {
                DataBaseHelper.TABLE_LOGENTRIES_COLUMN_DATE_UPDATED,
                DataBaseHelper.TABLE_LOGENTRIES_COLUMN_NUMBER_OF_OCCURRENCES,
                DataBaseHelper.TABLE_LOGENTRIES_COLUMN_BODY }, new int[] { R.id.errorslistitemheader,
                R.id.errorslistitemstatus, R.id.errorslistitemdetails }, 0);
        getLoaderManager().initLoader(LogEntriesContentProvider.UNIQUE_ID, null, this);
    }

    @Override
    public void onDetach()
    {
        Log.d(TAG, "ErrorsTabFragment.onDetach");
        super.onDetach();
    }

    @Override
    public void onResume()
    {
        Log.d(TAG, "ErrorsTabFragment.onResume");
        super.onResume();

        // Using activity is safe here.
        errorsListView.setAdapter(listViewAdapter);
        registerForContextMenu(errorsListView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.action_clear_errors:
            FlurryAgent.logEvent(Constants.FLURRY_EVENT_LOGS_DELETED);
            deleteAllLogs();
            Toast.makeText(applicationContext, R.string.lbl_errors_deleted, Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        Uri baseUri = LogEntriesContentProvider.CONTENT_URI;
        // Create and return a CursorLoader that will take care of creating a Cursor for the data being displayed.
        return new CursorLoader(applicationContext, baseUri, DataBaseHelper.TABLE_LOGENTRIES_ALL_COLUMNS,
                DataBaseHelper.TABLE_LOGENTRIES_COLUMN_VISIBLE_TO_USER + " = ?",
                new String[] { Integer.toString(DataBaseHelper.BOOLEAN_INT_VALUE_TRUE) },
                DataBaseHelper.TABLE_LOGENTRIES_COLUMN_DATE_UPDATED + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        // Swap in a new Cursor, returning the old Cursor.
        listViewAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        // Swap in a new Cursor, returning the old Cursor.
        listViewAdapter.swapCursor(null);
    }

    private void deleteAllLogs()
    {
        LogStoreHelper.deleteAllLogEntries(applicationContext);
    }

    protected static class RowViewHolder
    {
        public View mRowContents;
    }

    public class MyCursorAdapter extends SimpleCursorAdapter
    {
        public MyCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags)
        {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent)
        {
            View view = View.inflate(context, R.layout.errorslistitem, null);
            return view;
        }

        @Override
        public void setViewText(TextView v, String text)
        {
            if (v.getId() == R.id.errorslistitemstatus)
            {
                if (Integer.parseInt(text) > 1)
                {
                    v.setVisibility(View.VISIBLE);
                    v.setText(getString(R.string.lbl_error_header_repeats, text));
                }
                else
                    v.setVisibility(View.GONE);
            }
            else
            {
                String transformedText = text;
                if (v.getId() == R.id.errorslistitemheader)
                    transformedText = dateFormat.format(new Date(Long.parseLong(text)));
                super.setViewText(v, transformedText);
            }
        }

        @Override
        public void setViewImage(ImageView v, String value)
        {
            super.setViewImage(v, value);
        }
    }
}