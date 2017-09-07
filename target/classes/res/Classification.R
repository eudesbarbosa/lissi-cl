###################################################################################
### Author: Anne-Christin Hauschild 
### Email: a.hauschild@mpi-inf.mpg.de
### Date:   2013-08-29
### Motivation: Script to run classification using Random Forest
###################################################################################
# Parallel execution requirements (Linux only)
suppressMessages(require(foreach))
suppressMessages(require(doMC) )

###################################################################################
### Function runs classification using Random Forest
### data
### dataset
### saveDir 
### src
### k
### numberOfRuns
### myntree
### cores
###################################################################################
rf.Classification=function(data, dataset, saveDir, src, k, numberOfRuns, myntree, cores,...){
  # Source all complemantary functions
  source(paste(src,"useful_with_BoxPlot.R",sep=""))
  source(paste(src,"method_randomForest.R",sep=""))
  source(paste(src,"helper_methods.R",sep=""))
  
  
  registerDoMC(cores) #change  to number of cores you want to use
  
  ## Define Standard folders
  result_path <- paste(saveDir,"/summary/",dataset,"/", sep="")
  if(!file.exists(result_path)){
    dir.create(path=result_path,recursive=TRUE)
  }
  cvset_path <- paste(saveDir,"/cvsets/",dataset,"/", sep="")
  if(!file.exists(cvset_path)){
    dir.create(path=cvset_path,recursive=TRUE)
  }
  resultplot_path <- paste(saveDir,"/plots/",dataset,"/", sep="")
  if(!file.exists(resultplot_path)){
    dir.create(path=resultplot_path,recursive=TRUE)
  }
  featSelection_path <- paste(saveDir,"/features/",dataset,"/", sep="")
  if(!file.exists(featSelection_path)){
    dir.create(path=featSelection_path,recursive=TRUE)
  }
  
  # Read Data
  label <- as.factor(as.character(data[,2]))
  #print(paste("@@@@@@@ ",ncol(data), data[1,2], sep="\t"))
  if (ncol(data) > 3) {
    data <- data[,3:ncol(data)]
  } else if (ncol(data) == 3){
    data <- data[,3]
  } else {
    return();
  }
  
  randlabelfile=paste(cvset_path,"CV-Sets_randomlabel.RData", sep="")
  if(file.exists(randlabelfile)){
    getlabel <- load(randlabelfile)
    randomlabel <- get(getlabel[1]);
    names(randomlabel)<- rownames(data)
  } else {
    randomlabel <- sample(label)
    names(randomlabel)<- rownames(data)
    save(randomlabel, file=randlabelfile)
  }
  ## Since the cvset is stored, the corresponding random labels need to be stored as well.
  randomlabel= getRandomLabel(label, randlabelfile)
  numberOfClasses= length(unique(label));
  
  ###################################################################################
  ###   Random Forest   #############################################################
  ###################################################################################			
  result<-list()
  features <- list()
  i<-1
  foreach(i=1:numberOfRuns, .packages = "randomForest") %dopar% { #exchange %dopar% with %do% to avoid parallel computing
    cvsetname=paste(cvset_path,"CV-Sets_", k,"-fold_",length(label),"_",i,".RData", sep="")
    filename<-paste(result_path, "Random-Forest-", numberOfRuns, "-RealL_",dataset,"_", k,"-fold",i,".RData", sep="")
    ## Run random forest 
    #print("[R] Run Random Forest on classification problem - real data")
    result <- (runCrossvalidation(data, label, cvsetname, k, slfun=rf.train, predictfun=rf.predict, type="response",myntree=myntree,mymtry=10))
    save(result, file=filename)
    ## Extract features
    load(filename)
    l <- length(result$models)
    for(m in 1:l){
      featureFile<-paste(featSelection_path,"Random-Forest_RunNumber_", i, "_",dataset,"_", k,"-fold_",m,".csv", sep="");		
      res <- result$models[[m]];
      import <- as.matrix(res$importance);
      write.table(import,file=featureFile,sep=";",dec=".",row.names = TRUE,col.names = FALSE, quote = FALSE);
    }
  }
  
  ###################################################################################
  ###   Random Forest on Random Labels   ############################################
  ###################################################################################
  foreach(i=1:numberOfRuns, .packages="randomForest") %dopar% {
    cvsetname=paste(cvset_path,"RandomLabel-CV-Sets_", k,"-fold_",i,".RData", sep="")
    filename<-paste(result_path, "Random-Forest-", numberOfRuns, "-RandomL_",dataset,"_", k,"-fold",i,".RData", sep="")
    ## Run random forest 
    #("[R] Run Random Forest on classification problem - random labels")
    result <- (runCrossvalidation(data, randomlabel, cvsetname,k, slfun=rf.train, predictfun=rf.predict, type="response",myntree=myntree,mymtry=10))
    save(result, file= filename)
  }
  
  ###################################################################################
  ###   RUN EVALUATION   ############################################################
  ###################################################################################
  filename <- paste(resultplot_path,dataset,"_roc-plot_Data",  "_", numberOfRuns, "-class_", k,"-fold.png", sep="")
  retval=evaluateROC(result_path, selecter=dataset, format="png",  legendformat=c("names", "AUC"), filename=filename);
  write.table(retval, file=paste(result_path, "ClassificationResults_", dataset,".csv", sep=""), row.names=FALSE, col.names=TRUE, sep=",")
  retval <- as.matrix(retval)
  a<-paste("Mean AUC for Real Label:",aggregate(as.numeric(retval[,"AUC"]), by=list(as.character(retval[,"Method"])), FUN=mean)[2,2],sep=" ")
  b<-paste("Mean AUC for Random Label:",aggregate(as.numeric(retval[,"AUC"]), by=list(as.character(retval[,"Method"])), FUN=mean)[1,2],sep=" ")
  #print(paste(a,b,sep="@"))
  
  ###################################################################################
  ###   FEATURE SELECTION   #########################################################
  ###################################################################################
  ## Select all '.csv' files from a folder
  X <- data.frame()
  files <- list.files(path=featSelection_path,pattern=".csv$",full.names = TRUE) 
  for(i in files) {
    Xa <- as.data.frame(read.csv(i,h=F,sep=";"));
    Xa <- Xa[rev(order(as.numeric(Xa$V5)))[1:25],] #select 25 largest 
    X<-rbind(X,Xa)
  }
  colnames(X) <- c("cluster","0","1","MeanDecreaseAccuracy","MeanDecreaseGini")
  selectionFile <- paste(saveDir,"/",dataset,"_feature_selection_gini_index.csv", sep="")
  write.table(X,file=selectionFile,sep=";",dec=".",row.names = FALSE,col.names = FALSE, quote = FALSE);
  
  filename<-paste(resultplot_path,"/",dataset, "_feature_selection.pdf", sep="")
  pdf(filename, width=8, height=8)
  plotTopFeatures(X)
  invisible(dev.off())
  
  # Clean variables (not functions)
  rm(list = setdiff(ls(), lsf.str()))
  
}