package com.example.f1285.loginpink;

import java.util.ArrayList;

/**
 * Created by f1285 on 2017/5/28.
 */

public class Data {

    ArrayList<Double> longituble = new ArrayList<>();
    ArrayList<Double> latitude = new ArrayList<>();
    ArrayList<String> address = new ArrayList<>();
    ArrayList<Integer> type = new ArrayList<>();
    int size = 0;

    public void Data( Double longit, Double latit, String addr, int ty ){
        setLongituble(longit);
        setLatitude(latit);
        setAddress(addr);
        setType(ty);
    }

    public void setLongituble( Double longit ){
        longituble.add(longit);
        size++;
    }

    public void setLatitude( Double latit ){
        latitude.add( latit );
    }

    public void setAddress( String addr ){
        address.add( addr );
    }

    public void setType( int ty ){
        type.add( ty );
    }

    public double getLongituble( int index ){
        return longituble.get(index);
    }

    public double getLatituble( int index ){
        return latitude.get(index);
    }

    public String getAddress( int index ){
        return address.get(index);
    }

    public int getType( int index ){
        return type.get(index);
    }

    public int getSize(){
        return size;
    }

    public void clear(){
        longituble.clear();
        latitude.clear();
        address.clear();
        type.clear();
        size = 0;
    }
}
