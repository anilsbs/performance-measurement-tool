################################################################################
#                      Performance Measurement Tool 
################################################################################

Purpose :  To generate required performance report files automatically using HAR files (captured all network activities in the file) to filter out UI calls and REST calls (by filter REST word) to know overhead associated between UI layer and application server processing layer.
It measures total UI response time and end to end REST response time with associated server log timings. 
It also measures multiple iterations of the same transaction and generates xls report files contains minimum & median values. 
This HAR files will be processed by performance measurement utility tool.
This tool also has capability to connect linux machine automatically and download the required all log files into your local.
Then timings from the log files are fetched and compared with timings from HAR files for measurement.	  
This tool will generate sequence of files like csv, xls performance report files.

Please change following things before beginning :
Change required things in config.properties as per your need.
Download required JAR's mentioned in build.xml file and change respective paths.


For more details please drop an email to anilkumar.sbs@gmail.com