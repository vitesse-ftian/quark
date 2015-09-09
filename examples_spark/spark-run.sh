#!/bin/bash
SPARKX=/nx/software/spark
ARTIFACTS=$PWD/out/artifacts

CLS=$1
shift

$SPARKX/bin/spark-submit --master local[2] --conf spark.sql.shuffle.partitions=16 --conf spark.executor.memory=2g --class com.vitessedata.examples.spark.$CLS $ARTIFACTS/examples_spark_jar/examples_spark.jar $1 "$2" 
