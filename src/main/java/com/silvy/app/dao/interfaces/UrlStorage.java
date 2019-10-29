package com.silvy.app.dao.interfaces;

import com.silvy.app.models.DomainScore;

import java.util.List;

// As I implemented 2 different ways of storage,
// this interface helps to unify the expected behaviour of any other storage that could be implemented in a future.
// Factory Pattern
public interface UrlStorage {

    boolean add(String domain, String urlDomain, int score);
    boolean delete(String domain, String urlDomain);
    List<DomainScore> export();

}
