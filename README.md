# Contact-Tracing-Model
Derived from the FMD model and contact data from Sibylle.

## Background

This model has been derived from discussions ongoing as part of the RAMP collaborations to support modelling of the COVID-19 pandemic. 
This model uses SEIR compartments and a network of contact data to spread the infection.  

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

## Inputs

There are two input files that are user editable for starters. They are JSON files that contain:

### General Settings

```json
{
  "populationSize": 10000,
  "infected": 1000,
  "sid": 0,
  "timeLimit": 100,
  "steadyState": true
}
```

* **populationSize**: the number of people
* **infected**: the initial number of infections
* **sid**: the random number seed used for this run. 
* **timeLimit**: The max time it can run, regardless of contact data provided.
* **steadyState**: if the model continues when to a steady state when it has run out of contact data. 

### Population Demographics
```json
{
  "populationDistribution": {
    "0": 0.1759,
    "1": 0.1171,
    "2": 0.4029,
    "3": 0.1222,
    "4": 0.1819
  },
  "populationAges": {
    "0": {
      "min": 0,
      "max": 14
    },
    "1": {
      "min": 15,
      "max": 24
    },
    "2": {
      "min": 25,
      "max": 54
    },
    "3": {
      "min": 55,
      "max": 64
    },
    "4": {
      "min": 65,
      "max": 90
    }
  },
  "genderBalance": 0.99
}
```

NB this is hardcoded to 5 bins, starting at index 0. 

* **populationDistributions**: the proportion of the population in each bin
* **populationAges**: the widths of each population bin
* **genderBalance**: the proportion of men to women across the population

The data has been taken from the index mundi data found [here](https://www.indexmundi.com/united_kingdom/demographics_profile.html)

## Outputs

Besides the fairly coarse console output, a CSV file called "SEIR.csv" is output that contains the SEIR numbers for each day. 

```csv
Day,S,E,I,R
0,9000,0,1000,0
1,8562,438,1000,0
2,8241,759,1000,0
.....
87,3034,4,25,6937
88,3033,4,23,6940
89,3030,4,22,6944
```

## Build/Test/Run Guide

To compile the project without running tests:
```shell script
gradle assemble
```

To compile and run the tests (there are none at present :-/):
```shell script
gradle build
```

To run the project:
```shell script
gradle run
```

## Version History

0.1 - the initial implementation of an SEIR model


