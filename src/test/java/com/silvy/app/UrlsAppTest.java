package com.silvy.app;

import com.silvy.app.dao.MongoDataLayer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple UrlsApp.
 */
@RunWith(PowerMockRunner.class)
// Due the static initialization and binding, we need to use powermockito, and as we are preparing for test UrlsApp.class,
// even it has coverage, it shows as zero.
@PrepareForTest({Scanner.class, MongoDataLayer.class, UrlsApp.class})
public class UrlsAppTest
{
    private static int scanPosition;
    MongoDataLayer dataStorage;

    private Scanner createScanner(String operation){
        String [] values = new String[] {operation + "\n", "M\n"};
        scanPosition = 0;
        Scanner sc = new Scanner(cb -> {
            cb.append(values[scanPosition]);
            return values[scanPosition++].length();
        });

        return sc;
    }

    @Before
    public void prepare() throws Exception {
        dataStorage = Mockito.mock(MongoDataLayer.class);
        PowerMockito.whenNew(MongoDataLayer.class).withAnyArguments().thenReturn(dataStorage);
    }

    @Test
    public void testAddActionCorrectParameters() throws Exception {
        Scanner scannerMock = createScanner("ADD https://google.com/some 33");
        UrlsApp.initScanner(scannerMock);

        UrlsApp.main(new String[0]);

        Mockito.verify(dataStorage).add(
                Mockito.eq("google.com"),
                Mockito.eq("https://google.com/some"),
                Mockito.eq(33));
    }

    @Test
    public void testDeleteUpdateActionCorrectParameters() throws Exception {
        Scanner scannerMock = createScanner("REMOVE https://google2.com/some1");
        UrlsApp.initScanner(scannerMock);

        UrlsApp.main(new String[0]);

        Mockito.verify(dataStorage).delete(
                Mockito.eq("google2.com"),
                Mockito.eq("https://google2.com/some1"));
    }

    @Test
    public void testDeleteUpdateActionWithInCorrectParameters() throws Exception {
        //TODO to be implemented
    }

}
