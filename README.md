# stife

Purpose:

Implementation of the STIFE framework in the Research Project concerning Sequences of temporal Intervals.
The corresponding paper was submitted for the discovery science conference.


Quickstart:

Stife was developed as an eclipse project, so the simplest way to get it running is to import it in eclipse:
File -> Import -> Git -> Projects from Git -> clone URI
The experiments can then be executed by running the main methods in experiment.RuntimeComparisonRealDatasets and experiment.RuntimeComparisonSyntheticDatasets respectively.
In order to successfully run the classification algorithms for the real datasets the java process should be given 4GB Main Memory since large data structures will bee needed (at least for the multi-labeled datasets ASL-BU and ASL-BU-2).

