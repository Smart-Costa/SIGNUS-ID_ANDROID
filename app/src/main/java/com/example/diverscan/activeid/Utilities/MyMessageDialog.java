package com.example.diverscan.activeid.Utilities;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.diverscan.activeid.R;


public class MyMessageDialog {
    public static AlertDialog alertDialog;

    public MyMessageDialog() {
    }

    public static boolean showAlertDialogAsk(Activity activity, String title, String message,
                                             final Runnable responseOK, final Runnable responseCancel){

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_notificaciones, null);
        TextView txvTitulo = view.findViewById(R.id.txvTitleMessageDialog);
        TextView txvMessage = view.findViewById(R.id.txvMessageDialog);
        txvTitulo.setText(title);
        txvMessage.setText(message);
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                responseOK.run();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                responseCancel.run();
            }
        });
        builder.setIcon(android.R.drawable.stat_sys_warning);
        alertDialog = builder.show();

        return  true;
    }

    public static boolean showUniqueAlertDialogAsk(Activity activity, String title, String message,
                                                   final Runnable responseOK){

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_notificaciones, null);
        TextView txvTitulo = view.findViewById(R.id.txvTitleMessageDialog);
        TextView txvMessage = view.findViewById(R.id.txvMessageDialog);
        txvTitulo.setText(title);
        txvMessage.setText(message);
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                responseOK.run();
            }
        });
        builder.setIcon(android.R.drawable.stat_sys_warning);
        alertDialog = builder.show();

        return  true;
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
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setPositiveButton(_boton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setIcon(android.R.drawable.stat_sys_warning);
        alertDialog = builder.show();

        return  true;
    }

}
