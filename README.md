# Contact-Tracing-Model

![Java CI with Gradle](https://github.com/ScottishCovidResponse/Contact-Tracing-Model/workflows/Java%20CI%20with%20Gradle/badge.svg)

Derived from the FMD model and contact data from Sibylle.

## Overview

The Contact-Tracing Model is an individual-based stochastic network model which has been set-up within the RAMP / SCRC collaborations to support modelling of the COVID-19 pandemic. It comprises of S-E<sub>1</sub>-E<sub>2</sub>-I<sub>asymp</sub>-I<sub>symp</sub>-I<sub>sev</sub>-D-R disease progression compartments and operates on a network of contact data to spread the infection. There is no explicit spatial scale in the model, but some form of 'location' information is retained (e.g. where the contact happened, i.e. at school, work, home,...). The advantage of the model is that we can simulate flexible contact-tracing, with a focus on mobile contact-tracing apps. Nevertheless, the model can also be used to investigate the manual track-and-trace programme scenarios (with delayed contact-tracing). The aim of the model is to explore contact tracing policies and how best to identify and isolate infectious people. Our goals are to determine how effective contact tracing can be (given app uptake and compliance) and how much efficacy can be retained when also targeting and testing to reduce the number of people in isolation. Further documentation on the epidemiological model and the java build can be found in the 'docs' folder.


## Running Pre-requisites
### Java SDK
Please ensure you have a Java SDK installed. I recommend Java 11 as it is the current LTS version.

### Gradle
To compile, run or test this model, Gradle must be installed. This manages the libraries utilised and simplifies the build process.

#### For MacOS:
I recommend using [homebrew](www.brew.sh). 

To install homebrew:
```shell script
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh)"
```

To install Gradle:
```shell script
brew install gradle
```

#### For Debian, Ubuntu, Mint:
```shell script
sudo apt install gradle
``` 

#### For RedHat, Centos, Fedora:
```shell script
yum install gradle
```

#### For Windows:

Follow instructions [here](https://gradle.org/install/).

## Infection Map

The infection map shows how a single individual passes the infection to others sets of people. The receiving set is denoted in the square brackets. The depths of the tabbing shows how far away from the source case it is. 

```
8194  ->  [2135]
      ->  2135   ->  [3809, 2694, 6711]
         ->  3809   ->  [4753]
            ->  4753   ->  [9536]
               ->  9536   ->  [4035, 222]
                  ->  222   ->  [260, 3239]
                     ->  3239   ->  [6272]
         ->  6711   ->  [7153, 1922]
            ->  7153   ->  [2733]
               ->  2733   ->  [7984]
            ->  1922   ->  [8859]
```


## Build/Test/Run Guide

To compile the project without running tests:
```shell script
gradle assemble
```

To compile and run the tests:
```shell script
gradle build
```

To run the project:
```shell script
gradle run
```

To run with command line arguments:
```shell script
gradle run --args='--overrideInputFolderLocation=input/scenarios/scenario1a_tracinglevel1 --overrideOutputFolderLocation=output0 --seed=3
```

## Version History

0.1 - the initial implementation of an SEIR model

1.0 - new Schema with basic alerting

1.1 - removing some of the out of date input data. This is covered in the build docs.


