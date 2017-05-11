package com.example.myfirstapp;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

import static com.example.myfirstapp.BuildReminderActivity.REMINDER_OBJECT;


public class MainActivity extends AppCompatActivity {

    public static final String REMINDER_UPDATE = "reminderUpdate";
    public static final String REMINDER_NOTIFICATION = "reminderNotification";
    public static final String REMINDER_INDEX = "reminderIndex";
    private static final String PREF_NAME = "myPrefsFile";
    private ArrayList<Reminder> reminders = new ArrayList<Reminder>();
    private static final int BUTTONS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //instantiations
        Gson gson = new GsonBuilder().create();
        Reminder reminder;
        Intent intent = getIntent();
        SharedPreferences settings = getSharedPreferences(PREF_NAME, 0);

        //previous reminders in json format
        String objects = settings.getString("objects", "");
        //restore reminders to previous state
        Scanner scanner = new Scanner(objects);
        int index = 0;
        scanner.useDelimiter(Pattern.compile("[*]"));
        while (scanner.hasNext()){
            reminders.add(index, gson.fromJson(scanner.next(), Reminder.class));
            index++;
        }
        if (intent.hasExtra(REMINDER_NOTIFICATION)){
            reminders.remove(intent.getIntExtra(REMINDER_NOTIFICATION, -1));
        }
        if (intent.hasExtra(REMINDER_OBJECT)){
            //get new reminder
            reminder = gson.fromJson(intent.getStringExtra(REMINDER_OBJECT), Reminder.class);
            //add new reminder
            reminders.add(reminder);
            //put most urgent at top
            sort();
            setAlarm(reminder);
        }
        //make visible and add text
        updateButtons();
        System.out.println("starting ");

    }

    @Override
    public void onPause(){
        super.onPause();
        Gson gson = new GsonBuilder().create();
        //create and clear editor
        SharedPreferences settings = getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();

        String objects = "";
        for (int i = 0; i < reminders.size(); i++){
            objects += gson.toJson(reminders.get(i)) + "*";
        }
        if (objects.contains("*")) {
            objects = objects.substring(0, objects.length() - 1);
        }
        System.out.println("commiting " + objects);
        editor.putString("objects", objects);
        editor.commit();
    }

    public void updateButtons(){
        for (int i = 0; i < reminders.size(); i++){
            if (i >= BUTTONS){
                break;
            }
            Button button = getButton(i);
            button.setVisibility(View.VISIBLE);
            button.setText(reminders.get(i).getTitle() + reminders.get(i).getFormattedDate());
        }
    }

    private void setAlarm(Reminder reminder){
        int index = -1;
        for (int i = 0; i < reminders.size(); i++){
            if (reminders.get(i).equals(reminder)){
                index = i;
                break;
            }
        }
        Gson gson = new GsonBuilder().create();
        long timeDif = reminder.getDateDue().getTimeInMillis() - System.currentTimeMillis();
        System.out.println("time dif " + timeDif);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        myIntent.putExtra(REMINDER_UPDATE, gson.toJson(reminder));
        myIntent.putExtra(REMINDER_NOTIFICATION, index);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 5000  , pendingIntent);

    }

    public void down(View view){
        Reminder first = reminders.remove(0);
        reminders.add(first);
        updateButtons();
    }

    public void up(View view){
        Reminder last = reminders.get(reminders.size() - 1);
        for (int i = reminders.size() - 1; i > 0; i--){
            reminders.set(i, reminders.get(i - 1));
        }
        reminders.set(0, last);
        updateButtons();
    }

    public void sortButton(View view){
        System.out.println("sorting");
        sort();
        updateButtons();
    }

    private void sort(){
        for (int i = 1; i < reminders.size(); i++){
            Reminder x = reminders.get(i);
            int j = i - 1;
            while (j >= 0 && reminders.get(j).isGreaterThan(x)){
                reminders.set(j + 1, reminders.get(j));
                j = j - 1;
            }
            reminders.set(j + 1, x);
        }

    }

    private Button getButton(int index){
        switch (index){
            case 0:
                return (Button) findViewById(R.id.button0);
            case 1:
                return (Button) findViewById(R.id.button1);
            case 2:
                return (Button) findViewById(R.id.button2);
            case 3:
                return (Button) findViewById(R.id.button3);
            case 4:
                return (Button) findViewById(R.id.button4);
        }
        return null;
    }

    public void clearButtons(View view){
        reminders.clear();
        for (int i = 0; i < BUTTONS; i++){
            Button button = getButton(i);
            button.setVisibility(View.GONE);
        }
        SharedPreferences settings = getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();
    }

    public void buildReminder(View view){
        Intent intent = new Intent(this, BuildReminderActivity.class);
        startActivity(intent);
    }

    public void updateReminder(View view){
        Intent intent = new Intent(this, BuildReminderActivity.class);
        Gson gson = new GsonBuilder().create();
        switch(view.getId()){
            case R.id.button0:
                intent.putExtra(REMINDER_UPDATE, gson.toJson(reminders.get(0)));
                reminders.remove(0);
                break;
            case R.id.button1:
                intent.putExtra(REMINDER_UPDATE, gson.toJson(reminders.get(1)));
                reminders.remove(1);
                break;
            case R.id.button2:
                intent.putExtra(REMINDER_UPDATE, gson.toJson(reminders.get(2)));
                reminders.remove(2);
                break;
            case R.id.button3:
                intent.putExtra(REMINDER_UPDATE, gson.toJson(reminders.get(3)));
                reminders.remove(3);
                break;
            case R.id.button4:
                intent.putExtra(REMINDER_UPDATE, gson.toJson(reminders.get(4)));
                reminders.remove(4);
                break;
            default:
                throw new RuntimeException("Unknow button ID");
        }
        startActivity(intent);
    }
}