###################################################################################
### Adaptation: Markus List
### Date 2014-01-20
###################################################################################
### plot the most significant features using a random forest result ###
suppressMessages(library(ggplot2))

plotTopFeatures <- function(rf, threshold=1){
  colnames(rf) <- c("cluster","0","1","MeanDecreaseAccuracy","MeanDecreaseGini")
  data.rrf.top <- rf[rf$MeanDecreaseGini > threshold,]
  data.rrf.top <- transform(data.rrf.top, cluster = reorder(as.character(cluster), -MeanDecreaseGini))
  
  p <- qplot(x=cluster, y=MeanDecreaseGini, data=data.rrf.top) + stat_summary(fun.y="mean", geom="bar") + theme(axis.text.x=element_text(angle=45, hjust=1))
  print(p)
  return(data.rrf.top)
}
