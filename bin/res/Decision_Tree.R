###################################################################################
### Author: Eudes Barbosa
### Email: eudes@imada.sdu.dk
### Date:   2015-01-12
### Motivation: Script used to generation a decision tree for the subset.
###################################################################################
# Load libraries
suppressMessages(library(rpart))
suppressMessages(library(XML))
suppressMessages(library(pmml))
suppressMessages(library(rattle))

rf.DecisionTree=function(dataFile, dataSet, savDir,...){
  #
  saveFile <- paste(savDir,"/plots/",dataSet,"/","dt",sep="")
  
  # Read table
  data <- as.data.frame(get(load(dataFile)))
 # data <- read.table(dataFile,sep=";",h=T)
 # data <- data[,2:ncol(data)]
  fit <- rpart(data$label ~.,method="class",data=data, control=rpart.control(minsplit=2, minbucket=1, cp=0.001))
  # Grow Tree
 ##################
 # Change this!!!!
 ##################
 # fit <- rpart(data$label ~.,method="class",data=data)
  # Prune the tree
  pfit<- prune(fit, cp=fit$cptable[which.min(fit$cptable[,"xerror"]),"CP"])
  #plotcp(pfit) # visualize cross-validation results
  #printcp(pfit) # display the results
  #summary(pfit) # detailed summary of splits
  # Plot Tree
  # Each node box displays the classification, the probability of
  # each class at that node (i.e. the probability of the class conditioned
  # on the node) and the percentage of observations used at that node. 
  png(paste(saveFile,".png",sep=""), 
      width     = 600,
      height    = 450,
      units     = "px") # save figure start
      #fancyRpartPlot(fit,main="",sub="")  #root+leafs
      #if(FALSE) {
  if(length(pfit$frame$yval[]) > 1) {
    fancyRpartPlot(pfit,main="",sub="")  #root+leafs
  } else {
    plot(1, type="n", axes=F, xlab="", ylab="", main="Fit is not a tree, just a root")  #only root (empty)
  }
  #}
  invisible(dev.off()) # save figure end
  # Save PMML
  # http://www.dmg.org/v4-0-1/TreeModel.html
  invisible(saveXML(pmml(fit),file=paste(saveFile,".xml",sep="")));
}