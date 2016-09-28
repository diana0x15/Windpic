package com.wind.windpic.tools;

import com.wind.windpic.WindpicApplication;
import com.wind.windpic.objects.Friend;
import com.wind.windpic.objects.Message;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

/**
 * Created by dianapislaru on 26/09/15.
 */
public class FriendsListComparator implements Comparator<Friend> {

    public int compare(Friend friend1, Friend friend2) {

        Date date1;
        Date date2;
        Map<String,String> lastMessage = friend1.getLastMessage();
        if (lastMessage == null) {
            Message message = WindpicApplication.DATABASE.getLastMessage(friend1.getId());
            if (message != null) {
                date1 = getDate(message.getDate());
            } else {
                return 0;
            }
        } else {
            date1 = getDate(lastMessage.get("date"));
        }
        lastMessage = friend2.getLastMessage();
        if (lastMessage == null) {
            Message message = WindpicApplication.DATABASE.getLastMessage(friend2.getId());
            if (message != null) {
                date2 = getDate(message.getDate());
            } else {
                return 0;
            }
        } else {
            date2 = getDate(lastMessage.get("date"));
        }

        if (date1 == null || date2 == null)
            return 0;

        return (date1.compareTo(date2)) * -1;
    }

    private Date getDate(String stringDate) {
        if (stringDate == null)
            return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date date = null;
        try {
            date = dateFormat.parse(stringDate);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        if (date == null) {
            Calendar calendar = Calendar.getInstance();
            return calendar.getTime();
        }
        return date;
    }
}
