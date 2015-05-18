package com.sharemee.app.sharemee.util;

/**
 * Created by Marin on 27/04/2015.
 */

//This class helps us to developp by allowing us to change the baseURL

public class ConnectionConfig {


    //private String baseURL="http://192.168.1.34/sharemee/";
    //public String baseURL="http://192.168.0.23/sharemee/";
    public String baseURL = "http://sharemee.com/";
    //public String baseURL="http://10.0.2.2/sharemee/";


    public String getBaseURL() {
        return baseURL;
    }
}
