package com.silvy.app.models;

import org.bson.Document;

//This class is a helper to use in Export action when Using MongoDB
public class DomainScore {

    String domain;
    Integer count;
    Integer score;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public static DomainScore fromBson(Document doc){
        DomainScore score = new DomainScore();
        score.setScore(doc.getInteger("score"));
        score.setDomain(doc.getString("domain"));
        score.setCount(doc.getInteger("count"));

        return score;
    }
}
