package pt.ipp.estg.lifespanqr;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import pt.ipp.estg.lifespanqr.ingredient.Ingredient;

public class IngredientCalendar {

    private static IngredientCalendar instance;
    private static Context mContext;
    private static ContentResolver cr;

    private IngredientCalendar(Context context){
        this.mContext = context;
        cr = mContext.getContentResolver();
    }

    public static void createInstance(MainActivity mainActivity) {
        instance = new IngredientCalendar(mainActivity);
    }

    public static IngredientCalendar getInstance(){
        return instance;
    }

    public static void deleteEvent(Ingredient ingredient){
        if(MainActivity.hasCalendarPermissions()) {
            Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, ingredient.getEventId());
            cr.delete(deleteUri, null, null);
        }
    }

    public static Ingredient addEvent(Ingredient ingredient) {
        if (MainActivity.hasCalendarPermissions()) {
            try {
                String myDate = ingredient.getExpiry() + " 00:00:00";
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                long millis = sdf.parse(myDate).getTime();

                if (!ingredientExistsInCalendar(ingredient)) {
                    String timeZone = TimeZone.getDefault().getID();
                    ContentValues values = new ContentValues();
                    values.put(CalendarContract.Events.DTSTART, millis);
                    values.put(CalendarContract.Events.DTEND, millis);
                    values.put(CalendarContract.Events.TITLE, "LifespanTAG: " + ingredient.getName() + " " + ingredient.getBrand());
                    values.put(CalendarContract.Events.DESCRIPTION, "Expiration date");
                    values.put(CalendarContract.Events.CALENDAR_ID, 1);
                    values.put(CalendarContract.Events.ALL_DAY, 1);
                    values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone);
                    Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                    long eventID = Long.parseLong(uri.getLastPathSegment());
                    ingredient.setEventId(eventID);
                    return ingredient;
                }
            }catch (Exception e){
                System.out.println(e.toString());
            }
        }
        return ingredient;
    }

    private static boolean ingredientExistsInCalendar(Ingredient ingredient) throws ParseException {
        String myDate = ingredient.getExpiry() + " 00:00:00";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        long millis = sdf.parse(myDate).getTime();

        String[] proj = new String[]{
                CalendarContract.Events.TITLE};
        Cursor cursor = CalendarContract.Instances
                .query(cr, proj, millis, millis, "LifespanTAG: " + ingredient.getName() + " " + ingredient.getBrand());

        if (cursor.getCount() > 0){
            return true;
        }
        return false;
    }
}
