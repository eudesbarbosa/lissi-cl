###################################################################################
### Author: Anne-Christin Hauschild
### Date:   2013-08-29
###################################################################################


###################################################################################
###   REQUIRED PACKAGES:  #########################################################
###################################################################################

library(caret)
library(rpart)
library(gdata)
library(ROCR)
library(pROC)
library(graphics)



###################################################################################
###   PREDICTION FUNCTIONS   ######################################################
###################################################################################


###################################################################################
### Generate a Class Balanced CrossValidation Set (Only for Classification)########
### Parameter:
### k = number of Folds
### labels for classification
### example: sets <- giveBalancedCrossValidationSet(k=10, label)
###################################################################################
giveBalancedCrossValidationSet=function(k, label){
  classes<-unique(label)
  
  classlabels<-list()
  classnum<-list()
  # get idset for each class
  for(c in classes){
    classlabels[c]<-list(which(label==c))
    classnum[c]<-length(which(label==c))
  }
  sets <- list()
  # create the kth set
  for (i in k:1){
    # select number of samples per class / k samples for this set
    thisset<-c()
    for(c in classes){
      perset<-floor(length(classlabels[[c]])/i)
      #print(paste(i," + ", c, " - >", perset))
      
      ids <- sample(length(classlabels[[c]]),perset )
      #print(ids)
      thisset <- c(thisset, classlabels[[c]][ids])
      classlabels[[c]] <- classlabels[[c]][-ids]
    }
    sets[i]<-list(thisset)
  }
  return(sets)
}

getRandomLabel=function(label, randlabelfile){
  if(file.exists(randlabelfile)){
    getlabel <- load(randlabelfile)
    randomlabel <- get(getlabel[1]);
    names(randomlabel)<- names(label)
  }else{
    randomlabel <- sample(label)
    names(randomlabel)<- names(label)
    save(randomlabel, file= randlabelfile)
  }
  return(randomlabel);
}

###################################################################################
### Run Cross Validation on given data and class label (Classification) ########
### Parameter:
### data = matrix of features: samples=rows, features=columns
### lab = class label with: ALL names(label) ELEMENT OF  rownames(data) |( rownames(data), not in names(label) are not used.)
### cvsetname = specific (balanced) crossvalidation set used.
### k = number of Folds
### slfun = statistical function to train the model
### predictfun = matching prediction function used.
### ... = parameters for the prediction function
### example: r <- runCrossvalidation(data, label, cvsetname, k, slfun=linsvm.train, predictfun=linsvm.predict, myC = 100, myT=0.01)
###################################################################################

runCrossvalidation= function(dat, lab, cvsetname=paste(cvset_path, k,"fold-set.RData", sep=""), k, slfun, predictfun, ...){
  if(file.exists(cvsetname)){
    f <- load(cvsetname)
    sets <- get(f[1]);
  } else {
    sets <- giveBalancedCrossValidationSet(k, lab)
    save(sets, file=cvsetname)
  }
  classes<-unique(lab)
  avgacc<-0
  #Crossvalidation
  models <- list()
  predictions <- list()
  probabilities <- list()
  labels <- list()
  idx <- 1
  for(i in sets){
    ## Select Train and Testset
    test.data <- dat[i,]
    test.label<- lab[i]
    labels[[idx]]<- factor(test.label)
    train.data <- dat[-i,]
    train.label <- lab[-i]
    if(FALSE){ #"Commenting" this part of the code
    v <- length(unique(train.data[i]))
    ## Feature that do not vary, are removed (Otherwise errors occur!)
    if(sum(v==0)){
      train.data <- train.data[v != 1]
      test.data <-  test.data [v != 1]
    }}
    ## create Model and Predict
    res <- slfun(train.data, train.label, ...)
    models[[idx]]<- res
    p <- predictfun(res, test.data)
    prob <- predictfun(res, test.data, type="prob")
    predictions[[idx]]<- p
    probabilities [[idx]]<- prob
    idx<-idx+1
  }
  ## Save to retval variable and return.
  retval <- list()
  retval$models <- models
  retval$predictions <- predictions
  retval$probabilities <- probabilities
  retval$labels <- labels
  return(retval)
}

###################################################################################
### Run classification without CrossValidation ########
### Parameter:
### dat = matrix of features: samples=rows, features=columns
### lab = class label with: ALL names(label) ELEMENT OF  rownames(data) |( rownames(data), not in names(label) are not used.)
### slfun = statistical function to train the model
### predictfun = matching prediction function used.
### ... = parameters for the prediction function
###################################################################################
runTrainingsError= function(dat, lab, slfun, predictfun, ...){
  v <-apply(dat, 2, var)
  if(sum(v==0)){
    dat <- dat[v != 0]
    paste("dat[v==0]")
    paste(dat[v==0])
  }
  res <- slfun(dat, lab, ...)
  model<- res
  predictions <- predictfun(res, dat)
  retval <- list()
  retval$model <- model
  retval$predictions <- predictions
  retval$labels <- lab
  return(retval)
}


## Error functions:
errorferr = function(p,a){return(min(performance(prediction(p, a), 'err')@y.values[[1]]))}

errorfauc = function(p,a){return((performance(prediction(p, a), 'auc')@y.values[[1]]))}

errorfacc = function(p,a){return((performance(prediction(p, a), 'acc')@y.values[[1]]))}

errormult = function(lab,pred){ return(sum(lab == pred)*100/length(pred))}




###################################################################################
###   CLASSIFICATION EVALUATION  FUNCTIONS   ######################################
###################################################################################


###################################################################################
### Run classification without CrossValidation ########
### Parameter:
### folder = folder including the saved classification result files. Usually: ".Rdata"
### selecter = array of strings (length>=1).
###	           There will be a plot for each string entry. 
###            The results of all files whose filenames contain this string,
###            will be plotted as a single roc curve in the plot.
### format = defines the fileformat of the plot: 
###          pdf= Each plot is a page in the pdf file. 
###          png= A single png file for each string.
### legendformat= Defines the format of the legend. List of strings containing one or more of the following types: "names", "AUC", "ACC"
###            names: plotting only the names of the Method
###            AUC : plotting the AUC for each Method in the legend
###            ACC : plotting the ACC for each Method in the legend
### filename = defining the result-filename (a list of filenames is needed for the png format)
### resulttab <- evaluateROC(folder, selecter=paste("_",classificationfactors,"-confounder_",sep=""), format="pdf", legendformat=c("names", "ACC","AUC"), filename=paste(folder,"ROC-curves-Categorical.",format,sep=""))
### REMARK: I am not sure whether the ROC-plotting on >2 Classes roc-curves works fine!!!!!!!!!!!!!!!!!
###################################################################################

evaluateROC=function(folder, selecter="two", format="pdf", legendformat="names", filename=paste(folder,"ROC-curves.",format,sep="")){

  # Identify the file format (e.g., 'pdf') and create the file
  if(format == "pdf"){
    pdf(filename, width=8, height=8)
  }else if(format == "png"){
    # png(filename, width=600, height=600)
  }else if(format == "ps") {
    postscript(filename, width=6, height=6)
  }
  
  selectedold<- selecter[1] #retrieve first 'selector' from list
  fnum<-0;
  for(selected in selecter){ #foreach selector in list 
    #print(selecter);
    fnum<- fnum+1;
    if(format == "png"){ 
      # dev.off();
      # filename<- paste(folder,"ROC-curves",selected,".",format,sep="")#
      png(filename[fnum], width=600, height=450)
    }
    
    # Retrive list of files from the specified folder
    filelist <- list.files(folder); #read files in folder
    filelist <- rev(filelist); #sort and reverse version of list (why?)
    #'select function' - verify if 'a' contains BOTH strings: '.RData' AND 'selected'
    select <- function(a){ return( length(grep(".RData", a, ignore=TRUE))>0  && length(grep(selected, a, ignore=TRUE))>0)} 
    filelist <- filelist[sapply(filelist, FUN=select)] #returns only elements of 'filelist' that are true for select function
    listlength<- length(filelist)
    #print(filelist)
    res <- c() #create object that stores "Method","AUC","ACC","Sensitivity","Specificity" for all 'selected'

    if(listlength > 0){
      
      # Return the number of random label runs
      randtest<- lapply(filelist, FUN=function(x){return(length(grep("RandomL", x))>0)})
      randtest<- sum(unlist(randtest));
      #print(randtest)
      
      # Create objects that will be used to create plot and final output
      legends <- c()
      methodnames <- c()
      aucs <- c()
      sensitivities <- c()
      specificities <- c()
      accs <- c()
      combinedLabels <- c()
      combinedProbabilities <- c()
      combinedLabelsRandomL <- c()
      combinedProbabilitiesRandomL <- c()
      counter <- 0
      
      # Set colours used to create colour gradient in plot  
      cols= colorRampPalette(c("darkblue","cadetblue1"))(listlength);
      
      colid<-1
      for(i in (1:listlength)){ #foreach file in folder...
        linetype=1; #set line type in plot ("solid")
        ## Open File
        f <- filelist[i]
        #print(f);
        
        ## Get methodname
        info <- strsplit(f,"_")[[1]] #extract the method name and number of runs
        m <- paste(strsplit(info[1],"-")[[1]], collapse=" ") #remove "-" from string
        #print(m)
        #print(strsplit(m, "RandomL")[[1]])
          if(length(grep("RandomL", f))>0){
            linetype=2; #set line type in plot ("dashed")
            colid<-colid-1
            cols= colorRampPalette(c("darkblue","cadetblue1"))(round(listlength/2)); #set colours for colour gradient specific for random label
          }
        methodnames<- c(methodnames,m)
        #print(paste("Method:",m," "))
        
        ## Read Data from file
        current <- load(paste(folder,f,sep="")) #load file
        results <- get(current[1]) #extract result info
        labels <- unlist(results$labels )
        predictions <- unlist(results$predictions)
        models <- results$models
        probs <- results$probabilities
        probabilities<-c()
                    
        for(a in 1:length(probs)){ #extract and combine probabilities from each crossvalidation
          #print(probs[[a]])
          probabilities <-rbind(probabilities, probs[[a]])
        }
        
        ## Print labels and number of unique labels
        #print("paste(labels)");
        #print(paste(labels));
        #print(paste("length(unique(labels))", length(unique(labels))));
        
        ## Do prediction evaluation for two class problem:
        if(length(unique(labels))<3){
          
          # Convert character-label into numeric-label
          u <- unique(as.character(labels))
          p <- rep(1,length(labels))
          l <- rep(1,length(labels))
          l[labels==u[2]] <- 2
          
          ## Set plot title
          main<- trim(gsub("_", " ", selected, fixed=TRUE))
          main <- paste(main,"-prediction (Balance: #", u[1],"=",sum(u[1]==as.character(labels)),"| #", u[2],"=",sum(u[2]==as.character(labels)), ")", sep="")
          
          ### Construct plot
          if(length(selecter)==1 && listlength > 2){ # one method, multiple runs combined into one graph
            if(counter==0 && length(grep("RandomL", f))==0){
              r <- plot.roc(as.character(labels), probabilities[,1],new=TRUE, percent=TRUE, col="royalblue4",lwd=1,lty="solid",main=main, ci=TRUE)
            }else if (counter==0 && length(grep("RandomL", f))>0){
              r <-plot.roc(as.character(labels), probabilities[,1],new=TRUE, percent=TRUE, col="royalblue1",lwd=1,lty="dashed",main=main, ci=TRUE)
            }else if(counter>0 && length(grep("RandomL", f))==0){
              r <- plot.roc(as.character(labels), probabilities[,1],add=TRUE, percent=TRUE, col="royalblue4",lwd=1,lty="solid",main=main, ci=TRUE)
            }else if (counter>0 && length(grep("RandomL", f))>0){
              r <- plot.roc(as.character(labels), probabilities[,1],add=TRUE, percent=TRUE, col="royalblue1",lwd=1,lty="dashed",main=main, ci=TRUE)
            }
            counter <- counter+1
          }else{  # multiple methods, multiple runs plotted in individual graphs
            if(selected !=selectedold){ 
              r <- plot.roc(as.character(labels), probabilities[,1], new=T, percent = TRUE, add=(length(methodnames)>1), col=cols[colid], lwd=2, lty=linetype, main=main)
            }else{
              r <- plot.roc(as.character(labels), probabilities[,1], percent = TRUE, add=(length(methodnames)>1), col=cols[colid], lwd=2, lty=linetype, main=main)
            }#end multiple methods
          } #end of construct plot  
          
          ## Get AUC
          a <- r$auc #extract AUC from plot.roc object
          aucs <- c(aucs, a) #combine AUCs
          #print(paste("AUC ", a))
          
          ## Get Accuracy
          acc <- errormult(as.numeric(as.factor(predictions)), as.numeric(as.factor(as.character(labels))))
          accs <- c(accs, acc)
          r <- confusionMatrix(predictions,labels) #calculates a cross-tabulation of observed and predicted classes with associated statistics.
          
          ## Get sensitivity and specificity
          sensitivities <- c(sensitivities, as.numeric(r$byClass)[1]*100) #extract and combine sensitivities
          specificities <- c(specificities, as.numeric(r$byClass)[2]*100) #extract and combine specificities
          
        } else if(length(unique(labels))<4){
          ## Do prediction evaluation for three class problem:
          r <- multiclass.roc(as.numeric(as.factor(labels)), as.numeric(as.factor(predictions)), percent = TRUE)
          a <-r$auc
          #print(paste("AUC ", a))
          aucs <- c(aucs, a)
          ## Accuracy
          acc <- errormult(as.numeric(as.factor(predictions)), as.numeric(as.factor(labels)))
          accs <- c(accs, acc)
          ## Calculate sensitivity and specificity for three class problem:
          # 						   Predicted class
          # 						 A  	 B  	 C
          # Known class (class label in data) 	 A  	tpA 	eAB 	eAC
          # 					 B  	eBA 	tpB 	eBC
          # 					 C  	eCA 	eCB 	tpC
          # SensitivityA = tpA/(tpA+eAB+eAC) = 25/(25+5+2)  0.78
          # SpecificityA = tnA/(tnA+eBA+eCA), where tnA = tpB + eBC + eCB + tpC = 32+4+0+15 = 51
          t <- table(labels,predictions)
          u <- unique(as.character(labels))
          if("Kontroll" %in% u){ u <- c(u[-c(which("Kontroll" ==  u))],"Kontroll") }
          if("Control" %in% u){ u <- c(u[-c(which("Control" ==  u))],"Control") }
          
          sensitivity_c1 = t[u[1],u[1]]/(t[u[1],u[1]]+t[u[1],u[2]]+t[u[1],u[3]])
          tn_c1 = t[u[2],u[2]] + t[u[2],u[3]] + t[u[3],u[2]] + t[u[3],u[3]]
          specificity_c1 = tn_c1/(tn_c1 + t[u[2],u[1]] + t[u[3],u[1]])
          sensitivity_c2 = t[u[2],u[2]]/(t[u[2],u[2]]+t[u[2],u[1]]+t[u[2],u[3]])
          tn_c2 = t[u[1],u[1]] + t[u[1],u[3]] + t[u[3],u[1]] + t[u[3],u[3]]
          specificity_c2 = tn_c2/(tn_c2 + t[u[1],u[2]] + t[u[3],u[2]])
          sensitivities <- rbind(sensitivities, c(sensitivity_c1, sensitivity_c2))
          specificities <- rbind(specificities, c(specificity_c1, specificity_c2))
        #end label<4  
        }else{    
          ### MORE CLASSES ------------------------------> ATTENTION NEVER TESTED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
          r <- multiclass.roc(as.numeric(as.factor(labels)), as.numeric(as.factor(predictions)), percent = TRUE)
          a <-r$auc
          #print(paste("AUC ", a))
          aucs <- c(aucs, a)
          ## Accuracy
          acc <- errormult(as.numeric(as.factor(predictions)), as.numeric(as.factor(labels)))
          accs <- c(accs, acc)				
        }#end label>4 (aka: The Twilight Zone)
        colid<-colid+1
      }#end of foreach file
      
      decimalPlaces <- 1 
      ### Create Legends for this Plot
      if(length(selecter)==1 && listlength > 2){ #if TRUE  means that will generate combined ROC-plot (it has it own legend)
        # Extract info from table
        box.table <- as.data.frame(cbind(noquote(as.character(methodnames)),noquote(as.numeric(aucs))))
        boxes.input <- with(box.table, split(box.table, box.table$V1)) #split 'box.table' based on first column
        # Box Random Forest RandomL
        box.RF.randomL <- boxes.input[[1]]
        box.RF.randomL <- as.numeric(as.character(box.RF.randomL$V2)) #transform factor in nume
        # Box Random Forest
        box.RF <- boxes.input[[2]]
        box.RF <- as.numeric(as.character(box.RF$V2))
        
        ## Generate and include texts in ROC curve
        # Include texts 
        text(x=24,y=42,labels="Random Forest AUC")#, font = 50,cex=2)
        valuelabels <- c(round(fivenum(as.numeric(box.RF))[2], digits = 2), round(fivenum(as.numeric(box.RF))[4], digits = 2))
        text(x=c(35,14), y=c(27), labels = valuelabels)#, font = 60,cex=2)
        text(x=23,y=20,labels="Random Forest RandomL AUC")#, font = 60,cex=2)
        valuelabels <- c(round(fivenum(as.numeric(box.RF.randomL))[2], digits = 2), round(fivenum(as.numeric(box.RF.randomL))[4], digits = 2))
        text(x=c(35,14), y=c(4), labels = valuelabels)#, font = 50,cex=2)
        
        # Include and include  boxplot
        par(fig=c(0.5, 0.9, 0.175,0.6), new=T, las=1, ps=9)
        boxplot(as.numeric(box.RF),horizontal=TRUE, range=0,xaxt="n",yaxt="n",frame=FALSE,col="royalblue4",boxlwd=1,
                width=NULL,varwidth=FALSE,staplewex=0.7)
        par(fig=c(0.5, 0.9, 0, 0.430), new=T, las=1, ps=9)
        boxplot(as.numeric(box.RF.randomL),horizontal=TRUE, range=0,xaxt="n",yaxt="n",frame=FALSE,col="royalblue1",boxlwd=1,
                width=NULL,varwidth=FALSE,staplewex=0.7)
        par(fig=c(0,1,0,1)) #restore default parameters
        
        # Create 'res' output
        res <- cbind(methodnames, round(aucs, decimalPlaces), round(accs ,decimalPlaces), round(sensitivities,decimalPlaces), round(specificities,decimalPlaces))
        colnames(res)<-c("Method","AUC","ACC","Sensitivity","Specificity")  
      }else{
        if(length(unique(labels))<3){ ## Two class
          legends<- methodnames
          if("ACC" %in% legendformat){
            legends<- paste(legends, ", ACC=", round(accs,1))
          }
          if("AUC" %in% legendformat){
            legends<- paste(legends, ", AUC=", round(aucs,1))
          }
          if(listlength != length(cols)){
            legend("bottomright", legend=legends, col=unlist(lapply(cols, FUN=function(b){return(c(b,b))})), lwd=3, lty=rep(c(1,2), length(cols)))
          }else{
            legend("bottomright", legend=legends, col=cols, lwd=3)
          }
          res <- cbind(methodnames, round(aucs, decimalPlaces), round(accs ,decimalPlaces), round(sensitivities,decimalPlaces), round(specificities,decimalPlaces))
          colnames(res)<-c("Method","AUC","ACC","Sensitivity","Specificity")
        } else if(length(unique(labels))<4){
          res <- cbind(methodnames, round(aucs,decimalPlaces), round(accs,decimalPlaces), round(sensitivities*100,decimalPlaces),round(specificities*100,decimalPlaces))
          colnames(res)<-c("Method","AUC","ACC",paste("Sen",u[1]),paste("Sen",u[2]),paste("Spe",u[1]),paste("Spe",u[2]))
        }else{ ## More class
          res <- cbind(methodnames, round(aucs,decimalPlaces), round(accs,decimalPlaces))
          colnames(res)<-c("Method","AUC","ACC")
        }} #end create legends or boxplots
    } #end if file list > 0      
  } #end foreach selecter
  dev.off();
  return(res);
}

