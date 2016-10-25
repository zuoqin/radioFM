package com.nxcast.stations.il.fm100;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.nxcast.stations.il.fm100.busEvents.NotificationBusEvent;

import org.greenrobot.eventbus.EventBus;

/************************************************
 * BroadcastReciever which recives button presses on Notification and send out actions threw bus events
 ************************************************/

public class NotificationBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationBusEvent event = null;
        String action = intent.getAction();

        Log.i("100fm", "action ");

        if(action.equalsIgnoreCase("com.example.hpuser.rad100fm.ACTION_PAUSE")) {
            event = new NotificationBusEvent("pause");

        }
        else if(action.equalsIgnoreCase("com.example.hpuser.rad100fm.ACTION_PLAY")){
            event = new NotificationBusEvent("play");
        }
        else if(action.equalsIgnoreCase("com.example.hpuser.rad100fm.ACTION_DELETE")){
            event = new NotificationBusEvent("delete");
        }
        EventBus.getDefault().post(event);

    }
}