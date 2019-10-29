package com.silvy.app;

// this class will test the methods of MongoDataLayer.
// it could be easier if we have spring in the project so we can inject the connection for example

import com.mongodb.ServerAddress;
import com.silvy.app.dao.MongoDataLayer;
import com.silvy.app.models.DomainScore;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.List;

public class MongoDataLayerTest{

    private MongoDataLayer subjectUnderTest;

    @Before
    public void prepare(){
        MongoServer server = new MongoServer(new MemoryBackend());

        InetSocketAddress serverAddress = server.bind();

        subjectUnderTest = new MongoDataLayer("mongodb://"
                +new ServerAddress(serverAddress));

    }

    @Test
    public void testAddNew() {
        subjectUnderTest.add("p.com", "http://p.com", 22);

        // check in "server" if data is there ... assert that the data exist
        // we "can" check using subject under test. BUT it will became a non unitary test, we will
        // end testing 2 functionalities at the same time...
        List<DomainScore> scores = subjectUnderTest.export();
        Assert.assertEquals("The data stored should be 1 entry only", 1,
                scores.size());
    }
/*
    @Test
    public boolean delete(String urlDomain) {
        return false;
    }

    @Test
    public List<DomainScore> export() {
        return null;
    }
*/
}

