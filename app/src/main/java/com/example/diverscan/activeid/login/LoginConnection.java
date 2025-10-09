package com.example.diverscan.activeid.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;

public class LoginConnection extends AsyncTask<Void, Void, Boolean>
{

    //private String IP = "196.40.23.183";
    public interface EntoncesHacer
    {
        void cuandoHayInternet();
        void cuandoNOHayInternet();
        void sincronizarInfo();
    }

    // variables necesarias, el dialogo de progreso, el contexto de la actividad
    // y una instancia de EntoncesHacer, donde se ejecutan las acciones.
    private ProgressDialog dialog;
    private Context context;
    private LoginConnection.EntoncesHacer accion;

    // Constructor, recibe el contexto de la actividad actual,
    // y la instancia de EntoncesHacer
    public LoginConnection(Context context, LoginConnection.EntoncesHacer accion)
    {
        this.context = context;
        this.accion = accion;
    }

    // corre en el Thread de la UI antes de empezar la tarea en segundo plano.
    // aquí aprovechamos y mostramos el progress...
    @Override
    protected void onPreExecute()
    {
        // preparamos el cuadro de dialogo
        dialog = new ProgressDialog(context);
        dialog.setMessage("Iniciando Sesión, por favor espere...");
        dialog.setCancelable(false);
        dialog.show();

        // llamamos al padre
        super.onPreExecute();
    }


    @Override
    protected Boolean doInBackground(Void... arg0) {
        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("ping -c 2 -w 4 10.14.2.130");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return false;
    }
    //*******************************************************************************
    // aquí de acuerdo al resultado llamaremos a uno u otro método
    // de la interface EntoncesHacer
    @Override
    protected void onPostExecute(Boolean resultado)
    {
        // llamamos al padre
        super.onPostExecute(resultado);
        // cerramos el cuadro de progreso
        dialog.dismiss();
        // de acuerdo al resultado del ping, se ejecuta una acción o la otra
        if (resultado)
        {
            accion.sincronizarInfo();
            accion.cuandoHayInternet();
        }
        else
        {
            accion.cuandoNOHayInternet();
        }
    }
}
