package org.unitedinternet.cosmo.db;


import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test class for DBInitializer
 * @author ccoman
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:applicationContext-dao.xml",
        "classpath:applicationContext-services.xml",
        "classpath:applicationContext-security-dav.xml",
        "classpath:applicationContext-DBInitializer.xml",
        "classpath:applicationContext-test.xml"
        })
public class DBInitializerTest {


    /**
     * Init.
     */
    @BeforeClass
    public static void init() {
    }

    @Autowired
    private DbInitializer dbInitializer;
    
    /**
     * Delete test DB.
     * @throws IOException 
     */
    @After
    public void cleanup() throws IOException{
        //clean test db
        deleteFile("target/testdb.tmp");
        deleteFile("target/testdb.properties");
        deleteFile("target/testdb.script");
        deleteFile("target/testdb.lck");
        deleteFile("target/testdb.log");
    }

    /**
     * 
     * @param filePath 
     */
    private void deleteFile(String filePath) {
        File f = new File(filePath);
        Assert.assertTrue(f.exists());
        
           f.delete();

        Assert.assertFalse("File " + filePath + " still exists", f.exists());
    }
    /**
     * Test initialization.
     */
    @Test
    public void testInitialization(){
        dbInitializer.initialize();
        Assert.assertTrue(dbInitializer.isSchemaInitialized());
    }
}
