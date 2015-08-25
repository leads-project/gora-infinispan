package org.apache.gora.infinispan.store;

import org.apache.gora.persistency.impl.PersistentBase;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.infinispan.avro.client.Marshaller;
import org.infinispan.avro.client.Support;
import org.infinispan.avro.hotrod.QueryBuilder;
import org.infinispan.avro.hotrod.QueryFactory;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.api.BasicCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/*
 * @author Pierre Sutra, Valerio Schiavoni
 */
public class InfinispanClient<K, T extends PersistentBase> implements Configurable{

   public static final Logger LOG = LoggerFactory.getLogger(InfinispanClient.class);

   public static final String ISPN_CONNECTION_STRING_KEY = "infinispan.connectionstring";
   public static final String ISPN_CONNECTION_STRING_DEFAULT = "127.0.0.1:11222";

   private Configuration conf;

   private Class<K> keyClass;
   private Class<T> persistentClass;
   private RemoteCacheManager cacheManager;
   private QueryFactory qf;

   private RemoteCache<K, T> cache;
   private boolean cacheExists;

   private Map<K,T> toPut;

   public InfinispanClient() {
      conf = new Configuration();
   }

   public synchronized void initialize(Class<K> keyClass, Class<T> persistentClass, Properties properties) throws Exception {

      if (cache!=null)
         return; // already initialized.

      this.keyClass = keyClass;
      this.persistentClass = persistentClass;

      String host = properties.getProperty(ISPN_CONNECTION_STRING_KEY,
            getConf().get(ISPN_CONNECTION_STRING_KEY, ISPN_CONNECTION_STRING_DEFAULT));
      conf.set(ISPN_CONNECTION_STRING_KEY, host);
      properties.setProperty(ISPN_CONNECTION_STRING_KEY, host);
      LOG.info("Connecting client to "+host);

      Marshaller<T> marshaller = new Marshaller<T>(persistentClass);
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServers(host);
      builder.marshaller(marshaller);
      cacheManager = new RemoteCacheManager(builder.build());
      cacheManager.start();

      cache = cacheManager.getCache(persistentClass.getSimpleName());
      qf = org.infinispan.avro.hotrod.Search.getQueryFactory(cache);
      createSchema();

      toPut = new HashMap<>();
   }

   public boolean cacheExists(){
      return cacheExists;
   }

   public void createSchema() {
      try {
         Support.registerSchema(cacheManager, persistentClass.newInstance().getSchema());
      } catch (InstantiationException | IllegalAccessException e) {
         e.printStackTrace();
      }
   }

   public void createCache() {
      createSchema();
      cacheExists = true;
   }

   public void dropCache() {
      cache.clear();
      cacheExists = false;
   }

   public void deleteByKey(K key) {
      cache.remove(key);
   }

   public synchronized void put(K key, T val) {
      toPut.put(key, val);
   }

   public void putIfAbsent(K key, T obj) {
      this.cache.putIfAbsent(key,obj);
   }

   public T get(K key){
      return cache.get(key);
   }

   public boolean containsKey(K key) {
      return cache.containsKey(key);
   }

   public String getCacheName() {
      return this.persistentClass.getSimpleName();
   }

   public BasicCache<K, T> getCache() {
      return this.cache;
   }

   public QueryBuilder getQueryBuilder() {
      return (QueryBuilder) qf.from(persistentClass);
   }

   @Override
   public void setConf(Configuration conf) {
      this.conf =conf;
   }

   @Override
   public Configuration getConf() {
      return conf;
   }


   public void flush(){
      LOG.debug("flush()");
      if (!toPut.isEmpty()) cache.putAll(toPut);
      toPut.clear();
   }

   public synchronized void close() {
      LOG.debug("close()");
      flush();
      getCache().stop();
      cacheManager.stop();
   }
}
