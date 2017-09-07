###################################################################################
### Author: Eudes Barbosa
### Email: eudes@imada.sdu.dk
### Date:   2015-05-19
### Motivation: Script used to create histogram for TransClust results
###################################################################################
# Disable scientific notation
options(scipen=999) 
# Read parameters table
options <- commandArgs(trailingOnly = TRUE)
parameters <- read.table(noquote(options[1]), h=TRUE, sep=";")
#parameters <- as.data.frame(read.table("/media/eudesbarbosa/INTENSO/DK/Computational_Biology/LiSSI_2015/Actinobacteria/Results/Non-Pathogens_vs._Pathogens/RandomForest/parameters.tab", h=TRUE, sep=";"))
#parameters <- as.data.frame(read.table("/home/eudesbarbosa/temp/LISSI/parameters.tab", h=TRUE, sep=";"))
###################################################################################
### EXTRACT VARIABLES
look.for <- c("cores")
cores <- as.numeric(as.character(parameters[parameters$parameters %in% look.for, 2]))
#
look.for <- c("kfold")
kfold <- as.numeric(as.character(parameters[parameters$parameters %in% look.for, 2]))
#
look.for <- c("trees")
trees <- as.numeric(as.character(parameters[parameters$parameters %in% look.for, 2]))
#
look.for <- c("runs")
runs <- as.numeric(as.character(parameters[parameters$parameters %in% look.for, 2]))
#
look.for <- c("matrix")
matrix <- as.character(parameters[parameters$parameters %in% look.for, 2])
#
look.for <- c("dir")
saveDir <- as.character(parameters[parameters$parameters %in% look.for, 2])
#
look.for <- c("src")
src <- as.character(parameters[parameters$parameters %in% look.for, 2])


###################################################################################
### RUN SCRIPTS FOR FULL DATASET

# Source required scripts
source(paste(src,"Classification.R",sep=""))
source(paste(src,"Feature_Selection.R",sep=""))
source(paste(src,"Decision_Tree.R",sep=""))

# Run classification for Full dataset
data <- read.table(matrix, sep=";", h=T)
dataset <- "FullData"
print("[R] Running classification with Random Forest for Full Data")
rf.Classification(data, dataset, saveDir, src, kfold, runs, trees, cores)
print("[R] Running feature selection with Random Forest for Full Data")
featuresFile <- rf.FeatureSelection(data, dataset, saveDir, trees, cores)
print("[R] Creating Decision Tree for Full Data")
rf.DecisionTree(dataFile=featuresFile, dataset,saveDir)

#if (FALSE) {
###################################################################################
### SPLIT DATASET (INTRODUCE BIAS)

# Percentage of presence in cluster
result <- data.frame(matrix(ncol=2))  #R has a bug with row with only zeros 
for (i in 3:ncol(data)) {   #'A' = name of data object (matrix)
  # Generates contingency table with values of number of lifestyle with number of clusters
  counts <- as.data.frame(table(data[,i], data[,2])) 
  vec <- numeric()
  if (length(unique(data[,i])) != 1) {
    # Extract all necessary information from each 'i' counts table 
    for (j in seq(from=2, to=nrow(counts),by=2)) {
      l = j - 1
      a <- (counts[j,3])/((counts[l,3])+(counts[j,3]))   #each % in contingency table
      vec <- append(vec, a, after = length(vec)) 	#add each value to 'vec' 
    }
  } else if (sum(as.numeric(data[,i])) != 0) {  #special for columns with only '1'
    for (v in seq(from=1, to=nrow(counts),by=1)) {
      u <- (counts[v,1]) 	#each value in the table
      vec <- append(vec, u, after = length(vec)) 	#add each value to 'vec' 
      vec <- append(vec, u, after = length(vec))  #since value is the same it is added twice
    }
  }else{ # special for columns with only '0'
    vec <- rep(0,nrow(counts))
  }
  result <- rbind(result,vec);
}
# Extract lifestyles names
nam <-as.matrix(unique(data[,2]))	
# Change column name from 'result' to lifestyles names 
for(m in 1:length(nam)){
  colnames(result)[m] <- gsub("\\s","_", nam[m]) 
}
# Seting row names as clusters number
rownames(result) <- c(colnames(data[2:ncol(data)]))
result <- result[-1,] # Removing first (and unnecessary) row - Bug solving
# Set data to percentage
plife <- as.matrix(result)
plife <- (plife[which(rowSums(plife) > 0),])*100 #remove eventual rows with only zeros
# Selecting clusters more representative from each Lifestyle
plifeX <- plife[which(plife[,1]>plife[,2]),] 
plifeY <- plife[which(plife[,2]>plife[,1]),] 
# Selecting clusters from the original table (data)
xx <- rownames(plifeX) 
subsetX <- cbind( (data[,1:2]) , (data[,which(colnames(data) %in% c(xx))]) )
yy <- rownames(plifeY) 
subsetY <- cbind( (data[,1:2]) , (data[,which(colnames(data) %in% c(yy))]) )

###################################################################################
### RUN SCRIPTS FOR X DATASET
dataset <- colnames(plife)[1]
print(paste("[R] Running classification with Random Forest for lifestyle", dataset, sep=" "))
rf.Classification(data=subsetX, dataset, saveDir, src, kfold, runs, trees, cores)
print(paste("[R] Running feature selection with Random Forest for lifestyle", dataset, sep=" "))
featuresFile <- rf.FeatureSelection(data=subsetX, dataset, saveDir, trees, cores)
print(paste("[R] Creating Decision Tree for lifestyle", dataset, sep=" "))
rf.DecisionTree(dataFile=featuresFile, dataset, saveDir)

###################################################################################
### RUN SCRIPTS FOR Y DATASET
dataset <- colnames(plife)[2]
print(paste("[R] Running classification with Random Forest for lifestyle", dataset, sep=" "))
rf.Classification(data=subsetY, dataset, saveDir, src, kfold, runs, trees, cores)
print(paste("[R] Running feature selection with Random Forest for lifestyle", dataset, sep=" "))
featuresFile <- rf.FeatureSelection(data=subsetY, dataset, saveDir, trees, cores)
print(paste("[R] Creating Decision Tree for lifestyle", dataset, sep=" "))
rf.DecisionTree(dataFile=featuresFile, dataset, saveDir)

#}