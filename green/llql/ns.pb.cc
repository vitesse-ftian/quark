// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ns.proto

#define INTERNAL_SUPPRESS_PROTOBUF_FIELD_DEPRECATION
#include "ns.pb.h"

#include <algorithm>

#include <google/protobuf/stubs/common.h>
#include <google/protobuf/stubs/once.h>
#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/wire_format_lite_inl.h>
#include <google/protobuf/descriptor.h>
#include <google/protobuf/generated_message_reflection.h>
#include <google/protobuf/reflection_ops.h>
#include <google/protobuf/wire_format.h>
// @@protoc_insertion_point(includes)

namespace llql_proto {

namespace {

const ::google::protobuf::EnumDescriptor* Namespace_descriptor_ = NULL;

}  // namespace


void protobuf_AssignDesc_ns_2eproto() {
  protobuf_AddDesc_ns_2eproto();
  const ::google::protobuf::FileDescriptor* file =
    ::google::protobuf::DescriptorPool::generated_pool()->FindFileByName(
      "ns.proto");
  GOOGLE_CHECK(file != NULL);
  Namespace_descriptor_ = file->enum_type(0);
}

namespace {

GOOGLE_PROTOBUF_DECLARE_ONCE(protobuf_AssignDescriptors_once_);
inline void protobuf_AssignDescriptorsOnce() {
  ::google::protobuf::GoogleOnceInit(&protobuf_AssignDescriptors_once_,
                 &protobuf_AssignDesc_ns_2eproto);
}

void protobuf_RegisterTypes(const ::std::string&) {
  protobuf_AssignDescriptorsOnce();
}

}  // namespace

void protobuf_ShutdownFile_ns_2eproto() {
}

void protobuf_AddDesc_ns_2eproto() {
  static bool already_here = false;
  if (already_here) return;
  already_here = true;
  GOOGLE_PROTOBUF_VERIFY_VERSION;

  ::google::protobuf::DescriptorPool::InternalAddGeneratedFile(
    "\n\010ns.proto\022\nllql_proto*#\n\tNamespace\022\010\n\004L"
    "LQL\020\000\022\014\n\010Postgres\020\001B)\n\037com.vitessedata.l"
    "lql.llql_protoB\006LLQLNs", 102);
  ::google::protobuf::MessageFactory::InternalRegisterGeneratedFile(
    "ns.proto", &protobuf_RegisterTypes);
  ::google::protobuf::internal::OnShutdown(&protobuf_ShutdownFile_ns_2eproto);
}

// Force AddDescriptors() to be called at static initialization time.
struct StaticDescriptorInitializer_ns_2eproto {
  StaticDescriptorInitializer_ns_2eproto() {
    protobuf_AddDesc_ns_2eproto();
  }
} static_descriptor_initializer_ns_2eproto_;
const ::google::protobuf::EnumDescriptor* Namespace_descriptor() {
  protobuf_AssignDescriptorsOnce();
  return Namespace_descriptor_;
}
bool Namespace_IsValid(int value) {
  switch(value) {
    case 0:
    case 1:
      return true;
    default:
      return false;
  }
}


// @@protoc_insertion_point(namespace_scope)

}  // namespace llql_proto

// @@protoc_insertion_point(global_scope)
