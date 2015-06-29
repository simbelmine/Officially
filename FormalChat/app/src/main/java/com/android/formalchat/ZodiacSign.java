package com.android.formalchat;

public enum ZodiacSign {
    AQUARIUS(R.drawable.zodiac_aquarius),
    PISCES(R.drawable.zodiac_pisces),
    ARIES(R.drawable.zodiac_aries),
    TAURUS(R.drawable.zodiac_taurus),
    GEMINI(R.drawable.zodiac_gemini),
    CANCER(R.drawable.zodiac_cancer),
    LEO(R.drawable.zodiac_leo),
    VIRGO(R.drawable.zodiac_virgo),
    LIBRA(R.drawable.zodiac_libra),
    SCORPIO(R.drawable.zodiac_scorpio),
    SAGITTARIUS(R.drawable.sagittarius),
    CAPRICORN(R.drawable.zodiac_capricorn);

    private final int imageId;

    ZodiacSign(int imageId) {
        this.imageId = imageId;
    }

    public int getImageId() {
        return imageId;
    }
}
