## Spring Batch Demo

### Overview

This demo is built in order to showcase the spring batch capabilities using springboot with java configurations. Some of the examples are taken from the spring batch sample project and are enhanced to display more capabilities.

Here is a list of samples and features each one demonstrates -


| Job        | Feature           |
| ------------- | ------------- |
| normalJob      | This is simple job which read from a file |
| multiThreadedJob      | This job executes the step with chunk in a taskexecutor, and showcases why these job couldn't be restarted as all read, process and write are done in random order      |
| asyncJob | In very typical batch reads are faster whereas the processing is slow. This job showcases how you can process the items parallelly to boost performance      |
| **asyncMultiRecordJob**      | This job reads multiple records in a single read step and then asynchronously processes them. This becomes handy when you want to do another reader based on the records from previous read |
| [**multilineJob**](#multiline)      | This job is enhanced version of out of box demo from spring-batch-sample using spring boot enabling to use a processor  |


#### Multiline

The goal of this sample is to show some common tricks with multiline records in file input jobs.

The input file in this case consists of two groups of trades
delimited by special lines in a file (BEGIN and END):

    BEGIN
    UK21341EAH4597898.34customer1
    UK21341EAH4611218.12customer2
    END
    BEGIN
    UK21341EAH4724512.78customer3
    UK21341EAH4810809.25customer4
    UK21341EAH4985423.39customer5
    END
    BEGIN
    UK21341EAH4724512.78customer6
    UK21341EAH4810809.25customer7
    UK21341EAH4985423.39customer8
    END

spring-sample-batch output - do not separates the multiline read in separate line, and no processing can be done

    [Trade: [isin=UK21341EAH45,quantity=978,price=98.34,customer=customer1,processed=false], Trade: [isin=UK21341EAH46,quantity=112,price=18.12,customer=customer2,processed=false]]
    [Trade: [isin=UK21341EAH47,quantity=245,price=12.78,customer=customer3,processed=false], Trade: [isin=UK21341EAH48,quantity=108,price=9.25,customer=customer4,processed=false], Trade: [isin=UK21341EAH49,quantity=854,price=23.39,customer=customer5,processed=false]]
    [Trade: [isin=UK21341EAH47,quantity=245,price=12.78,customer=customer6,processed=false], Trade: [isin=UK21341EAH48,quantity=108,price=9.25,customer=customer7,processed=false], Trade: [isin=UK21341EAH49,quantity=854,price=23.39,customer=customer8,processed=false]]

we enhanced this job in order to  - 
  * single record per line 
  * enables processing of each item
  * processor can be asynchronous (refer asyncMultiRecordJob)

thus we have the below output -

    Trade: [isin=UK21341EAH45,quantity=978,price=98.34,customer=customer1,processed=true]
    Trade: [isin=UK21341EAH46,quantity=112,price=18.12,customer=customer2,processed=true]
    Trade: [isin=UK21341EAH47,quantity=245,price=12.78,customer=customer3,processed=true]
    Trade: [isin=UK21341EAH48,quantity=108,price=9.25,customer=customer4,processed=true]
    Trade: [isin=UK21341EAH49,quantity=854,price=23.39,customer=customer5,processed=true]
    Trade: [isin=UK21341EAH47,quantity=245,price=12.78,customer=customer6,processed=true]
    Trade: [isin=UK21341EAH48,quantity=108,price=9.25,customer=customer7,processed=true]
    Trade: [isin=UK21341EAH49,quantity=854,price=23.39,customer=customer8,processed=true]

### References
  * Spring Batch Samples - https://github.com/spring-projects/spring-batch/tree/main/spring-batch-samples
