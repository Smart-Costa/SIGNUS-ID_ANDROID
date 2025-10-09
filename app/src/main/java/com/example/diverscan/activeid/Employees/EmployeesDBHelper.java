package com.example.diverscan.activeid.Employees;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

public class EmployeesDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Test_ActiveId_v1";
    private static final int DATABASE_VERSION = 3;

    public EmployeesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
    }

    public Map<Integer, EntidadEmployees> GetEmployees (String description){
        Map<Integer, EntidadEmployees> mapEmployees = new HashMap<Integer, EntidadEmployees>();
        String query="Select * from Employees where Name like "+"'%"+description+"%' or  Id like "+"'%"+description+"%'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor employeesCursor = db.rawQuery(query, null);
        int cantidadDatos = employeesCursor.getCount();
        employeesCursor.moveToFirst();
        for (int i = 0; i < cantidadDatos;i++) {

            String employeeSysId = employeesCursor.getString(employeesCursor.getColumnIndex("EmployeeSysId"));
            String name= employeesCursor.getString(employeesCursor.getColumnIndex("Name"));
            String lastName = employeesCursor.getString(employeesCursor.getColumnIndex("LastName"));
            String id= employeesCursor.getString(employeesCursor.getColumnIndex("Id"));
            String companyIdExtern= employeesCursor.getString(employeesCursor.getColumnIndex("CompanyIdExtern"));

            EntidadEmployees entidadEmployees = new EntidadEmployees(employeeSysId,name, lastName,id,companyIdExtern);
            mapEmployees.put(i, entidadEmployees);
            employeesCursor.moveToNext();

        }
        return mapEmployees;

    }

    public EntidadEmployees GetIdEmployees (String EmployeeSysId){
        String query="Select * from Employees where EmployeeSysId = '"+EmployeeSysId+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor employeesCursor = db.rawQuery(query, null);
        int cantidadDatos = employeesCursor.getCount();
        employeesCursor.moveToFirst();
        for (int i = 0; i < cantidadDatos;i++) {
            String employeeSysId = employeesCursor.getString(employeesCursor.getColumnIndex("EmployeeSysId"));
            String name= employeesCursor.getString(employeesCursor.getColumnIndex("Name"));
            String lastName = employeesCursor.getString(employeesCursor.getColumnIndex("LastName"));
            String id= employeesCursor.getString(employeesCursor.getColumnIndex("Id"));
            String companyIdExtern= employeesCursor.getString(employeesCursor.getColumnIndex("CompanyIdExtern"));

            EntidadEmployees Employees = new EntidadEmployees(employeeSysId,name, lastName,id,companyIdExtern);
            return Employees;
        }
        return null;
    }

}
