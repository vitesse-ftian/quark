// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: err.proto

#define INTERNAL_SUPPRESS_PROTOBUF_FIELD_DEPRECATION
#include "err.pb.h"

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

const ::google::protobuf::Descriptor* Error_descriptor_ = NULL;
const ::google::protobuf::internal::GeneratedMessageReflection*
  Error_reflection_ = NULL;
const ::google::protobuf::EnumDescriptor* ErrorCode_descriptor_ = NULL;

}  // namespace


void protobuf_AssignDesc_err_2eproto() {
  protobuf_AddDesc_err_2eproto();
  const ::google::protobuf::FileDescriptor* file =
    ::google::protobuf::DescriptorPool::generated_pool()->FindFileByName(
      "err.proto");
  GOOGLE_CHECK(file != NULL);
  Error_descriptor_ = file->message_type(0);
  static const int Error_offsets_[2] = {
    GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(Error, ec_),
    GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(Error, msg_),
  };
  Error_reflection_ =
    new ::google::protobuf::internal::GeneratedMessageReflection(
      Error_descriptor_,
      Error::default_instance_,
      Error_offsets_,
      GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(Error, _has_bits_[0]),
      GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(Error, _unknown_fields_),
      -1,
      ::google::protobuf::DescriptorPool::generated_pool(),
      ::google::protobuf::MessageFactory::generated_factory(),
      sizeof(Error));
  ErrorCode_descriptor_ = file->enum_type(0);
}

namespace {

GOOGLE_PROTOBUF_DECLARE_ONCE(protobuf_AssignDescriptors_once_);
inline void protobuf_AssignDescriptorsOnce() {
  ::google::protobuf::GoogleOnceInit(&protobuf_AssignDescriptors_once_,
                 &protobuf_AssignDesc_err_2eproto);
}

void protobuf_RegisterTypes(const ::std::string&) {
  protobuf_AssignDescriptorsOnce();
  ::google::protobuf::MessageFactory::InternalRegisterGeneratedMessage(
    Error_descriptor_, &Error::default_instance());
}

}  // namespace

void protobuf_ShutdownFile_err_2eproto() {
  delete Error::default_instance_;
  delete Error_reflection_;
}

void protobuf_AddDesc_err_2eproto() {
  static bool already_here = false;
  if (already_here) return;
  already_here = true;
  GOOGLE_PROTOBUF_VERIFY_VERSION;

  ::google::protobuf::DescriptorPool::InternalAddGeneratedFile(
    "\n\terr.proto\022\nllql_proto\"7\n\005Error\022!\n\002ec\030\001"
    " \002(\0162\025.llql_proto.ErrorCode\022\013\n\003msg\030\002 \002(\t"
    "*\201\003\n\tErrorCode\022\t\n\005EC_OK\020\000\022\013\n\007EC_STOP\020\001\022\024"
    "\n\020EC_RUNTIME_ERROR\020\002\022\026\n\022EC_JIT_INIT_FAIL"
    "ED\020d\022\021\n\rEC_JIT_FAILED\020e\022\027\n\022EC_OUT_OF_RES"
    "OURCE\020\350\007\022\025\n\020EC_OUT_OF_MEMORY\020\351\007\022\017\n\nEC_BA"
    "D_MSG\020\320\017\022\026\n\021EC_BAD_MSG_CHKSUM\020\321\017\022\034\n\027EC_Q"
    "UERY_NOT_REGISTERED\020\322\017\022\020\n\013EC_OBSOLETE\020\323\017"
    "\022\025\n\020EC_NETWORK_ERROR\020\270\027\022\031\n\024EC_CSV_PARSIN"
    "G_ERROR\020\240\037\022\031\n\024EC_INT_PARSING_ERROR\020\241\037\022\033\n"
    "\026EC_FLOAT_PARSING_ERROR\020\242\037\022\014\n\006EC_NYI\020\300\204="
    "\022\032\n\024EC_TEST_WRONG_RESULT\020\301\204=B,\n\037com.vite"
    "ssedata.llql.llql_protoB\tLLQLError", 514);
  ::google::protobuf::MessageFactory::InternalRegisterGeneratedFile(
    "err.proto", &protobuf_RegisterTypes);
  Error::default_instance_ = new Error();
  Error::default_instance_->InitAsDefaultInstance();
  ::google::protobuf::internal::OnShutdown(&protobuf_ShutdownFile_err_2eproto);
}

// Force AddDescriptors() to be called at static initialization time.
struct StaticDescriptorInitializer_err_2eproto {
  StaticDescriptorInitializer_err_2eproto() {
    protobuf_AddDesc_err_2eproto();
  }
} static_descriptor_initializer_err_2eproto_;
const ::google::protobuf::EnumDescriptor* ErrorCode_descriptor() {
  protobuf_AssignDescriptorsOnce();
  return ErrorCode_descriptor_;
}
bool ErrorCode_IsValid(int value) {
  switch(value) {
    case 0:
    case 1:
    case 2:
    case 100:
    case 101:
    case 1000:
    case 1001:
    case 2000:
    case 2001:
    case 2002:
    case 2003:
    case 3000:
    case 4000:
    case 4001:
    case 4002:
    case 1000000:
    case 1000001:
      return true;
    default:
      return false;
  }
}


// ===================================================================

#ifndef _MSC_VER
const int Error::kEcFieldNumber;
const int Error::kMsgFieldNumber;
#endif  // !_MSC_VER

Error::Error()
  : ::google::protobuf::Message() {
  SharedCtor();
}

void Error::InitAsDefaultInstance() {
}

Error::Error(const Error& from)
  : ::google::protobuf::Message() {
  SharedCtor();
  MergeFrom(from);
}

void Error::SharedCtor() {
  _cached_size_ = 0;
  ec_ = 0;
  msg_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  ::memset(_has_bits_, 0, sizeof(_has_bits_));
}

Error::~Error() {
  SharedDtor();
}

void Error::SharedDtor() {
  if (msg_ != &::google::protobuf::internal::kEmptyString) {
    delete msg_;
  }
  if (this != default_instance_) {
  }
}

void Error::SetCachedSize(int size) const {
  GOOGLE_SAFE_CONCURRENT_WRITES_BEGIN();
  _cached_size_ = size;
  GOOGLE_SAFE_CONCURRENT_WRITES_END();
}
const ::google::protobuf::Descriptor* Error::descriptor() {
  protobuf_AssignDescriptorsOnce();
  return Error_descriptor_;
}

const Error& Error::default_instance() {
  if (default_instance_ == NULL) protobuf_AddDesc_err_2eproto();
  return *default_instance_;
}

Error* Error::default_instance_ = NULL;

Error* Error::New() const {
  return new Error;
}

void Error::Clear() {
  if (_has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    ec_ = 0;
    if (has_msg()) {
      if (msg_ != &::google::protobuf::internal::kEmptyString) {
        msg_->clear();
      }
    }
  }
  ::memset(_has_bits_, 0, sizeof(_has_bits_));
  mutable_unknown_fields()->Clear();
}

bool Error::MergePartialFromCodedStream(
    ::google::protobuf::io::CodedInputStream* input) {
#define DO_(EXPRESSION) if (!(EXPRESSION)) return false
  ::google::protobuf::uint32 tag;
  while ((tag = input->ReadTag()) != 0) {
    switch (::google::protobuf::internal::WireFormatLite::GetTagFieldNumber(tag)) {
      // required .llql_proto.ErrorCode ec = 1;
      case 1: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_VARINT) {
          int value;
          DO_((::google::protobuf::internal::WireFormatLite::ReadPrimitive<
                   int, ::google::protobuf::internal::WireFormatLite::TYPE_ENUM>(
                 input, &value)));
          if (::llql_proto::ErrorCode_IsValid(value)) {
            set_ec(static_cast< ::llql_proto::ErrorCode >(value));
          } else {
            mutable_unknown_fields()->AddVarint(1, value);
          }
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(18)) goto parse_msg;
        break;
      }

      // required string msg = 2;
      case 2: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
         parse_msg:
          DO_(::google::protobuf::internal::WireFormatLite::ReadString(
                input, this->mutable_msg()));
          ::google::protobuf::internal::WireFormat::VerifyUTF8String(
            this->msg().data(), this->msg().length(),
            ::google::protobuf::internal::WireFormat::PARSE);
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectAtEnd()) return true;
        break;
      }

      default: {
      handle_uninterpreted:
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_END_GROUP) {
          return true;
        }
        DO_(::google::protobuf::internal::WireFormat::SkipField(
              input, tag, mutable_unknown_fields()));
        break;
      }
    }
  }
  return true;
#undef DO_
}

void Error::SerializeWithCachedSizes(
    ::google::protobuf::io::CodedOutputStream* output) const {
  // required .llql_proto.ErrorCode ec = 1;
  if (has_ec()) {
    ::google::protobuf::internal::WireFormatLite::WriteEnum(
      1, this->ec(), output);
  }

  // required string msg = 2;
  if (has_msg()) {
    ::google::protobuf::internal::WireFormat::VerifyUTF8String(
      this->msg().data(), this->msg().length(),
      ::google::protobuf::internal::WireFormat::SERIALIZE);
    ::google::protobuf::internal::WireFormatLite::WriteString(
      2, this->msg(), output);
  }

  if (!unknown_fields().empty()) {
    ::google::protobuf::internal::WireFormat::SerializeUnknownFields(
        unknown_fields(), output);
  }
}

::google::protobuf::uint8* Error::SerializeWithCachedSizesToArray(
    ::google::protobuf::uint8* target) const {
  // required .llql_proto.ErrorCode ec = 1;
  if (has_ec()) {
    target = ::google::protobuf::internal::WireFormatLite::WriteEnumToArray(
      1, this->ec(), target);
  }

  // required string msg = 2;
  if (has_msg()) {
    ::google::protobuf::internal::WireFormat::VerifyUTF8String(
      this->msg().data(), this->msg().length(),
      ::google::protobuf::internal::WireFormat::SERIALIZE);
    target =
      ::google::protobuf::internal::WireFormatLite::WriteStringToArray(
        2, this->msg(), target);
  }

  if (!unknown_fields().empty()) {
    target = ::google::protobuf::internal::WireFormat::SerializeUnknownFieldsToArray(
        unknown_fields(), target);
  }
  return target;
}

int Error::ByteSize() const {
  int total_size = 0;

  if (_has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    // required .llql_proto.ErrorCode ec = 1;
    if (has_ec()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::EnumSize(this->ec());
    }

    // required string msg = 2;
    if (has_msg()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::StringSize(
          this->msg());
    }

  }
  if (!unknown_fields().empty()) {
    total_size +=
      ::google::protobuf::internal::WireFormat::ComputeUnknownFieldsSize(
        unknown_fields());
  }
  GOOGLE_SAFE_CONCURRENT_WRITES_BEGIN();
  _cached_size_ = total_size;
  GOOGLE_SAFE_CONCURRENT_WRITES_END();
  return total_size;
}

void Error::MergeFrom(const ::google::protobuf::Message& from) {
  GOOGLE_CHECK_NE(&from, this);
  const Error* source =
    ::google::protobuf::internal::dynamic_cast_if_available<const Error*>(
      &from);
  if (source == NULL) {
    ::google::protobuf::internal::ReflectionOps::Merge(from, this);
  } else {
    MergeFrom(*source);
  }
}

void Error::MergeFrom(const Error& from) {
  GOOGLE_CHECK_NE(&from, this);
  if (from._has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    if (from.has_ec()) {
      set_ec(from.ec());
    }
    if (from.has_msg()) {
      set_msg(from.msg());
    }
  }
  mutable_unknown_fields()->MergeFrom(from.unknown_fields());
}

void Error::CopyFrom(const ::google::protobuf::Message& from) {
  if (&from == this) return;
  Clear();
  MergeFrom(from);
}

void Error::CopyFrom(const Error& from) {
  if (&from == this) return;
  Clear();
  MergeFrom(from);
}

bool Error::IsInitialized() const {
  if ((_has_bits_[0] & 0x00000003) != 0x00000003) return false;

  return true;
}

void Error::Swap(Error* other) {
  if (other != this) {
    std::swap(ec_, other->ec_);
    std::swap(msg_, other->msg_);
    std::swap(_has_bits_[0], other->_has_bits_[0]);
    _unknown_fields_.Swap(&other->_unknown_fields_);
    std::swap(_cached_size_, other->_cached_size_);
  }
}

::google::protobuf::Metadata Error::GetMetadata() const {
  protobuf_AssignDescriptorsOnce();
  ::google::protobuf::Metadata metadata;
  metadata.descriptor = Error_descriptor_;
  metadata.reflection = Error_reflection_;
  return metadata;
}


// @@protoc_insertion_point(namespace_scope)

}  // namespace llql_proto

// @@protoc_insertion_point(global_scope)
