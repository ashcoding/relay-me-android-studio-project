package com.tinywebgears.relayme.view;

import java.util.ArrayList;
import java.util.List;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.model.IssueCategory;
import com.tinywebgears.relayme.view.ContactSupportDialogFragment.ContactSupportDialogListener;

@EFragment(R.layout.dialog_contact_support)
public class ContactSupportDialogFragment extends AbstractDialogFragment<ContactSupportDialogListener>
{
    private static final String TAG = ContactSupportDialogFragment.class.getName();

    @ViewById(R.id.contactsupportdialogcrashed)
    TextView crashedTextView;

    @ViewById(R.id.contactsupportdialogheading)
    TextView headingTextView;

    @ViewById(R.id.issuecategory)
    Spinner issueCategorySpinner;

    @ViewById(R.id.supportnotes)
    EditText supportNotesEditText;

    // Transient fields

    private boolean crashed = false;

    public ContactSupportDialogFragment setCrashed(boolean crashed)
    {
        this.crashed = crashed;
        return this;
    }

    @Override
    protected Class<ContactSupportDialogListener> getListenerClass()
    {
        return ContactSupportDialogListener.class;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        int style = DialogFragment.STYLE_NORMAL, theme = 0;
        setStyle(style, theme);
    }

    @Override
    public void onResume()
    {
        Log.d(TAG, "ContactSupportDialogFragment.onResume");
        super.onResume();

        getDialog().setTitle(R.string.lbl_contact_support_dialog_title);
        crashedTextView.setVisibility(crashed ? View.VISIBLE : View.GONE);
        headingTextView.setVisibility(crashed ? View.GONE : View.VISIBLE);
        List<String> allCategories = new ArrayList<String>();
        int crashIndex = 0;
        int otherIndex = 0;
        int index = 0;
        for (IssueCategory category : IssueCategory.values())
        {
            allCategories.add(getActivity().getString(category.getTextResourceId()));
            if (category == IssueCategory.CRASH)
                crashIndex = index;
            else if (category == IssueCategory.OTHER)
                otherIndex = index;
            index++;
        }
        issueCategorySpinner.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, allCategories));
        if (crashed)
            issueCategorySpinner.setSelection(crashIndex);
        else
            issueCategorySpinner.setSelection(otherIndex);
    }

    @Click(R.id.canceldialog)
    public void onCancelDialogButtonClick(View v)
    {
        ContactSupportDialogFragment.this.dismiss();
    }

    @Click(R.id.sendlogs)
    public void onSendLogsButtonClick(View v)
    {
        String category = (String) issueCategorySpinner.getSelectedItem();
        String comments = supportNotesEditText.getText().toString();
        getListener().onFinishContactSupportDialog(ContactSupportDialogAction.SEND_LOGS, true, category, comments);
        ContactSupportDialogFragment.this.dismiss();
    }

    public enum ContactSupportDialogAction
    {
        WIKI, SEND_LOGS
    }

    public interface ContactSupportDialogListener
    {
        void onFinishContactSupportDialog(ContactSupportDialogAction action, boolean includeSettings, String category,
                String comments);
    }
}