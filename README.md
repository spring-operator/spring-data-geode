Spring Data for Apache Geode
============================

The primary goal of the [Spring Data for Apache Geode](https://projects.spring.io/spring-data-gemfire) project is to 
make it easier to build highly scalable, _Spring_ powered applications using [Apache Geode](https://geode.apache.org/) 
as the underlying distributed, in-memory data management platform.

# Examples

For examples on using the _Spring Data for Apache Geode_, see the 
[spring-gemfire-examples](https://github.com/SpringSource/spring-gemfire-examples) project.

# Getting Help

Read the main project [website](https://projects.spring.io/spring-data-gemfire/) along with the [User Guide](https://docs.spring.io/spring-data-gemfire/docs/current/reference/html/).

Look at the source code and the [JavaDocs](https://docs.spring.io/spring-data-gemfire/docs/current/api/).

For more detailed questions, visit [StackOverflow](https://stackoverflow.com/questions/tagged/spring-data-gemfire).

If you are new to _Spring_ as well as _Spring Data for Apache Geode_, look for information about 
[Spring projects](https://spring.io/projects).

Quick Start
-----------

For those in a hurry:

* Download the jar through

* Maven:

~~~~~ xml
<dependency>
  <groupId>org.springframework.data</groupId>
  <artifactId>spring-data-geode</artifactId>
  <version>${version}</version>
</dependency>

<!-- nightly builds -->
<repository>
  <id>spring-maven-snapshot</id>
  <name>Spring Maven SNAPSHOT Repository</name>
  <url>https://repo.spring.io/snapshot</url>
  <snapshots><enabled>true</enabled></snapshots>
</repository>

<!-- milestones/release candidates-->
<repository>
  <id>spring-maven-milestone</id>
  <name>Spring Maven Milestone Repository</name>
  <url>https://repo.spring.io/milestone</url>
</repository>
~~~~~

* Gradle:

~~~~~ groovy
repositories {
   mavenRepo name: "spring-snapshot", urls: "https://repo.spring.io/snapshot"
   mavenRepo name: "spring-milestone", urls: "https://repo.spring.io/milestone"
   mavenRepo name: "spring-plugins" , urls: "https://repo.spring.io/plugins-release"
}

dependencies {
   compile "org.springframework.data:spring-data-geode:${version}"
}
~~~~~

* Configure a Geode cache and Region (REPLICATE, PARTITION and so on):

~~~~~ xml
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:gfe="http://www.springframework.org/schema/geode"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance
  xsi:schemaLocation="
    http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd
    http://www.springframework.org/schema/geode https://www.springframework.org/schema/geode/spring-geode.xsd">

  <gfe:cache/>

  <gfe:partitioned-region id="ExampleRegion" copies="2">

  <bean id="gemfireTemplate" class="org.springframework.data.gemfire.GemfireTemplate" p:region-ref="ExampleRegion"/>
</beans>
~~~~~

* Use the Region to read/write data:

~~~~~ java
region.put(Long.valueOf(1), new Person("Jane", "Smith"));
~~~~~

* And/Or `GemFireTemplate` to interact with Geode:

~~~~~ java
template.query("person = 1");
~~~~~

# Building

_Spring Data for Apache Geode_ uses Maven as its build system. To compile the project, simply type from the root folder

    mvn clean install

# Contributing


Here are some ways for you to get involved in the community:

* Get involved with the Spring community on the Spring Community Forums (now on StackOverflow).  
Please help out on the [forum](https://stackoverflow.com/questions/tagged/spring-data-gemfire) 
by responding to questions and joining the debate.
* Create [JIRA](https://jira.spring.io/browse/SGF) tickets for bugs and new features and comment and vote on the bugs 
you are interested in.
* GitHub is for social coding. If you want to write code, we encourage contributions through pull requests 
from [forks of this repository](https://help.github.com/forking/). If you want to contribute code this way, 
please reference a JIRA ticket as well covering the specific issue you are addressing.
* Watch for upcoming articles on Spring by [subscribing](https://spring.io/blog) to spring.io.

Before we accept a non-trivial patch or pull request we will need you to 
[sign the Contributor License Agreement](https://cla.pivotal.io/sign/spring). Signing the contributor’s agreement 
does not grant anyone commit rights to the main repository, but it does mean that we can accept your contributions, 
and you will get an author credit if we do. If you forget to do so, you'll be reminded when you submit a pull request. 
Active contributors might be asked to join the core team, and given the ability to merge pull requests.
