###################################################################################
### Author: Eudes Barbosa
### Email: eudes@imada.sdu.dk
### Date:   2014-01-20
### Motivation: Script to run feature selectio using Random Forest (varSelRF)
###################################################################################
# Load libraries
suppressMessages(library(snow))
suppressMessages(library(stringr))
suppressMessages(library(varSelRF))

rf.FeatureSelection=function(data, dataset, saveDir, myntree, cores,...){  
  featSelection_path <- paste(saveDir,"/features/",dataset,"/", sep="")
  # Read Data
  label <- as.factor(as.character(data[,2]))
  #print(paste("@@@@@@@ ",ncol(data), sep=""))
  if (ncol(data) > 3) {
    data <- data[,3:ncol(data)]
  } else {
    data <- data[,3]
  }
  # Run Bootstrap Selection RF with multiple cores
  if (cores > 1) {
    cl <- makeMPIcluster(cores)
    data.rf <- varSelRFBoot(data, label, bootnumber=10, usingCluster=T, 
                            TheCluster=cl, 
                            keep.forest=T,vars.drop.frac = 0.2, ntree = myntree)
    cols <- data.rf$all.data.vars;
    bootstrapFile <- paste(featSelection_path,dataset,"_FS_result_bootstrap.RData",sep="")
    save(data.rf, file=bootstrapFile)
    stopCluster(cl)
    # Run Bootstrap Selection RF with single cores
  } else if (cores==1) {
    data.rf <- varSelRFBoot(data, label, bootnumber=10, usingCluster=F, 
                            keep.forest=F,vars.drop.frac = 0.2, ntree = myntree)
    cols <- data.rf$all.data.vars;
    bootstrapFile <- paste(featSelection_path,dataset,"_FS_result_bootstrap.RData",sep="")
    save(data.rf, file=bootstrapFile)
  }
  # Save best features
  feature.selected <- cbind(label,data[,which(colnames(data) %in% c(cols))])
  rapidFile <- paste(saveDir,"/",dataset,"_bootstrap_selected_features.RData",sep="")
  save(feature.selected,file=rapidFile)
  # Save Mean Decrease Gini Index
  table <- as.data.frame(data.rf$all.data.randomForest$importance)
  meanDecrGini <- paste(saveDir,"/plots/",dataset,"/MeanDecreaseGini_features.csv",sep="")
  write.table(table, file = meanDecrGini, append = FALSE, quote = FALSE, sep = "\t",
              eol = "\n", na = "NA", dec = ".", row.names = TRUE,
              col.names = FALSE, qmethod = "double"
              )
  #
  return(rapidFile)
}
