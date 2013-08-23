Anonymouth
================================================

Document Anonymization Tool, Version 0.5<br>

The Privacy, Security and Automation Lab (PSAL)<br>
Drexel University, Philadelphia PA<br>
http://psal.cs.drexel.edu/

![](/src/edu/drexel/psal/resources/graphics/anonymouth_LOGO.png)

Introduction
------------------------------------------------

Anonymouth is a Java-based application that aims to give users to tools and knowledge needed to begin anonymizing documents they have written.

It does this by firing up JStylo libraries (an author detection application also develped by PSAL) to detect stylometric patterns and determine features (like word length, bigrams, trigrams, etc.) that the user should remove/add to help obsure their style and identity.

Though Anonymouth and it's team works hard to provide you with tools to help remove your identity from documents you have written, WE CAN IN NO WAY GUARANTEE THAT YOUR DOCUMENT IS ANONYMOUS OR NOT ANONYMOUS. Anonymouth is always giving you it's best guess, it's best idea of where your document stands, though that should not any any time be taken as an absolute (for example, you could have forgotten to remove your name from the document and Anonymouth has no way to know that that's your name and should remove it). What we can say is Anonymouth is only as good as you make it, and when used right can be helpful in guiding your document towards the right direction.

Installation
------------------------------------------------

There quickest and easiest way to install Anonymouth is to clone or download the zip of the Anonymouth github project here: https://github.com/psal/anonymouth

And then import this project as an existing project into Eclipse (or clone and import directly within Eclipse if you have the Eclipse eGit plugin).

This is currently the only ready way to compile and run Anonymouth. We will be including a updated build file soon so that you may build and run Anonymouth easily within the command land, but sadly it hasn't been done yet so this is the only option currently

Build and Run
------------------------------------------------

Once Anonymouth is all set up in Eclipse, you need only run `ThePresident` from the package `edu.drexel.psal.anonymouth.gooie` to begin using it.

Please note that there are two main package categories, JStylo and Anonymouth. The majority of Anonymouth developement should be in the Anonymouth packages as Anonymouth simply uses the JStylo libraries for parts of the initial document process, so beginners need only concern themselves with the Anonymouth packages.

Dependencies
------------------------------------------------

Java 7 is required to run Anonymouth. If you don't yet have it, get it at Oracle's website here and follow the installation instructions there: http://java.com/en/download/index.jsp

If you are unsure whether or not you have it installed, follow these steps to see:
* OS X:
	1.  Open up Terminal (Applications/Utilities)
	2.  Type "java -version" without the quotes
	3.  If you see something like `java version "1.7.x_xx"` then you're ready to go! If not, then that means you most likely don't have Java 7 installed, in which case you should go to the download link above
* Windows:
	1.  Follow the instructions here: http://www.java.com/en/download/help/version_manual.xml. if you have version "1.7.x_xx", then you're good to go! If not, then that means you most likely don't have Java 7 installed, in which case you should go to the download link above

If you are using Eclipse, also make sure that Java 7 is your selected compiler by checking `Preferences/Java/Compiler` and is an included Library in your java Build Path (Not sure how to do this? Google is your friend).

Anonymouth requires the included `jsan_resources` directory in it's running directory (The main Anonymouth directory containing lib, src, etc.). It should be in the correct directy by default.

Anonymouth requires a corpus (basically a database of other authors and documents they have written) to run. It needs this so it can classify your documents with respect to these other documents and their styles so that Anonymouth can give you an idea of how anonymous it thinks your document is and what features to remove/add to help you get there. Three different corpi are included in the project directory for you to choose and are located at:

* `./anonymouth/jsan_resources/corpora/amt`
* `./anonymouth/jsan_resources/corpora/drexel_1`
* `./anonymouth/jsan_resources/enron_demo`

Though we included corpi, you are more than welcome to use any other corpus you may have. It is recommended to use many different combinations of authors so you can get the best posisble picture of where your document stands anonymously with respect to others.

Anonymouth also needs the following jars in the lib directory (everything should already be included):

<table>
  <tr>
    <th>Package Name</th><th>Version</th>
  </tr>
  <tr>
    <td>weka</td><td>3.7.9</td>
  </tr>
  <tr>
    <td>fasttag</td><td>2.0</td>
  </tr>
  <tr>
    <td>Jama</td><td>1.0.3</td>
  </tr>
  <tr>
    <td>jaws</td><td>1.3</td>
  </tr>
  <tr>
    <td>jcommon</td><td>1.0</td>
  </tr>
  <tr>
    <td>freechart</td><td>1.0.14</td>
  </tr>
  <tr>
    <td>jgaap</td><td>5.4.0</td>
  </tr>
  <tr>
    <td>microsoft translator</td><td>0.6.1</td>
  </tr>
  <tr>
    <td>miglayout</td><td>4.0</td>
  </tr>
  <tr>
    <td>tt4j</td><td>1.0.15</td>
  </tr>
  <tr>
    <td>Stanford postagger</td><td>-</td>
  </tr>
  <tr>
    <td>ui</td><td>-</td>
  </tr>
</table>

Developers
-------------------------------------------------
P.I.<br>
  * Dr. Rachel Greenstadt: greenie@cs.drexel.edu

Developed by:<br>
  * Andrew W.E. McDonald: awm32@cs.drexel.edu<br>
  * Marc Barrowclift: meb388@cs.drexel.edu<br>
  * Jeff Ulman<br>
  * Joe Muoio<br>

License
-------------------------------------------------

Anonymouth was released by the Privacy, Security and Automation lab at Drexel University in 2011 under the AGPLv3 license. A copy of this license is included with the repository/program. If for some reason it is absent, it can be viewed here: http://www.gnu.org/licenses/agpl.htmla
