#!/bin/sh

protoc -I=. --java_out=../llql/llql_proto/src ns.proto
protoc -I=. --java_out=../llql/llql_proto/src data.proto
protoc -I=. --java_out=../llql/llql_proto/src err.proto
protoc -I=. --java_out=../llql/llql_proto/src expr.proto
protoc -I=. --java_out=../llql/llql_proto/src query.proto
protoc -I=. --java_out=../llql/llql_proto/src quark.proto

protoc -I=. --cpp_out=../green/llql ns.proto
protoc -I=. --cpp_out=../green/llql data.proto
protoc -I=. --cpp_out=../green/llql err.proto
protoc -I=. --cpp_out=../green/llql expr.proto
protoc -I=. --cpp_out=../green/llql query.proto
protoc -I=. --cpp_out=../green/llql quark.proto


