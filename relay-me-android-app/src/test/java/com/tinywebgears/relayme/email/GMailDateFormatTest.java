package com.tinywebgears.relayme.email;

import java.util.Date;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class GMailDateFormatTest
{
    @Test
    public void testDate() throws Exception
    {
        String date1Str = "Thu, 29 Aug 2013 18:07:23 +0430";
        Date date1 = ImapEmailReceiver.parseGmailDate(date1Str);
        assertEquals(date1.getTime(), 1377783443000l);
        String date2Str = "20 Jun 2014 02:42:12 -0700";
        Date date2 = ImapEmailReceiver.parseGmailDate(date2Str);
        assertEquals(date2.getTime(), 1403257332000l);
    }
}
