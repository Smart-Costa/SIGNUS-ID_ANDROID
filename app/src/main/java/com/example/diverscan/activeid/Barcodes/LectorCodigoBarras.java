package com.example.diverscan.activeid.Barcodes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Set;

public class LectorCodigoBarras  {


    Intent i = new Intent();

    String scannerInputPlugin = "com.symbol.datawedge.api.ACTION";
    String extraData = "com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN";

    public void onResume(){

        i.setAction(scannerInputPlugin);
        i.putExtra(extraData, "DISABLE:PLUGIN");
        this.sendBroadcast(i);
    }

    public void onReceive(Context context, Intent intent){

          String command = i.getStringExtra("COMMAND");
          String commandidentifier = i.getStringExtra("COMMAND_IDENTIFIER");
          String result = i.getStringExtra("RESULT");

          Bundle bundle = new Bundle();
          String resultInfo = "";
          if(i.hasExtra("RESULT_INFO")){
              bundle = i.getBundleExtra("RESULT_INFO");
              Set<String> keys = bundle.keySet();
              for(String key : keys){
                  resultInfo += key + ": "+ bundle.getString(key) + "\n";
              }
          }

          String text = "Command: "+command+"\n" +
                  "Result: " +result+"\n" +
                  "Result Info: " +resultInfo + "\n" +
                  "CID:"+commandidentifier;

          Toast.makeText(context, text, Toast.LENGTH_LONG).show();
      }

    private void sendBroadcast(Intent i) {
    }


}
