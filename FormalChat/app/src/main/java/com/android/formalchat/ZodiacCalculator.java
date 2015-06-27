package com.android.formalchat;

import android.content.Context;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Sve on 6/26/15.
 */
public class ZodiacCalculator {
    private final static int DUMMY_POSITION = 22;
    private Context context;
    private String date;
    private String zodiacSign;

    public ZodiacCalculator(Context context, String date) {
        this.context = context;
        this.date = date;
        setZodiacSign();
    }

    public String getZodiacSign() {
        return zodiacSign;
    }

    public void setZodiacSign() {
        String[] zodiacSignsArray = this.context.getResources().getStringArray(R.array.zodiac_values);
        int zodiacalSignPosition = getZodiacSignPosition();
        if(zodiacalSignPosition != DUMMY_POSITION) {
            this.zodiacSign = zodiacSignsArray[zodiacalSignPosition];
        }
        else {
            this.zodiacSign = null;
        }
    }

    private int getZodiacSignPosition() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM", Locale.US);
        Date initialDate = stringToCalendarDate();
        String[] zodiacStartDates = this.context.getResources().getStringArray(R.array.zodiac_start_dates);
        String[] zodiacEndDates = this.context.getResources().getStringArray(R.array.zodiac_end_dates);

        int datesArraySize = 0;
        if(zodiacStartDates.length == zodiacEndDates.length) {
            datesArraySize = zodiacStartDates.length;
        }

        Date startDate;
        Date endDate;
        if(datesArraySize != 0 && initialDate != null) {
            for(int counter = 0; counter < datesArraySize; counter++) {
                try {
                    startDate = formatter.parse(zodiacStartDates[counter]);
                    endDate = formatter.parse(zodiacEndDates[counter]);

                    // Check if it's Capricorn - Corner Case
                    if(counter == datesArraySize-1) {
                        return counter;
                    }

                    if ((initialDate.after(startDate) || initialDate.equals(startDate))
                            && ((initialDate.before(endDate)) || initialDate.equals(endDate)))
                    {
                        return counter;
                    }
                }
                catch (ParseException ex) {
                    Log.e("formalchat", ex.getMessage());
                }
            }
        }
        return DUMMY_POSITION;
    }

    private Date stringToCalendarDate() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        SimpleDateFormat formatToReturn = new SimpleDateFormat("dd/MM", Locale.US);
        try {
            if (this.date != null) {
                Date dateToFormat = dateFormatter.parse(this.date);
                String dateString = formatToReturn.format(dateToFormat);
                Date dateToReturn = formatToReturn.parse(dateString);

                return dateToReturn;
            }
        }
        catch (ParseException ex) {
            Log.e("formalchat", ex.getMessage());
        }
        return null;
    }

}
