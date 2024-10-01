package pt.ipp.estg.lifespanqr.ingredient;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
public class Ingredient implements Comparable<Ingredient> {

    @PrimaryKey(autoGenerate = true)
    public int id;
    @Ignore
    public String key;
    private static final int FIELDS = 3;
    private long eventId;
    private String name, brand;
    private String expiry;
    private boolean isConsumed;


    public Ingredient(){}

   @Ignore
    public Ingredient(String name, String brand, String expiry){
        this.name = name;
        this.brand = brand;
        this.expiry = expiry;
        this.isConsumed = false;
    }

    public Ingredient(String name, String brand, String expiry, long eventId){
        this.name = name;
        this.brand = brand;
        this.expiry = expiry;
        this.eventId = eventId;
        this.isConsumed = false;
    }

    public static Ingredient parse(@NonNull String rawValues){
        String[] values = rawValues.split("//");
        if (values.length == FIELDS){
            try {
                String name = values[0];
                String brand = values[1];
                String expiry = values[2];

                LocalDate.parse(expiry, DateTimeFormatter.ofPattern("dd/MM/yyyy")); // verifica se a data é válida
                return new Ingredient(name, brand, expiry);
            } catch (Exception e){
                return null;
            }
        }
        return null;
    }

    public boolean isConsumed() {
        return isConsumed;
    }

    public boolean isExpired(){
       return (LocalDate.now().compareTo(LocalDate.parse(expiry, DateTimeFormatter.ofPattern("dd/MM/yyyy")))>0);
    }

    public void setConsumed(boolean consumed) {
        isConsumed = consumed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getExpiry(){
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public static String buildDateString(int day, int month, int year){
        String monthStr = String.valueOf(month);
        String dayStr = String.valueOf(day);
        if (month < 10){
            monthStr = "0".concat(monthStr);
        }
        if(day < 10){
            dayStr = "0".concat(dayStr);
        }
        return String.format("%s/%s/%s", dayStr, monthStr, year);
    }

    @Override
    public String toString(){
        return String.format("%s//%s//%s//%s", name, brand, expiry, isConsumed);
    }

    public String toQRString(){
        return String.format("%s//%s//%s", name, brand, expiry);
    }

    public String toFriendlyString(){
        return String.format("Name: %s\nBrand: %s\nExpiry: %s", name, brand, expiry);
    }
    @Override
    public int compareTo(Ingredient o) {
        return LocalDate.parse(expiry, DateTimeFormatter.ofPattern("dd/MM/yyyy")).compareTo(
                LocalDate.parse(o.expiry, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public static int getFIELDS() {
        return FIELDS;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
