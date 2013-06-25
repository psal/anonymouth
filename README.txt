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

JStylo was released by the Privacy, Security and Automation lab at Drexel University under the AGPLv3 license.
A copy of this license is included with the repository/program. If for some reason it is absent, it can be viewed here: http://www.gnu.org/licenses/agpl.html

Dependencies:

JStylo is built upon the following libraries:

weka 	3.7.9
Jama 	1.0.3
Stanford postagger 	2012-01-06
jgaap	 5.2
jaws 	1.3
fasttag v2
tt4j    1.0.15

Usage:

Jstylo requires Java 7 to run properly

In windows: double-click jstylo.jar
In other platforms / to view on-the-fly log:

> java [-Xmx2048m] -jar jsan.jar

Note:
For usage with large corpora or feature sets, it is recommended
to increase the JVM heap size using the -Xmx option.
 