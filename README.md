# gora-infinispan

### Description 

Apache Gora is an open source framework that provides an in-memory data model with persistence for big data. Data persistence supports column stores, key value stores, document stores and RDBMSs. Big data analyzing relies on the the [MapReduce](https://en.wikipedia.org/wiki/MapReduce) support of [Apache Hadoop](https://hadoop.apache.org/).

This project provides a Gora support for the [Infinispan](http://infinispan.org) storage system, allowing it to store data for Hadoop based applications, such as Apache [Nutch](http://nutch.apache.org/) or [Giraph](http://giraph.apache.org/).

### Requirements

[infinispan-7.2.5.Final](infinispan.org)

[infinispan-avro-1.0.Final](https://github.com/leads-project/infinispan-avro)

### Installation 

This project is based upon Maven. It makes use of Infinispan 7.2.5.Final and the Avro support for Infinispan that is available [here](https://github.com/leads-project/infinispan-avro). Below, we explain how to execute an installation.

```
git clone https://github.com/leads-project/gora-infinispan.git
cd gora-infinispan
mvn clean install -DskipTests
```

### Usage

Gora allows a user application to store, retrieve and query Avro defined types. As of version 0.6, it offers [CRUD operations](http://gora.apache.org/current/api/apidocs-0.6/org/apache/gora/store/DataStore.html) and [query](http://gora.apache.org/current/api/apidocs-0.6/org/apache/gora/query/Query.html) that handle pagination, key range restriction, [filtering](http://gora.apache.org/current/api/apidocs-0.6/org/apache/gora/filter/Filter.html) and projection. 

The key interest of Gora is to offer a direct support for Hadoop to the data stores that implement its API. Under the hood, such a feature comes from a bridge between the [ImputFormat](http://gora.apache.org/current/api/apidocs-0.6/org/apache/gora/mapreduce/GoraInputFormat.html) and [OutputFormat](http://gora.apache.org/current/api/apidocs-0.6/org/apache/gora/mapreduce/GoraOutputFormat.html) classes and the [DataStore](http://gora.apache.org/current/api/apidocs-0.6/org/apache/gora/store/DataStore.html) class.

This Infinispan support for Gora passes all the unit tests of the framework. All the querying operations are handled at the server side. Query splitting is also supported and it allows a query to execute locally at each of the Infinispan server, close to the data. Thanks to this last feature, Hadoop MapReduce jobs that run atop of Infinisapn are locality-aware. 

## Code Sample

In the sample below, we first split a query across all the servers, then we execute two filtering operations, before asserting the validity of our result.

```java
Utils.populateEmployeeStore(employeeStore, NEMPLOYEE);
InfinispanQuery<String,Employee> query;

// Partitioning
int retrieved = 0;
query = new InfinispanQuery<>(employeeDataStore);
query.build();
for (PartitionQuery<String,Employee> q : employeeDataStore.getPartitions(query)) {
retrieved+=((InfinispanQuery<String,Employee>) q).list().size();
}
assert retrieved==NEMPLOYEE;

// Test matching everything
query = new InfinispanQuery<>(employeeDataStore);
SingleFieldValueFilter filter = new SingleFieldValueFilter();
filter.setFieldName("name");
filter.setFilterOp(FilterOp.EQUALS);
List<Object> operaands = new ArrayList<>();
operaands.add("*");
filter.setOperands(operaands);
query.setFilter(filter);
query.build();
List<Employee> result = new ArrayList<>();
for (PartitionQuery<String,Employee> q : employeeDataStore.getPartitions(query)) {
result.addAll(((InfinispanQuery<String,Employee>)q).list());
}
assertEquals(NEMPLOYEE,result.size());
```
