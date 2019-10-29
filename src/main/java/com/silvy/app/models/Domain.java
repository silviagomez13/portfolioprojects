package com.silvy.app.models;

import java.util.Map;

//POJO class used in MongoDB storage
public class Domain {

    private String urlDomain;
    private Map<String, Integer> pages;
    private Integer totalScore;

    public Map<String, Integer> getPages() {
        return pages;
    }

    public void setPages(Map<String, Integer> pages) {
        this.pages = pages;
    }

    public String getUrlDomain() {
        return urlDomain;
    }

    public void setUrlDomain(String urlDomain) {
        this.urlDomain = urlDomain;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Domain(){
       super();
    }

    public Domain(String urlDomain, Map<String, Integer> pages){
        this.setUrlDomain(urlDomain);
        this.setPages(pages);
    }
}
