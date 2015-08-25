package org.apache.gora.infinispan.mapreduce;

import org.apache.gora.examples.generated.WebPage;
import org.apache.gora.infinispan.GoraInfinispanTestDriver;
import org.apache.gora.infinispan.store.InfinispanStore;
import org.apache.gora.mapreduce.DataStoreMapReduceTestBase;
import org.apache.gora.store.DataStore;
import org.apache.hadoop.conf.Configuration;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.apache.gora.infinispan.store.InfinispanClient.ISPN_CONNECTION_STRING_KEY;

/**
 * @author Pierre Sutra
 */
public class InfinispanStoreMapReduceTest extends DataStoreMapReduceTestBase {

   private GoraInfinispanTestDriver driver;
   private Configuration conf;

   public InfinispanStoreMapReduceTest() throws IOException {
      super();
      List<String> cacheNames = new ArrayList<>();
      cacheNames.add(WebPage.class.getSimpleName());
      driver = new GoraInfinispanTestDriver(3, cacheNames);
   }

   @Override
   @Before
   public void setUp() throws Exception {
      driver.setUpClass();
      super.setUp();
   }

   @Override
   @After
   public void tearDown() throws Exception {
      super.tearDown();
      driver.tearDownClass();
   }

   @Override
   protected DataStore<String, WebPage> createWebPageDataStore() throws IOException {
      conf = driver.getConfiguration();
      conf.set(ISPN_CONNECTION_STRING_KEY,driver.connectionString());
      try {
         InfinispanStore<String,WebPage> store = new InfinispanStore<>();
         store.setConf(conf);
         store.initialize(String.class, WebPage.class, new Properties());
         return store;
      } catch (Exception e) {
         e.printStackTrace();
      }
      return null;
   }

}
