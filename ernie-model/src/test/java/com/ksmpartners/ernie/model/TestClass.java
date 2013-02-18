package com.ksmpartners.ernie.model;

public class TestClass {

    private String name;
    private int id;

    public TestClass(){}

    public TestClass(String name, int id)
    {
        this.name = name;
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public int getId()
    {
        return id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    @Override
    public boolean equals(Object another)
    {
        if(!(another instanceof TestClass))
            return false;

        if(getId() == ((TestClass) another).getId())
            return true;
        else
            return false;
    }

}
