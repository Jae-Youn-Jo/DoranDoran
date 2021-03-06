package com.doraesol.dorandoran.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.doraesol.dorandoran.MainActivity;
import com.doraesol.dorandoran.MenuActivity;
import com.doraesol.dorandoran.R;
import com.doraesol.dorandoran.config.DataConfig;
import com.doraesol.dorandoran.config.ResultCode;
import com.doraesol.dorandoran.config.Server;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by YOONGOO on 2017-04-02.
 */

public class FCMService extends FirebaseMessagingService {

    private final String LOG_TAG = FCMService.class.getSimpleName();
    private final int GET_ACK_PACKET_CODE   = 1000;
    private final int GET_REQUEST_USER_ID   = 1001;
    private final int GET_MESSAGE           = 1002;
    private final int GET_FAMILY_TREE_DATA  = 1003;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(LOG_TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(LOG_TAG, "Message data payload: " + remoteMessage.getData());
        }



        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(LOG_TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());


        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        sendNotification(remoteMessage.getData().get("message"));
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MenuActivity.class);
        String recvMessage = "";

        try {
            recvMessage = getRequestMessage(messageBody, GET_MESSAGE);
        }catch (NullPointerException ex){
            Log.d(LOG_TAG, "NullPointerException : " + ex.toString());
        }

        String packet = getRequestMessage(messageBody, GET_ACK_PACKET_CODE);

        // 가계도를 요청한 경우
        if(packet.equals(Server.REQUEST_USER_FAMILYTREE)){
            intent.putExtra("REQUEST_CODE", Server.REQUEST_USER_FAMILYTREE);
            String from_user = getRequestMessage(messageBody, GET_REQUEST_USER_ID);
            intent.putExtra("FROM_USER", from_user);
        }
        // 가계도가 도착한 경우
        else if(packet.equals(Server.RESPONSE_USER_FAMILYTREE)){
            String json_data = getRequestMessage(messageBody, GET_FAMILY_TREE_DATA);
            String shared_user = getRequestMessage(messageBody, GET_REQUEST_USER_ID);
            // User 리스트를 저장한 후
            saveSharedUserList(shared_user);
            // 사용자의 데이터를 저장
            saveUserFamilyTreeInfo(shared_user, json_data);

            Log.d(LOG_TAG, "가계도 도착 ! " + json_data);
        }


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("도란도란")
                .setContentText(recvMessage)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    /* 0 : ACK_PACKET
     * 1 : REQUEST USER ID
     * 2 : MESSAGE
     */
    private String getRequestMessage(String paramMessage, int flag){
        String[] tokenMessage = paramMessage.split("#");

        if(flag == GET_ACK_PACKET_CODE){
            return tokenMessage[0];
        }
        else if(flag == GET_REQUEST_USER_ID){
            return tokenMessage[1];
        }
        else if(flag == GET_MESSAGE){
            return tokenMessage[1] + tokenMessage[2];
        }
        else if(flag == GET_FAMILY_TREE_DATA){
            return tokenMessage[3];
        }

        return null;
    }

    private void saveSharedUserList(String shared_user){
        // 기존에 공유 했던 사용자 목록을 불러와서
        String load_user_list = loadSharedUserList();

        String[] token_user_list = load_user_list.split("#");


        for(int i=0; i<token_user_list.length; i++){

            // 기존에 유저가 존재하는 경우 함수를 탈출
            if(token_user_list[i].equals(shared_user)){
                return;
            }
        }

        // 분류 토큰을 넣고 추가시킴
        load_user_list += '#' + shared_user;

        // 유저 리스트를 SharedPref 공간에 저장
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(DataConfig.SHARED_USER_LIST, load_user_list);
        editor.commit();

        Log.d(LOG_TAG, "shared user list : " + load_user_list);
    }

    private String loadSharedUserList(){
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // data1#data2#data3
        String shared_user_list = mPref.getString(DataConfig.SHARED_USER_LIST, DataConfig.EMPTY_DATA);

        return shared_user_list;
    }

    private void saveUserFamilyTreeInfo(String shared_user, String paramJsonData){
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(shared_user, paramJsonData);
        editor.commit();

        Log.d(LOG_TAG, "user : " + shared_user + "  json : " + paramJsonData);
    }
}
