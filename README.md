# BGLL_community_detection
A java implementation of BGLL algorithm in [Fast unfolding of communities in large networks](https://arxiv.org/pdf/0803.0476.pdf)
## Qucik Start
### example
```Java
    BGLL bgll = new BGLL()
    //inputPath is a origin network file
    //outPath is the result
    bgll.excuteBGLL(String inputPath, String outPath)
``` 
### input
a file like this:
first is nodeA,second is nodeB,last is weight
```
1,2,14
2,3,10
1,4,9
2,3,6
```

### output
the output is the result of Hierarchical Clustering.there shows a faked result so that we can understand the output.
```
1,1,1
2,1,1
3,2,1
4,2,1
5,3,2
6,3,2
7,4,2
8,4,2
```
As the output show,there are 8 nodes in the network.the first column is the id of nodes,the the second column is the community id of the first clustering,we get 4 community in the first step.The final column is the community id of the last step clustering,the 8 nodes final divided into 4 cluster.
