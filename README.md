# What is LiSSI?

LiSSI stands for LifeStyle-Specific-Islands; and it is a bioinformatic tool
developed to facilitate the identification of genomic features that might
influence bacterial adaptation to specific niches.

LiSSI uses a comparative method: it is dedicated (and limited) to detecting
genetic features that differ between two sets of species, i.e. genes or
genomic islands appearing in most species of one lifestyle but rarely in any
species of the other lifestyle. Genetic elements that are not conserved among
species of one of the two sets may remain undetected.


### Quick start
Modify 'lissi-conf.xml' to contain your own paths. On the terminal use the
following code to start LiSSI:
```{bash}
java -jar LiSSI_commandline_beta.jar -c lissi.conf
```
### Links
- [Paper](https://doi.org/10.1515/jib-2017-0010)
- [(GUI) Tutorial]
(https://lissi.compbio.sdu.dk/tutorial.html)
- [On the news]
(http://sciencediscoveries.degruyter.com/exploring-genetic-background-bacterial-lifestyles-lissi/)

---

## System Requirements

### Applications
- Java 7+ :
[All Platforms](https://www.java.com/en/download/help/index_installing.xml?j=7)
- BLAST :
   [All Platforms](ftp://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/LATEST/)
- OpenMPI :
  [Unix](https://lissi.compbio.sdu.dk/install.mpi.html)
- R :
  [Unix](https://lissi.compbio.sdu.dk/install.r.html)

### R packages
LiSSI requires quite the extensive list of packages. To automatically install
them follow the commands on this [link](https://lissi.compbio.sdu.dk/Download/R/install.packages.R).

- caret
- doMC
- gdata
- ggplot2
- pmml
- pROC
- e1071
- foreach
- rattle
- ROCR
- rpart
- snow
- stringr
- varSelRF
- XML
