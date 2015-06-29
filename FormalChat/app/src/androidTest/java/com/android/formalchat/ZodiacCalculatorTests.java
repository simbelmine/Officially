package com.android.formalchat;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

public class ZodiacCalculatorTests extends ApplicationTestCase<Application> {

    public ZodiacCalculatorTests() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public void testVariousZodiaSigns() {
        String[] dates = {
                        "22/12/1980", "25/12/1980", "19/01/1980",
                        "20/01/1980", "10/02/1980", "18/02/1980",
                        "19/02/1980", "25/02/1980", "20/03/1980",
                        "21/03/1980", "25/03/1980", "19/04/1980",
                        "20/04/1980", "25/04/1980", "20/05/1980",
                        "21/05/1999", "25/05/1999", "20/06/1999",
                        "21/06/1999", "25/06/1999", "22/07/1999",
                        "23/07/1999", "25/07/1999", "22/08/1999",
                        "23/08/1999", "25/08/1999", "22/09/1999",
                        "23/09/1999", "25/09/1999", "22/10/1999",
                        "23/10/1999", "25/10/1999", "21/11/1999",
                        "22/11/1999", "25/11/1999", "21/12/1999"
        };

        ZodiacSign[] signs = {
                ZodiacSign.CAPRICORN, ZodiacSign.CAPRICORN, ZodiacSign.CAPRICORN,
                ZodiacSign.AQUARIUS, ZodiacSign.AQUARIUS, ZodiacSign.AQUARIUS,
                ZodiacSign.PISCES, ZodiacSign.PISCES, ZodiacSign.PISCES,
                ZodiacSign.ARIES, ZodiacSign.ARIES, ZodiacSign.ARIES,
                ZodiacSign.TAURUS, ZodiacSign.TAURUS, ZodiacSign.TAURUS,
                ZodiacSign.GEMINI, ZodiacSign.GEMINI, ZodiacSign.GEMINI,
                ZodiacSign.CANCER, ZodiacSign.CANCER, ZodiacSign.CANCER,
                ZodiacSign.LEO, ZodiacSign.LEO, ZodiacSign.LEO,
                ZodiacSign.VIRGO, ZodiacSign.VIRGO, ZodiacSign.VIRGO,
                ZodiacSign.LIBRA, ZodiacSign.LIBRA, ZodiacSign.LIBRA,
                ZodiacSign.SCORPIO, ZodiacSign.SCORPIO, ZodiacSign.SCORPIO,
                ZodiacSign.SAGITTARIUS, ZodiacSign.SAGITTARIUS, ZodiacSign.SAGITTARIUS };

        ZodiacCalculator zodiaCalculator = new ZodiacCalculator(getContext());
        for(int i = 0; i < dates.length; i++) {
            String date = dates[i];
            assertEquals(signs[i], zodiaCalculator.calculateZodiacSign(date));
        }
    }
}
