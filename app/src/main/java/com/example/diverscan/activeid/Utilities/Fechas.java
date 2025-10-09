package com.example.diverscan.activeid.Utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Fechas {
    public static String parserJSONDate(String date){
        date = date.replace("/Date(","").replace(")/","");
        String[] dateSplit = date.split("[+-]");
        Calendar MYCALENDAR = Calendar.getInstance();
        MYCALENDAR.setTimeInMillis(Long.parseLong(dateSplit[0]));
        MYCALENDAR.setTimeZone(TimeZone.getTimeZone(dateSplit[1]));

        SimpleDateFormat dateFormat;
        dateFormat   = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return dateFormat.format(MYCALENDAR.getTime());
    }
    /*
        @RequiresApi(api = Build.VERSION_CODES.O)
        public static String StringDateToJson(String Fecha) throws ParseException {

            SimpleDateFormat dateFormat;
            Date date;
            if(app.glo_idioma.equals("esp")){
                dateFormat   = new SimpleDateFormat("dd-MM-yyyy");
                date =  dateFormat.parse(Fecha);
            }else{
                String[] FechaSeparada = Fecha.split("[-]");
                dateFormat   = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
                date =  dateFormat.parse( FechaSeparada[0]+"-"+ FechaSeparada[1]+"-"+ FechaSeparada[2]);
           // }

            Calendar MyCalendar = Calendar.getInstance();
            MyCalendar.setTime(date);
            long millis = MyCalendar.getTimeInMillis();

            String timeZone = MyCalendar.getTimeZone().getID();
            String hora = ZoneId.of(timeZone).getRules().getOffset(Instant.now()).toString();
            String[] ZonaSplit = hora.split("[:]");
            String ZonaHoraria = ZonaSplit[0] + ZonaSplit[1];

            String JsonFormatter = "/Date("+Long.toString(millis)+ZonaHoraria+")/";
            return JsonFormatter;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public static String DateToJson(Date Fecha){
            Calendar MyCalendar = Calendar.getInstance();
            MyCalendar.setTime(Fecha);
            long millis = MyCalendar.getTimeInMillis();

            String timeZone = MyCalendar.getTimeZone().getID();
            String hora = ZoneId.of(timeZone).getRules().getOffset(Instant.now()).toString();
            String[] ZonaSplit = hora.split("[+-:]");
            String ZonaHoraria = ZonaSplit[0] + ZonaSplit[1];

            String JsonFormatter = "/Date("+Long.toString(millis)+"-"+ZonaHoraria+")/";

            return JsonFormatter;
        }
    */
    public static String ConvertirFechaIdioma(Date Fecha){
        SimpleDateFormat dateFormat;

       // if(app.glo_idioma.equals("esp")){
       //     dateFormat   = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        //}else{
            dateFormat   = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
       // }
        return dateFormat.format(Fecha);
    }

    public static Date EnviarFechaWCF(String Fecha) throws ParseException {
        SimpleDateFormat dateFormat;
        Date date;
      //  if(app.glo_idioma.equals("esp")){
       //     dateFormat   = new SimpleDateFormat("dd-MM-yyyy");
      //      date =  dateFormat.parse(Fecha);
      //  }else{
            dateFormat   = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            date =  dateFormat.parse(Fecha);
     //   }
        return date;
    }

    public static String FechaActual(){
        SimpleDateFormat dateFormat;
        Date date = new Date();
       /* if(app.glo_idioma.equals("esp")){
            dateFormat   = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        }else{
       */     dateFormat   = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        //}
        String fecha = dateFormat.format(date);

        return fecha;
    }


    public static int fechora_mes_int(String fh) {

        try {
           /* if(app.glo_idioma.equals("esp")){
                return Integer.valueOf(fh.substring(3, 5));

            }else{
          */      return Integer.valueOf(fh.substring(3,5));
           // }

        } catch (Exception exc) {

        }
        return -1;
    }
    //----------------------------------
    public static int fechora_dia_int(String fh) {

        try {
           /* if(app.glo_idioma.equals("esp")){
                return Integer.valueOf(fh.substring(0,2));

            }else{
           */     return Integer.valueOf(fh.substring(0, 2));
          //  }

        } catch (Exception exc) {

        }
        return -1;
    }

    //----------------------------------
    public static int fechora_ano_int(String fh) {

        try {
          /*  if(app.glo_idioma.equals("esp")){
                return Integer.valueOf(fh.substring(6, 10));
            }else{
          */      return Integer.valueOf(fh.substring(6, 10));
          //  }

        } catch (Exception exc) {

        }
        return -1;
    }

    //----------------------------------
    public static String fecha_pantalla(int dia , int mes, int ano) {

        try {
            String m = (mes < 10) ? "0" + String.valueOf(mes) : String.valueOf(mes);
            String d = (dia < 10) ? "0" + String.valueOf(dia) : String.valueOf(dia);

          /*  if(app.glo_idioma.equals("esp")){
                return  d + "-" + m + "-" + ano;
            }else {
            */    return d + "-" + m + "-" + ano;
           // }
        } catch (Exception exc) {

        }
        return "";
    }

    public static String ano_pantalla(int dia , int mes, int ano) {

        try {
            String m = (mes < 10) ? "0" + String.valueOf(mes) : String.valueOf(mes);
            String d = (dia < 10) ? "0" + String.valueOf(dia) : String.valueOf(dia);

          /*  if(app.glo_idioma.equals("esp")){
                return  d + "-" + m + "-" + ano;
            }else {
            */    return d + "-" + m + "-" + ano;
            // }
        } catch (Exception exc) {

        }
        return "";
    }
    public static String anno_pantalla(int dayNow, int i, int ano) {

        try {


          /*  if(app.glo_idioma.equals("esp")){
                return  d + "-" + m + "-" + ano;
            }else {
            */    return ""+ ano;
            // }
        } catch (Exception exc) {

        }
        return "";
    }

}

