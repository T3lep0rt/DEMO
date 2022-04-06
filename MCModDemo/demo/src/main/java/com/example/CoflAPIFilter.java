package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONObject;

public class CoflAPIFilter {

    public void coflFilter(ArrayList<String> uuidAuctions, int profit) throws IOException {

        // new url https://sky.coflnet.com/api/auction/ + auction uuid
        // safe the rsult in the list of auctions

        ArrayList<JSONObject> auctions = new ArrayList<JSONObject>();
        AusgabeHandler ausgabeHandler = new AusgabeHandler();
        BufferedReader br;
        BufferedReader br2;
        URL url;
        int auctionSize = uuidAuctions.size();
        JSONObject auction;

        for (int i = 0; i < auctionSize; i++) {

            auction = new JSONObject();

            // System.out.println("https://sky.coflnet.com/api/auction/" +
            // uuidAuctions.get(i));

            url = new URL("https://sky.coflnet.com/api/auction/" + uuidAuctions.get(i));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.60 Safari/537.36");

            // check for error
            // if (conn.getResponseCode() != 200) { print error but continue
            // throw new RuntimeException("Failed : HTTP error code : "
            if (conn.getResponseCode() == 429) {

                // add the item at index i to the end of the list of auctions and go to the next
                // item
                // get the end of the list of auctions

                uuidAuctions.add(uuidAuctions.get(i));

                continue;
            } else if (conn.getResponseCode() != 200) {
                System.out.println("Failed : HTTP error code : " + conn.getResponseCode());
                continue;
            }

            // read the data from the url
            br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            // save every uuid as a sinle string in the auctions list
            // but only if "claimed" is false and "bin" is true
            String output;
            while ((output = br.readLine()) != null) {
                auction = new JSONObject(output);
            }

            br.close();
            conn.disconnect();



            // item_name has to be divided by _ instead of spaces

            // System.out.println(item_name);
            // connect to result of craftURL
            String urlString = craftURL(auction);
            url = new URL(urlString);
            HttpURLConnection conn2 = (HttpURLConnection) url.openConnection();
            conn2.setRequestMethod("GET");
            // conn.setRequestProperty("Accept", "application/json");
            conn2.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.60 Safari/537.36");

            // if the response code is not 200 delete the item from the list of auctions and
            // go to the next item
            // or if the item_nam is Enchanted_Book

            // find out how to solve 429 error

            if (conn2.getResponseCode() != 200 || auction.getString("itemName").equals("Enchanted_Book")) {
                //auctions.remove(i);
                System.out.println("response code: " + conn2.getResponseCode());
                //System.out.println("Item removed : " + auction.getString("item_name"));
                continue;
            }

            // read the data from the url
            br2 = new BufferedReader(new InputStreamReader(
                    (conn2.getInputStream())));

            // add the median and the volume to the JsonObject
            String output2;
            while ((output2 = br2.readLine()) != null) {
                JSONObject obj = new JSONObject(output2);
                auction.put("median", obj.get("median"));
                auction.put("volume", obj.get("volume"));
            }

            // if median is higher than price print the item_name and the price and the
            // median and "/viewauction "+uuid

            if (auction.getDouble("median") > auction.getDouble("startingBid") + profit) {
                    ausgabeHandler.ausgabe(auction.getString("itemName"),
                        auction.getDouble("startingBid"), auction.getDouble("median"),
                        auction.getString("uuid")
                    );
                // System.out.println(auctions.get(i).getString("itemName")+"
                // "+auctions.get(i).getDouble("startingBid")+"
                // "+auctions.get(i).getDouble("median")+" "+auctions.get(i).getString("uuid"));
            }

            conn2.disconnect();

            br2.close();

            auctionSize = uuidAuctions.size();

        }

        System.out.println("CoflAPIFilter: coflFilter");

    }

    public boolean checkIfSorted(ArrayList<JSONObject> auctions) {

        // check if the list is sorted by starting_bid in descending order
        // if it is sorted return true
        // if it is not sorted return false

        for (int i = 0; i < auctions.size() - 1; i++) {
            if (auctions.get(i).getDouble("starting_bid") < auctions.get(i + 1).getDouble("starting_bid")) {
                return false;
            }
        }

        return true;
    }

    public String craftURL(JSONObject auction) {

        String urlString = "https://sky.coflnet.com/api/item/price/";

        String item_name = "";

        if (!auction.isNull("tag")) {
            item_name = auction.getString("tag");
        } else {
            item_name = auction.getString("itemName");
            // swap space for _
            item_name = item_name.replace(" ", "_");
        }

        int countStars = 0;
        // System.out.println("has dungeon stars? :
        // "+auction.getJSONObject("nbtData").getJSONObject("data").has("dungeon_item_level"));
        if (auction.getJSONObject("nbtData").getJSONObject("data").has("dungeon_item_level")) {
            countStars = auction.getJSONObject("nbtData").getJSONObject("data").getInt("dungeon_item_level");
        }
        String reforge = auction.getString("reforge");
        String rarity = auction.getString("tier");

        // urlString += item_name;
        // build url string to be like this :
        // https://sky.coflnet.com/api/item/price/GOLD_SADAN_HEAD?Stars=3&Reforge=ancient&Rarity=SPECIAL
        // only add stars or reforge or rarity if they are not null

        urlString += item_name;

        if (rarity != null) {
            urlString += "?Rarity=" + rarity;
        }
        if (reforge != null) {
            urlString += "&Reforge=" + reforge;
        }
        if (countStars != 0) {
            urlString += "&Stars=" + countStars;
        }

        // System.out.println("urlString: "+urlString);

        return urlString;
    }
}