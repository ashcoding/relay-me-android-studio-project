package com.tinywebgears.relayme.model;

import com.tinywebgears.relayme.R;

public enum IssueCategory
{
    CRASH(R.string.lbl_contact_support_dialog_category_crash), GMAIL_OAUTH_SETUP(
            R.string.lbl_contact_support_dialog_category_gmail_oauth), FORWARDING(
            R.string.lbl_contact_support_dialog_category_text_forwarding), REPLYING(
            R.string.lbl_contact_support_dialog_category_replying), DUPLICATION(
            R.string.lbl_contact_support_dialog_category_duplicates), UPGRADE(
            R.string.lbl_contact_support_dialog_category_billing), OTHER(
            R.string.lbl_contact_support_dialog_category_other);

    private final int textResourceId;

    IssueCategory(int textResourceId)
    {
        this.textResourceId = textResourceId;
    }

    public int getTextResourceId()
    {
        return textResourceId;
    }
}
