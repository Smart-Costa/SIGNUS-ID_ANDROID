package com.example.diverscan.activeid.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.diverscan.activeid.R;

import java.util.Timer;
import java.util.TimerTask;

public class AlertasPersonalizadas {

    public static AlertDialog alertDialog;

    public AlertasPersonalizadas(){}

    public static boolean showAlertDialogAsk(Activity activity, String title, String message,
                                             final Runnable responseOK, final Runnable responseCancel) {

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_notificaciones, null);
        TextView txvTitulo = view.findViewById(R.id.txvTitleMessageDialog);
        TextView txvMessage = view.findViewById(R.id.txvMessageDialog);
        txvTitulo.setText(title);
        txvMessage.setText(message);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setPositiveButton(Html.fromHtml("<font color='#CC2C1F'>Aceptar</font>"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                responseOK.run();
            }
        });
        builder.setNegativeButton(Html.fromHtml("<font color='#CC2C1F'>Cancelar</font>"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                responseCancel.run();
            }
        });
        builder.setIcon(android.R.drawable.stat_sys_warning);
        alertDialog = builder.show();

        return true;
    }
    public static boolean showInfoDialogAsk(Activity activity, String title, String message,
                                            final Runnable responseOK, final Runnable responseCancel) {

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_notificacion_aceptada, null);
        TextView txvTitulo = view.findViewById(R.id.txvTitleMessageDialog);
        TextView txvMessage = view.findViewById(R.id.txvMessageDialog);
        txvTitulo.setText(title);
        txvMessage.setText(message);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setPositiveButton(Html.fromHtml("<font color='#04A54D'>Aceptar</font>"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                responseOK.run();
            }
        });
        builder.setNegativeButton(Html.fromHtml("<font color='#04A54D'>Cancelar</font>"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                responseCancel.run();
            }
        });
        builder.setIcon(android.R.drawable.stat_sys_warning);
        alertDialog = builder.show();

        return true;
    }
    public static boolean showUniqueAlertDialogAsk(Activity activity, String title, String message,
                                                   final Runnable responseOK) {


        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_notificaciones, null);
        TextView txvTitulo = view.findViewById(R.id.txvTitleMessageDialog);
        TextView txvMessage = view.findViewById(R.id.txvMessageDialog);
        txvTitulo.setText(title);
        txvMessage.setText(message);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                responseOK.run();
            }
        });
        builder.setIcon(android.R.drawable.stat_sys_warning);
        alertDialog = builder.show();

        return true;
    }


    public static boolean showAlertDialog(Activity activity, String title, String message, boolean conexion) {


        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_notificaciones, null);
        TextView txvTitulo = view.findViewById(R.id.txvTitleMessageDialog);
        TextView txvMessage = view.findViewById(R.id.txvMessageDialog);
        txvTitulo.setText(title);
        txvMessage.setText(message);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        if (conexion) {
            builder.setIcon(R.drawable.ic_offline);
        } else {
            builder.setIcon(android.R.drawable.stat_sys_warning);
        }

        alertDialog = builder.show();

        return true;
    }


    public static boolean showAlertDialog(Activity activity, String title, String message){
        String _boton;

        _boton = "Aceptar";

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_notificaciones, null);
        TextView txvTitulo = view.findViewById(R.id.txvTitleMessageDialog);
        TextView txvMessage = view.findViewById(R.id.txvMessageDialog);
        txvTitulo.setText(title);
        txvMessage.setText(message);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setPositiveButton(Html.fromHtml("<font color='#CC2C1F'>Aceptar</font>"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setIcon(android.R.drawable.stat_sys_warning);
        alertDialog = builder.show();

        return  true;
    }
    public static boolean showAcceptDialog(Activity activity, String title, String message) {

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_notificacion_aceptada, null);
        TextView txvTitulo = view.findViewById(R.id.txvTitleMessageDialog);
        TextView txvMessage = view.findViewById(R.id.txvMessageDialog);
        txvTitulo.setText(title);
        txvMessage.setText(message);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setPositiveButton(Html.fromHtml("<font color='#04A54D'>Aceptar</font>"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setIcon(R.drawable.ic_aceptada);
        alertDialog = builder.show();

        return true;
    }
    public static boolean showAcceptDialogTimer(Activity activity, String title, String message) {

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_notificacion_aceptada, null);
        TextView txvTitulo = view.findViewById(R.id.txvTitleMessageDialog);
        TextView txvMessage = view.findViewById(R.id.txvMessageDialog);
        txvTitulo.setText(title);
        txvMessage.setText(message);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setIcon(R.drawable.ic_aceptada);
        alertDialog = builder.create();
        alertDialog.show();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                alertDialog.dismiss();
                timer.cancel();
            }
        }, 4000);
        return true;
    }

    public static boolean showAlertDialogTimer(Activity activity, String title, String message){

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_notificaciones, null);
        TextView txvTitulo = view.findViewById(R.id.txvTitleMessageDialog);
        TextView txvMessage = view.findViewById(R.id.txvMessageDialog);
        txvTitulo.setText(title);
        txvMessage.setText(message);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setIcon(android.R.drawable.stat_sys_warning);
        alertDialog = builder.create();
        alertDialog.show();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                alertDialog.dismiss();
                timer.cancel();
            }
        }, 4000);
        return  true;
    }
}
