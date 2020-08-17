# SCRC Software checklist

This checklist is part of ongoing work on a model scoresheet for SCRC models. It relates to software implementation, and assumes that other documents cover questions about model validation, data provenance and quality, quality of science, and policy readiness.

## Software Details

### Model / software name

> Contact-Tracing-Model

### Date

> 29/07/2020

### Version identifier

> 1.0-SNAPSHOT

## Overall statement

Do we have sufficient confidence in the correctness of the software to trust the results?

This is your overall judgement on the level of confidence based on all the aspects of the checklist. There is no formulaic way to arrive at this overall assessment based on the individual checklist answers but please explain how the measures in place combine to reach this level of confidence and make clear any caveats (eg applies for certain ways of using the software and not others).

> - [ ] Yes
> - [x] Yes, with caveats
> - [ ] No
>
> Documentation needs updating. The data pipeline is not yet being used.  There could be more high-level tests.  Everything else is looking pretty good.

## Checklist

Please use a statement from this list: "Sufficiently addressed", "Some work remaining or caveats", or "Needs to be addressed" to begin each response.

Additionally, for each question please explain the situation and include any relevant links (eg tool dashboards, documentation). The sub bullet points are to make the scope of the question clear and should be covered if relevant but do not have to be answered individually.

### Can a run be repeated and reproduce exactly the same results?

- How is stochasticity handled?
- Is sufficient meta-data logged to enable a run to be reproduced: Is the exact code version recorded (and whether the repository was "clean"), including versions of dependent libraries (e.g. an environment.yml file or similar) along with all command line arguments and the content of any configuration files? 
- Is there up-to-date documentation which explains precisely how to run the code to reproduce existing results? 

> - [ ] Sufficiently addressed
> - [X] Some work remaining or caveats
> - [ ] Needs to be addressed
> 
> Input data and git hash are not yet recorded, but this will soon be addressed with data pipeline integration.  Results are reproducible.  Seeds can be specified up front or recorded. Library versions are listed in build.gradle.  Documentation for how to run the code is up to date.

### Are there appropriate tests?  (And are they automated?)

- Are there unit tests? What is covered?
- System and integration tests?  Automated model validation tests?
- Regression tests? (Which show whether changes to the code lead to changes in the output. Changes to the model will be expected to change the output, but many other changes, such as refactoring and adding new features, should not. Having these tests gives confidence that the code hasn't developed bugs due to unintentional changes.)
- Is there CI?
- Is everything you need to run the tests (including documentation) in the repository (or the data pipeline where appropriate)?

> - [ ] Sufficiently addressed
> - [X] Some work remaining or caveats
> - [ ] Needs to be addressed
> 
> There is ~90% unit test coverage, with tests running in CI, but no regression or top level testing yet.

### Are the scientific results of runs robust to different ways of running the code?

- Running on a different machine?
- With different number of processes?
- With different compilers and optimisation levels?
- Running in debug mode?

(We don't require bitwise identical results here, but the broad conclusions after looking at the results of the test case should be the same.) 

> - [X] Sufficiently addressed
> - [ ] Some work remaining or caveats
> - [ ] Needs to be addressed
> 
> This is straight-forward with Java.

### Has any sort of automated code checking been applied?

- For C++, this might just be the compiler output when run with "all warnings". It could also be more extensive static analysis. For other languages, it could be e.g. pylint, StaticLint.jl, etc.
- If there are possible issues reported by such a tool, have they all been either fixed or understood to not be important?

> - [X] Sufficiently addressed
> - [ ] Some work remaining or caveats
> - [ ] Needs to be addressed
> 
> The code has been run against Sonarqube and any bugs, vulnerabilities and code smells are reviewed and addressed as part of the reviewing process.

### Is the code clean, generally understandable and readable and written according to good software engineering principles?

- Is it modular?  Are the internal implementation details of one module hidden from other modules?
- Commented where necessary?
- Avoiding red flags such as very long functions, global variables, copy and pasted code, etc.?

> - [X] Sufficiently addressed
> - [ ] Some work remaining or caveats
> - [ ] Needs to be addressed
> 
> The code is generally well laid out and self-commenting.

### Is there sufficient documentation?

- Is there a readme?
- Does the code have user documentation?
- Does the code have developer documentation?
- Does the code have algorithm documentation? e.g. something that describes how the model is actually simulated, or inference is performed?
- Is all the documentation up to date? 

> - [ ] Sufficiently addressed
> - [X] Some work remaining or caveats
> - [ ] Needs to be addressed
> 
> There is user, developer and algorithm documentation.  (The developer and technical documentation are the same document.)  It is currently being updated.

### Is there suitable collaboration infrastructure?

- Is the code in a version-controlled repository?
- Is there a license?
- Is an issue tracker used?
- Are there contribution guidelines?

> - [X] Sufficiently addressed
> - [ ] Some work remaining or caveats
> - [ ] Needs to be addressed
> 
> GitHub for issue tracking, reviews, version control and CI, licence included.

### Are software dependencies listed and of appropriate quality?

> - [X] Sufficiently addressed
> - [ ] Some work remaining or caveats
> - [ ] Needs to be addressed
> 
> Libraries are listed in build.gradle and of appropriate quality.

### Is input and output data handled carefully?

- Does the code use the data pipeline for all inputs and outputs?
- Is the code appropriately parameterized (i.e. have hard coded parameters been removed)?

> - [ ] Sufficiently addressed
> - [ ] Some work remaining or caveats
> - [X] Needs to be addressed
> 
> The code is not yet using the data pipeline. It is appropriately parameterized.
