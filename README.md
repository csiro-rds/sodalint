SODA Lint
=========

This program validates a Service-side Operations for Data Access (SODA) Virtual 
Observatory service for compliance with the SODA v1 specification. 
It can be used either as a library or as a command line tool. 

SODALint is intended for use both by central validation services and teams implementing SODA services.

This program has been produced by the CSIRO ASKAP Science Data Archive team. It is heavily based on the [taplint](http://www.star.bristol.ac.uk/~mbt/stilts/sun256/taplint.html) program written by Mark Taylor.

Usage
=====

The command line can be used as follows:
 
```
java -jar sodalint-all-1.0.1.jar 
		[stages="CPV|CAP|AVV|EXM|SVD|ERR|SYN|ASY[ ...]"]
		[maxrepeat=<int-value>]
		[truncate=<int-value>]
		[sodaurl=]<url-value>
```

The parameters are:
* stages: A space separated list (so it must be quoted) of the stages to be run. If not provided the default stages of "CPV, CAP, AVV, SVD, SYN, ASY" will be used. The stages are:
  * CPV: Validate the capabilities document with the schema
  * CAP: Validate the capabilities document using targeted rules
  * AVV: Validate the availability document with the schema
  * EXM: Validate the examples document (not yet supported)
  * SVD: Validate the service description returned by an empty sync query.
  * ERR: Test the response to an erroneous query (not yet supported).
  * SYN: Validate the sync endpoint.
  * ASY: Validate the async endpoint.
  
* maxrepeat: The maximum number of a particular message that will be output. The default value is 9.

* truncate: The maximum line length that will be output. The default value is 1024.

* sodaurl: The url to be tested. The prefix is optional. 	 


Example
-------
```
java -jar sodalint-all-1.0.1.jar stages="CPV AVV CAP SVD ASY" https://casda.csiro.au/casda_data_access/data/
```

This will run the CPV, CAP, AVV, SVD, ASY stages (in that order) against the CASDA SODA service. The default maxrepeat and line length values are used. 

License
=======

The code in this project is licensed under the CSIRO Open Source Licence (Apache styled) however some 
libraries used include GPL code so the combined product is licensed currently under the GPL.

Build
=====

To build the project, checkout the project, cd to the project folder and use the following command line

``` 
gradlew clean build alljar
```

Note that while most libraries will be automatically retrieved by this build process, some are not available in public repositories and must be sourced manually and then locally installed. These are listed below.


* Stilts - available from http://www.star.bristol.ac.uk/~mbt/stilts/#install and can be installed using the command:
```
mvn install:install-file -Dfile=stilts.jar -DgroupId=uk.ac.starlink -DartifactId=stilts -Dversion=3.0.6 -Dpackaging=jar
```
