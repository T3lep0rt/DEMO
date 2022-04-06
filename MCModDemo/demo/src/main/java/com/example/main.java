package com.example;

import java.io.IOException;
import java.util.ArrayList;

class main{

    static APIHandler apiHandler = new APIHandler();
    static CoflAPIFilter coflAPIFilter = new CoflAPIFilter();
    static AusgabeHandler ausgabeHandler = new AusgabeHandler();
    static boolean isactive = true;
    static ArrayList<String> auctions = new ArrayList<String>();

    public static void main(String args[]) throws IOException {
        start();
    }

    public static void start() throws IOException{
        while(isactive){
            auctions = apiHandler.getAPI();
            coflAPIFilter.coflFilter(auctions,100000);
            //ausgabeHandler.ausgabe(auctions);
        }
    }

}