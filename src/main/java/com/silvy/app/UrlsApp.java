package com.silvy.app;

import com.silvy.app.dao.SQLDataLayer;
import com.silvy.app.dao.MongoDataLayer;
import com.silvy.app.dao.interfaces.UrlStorage;
import com.silvy.app.models.DomainScore;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;


public class UrlsApp
{

    private static Scanner inputScanner;

    public static void main( String[] args ) throws IOException
    {

        //Get input from user command prompt
        System.out.println("Please To Add an URL type: ADD URL-TO-ADD scoreInNumber");
        System.out.println("Please To Remove an URL type: REMOVE URL-TO-REMOVE");
        System.out.println("Please To Export a report of domains type: EXPORT");

        //check the input
        initScanner();
        boolean success = false;
        String action = "";
        String[] params = {};
        // read until the action entered is well-formed
        while (!success) {
            try {
                //Getting input line;
                String line = inputScanner.nextLine();

                if (null == line || "".equals(line.trim())) {
                    throw new InputMismatchException("Missing arguments.");
                }

                params = line.split(" ");

                //depending on the ACTION the user entered, I must check if the input is well-formed
                action = params[0];

                if(action.equals("ADD")){
                    if(params.length == 3){
                        if(!UrlsApp.isValid(params[1])){
                           throw new InputMismatchException("Invalid URL entered.");
                        }
                    }else{
                        throw new InputMismatchException("Mismatch arguments for ADD.");
                    }

                }else if(action.equals("REMOVE")){
                    if(params.length == 2){
                        if(!UrlsApp.isValid(params[1])){
                            throw new InputMismatchException("Invalid URL entered.");
                        }
                    }else{
                        throw new InputMismatchException("Mismatch arguments for REMOVE.");
                    }

                }else if(action.equals("EXPORT")){
                    if(params.length > 1){
                        throw new InputMismatchException("Error argument for EXPORT ");
                    }
                }else{
                    throw new InputMismatchException("Incorrect Action entered.");
                }

                success = true;


            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid Action - "+e.getMessage());
            }
        }

        if(success){

            //Select method of storage
            boolean ok  = false;
            String store = "";
            do{
                System.out.println("Indicate if you want to store data in SqLite MongoDB; Type S or M");
                store = inputScanner.next();

            }while((!store.equals("S") && !store.equals("M")) );

            UrlStorage storage;
            if(store.equals("M")){
                storage =  new MongoDataLayer();
            }else{
                storage = new SQLDataLayer();
            }

            //Depending on the action,  I call different method of the DataLayer
            if(action.equals("ADD")){
                String url = params[1];
                String domain;
                try {
                    domain = UrlsApp.getDomainName(url);

                } catch (URISyntaxException e) {
                    throw new InputMismatchException("Invalid URL entered.");
                }


                int score = Integer.valueOf(params[2]);

                if(storage.add(domain,url,score)){
                    System.out.println(">> The url was successfully added");
                }
            }

            if(action.equals("REMOVE")){
                String url = params[1];
                String domain;
                try {
                    domain = UrlsApp.getDomainName(url);

                } catch (URISyntaxException e) {
                    throw new InputMismatchException("Invalid URL entered.");
                }

                if(storage.delete(domain, url)){
                    System.out.println(">> The url was successfully removed");
                }else{
                    System.out.println(">> The url you want to remove was not found in the database");
                }
            }

            if(action.equals("EXPORT")){
                List<DomainScore> result =  storage.export();
                if(!result.isEmpty()){
                    System.out.println(">> EXPORT RESULT <<");
                    System.out.println("DOMAIN; COUNT_URLs; SOCIAL_SCORE");
                    for(DomainScore obj : result){
                        System.out.println(obj.getDomain()+";"+obj.getCount()+";"+obj.getScore());
                    }
                }
            }

        }
     }

    public static void initScanner(Scanner scanner) {
         inputScanner = scanner;
        HashMap<Integer, Boolean> aux = new HashMap<>();

        Iterator it =  aux.keySet().iterator();
        while(it.hasNext()){
            if(aux.get(it.next()).equals(Boolean.FALSE)){
                Integer.valueOf(it.next().toString());
                Object value = it.next();
                Integer.valueOf(value.toString());
            }
        }


    }



    private static void initScanner() {
        if(inputScanner==null){
            initScanner(new Scanner(System.in));
        }
    }


    /* Returns true if url is valid using java.net.url class to validate a URL*/
    public static boolean isValid(String url)
    {
        /* Try creating a valid URL */
        try {
            new URL(url).toURI();
            return true;
        }

        // If there was an Exception
        // while creating URL object
        catch (Exception e) {
            return false;
        }
    }

    /* This method return the domain of a given URL*/
    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }


}
