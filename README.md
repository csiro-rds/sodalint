SODA Lint
=========

This program validates a Service-side Operations for Data Access (SODA) Virtual 
Observatory service for compliance with the SODA v1 specification (PR-SODA-1.0-20160920). 
It can be used either as a library or as a command line tool. 

SODALint is intended for use both by central validation services and teams implementing SODA services.

This program has been produced by the CSIRO ASKAP Science Data Archive team. It is heavily based on the [taplint](http://www.star.bristol.ac.uk/~mbt/stilts/sun256/taplint.html) 
program written by Mark Taylor.

Usage
=====

The command line can be used as follows:
 
```
java -jar sodalint-all-1.0.3.jar 
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

* sodaurl: The url to be tested. This should be the base url for the service, which is the parent of the capabilities endpoint. The prefix is optional. 	 


Example
-------
```
java -jar sodalint-all-1.0.3.jar stages="CPV AVV CAP SVD ASY" https://casda.csiro.au/casda_data_access/data/
```

This will run the CPV, CAP, AVV, SVD, ASY stages (in that order) against the CASDA SODA service. The default maxrepeat and line length values are used. 

Release History
---------------

Current Release: v1.0.3

Notes for each release are available at  [release_notes.md](./release_notes.md)

License
=======

The code in this project is licensed under the CSIRO Open Source Licence (MIT/BSD styled).


Build
=====

To build the project, checkout the project, cd to the project folder and use the following command line


On Windows:

> `gradlew clean build allJar`

On Unix or Mac:

> `./gradlew clean build allJar`

The Gradle build is configured to download all dependencies, compile the code, run the unit tests and build the jar files. 
