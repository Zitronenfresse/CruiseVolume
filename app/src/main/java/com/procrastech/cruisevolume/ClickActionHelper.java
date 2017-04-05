package com.procrastech.cruisevolume;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by IEnteramine on 05.04.2017.
 */

public class ClickActionHelper {

    public static void startActivity(String className, Bundle extras, Context context){
        Class cls = null;
        try {
            cls = Class.forName(className);
        }catch(ClassNotFoundException e){
            //means you made a wrong input in firebase console
            return;
        }
        Intent i = new Intent(context, cls);
        i.putExtras(extras);
        context.startActivity(i);
    }
}
