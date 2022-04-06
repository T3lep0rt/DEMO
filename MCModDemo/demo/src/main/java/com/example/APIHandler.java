package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class APIHandler {

    public ArrayList<String> getAPI() throws IOException {
        // access https://api.hypixel.net/skyblock/auctions?page=0 to get the first page
        // of auctions and save the data it in a json array called auctions
        // new url = https://api.hypixel.net/skyblock/auctions?page=0

        
        ArrayList<String> auctions = new ArrayList<String>();
        //just get the uuid
        URL url = new URL("https://api.hypixel.net/skyblock/auctions?page=0");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        // check for error
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode());
        }

        // read the data from the url
        BufferedReader br = new BufferedReader(new InputStreamReader(
                (conn.getInputStream())));

        // save every uuid as a single string in the auctions list
        //but only if "claimed" is false and "bin" is true
        // and sort it by highest bid to lowest bid

        String output;
        while ((output = br.readLine()) != null) {
            JSONObject obj = new JSONObject(output);
            JSONArray arr = obj.getJSONArray("auctions");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.getJSONObject(i);
                if (item.getBoolean("claimed") == false && item.getBoolean("bin") == true) {
                    auctions.add(item.getString("uuid"));
                }
            }
        }

        br.close();
        conn.disconnect();

        System.out.println("auctions.size() = " + auctions.size());
        
        
        return auctions;

    }


}