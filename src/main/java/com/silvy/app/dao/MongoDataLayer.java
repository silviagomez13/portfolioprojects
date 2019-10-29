package com.silvy.app.dao;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.result.UpdateResult;
import com.silvy.app.models.DomainScore;
import com.silvy.app.dao.interfaces.UrlStorage;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


public class MongoDataLayer implements UrlStorage {

    private final Logger log;


    private final MongoCollection<Document> domainCollection;

    public MongoDataLayer() {
        //TODO put all these details in a property file
        this("mongodb+srv://" + URLEncoder.encode("silvy") + ":" + URLEncoder.encode("admin") + "@cluster0-gkgnh.mongodb.net/");
    }

    public MongoDataLayer(String connectionString) {
        // Connect to MongoDB atlas server
        MongoClient mongoClient = MongoClients.create(connectionString);

        // Connect to MongoDB atlas server
        MongoDatabase database = mongoClient.getDatabase("url_manager");

        CodecRegistry pojoCodecRegistry =
                fromRegistries(
                        MongoClientSettings.getDefaultCodecRegistry(),
                        fromProviders(PojoCodecProvider.builder().automatic(true).build()));


        // Get tasks collection
        domainCollection = database.getCollection("urls_collection").withCodecRegistry(pojoCodecRegistry);
        log = LoggerFactory.getLogger(this.getClass());
    }

    public boolean add(String domain, String urlDomain, int score) {

        Document doc = domainCollection.find(new Document("domain", domain)).first();

        if(doc == null){
            //if the domain does not exist I created
            List<Pair> pages  = new ArrayList<Pair>();
            pages.add(new Pair(urlDomain, score));

            Document newDomain = new Document("domain", domain)
                    .append("pages", pages)
                    .append("score", score);


            domainCollection.insertOne(newDomain);

            return true;

        } else {
            // If the domiain exists I update the object with the new url and score
            Integer valueDelta = replaceOrInsert(urlDomain, score, (List<Document>) doc.get("pages"));
            doc.append("score", (Integer)doc.get("score") + valueDelta);
            UpdateResult result = domainCollection.updateOne(new Document("domain", doc.get("domain")), new Document("$set", doc));
            if(result.getModifiedCount()==1){
                return true;
            }
        }
        log.debug("The Add method failed. Check UpdateResult.("); //TODO this log can be improved.
        return false;

    }

    public boolean delete(String domain, String urlDomain) {

        Document doc = domainCollection.find(new Document("domain", domain)).first();

        if(doc != null){
            List<Document> pages = (ArrayList<Document>)doc.get("pages");
            //search url inside the pages
            Integer scoreToSubtract = null;
            for(Document p : pages){
                if(p.get("key").equals(urlDomain)){
                    scoreToSubtract = (Integer)p.get("val");
                    pages.remove(p);
                    // Break, to leave the foreach iterator simpler, is not good practice but avoid keep comparing in if
                    break;
                }
            }
            if(scoreToSubtract != null) {
                // if the url was found, I update the list of pages
                doc.append("pages",pages);
                doc.append("score", (Integer) doc.get("score") - scoreToSubtract);
                domainCollection.updateOne(new Document("domain", doc.get("domain")), new Document("$set", doc));
                return true;
            }else{
                log.debug("The url was not found in the list of urls (pages)");
                return false;
            }
        }
         return false;

    }

    public List<DomainScore> export() {
        List<Bson> pipeline = new ArrayList<>();
        List<Document> domains = new ArrayList<>();

        Bson projection = new Document("$size", "$pages" );
        Bson project = Aggregates.project(new Document("count", projection).append("_id",0).append("domain", 1).append("score",1) );
        pipeline.add(project);
        domainCollection.aggregate(pipeline).into(domains);


        return domains.stream().map(d->DomainScore.fromBson(d)).collect(Collectors.toList());
    }

    /**
     * Search for an element in the list, replace if exist. And return the delta from the total from previous state
     * to new state
     *
     * @param urlDomain
     * @param score
     * @param list
     * @return
     */
    private Integer replaceOrInsert(final String urlDomain, int score, List<Document> list) {
        Integer delta = null;
        for(Document p : list){
            if(p.get("key").equals(urlDomain)){
                delta = score - (Integer)p.get("val");
                p.append("val", score);
                // Break, to leave the foreach iterator simpler, is not good practice but avoid more comparations in if
                break;
            }
        }

        if(delta == null) {
            // Element not updated
            list.add(new Document("key", urlDomain).append("val", score));
            delta = score;
        }
        return delta;
    }


    /* There is no elegant method that I have found for persisting a hashTable into Mongo, it has to be converted to List...I will keep investigating*/

    public static final class Pair{
        private String key;
        private Integer val;
        public Pair(String k, Integer v){
            key = k;
            val = v;
        }

        public Integer getVal() {
            return val;
        }

        public void setVal(Integer val) {
            this.val = val;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }






}
