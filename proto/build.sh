#!/bin/sh

${TOOLCHAIN_DIR}/installed/bin/protoc -I=. --java_out=../llql/llql_proto/src ns.proto
${TOOLCHAIN_DIR}/installed/bin/protoc -I=. --java_out=../llql/llql_proto/src data.proto
${TOOLCHAIN_DIR}/installed/bin/protoc -I=. --java_out=../llql/llql_proto/src err.proto
${TOOLCHAIN_DIR}/installed/bin/protoc -I=. --java_out=../llql/llql_proto/src expr.proto
${TOOLCHAIN_DIR}/installed/bin/protoc -I=. --java_out=../llql/llql_proto/src query.proto
${TOOLCHAIN_DIR}/installed/bin/protoc -I=. --java_out=../llql/llql_proto/src quark.proto

