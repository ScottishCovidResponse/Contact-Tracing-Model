# Contact-Tracing-Model

![Java CI with Gradle](https://github.com/ScottishCovidResponse/Contact-Tracing-Model/workflows/Java%20CI%20with%20Gradle/badge.svg)

Derived from the FMD model and contact data from Sibylle.

## Background

This model has been derived from discussions ongoing as part of the RAMP collaborations to support modelling of the COVID-19 pandemic. 
This model uses S-E<sub>1</sub>-E<sub>2</sub>-I<sub>asymp</sub>-I<sub>symp</sub>-D-R compartments and a network of contact data to spread the infection.  

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

There are three input files that are user editable for starters. They are JSON files that contain:

### General Settings

```json
{
  "populationSize": 10000,
  "timeLimit": 1000,
  "infected": 10,
  "seed": 123,
  "steadyState": true,
  "contactsFile": "input/homogeneous_contacts.csv"
}
```

* **populationSize**: the number of people
* **infected**: the initial number of exposed individuals (TODO: update name)
* **seed**: the random number seed used for this run. 
* **timeLimit**: The max time it can run, regardless of contact data provided.
* **steadyState**: if the model continues when to a steady state when it has run out of contact data. 
* **contactsFile**: the location of the contacts.csv input file

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

## Contacts.csv

This file describes the networks and the interactions between individuals

```csv
"time","from","to","weight"
1,9999,10000,6.71441028630399
1,9998,9999,8.27809024361994
1,9997,9998,1.75194953106576
1,9996,9997,8.28598198646246
```

* **time**: the time the interaction occurs
* **from**: the initiator of the interaction
* **to**: the receipient of the interaction
* **weight**: the relative strength of the interaction, high may be family, low may be shop worker


## Outputs

Besides the fairly coarse console output, a CSV file called "Compartments.csv" is output that contains the SEIR numbers for each day. 

```csv
"time","s","e1","e2","ia","is","r","d"
0,9990,10,0,0,0,0,0
1,9984,14,2,0,0,0,0
2,9980,12,6,2,0,0,0
3,9975,14,8,2,0,1,0
```

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

1.0 - new Schema with basic alerting


