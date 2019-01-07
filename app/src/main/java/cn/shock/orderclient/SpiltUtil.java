package cn.shock.orderclient;

import android.util.Log;

public class SpiltUtil {

    public static int getid(String str){
        int id = -1;

        String[] strings = str.split(" ");
        if(strings.length>=2) {
            id = new Integer(strings[1]);
        }
        Log.i("shockc","getid = "+id);
        return id;
    }
}
