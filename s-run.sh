#!/bin/bash
SPARKX=/nx/software/spark
ARTIFACTS=$PWD/out/artifacts

CLS=$1
shift

# $SPARKX/bin/spark-submit --conf spark.sql.codegen=true --jars $ARTIFACTS/squark_jar/squark.jar --class com.vitessedata.examples.quark.$CLS $ARTIFACTS/examples_jar/examples.jar $1 sql "$2" 
$SPARKX/bin/spark-submit --master local[2] --conf spark.sql.shuffle.partitions=16 --conf spark.executor.memory=2g --jars $ARTIFACTS/squark_jar/squark.jar --class com.vitessedata.examples.quark.$CLS $ARTIFACTS/examples_jar/examples.jar $1 sql "$2" 
