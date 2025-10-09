package com.example.diverscan.activeid.Employees;

public class EntidadEmployees {

    private String employeeSysId ;
    private String name;
    private String lastName ;
    private String id;
    private String companyIdExtern;

    public EntidadEmployees(String pemployeeSysId,String pname, String plastName ,String pid, String pcompanyIdExtern){

        employeeSysId = pemployeeSysId;
        name= pname;
        lastName = plastName;
        id = pid;
        companyIdExtern = pcompanyIdExtern;
    }

    public EntidadEmployees(){
    }

    public String getEmployeeSysId() {
        return employeeSysId;
    }

    public void setEmployeeSysId(String pemployeeSysId) {
        this.employeeSysId = pemployeeSysId;
    }

    public String getName() {
        return name;
    }

    public void setName(String pname) {
        this.name = pname;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String plastName) {
        this.lastName = plastName;
    }

    public String getId() {
        return id;
    }

    public void setId(String pid) {
        this.id = pid;
    }

    public String getCompanyIdExtern() {
        return companyIdExtern;
    }

    public void setCompanyIdExtern(String pcompanyIdExtern) {
        this.companyIdExtern = pcompanyIdExtern;
    }
    @Override
    public String toString(){
        return name+ "--"+companyIdExtern;
    }
}
