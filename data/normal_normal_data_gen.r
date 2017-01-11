setwd("Dropbox/Research/repo/polya/data/")
set.seed(10)

# generate the sample data from three normal distributions with different means
var<-1
x1<-rnorm(n=100, 0, var)
x2<-rnorm(n=100, 7, var)
x3<-rnorm(n=100, 12, var)
xx<-c(x1, x2, x3)

temp<-as.matrix(xx)
dim(temp)
# output to file
write.table(temp, file="normal_normal_data.csv", row.names=FALSE, col.names=FALSE)

dat<-data.frame(x=x1, type="1")
dat<-rbind(dat, data.frame(x=x2, type="2"))
dat<-rbind(dat, data.frame(x=x3, type="3"))

library(ggplot2)

ggplot(dat, aes(x=x, col=type))+geom_density()

