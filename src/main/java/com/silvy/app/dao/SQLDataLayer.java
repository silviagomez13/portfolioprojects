package com.silvy.app.dao;

import com.silvy.app.models.DomainScore;
import com.silvy.app.dao.interfaces.UrlStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLDataLayer implements UrlStorage {
    private static Connection con;
    private static boolean hasData = false;
    private final Logger log;

    public SQLDataLayer() {
        log = LoggerFactory.getLogger(this.getClass());
    }

    private void getConnection(){
        try {
            Class.forName("org.sqlite.JDBC"); //make the driver available
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            con = DriverManager.getConnection("jdbc:sqlite:SQLiteDomains.db");
        } catch (SQLException e) {
            log.error("Error Message - getConnection method: ",e);
        }
        init();
    }



    public void init() {
        ResultSet res = null;

        if(!hasData){
            hasData = true;
        }

        Statement state = null;
        try {
            state = con.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            res = state.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='url'");

            if(res != null && !res.next()){
                System.out.println("Building the url table");

                //It is not normalized  in order to gain query speed
                //In the case that we need to store more information about every domain I will create a domain table and a url table with a FK to domain.
                Statement state2 = con.createStatement();
                state2.execute("CREATE TABLE url (urlId integer," +
                        "domainName varchar(120)," +
                        "url varchar(300)," +
                        "score integer, " +
                        "primary key(urlId));");

                log.info("The url table was created."); //TODO this log can be improved.
            }

        } catch (SQLException e) {
            log.error("Error Message - init method: ",e);
        }
    }

    //TODO for multi threads requests this should be transactional!
    public boolean add(String domain, String urlDomain, int score) {
        if(con == null){
            getConnection();
        }

        String sqlSelect = "SELECT urlId FROM url WHERE url = ?";

        String sqlUpdate = "UPDATE url SET score = ? WHERE urlId = ?";

        String sqlInsert = "INSERT INTO url(domainName, url, score) VALUES (?,?,?);";

        try {
            //First I search if the url is already stored
            Integer urlId = null;
            try(PreparedStatement stmt  = con.prepareStatement(sqlSelect);
                PreparedStatement prep = con.prepareStatement(sqlInsert);
                PreparedStatement prep2 = con.prepareStatement(sqlUpdate);
                ){

                stmt.setString(1, urlDomain);
                ResultSet rs  = stmt.executeQuery();

                if(rs.next()){
                    urlId = rs.getInt("urlId");
                }
                //If the url is not stored, I add it
                if(urlId == null) {
                        prep.setString(1, domain);
                        prep.setString(2, urlDomain);
                        prep.setInt(3, score);
                        int count = prep.executeUpdate();
                        return count == 1;

                }else{ //if the ulr is already store, I update the score.
                        prep2.setInt(1, score);
                        prep2.setInt(2, urlId);
                        int count = prep2.executeUpdate();
                        return count == 1;
                }
            }
        }catch (SQLException e) {
            log.error("Error Message - add method: ",e);
        }
        return false;
    }

    public boolean delete(String domain, String urlDomain) {
        if(con == null){
            getConnection();
        }

        try {
            String sql = "DELETE FROM url WHERE url = ?";
            try(PreparedStatement prep = con.prepareStatement(sql)) {
                prep.setString(1, urlDomain);

                int count = prep.executeUpdate();
                
                return count == 1;
            }

        }catch (SQLException e) {
            log.error("Error Message - delete method: ",e);
        }
        return false;
    }

    public List<DomainScore> export() {
        List<DomainScore> result = new ArrayList<>();

        if(con == null){
            getConnection();
        }
        try {

            // Handle the resources and close them when necessary
            try(Statement state = con.createStatement();
                ResultSet res = state.executeQuery("SELECT domainName, COUNT(*)as totalUrls, SUM(score) as totalScore FROM url GROUP BY domainName; ")){

                //TODO This method should go in other object ->refactor!
                while(res.next()) {
                    DomainScore ds = new DomainScore();
                    ds.setDomain(res.getString("domainName"));
                    ds.setScore(res.getInt("totalScore"));
                    ds.setCount(res.getInt("totalUrls"));
                    result.add(ds);
                }
            }

        } catch (SQLException e) {
            log.error("Error Message - export method: ",e);
        }
        return result;
    }
}
