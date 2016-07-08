# DDG-Explorer

DDG Explorer is a tool that allows the user to view and query Data Derivation Graphs (DDGs). It has the following functionality:
* Visualization of DDGs, with the ability to expand and contract portions of the graph to selectively show or hide details.
* Ability to view the data or R functions referenced by pieces of the DDG
* Ability to query a DDG to discover how an input data value gets used, or what data and processing steps lead to the derivation of a particular output value
* Ability to compare R scripts used to generate different DDGs
* Ability to search for where a particular data file is used or generated.

## Build Instructions on Ubuntu (> 16.04)

1. Install 'ant' : `sudo apt-get install ant`
2. Install 'javac' : `sudo apt-get install default-jdk`
3. Clone the project: `git clone https://github.com/End-to-end-provenance/DDG-Explorer.git`
4. Build the src files present in the /src/ directory : `ant build-project`
5. Create the executable jar file : `ant ddg-explorer`
6. The working directory should now contain the jar : `ddg-explorer_{version}.jar`
