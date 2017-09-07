###################################################################################
### Author: Anne-Christin Hauschild
### Date:   2013-08-29
###################################################################################

library(randomForest)

rf.train=function(data, label, myntree=0, mymtry= floor(sqrt(ncol(data))), ...){
    label <- as.factor(label)
    if(myntree==0){
	model<- randomForest(data, label, importance=T)
    }else{
	model<- randomForest(data, label, importance=T, ntree=myntree, mtry=mymtry)
    }
    return(model)
}

rf.predict=function(model, testdata, type="class", ...){
    X <- testdata
    if(type=="class"){
	prediction <- predict(model, X, type="response",  ...)
    }else {
	prediction <- predict(model, X, type = "prob",  ...)
    }
    return(prediction)
}

rf.coefficients=function(model, filename="" , name="" ){
    if(filename != "" && name != ""){
        print("plotting RandomForest Importance")
        main <- paste("Random Forest Feature Selection for ", name, " data (#predictors:",model$mtry,"). ")
        pdf(filename, width=12, height=12)
            features <- varImpPlot(model, main=main)
        dev.off();
    } else{
        features <- varImpPlot(model)
    }
    return (features);
}


