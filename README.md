# Anonymouth

![Anonymouth Logo](src/main/resources/graphics/readme_Logo.png)

Document Anonymization Tool, Version 0.5

The Privacy, Security and Automation Lab (PSAL)
Drexel University, Philadelphia PA
<http://psal.cs.drexel.edu/>

## Table of Contents

1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
   - [Dependencies](#dependencies)
   - [Installation](#installation)
   - [How to Build and Run](#how-to-build-and-run)
3. [Usage Guide](#usage-guide)
   - [Start-up](#start-up)
   - [Documents Tab](#documents-tab)
   - [Feature Tab](#feature-tab)
   - [Classifier Tab](#classifier-tab)
   - [Editor Tab I](#editor-tab-i)
   - [Cluster Tab](#cluster-tab)
   - [Editor Tab II](#editor-tab-ii)
   - [Other Notes](#other-notes)
4. [Development](#development)
   - [Basic Logic Flow](#basic-logic-flow)
   - [Basic Class Structure](#basic-class-structure)
   - [To Do List](#to-do-list)
   - [Known Bugs](#known-bugs)
5. [Credits](#credits)
   - [Developers](#developers)
   - [License](#license)

## Introduction

Anonymouth is a Java-based application that aims to give users the tools and knowledge needed to begin anonymizing documents they have written.

It does this by firing up JStylo libraries (an author detection application also developed by PSAL) to detect stylometric patterns and determine features (like word length, bigrams, trigrams, etc.) that the user should remove/add to help obscure their style and identity.

Though Anonymouth and its team works hard to provide you with tools to help remove your identity from documents you have written, **WE CAN IN NO WAY GUARANTEE THAT YOUR DOCUMENT IS ANONYMOUS OR NOT ANONYMOUS**. Anonymouth is always giving you its best guess, its best idea of where your document stands, though that should not at any time be taken as an absolute (for example, you could have forgotten to remove your name from the document and Anonymouth has no way to know that that's your name and should remove it). What we can say is Anonymouth is only as good as you make it, and when used right can be helpful in guiding your document towards the right direction.

## Getting Started

### Dependencies

**Java 8** is required to run Anonymouth. If you don't yet have it, get it at [Oracle's website](http://java.com/en/download/index.jsp) and follow the installation instructions provided there.

If you are unsure whether or not you have it installed, follow these steps to see:

**OS X:**
1. Open up Terminal (Applications/Utilities)
2. Type `java -version` without the quotes
3. If you see something like `java version "1.8.x_xx"` then you're ready to go! If not, then that means you most likely don't have Java 8 installed, in which case you should go to the download link above

**Windows:**
1. Follow the instructions [here](http://www.java.com/en/download/help/version_manual.xml). If you have version "1.8.x_xx", then you're good to go! If not, then that means you most likely don't have Java 8 installed, in which case you should go to the download link above

If you are using Eclipse, also make sure that Java 8 is your selected compiler by checking `Preferences/Java/Compiler` and is an included Library in your java Build Path.

Anonymouth requires the included `jsan_resources` directory in its running directory (The main Anonymouth directory containing lib, src, etc.). It should be in the correct directory by default.

Anonymouth requires a corpus (basically a database of other authors and documents they have written) to run. It needs this so it can classify your documents with respect to these other documents and their styles so that Anonymouth can give you an idea of how anonymous it thinks your document is and what features to remove/add to help you get there. Three different corpora are included in the project directory for you to choose and are located at:

- `./anonymouth/jsan_resources/corpora/amt`
- `./anonymouth/jsan_resources/corpora/drexel_1`
- `./anonymouth/jsan_resources/enron_demo`

Though we included corpora, you are more than welcome to use any other corpus you may have. It is recommended to use many different combinations of authors so you can get the best possible picture of where your document stands anonymously with respect to others.

Anonymouth also needs the following jars in the lib directory (everything should already be included):

| Package Name | Version |
|--------------|---------|
| weka | 3.7.9 |
| fasttag | 2.0 |
| Jama | 1.0.3 |
| jaws | 1.3 |
| jcommon | 1.0.17 |
| jfreechart | 1.0.14 |
| jgaap | 5.2.0 |
| microsoft translator | 0.6.1 |
| miglayout | 4.0 |
| tt4j | 1.0.15 |
| Stanford postagger | 2012-01-06 |
| ui | 1.0 |

### Installation

The quickest and easiest way to install Anonymouth is to clone or download the zip of the [Anonymouth github project](https://github.com/psal/anonymouth).

You can then build and run using Maven:

```bash
./mvnw clean compile
./mvnw exec:java -Dexec.mainClass="edu.drexel.psal.anonymouth.gooie.ThePresident"
```

Alternatively, you can import this project as an existing project into Eclipse (or clone and import directly within Eclipse if you have the Eclipse eGit plugin).

### How to Build and Run

**Using Maven (Recommended):**

1. Navigate to the project directory
2. Run the application:
   ```bash
   ./mvnw clean compile exec:java -Dexec.mainClass="edu.drexel.psal.anonymouth.gooie.ThePresident"
   ```

**Using Eclipse:**

Once Anonymouth is all set up in Eclipse, you need only run `ThePresident` from the package `edu.drexel.psal.anonymouth.gooie` to begin using it.

Please note that there are two main package categories, JStylo and Anonymouth. The majority of Anonymouth development should be in the Anonymouth packages as Anonymouth simply uses the JStylo libraries for parts of the initial document process, so beginners need only concern themselves with the Anonymouth packages.

## Usage Guide

### Start-up

To execute Anonymouth.jar, navigate to the directory it is in (must be in same directory as its folders), and in a terminal, execute:

```bash
java -jar -Xmx512m Anonymouth.jar
```

(you may need to increase the memory to 1024m if using many documents with Writeprints classifier)

### Documents Tab

1. Input one (1) document of approximately 500 words in the leftmost window
2. Input 6500 words of your own writing broken up into ~500 word files (so, about 13 documents) into the middle window
3. Input 6500 words of at least three (3) other people's writing (about 13 500 word documents per person/author) into the rightmost window
4. Click the "add documents" button to select a group of folders, each folder containing one author's documents, to avoid having to first add an author and then that author's documents. NOTE: Doing this will take each folder to be the author's name, and the documents within that folder will be treated as that author's documents
5. Click next (unless you would like to preview any documents)

### Feature Tab

1. Select one of the available feature sets from the drop down menu. At this point, due to the interdependent nature of stylometric features and Anonymouth's stubborn nature, the feature sets may not be modified.
2. Click next

### Classifier Tab

1. Select one of the available classifiers from the top left window. In the bar below, you will see the classifier, and its arguments. You may alter these if you like.
   - NOTE: If unsure of what classifier to use, try either the 'MultiLayerPerceptron' (neural net), or the 'SMO' (SVM).
   - The MultiLayerPerceptron takes more time, and should probably be avoided if the Writeprints feature set has been selected, or if you are using many (over 10) authors.
2. Once satisfied with your classifier, click "Add". You will see the classifier move up to the top right window.
3. Since you may only use one classifier at a time with Anonymouth, click next.

### Editor Tab I

1. Read the message presented, and then click 'Process'. Don't click anything else.
2. The progress bar in the bottom left hand corner will turn to an indeterminate state while Anonymouth is working, and the label above it will change as Anonymouth progresses.
   - NOTE: If it seems as though Anonymouth is taking far too long (this depends on session specific configurations), check the console window to see if an exception has been thrown.
   - In this case, please send any information you can (including the log file in the log directory where Anonymouth resides) to the developers.

### Cluster Tab

1. You will eventually be taken to a colorful screen that visually explains what is going on. The blue dots are the values in other people's documents for the feature specified above the plot; the empty green ellipses are the clusters of those peoples features; the empty black circles are the averages of the elements within the cluster (centroid); the shaded purple ellipses is your confidence interval (average +/- 1.96*stdDev) - for that feature; and the red circle is the value of the document that you want to modify for that feature. The pop-up window explains what to do.
2. Select one of the ordered cluster configurations from the drop down menu. The shaded green ellipses you see show you where your red dot will end up, should you choose to use that configuration, and follow the suggestions that will be given.
   - The idea is to get your red dot as far away from the purple ellipse as possible for each feature, while also taking into account the number of other documents that have feature values in that cluster (more is better)
3. Once satisfied with your cluster group, click the button next to the drop down menu to be taken back to the editor screen.

### Editor Tab II

1. Any document you have processed is no longer editable. However, clicking on almost anything (including the document), will allow you to spawn a new inner tab and begin editing.
2. The table beneath your document shows the probability distribution regarding the author of the document you are modifying as of the last processing. If the document has been classified as belonging to you, the probability is colored red. If not, it is green.
3. Follow the suggestions given in the list in the bottom right hand corner of the outer tab.
4. You may use the provided dictionary to search for potential words.
5. The drop down 'Highlight' menu can also come in handy.
   - All of the colors are somewhat transparent; so, you can overlay things like '3 syllable words' and 'repeated words' to see what the optimal changes are.
   - If using the Writeprints feature set, most of the highlighter functions require you to input one or more letters or words. If it is a 'letter' one, do not include any spaces. If it is a 'word' one, include spaces (how many makes no difference, as long as each word is separated by at least one space). Once you have entered the letter/word you want to find, you MUST PRESS ENTER. Otherwise, nothing will happen.
6. Check your progress by clicking one of the suggestions, and watching its present value move as you type. If it is moving toward your target value, you are doing well.
7. Once you think you may have done enough, or are fed up, click 'Re-process'. You will again be taken to the Cluster Tab, and the process repeats.
8. When the table beneath your document no longer indicates that your document has been classified as belonging to you – depending on the probability of that classification – your document ranges from somewhat anonymized, to completely anonymized. How long you spend, is up to you, and your desired level of anonymity.

### Other Notes

- You can delete a tab you don't want anymore by right clicking on it. The original document cannot be deleted.
- The inner tabs display where the document came from, and what number the document is, e.g. Original -> 2, means that this document spawned from the original one (unmodified), and is the second document you have spawned (in total).
- Each time you process the document (via 'Re-process'), the new clusters are formed. Because of the way the clustering works, you may get slightly different configurations – however, they are still ordered from best > worst. If you have made a reasonable amount of change to your document, the red circle will not be in the same place.
- Should you want to save a copy of a document, click the tab of the document you want to save (to make it active), and then click the save button in the bottom right hand corner of the outer tab.

## Development

### Basic Logic Flow

#### ↓ ---LAUNCH---

**Setup and start up:** `anonymouth.gooie.ThePresident`

Should never be accessed in any other class (or at least limit it). Its only purpose is to initialize the main class and other start up classes.

- Readies and displays splash screen
- Prepares the Logger
- Initializes the `GUIMain` instance (and with it all Anonymouth class instances)
- Displays the start window, which from there it takes over

#### ↓ ---AUTOMATICALLY DISPLAYS---

**Start up window:** `anonymouth.gooie.StartWindow`

The first window that shows up (not counting the splash screen) when Anonymouth loads up. Allows the user to change advanced processing settings, access the pre process set up wizard, or start Anonymouth

#### ↓ ---USER CLICKED START BUTTON---

**Initial document processing begins immediately with:** `anonymouth.engine.DocumentProcessor`

Initialized within GUIMain as should nearly all Anonymouth class instances, this holds the main method and thread that handles processing and reprocessing documents. All processing events can be traced back to this class.

#### ↓ ---PROCESSING COMPLETE---

**Main GUI Code:** `anonymouth.gooie.GUIMain`

The main gui window is displayed. This is also the central "hub" for Anonymouth. This should be the main instance center, and anytime you want to access code from other classes from another class you more than likely will be going through this.

- Houses and initialized nearly all class instances in Anonymouth
- Lays out and creates the main Anonymouth window

**From here the logic flow depends largely on what the user does:**

- If they're editing in the editor, the main class handling that is `anonymouth.googie.EditorDriver`
- If they are using translations the main class handling that is `anonymouth.gooie.TranslationsPanel`
- If they are using word suggestions, the main class handling that is `anonymouth.gooie.WordSuggestionsDriver`
- If they are changing Preferences, the main class handling that is `anonymouth.gooie.PreferencesWindow`
- etc.

### Basic Class Structure

#### ---Naming Convention---

For the most part, Anonymouth splits UI objects into two classes: [Class name]Panel/Window and [Class name]Driver. By convention and a general guideline, the Panel/Window class:

- Creates and lays out all swing components
- Handles all get, set, and is methods (if any)
- Handles assert methods (if any)
- Handles UI update/panel switch methods (if any)

While the corresponding Driver class:

- Handles all listeners
- Handles most backend/data manipulation and updating

Again, these are just general guidelines. Sometimes it makes more sense to just have one class handle everything if it's a small object, or sometimes it doesn't make sense to have a separate Panel/Window class but it does to have a Driver class, etc. You just need to use your best judgment on what will make things more organized and easier to understand.

#### ---Package / Organizing Convention---

Anonymouth loosely follows these guidelines for class organization in packages:

- `anonymouth.engine` For all processing code and any classes you deem "enginey" (for example, `HighlighterEngine`, `VersionControl`, etc.)
- `anonymouth.gooie` For all classes displaying or creating swing components and their respective Driver classes
- `anonymouth.helpers` For classes that aren't necessarily Anonymouth specific, but are used by Anonymouth for general purposes and tasks (for example, `FileHelper`, `ScrollToTop`, `ImageLoader`, etc.)
- `anonymouth.utils` For classes that serve only as a means for storing and manipulating data (For example, `TaggedSentence`, `TaggedDocument`, `Word`, `TextWrapper`, etc.)

There are still quite a few classes that are clearly where they don't belong, so feel free to organize Anonymouth so that it best fits these guidelines.

### To Do List

Add features as they are conceived and ~~strikethrough~~ as they are completed ([1] means most important and [5] means relatively small and not particularly a priority at the moment)

- [1] AUTOMATE AS MUCH OF THE ANONYMIZATION PROCESS AS POSSIBLE. This should be the top priority as of now, see Andrew for the plan and details.
- [1] An internal thesaurus must be implemented to help users change words to remove (that or nicely implement the one built into the system, preferable if possible)
- [2] An intelligent method to search and filter through words to add (start with simple search box, then possibly extend to automatic filtering based on synonyms?)
- [5] The clusters window should be updated to be easier to understand and use (it's hidden away in `Window > Clusters`)

### Known Bugs

Add bugs as they are discovered and ~~strikethrough~~ as they are completed ([1] means fatal or breaks usability and [5] means relatively small and does not have much of an impact on usability)

- [1] During processing on OS X (though the problem may extend to other operating systems as well) the Stanford POS tagger is extremely prone to breaking due to a fatal threading issue which results in heap space or out of memory exceptions. This absolutely MUST be fixed.
- [2] The threading with the words to add refresh is not that great, fails to refresh at times and throws exceptions every once in a while.
- [3] For whatever reason, Anonymouth does not seem to process or recognize all cap words LIKE THIS when working in the editor.
- [5] The max features slider in Preferences does not work as expected at times and is a little finicky.
- [5] Currently Anonymouth is all running on the initial thread which [should NOT be the case](http://docs.oracle.com/javase/tutorial/uiswing/concurrency/initial.html). This should be done in a GUI creation and show task thread, though when I tried this in the past it breaks the splash screen.

## Credits

### Developers

**P.I. Dr. Rachel Greenstadt:**
- Forward questions or concerns pertaining to the lab or its other projects to <greenie@cs.drexel.edu>

**Developed by:**
- **Andrew W.E. McDonald** - Forward questions or concerns pertaining to Anonymouth in general or document processing to <awm32@cs.drexel.edu>
- **Marc Barrowclift** - Forward questions or concerns pertaining to Anonymouth's UI or front end/editor to <meb388@drexel.edu>
- **Jeff Ulman**
- **Joe Muoio**

### License

Anonymouth was released by the Privacy, Security and Automation lab at Drexel University in 2011 under the AGPLv3 license. A copy of this license is included with the repository/program. If for some reason it is absent, it can be viewed [here](http://www.gnu.org/licenses/agpl.html).
