JSAN - The Integrated JStylo and Anonymouth Package
====================================================

The Privacy, Security and Automation lab (PSAL)
Drexel University, Philadelphia, PA
http://psal.cs.drexel.edu/

----------------------------------------------------

JStylo
- Authorship recognition analysis tool
- Version: 1.1

----------------------------------------------------

License:

JSAN, JStylo and Anonymouth are released under the AGPL v3.0, a copy should be included 
if not it can be found at http://www.gnu.org/licenses/agpl.html

Dependencies:

JStylo is built upon the following libraries:

weka 	3.6.5
Jama 	1.0.3
Stanford postagger 	2012-01-06
jgaap	 5.2
jaws 	1.3
fasttag v2

Usage:

Jstylo requires Java 7 to run properly

In windows: double-click jstylo.jar
In other platforms / to view on-the-fly log:

> java [-Xmx2048m] -jar jsan.jar

Note:
For usage with large corpora or feature sets, it is recommended
to increase the JVM heap size using the -Xmx option.
 