// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: data.proto

#ifndef PROTOBUF_data_2eproto__INCLUDED
#define PROTOBUF_data_2eproto__INCLUDED

#include <string>

#include <google/protobuf/stubs/common.h>

#if GOOGLE_PROTOBUF_VERSION < 2005000
#error This file was generated by a newer version of protoc which is
#error incompatible with your Protocol Buffer headers.  Please update
#error your headers.
#endif
#if 2005000 < GOOGLE_PROTOBUF_MIN_PROTOC_VERSION
#error This file was generated by an older version of protoc which is
#error incompatible with your Protocol Buffer headers.  Please
#error regenerate this file with a newer version of protoc.
#endif

#include <google/protobuf/generated_message_util.h>
#include <google/protobuf/message.h>
#include <google/protobuf/repeated_field.h>
#include <google/protobuf/extension_set.h>
#include <google/protobuf/generated_enum_reflection.h>
#include <google/protobuf/unknown_field_set.h>
#include "ns.pb.h"
// @@protoc_insertion_point(includes)

namespace llql_proto {

// Internal implementation detail -- do not call these.
void  protobuf_AddDesc_data_2eproto();
void protobuf_AssignDesc_data_2eproto();
void protobuf_ShutdownFile_data_2eproto();

class DataType;
class DataType_TypeInfo;
class Schema;
class Schema_Col;

enum DataType_BuiltinType {
  DataType_BuiltinType_BOOLEAN = 1,
  DataType_BuiltinType_INT8 = 10,
  DataType_BuiltinType_INT16 = 11,
  DataType_BuiltinType_INT32 = 12,
  DataType_BuiltinType_INT64 = 13,
  DataType_BuiltinType_INT128 = 14,
  DataType_BuiltinType_FLOAT32 = 20,
  DataType_BuiltinType_FLOAT64 = 21,
  DataType_BuiltinType_NUMERIC = 30,
  DataType_BuiltinType_DECIMAL64 = 31,
  DataType_BuiltinType_DECIMAL128 = 32,
  DataType_BuiltinType_DATE = 40,
  DataType_BuiltinType_TIME = 41,
  DataType_BuiltinType_TIMESTAMP = 42,
  DataType_BuiltinType_BINARY = 50,
  DataType_BuiltinType_STRING = 51,
  DataType_BuiltinType_JSON = 52,
  DataType_BuiltinType_ARRAY = 100,
  DataType_BuiltinType_SET = 101
};
bool DataType_BuiltinType_IsValid(int value);
const DataType_BuiltinType DataType_BuiltinType_BuiltinType_MIN = DataType_BuiltinType_BOOLEAN;
const DataType_BuiltinType DataType_BuiltinType_BuiltinType_MAX = DataType_BuiltinType_SET;
const int DataType_BuiltinType_BuiltinType_ARRAYSIZE = DataType_BuiltinType_BuiltinType_MAX + 1;

const ::google::protobuf::EnumDescriptor* DataType_BuiltinType_descriptor();
inline const ::std::string& DataType_BuiltinType_Name(DataType_BuiltinType value) {
  return ::google::protobuf::internal::NameOfEnum(
    DataType_BuiltinType_descriptor(), value);
}
inline bool DataType_BuiltinType_Parse(
    const ::std::string& name, DataType_BuiltinType* value) {
  return ::google::protobuf::internal::ParseNamedEnum<DataType_BuiltinType>(
    DataType_BuiltinType_descriptor(), name, value);
}
// ===================================================================

class DataType_TypeInfo : public ::google::protobuf::Message {
 public:
  DataType_TypeInfo();
  virtual ~DataType_TypeInfo();

  DataType_TypeInfo(const DataType_TypeInfo& from);

  inline DataType_TypeInfo& operator=(const DataType_TypeInfo& from) {
    CopyFrom(from);
    return *this;
  }

  inline const ::google::protobuf::UnknownFieldSet& unknown_fields() const {
    return _unknown_fields_;
  }

  inline ::google::protobuf::UnknownFieldSet* mutable_unknown_fields() {
    return &_unknown_fields_;
  }

  static const ::google::protobuf::Descriptor* descriptor();
  static const DataType_TypeInfo& default_instance();

  void Swap(DataType_TypeInfo* other);

  // implements Message ----------------------------------------------

  DataType_TypeInfo* New() const;
  void CopyFrom(const ::google::protobuf::Message& from);
  void MergeFrom(const ::google::protobuf::Message& from);
  void CopyFrom(const DataType_TypeInfo& from);
  void MergeFrom(const DataType_TypeInfo& from);
  void Clear();
  bool IsInitialized() const;

  int ByteSize() const;
  bool MergePartialFromCodedStream(
      ::google::protobuf::io::CodedInputStream* input);
  void SerializeWithCachedSizes(
      ::google::protobuf::io::CodedOutputStream* output) const;
  ::google::protobuf::uint8* SerializeWithCachedSizesToArray(::google::protobuf::uint8* output) const;
  int GetCachedSize() const { return _cached_size_; }
  private:
  void SharedCtor();
  void SharedDtor();
  void SetCachedSize(int size) const;
  public:

  ::google::protobuf::Metadata GetMetadata() const;

  // nested types ----------------------------------------------------

  // accessors -------------------------------------------------------

  // required .llql_proto.Namespace ns = 1;
  inline bool has_ns() const;
  inline void clear_ns();
  static const int kNsFieldNumber = 1;
  inline ::llql_proto::Namespace ns() const;
  inline void set_ns(::llql_proto::Namespace value);

  // required int32 type_id = 2;
  inline bool has_type_id() const;
  inline void clear_type_id();
  static const int kTypeIdFieldNumber = 2;
  inline ::google::protobuf::int32 type_id() const;
  inline void set_type_id(::google::protobuf::int32 value);

  // required int32 len = 3;
  inline bool has_len() const;
  inline void clear_len();
  static const int kLenFieldNumber = 3;
  inline ::google::protobuf::int32 len() const;
  inline void set_len(::google::protobuf::int32 value);

  // required int32 align = 4;
  inline bool has_align() const;
  inline void clear_align();
  static const int kAlignFieldNumber = 4;
  inline ::google::protobuf::int32 align() const;
  inline void set_align(::google::protobuf::int32 value);

  // optional int32 opt1 = 5;
  inline bool has_opt1() const;
  inline void clear_opt1();
  static const int kOpt1FieldNumber = 5;
  inline ::google::protobuf::int32 opt1() const;
  inline void set_opt1(::google::protobuf::int32 value);

  // optional int32 opt2 = 6;
  inline bool has_opt2() const;
  inline void clear_opt2();
  static const int kOpt2FieldNumber = 6;
  inline ::google::protobuf::int32 opt2() const;
  inline void set_opt2(::google::protobuf::int32 value);

  // @@protoc_insertion_point(class_scope:llql_proto.DataType.TypeInfo)
 private:
  inline void set_has_ns();
  inline void clear_has_ns();
  inline void set_has_type_id();
  inline void clear_has_type_id();
  inline void set_has_len();
  inline void clear_has_len();
  inline void set_has_align();
  inline void clear_has_align();
  inline void set_has_opt1();
  inline void clear_has_opt1();
  inline void set_has_opt2();
  inline void clear_has_opt2();

  ::google::protobuf::UnknownFieldSet _unknown_fields_;

  int ns_;
  ::google::protobuf::int32 type_id_;
  ::google::protobuf::int32 len_;
  ::google::protobuf::int32 align_;
  ::google::protobuf::int32 opt1_;
  ::google::protobuf::int32 opt2_;

  mutable int _cached_size_;
  ::google::protobuf::uint32 _has_bits_[(6 + 31) / 32];

  friend void  protobuf_AddDesc_data_2eproto();
  friend void protobuf_AssignDesc_data_2eproto();
  friend void protobuf_ShutdownFile_data_2eproto();

  void InitAsDefaultInstance();
  static DataType_TypeInfo* default_instance_;
};
// -------------------------------------------------------------------

class DataType : public ::google::protobuf::Message {
 public:
  DataType();
  virtual ~DataType();

  DataType(const DataType& from);

  inline DataType& operator=(const DataType& from) {
    CopyFrom(from);
    return *this;
  }

  inline const ::google::protobuf::UnknownFieldSet& unknown_fields() const {
    return _unknown_fields_;
  }

  inline ::google::protobuf::UnknownFieldSet* mutable_unknown_fields() {
    return &_unknown_fields_;
  }

  static const ::google::protobuf::Descriptor* descriptor();
  static const DataType& default_instance();

  void Swap(DataType* other);

  // implements Message ----------------------------------------------

  DataType* New() const;
  void CopyFrom(const ::google::protobuf::Message& from);
  void MergeFrom(const ::google::protobuf::Message& from);
  void CopyFrom(const DataType& from);
  void MergeFrom(const DataType& from);
  void Clear();
  bool IsInitialized() const;

  int ByteSize() const;
  bool MergePartialFromCodedStream(
      ::google::protobuf::io::CodedInputStream* input);
  void SerializeWithCachedSizes(
      ::google::protobuf::io::CodedOutputStream* output) const;
  ::google::protobuf::uint8* SerializeWithCachedSizesToArray(::google::protobuf::uint8* output) const;
  int GetCachedSize() const { return _cached_size_; }
  private:
  void SharedCtor();
  void SharedDtor();
  void SetCachedSize(int size) const;
  public:

  ::google::protobuf::Metadata GetMetadata() const;

  // nested types ----------------------------------------------------

  typedef DataType_TypeInfo TypeInfo;

  typedef DataType_BuiltinType BuiltinType;
  static const BuiltinType BOOLEAN = DataType_BuiltinType_BOOLEAN;
  static const BuiltinType INT8 = DataType_BuiltinType_INT8;
  static const BuiltinType INT16 = DataType_BuiltinType_INT16;
  static const BuiltinType INT32 = DataType_BuiltinType_INT32;
  static const BuiltinType INT64 = DataType_BuiltinType_INT64;
  static const BuiltinType INT128 = DataType_BuiltinType_INT128;
  static const BuiltinType FLOAT32 = DataType_BuiltinType_FLOAT32;
  static const BuiltinType FLOAT64 = DataType_BuiltinType_FLOAT64;
  static const BuiltinType NUMERIC = DataType_BuiltinType_NUMERIC;
  static const BuiltinType DECIMAL64 = DataType_BuiltinType_DECIMAL64;
  static const BuiltinType DECIMAL128 = DataType_BuiltinType_DECIMAL128;
  static const BuiltinType DATE = DataType_BuiltinType_DATE;
  static const BuiltinType TIME = DataType_BuiltinType_TIME;
  static const BuiltinType TIMESTAMP = DataType_BuiltinType_TIMESTAMP;
  static const BuiltinType BINARY = DataType_BuiltinType_BINARY;
  static const BuiltinType STRING = DataType_BuiltinType_STRING;
  static const BuiltinType JSON = DataType_BuiltinType_JSON;
  static const BuiltinType ARRAY = DataType_BuiltinType_ARRAY;
  static const BuiltinType SET = DataType_BuiltinType_SET;
  static inline bool BuiltinType_IsValid(int value) {
    return DataType_BuiltinType_IsValid(value);
  }
  static const BuiltinType BuiltinType_MIN =
    DataType_BuiltinType_BuiltinType_MIN;
  static const BuiltinType BuiltinType_MAX =
    DataType_BuiltinType_BuiltinType_MAX;
  static const int BuiltinType_ARRAYSIZE =
    DataType_BuiltinType_BuiltinType_ARRAYSIZE;
  static inline const ::google::protobuf::EnumDescriptor*
  BuiltinType_descriptor() {
    return DataType_BuiltinType_descriptor();
  }
  static inline const ::std::string& BuiltinType_Name(BuiltinType value) {
    return DataType_BuiltinType_Name(value);
  }
  static inline bool BuiltinType_Parse(const ::std::string& name,
      BuiltinType* value) {
    return DataType_BuiltinType_Parse(name, value);
  }

  // accessors -------------------------------------------------------

  // required .llql_proto.DataType.TypeInfo type_info = 1;
  inline bool has_type_info() const;
  inline void clear_type_info();
  static const int kTypeInfoFieldNumber = 1;
  inline const ::llql_proto::DataType_TypeInfo& type_info() const;
  inline ::llql_proto::DataType_TypeInfo* mutable_type_info();
  inline ::llql_proto::DataType_TypeInfo* release_type_info();
  inline void set_allocated_type_info(::llql_proto::DataType_TypeInfo* type_info);

  // optional .llql_proto.DataType.TypeInfo type_info_native = 2;
  inline bool has_type_info_native() const;
  inline void clear_type_info_native();
  static const int kTypeInfoNativeFieldNumber = 2;
  inline const ::llql_proto::DataType_TypeInfo& type_info_native() const;
  inline ::llql_proto::DataType_TypeInfo* mutable_type_info_native();
  inline ::llql_proto::DataType_TypeInfo* release_type_info_native();
  inline void set_allocated_type_info_native(::llql_proto::DataType_TypeInfo* type_info_native);

  // repeated .llql_proto.DataType elem_types = 3;
  inline int elem_types_size() const;
  inline void clear_elem_types();
  static const int kElemTypesFieldNumber = 3;
  inline const ::llql_proto::DataType& elem_types(int index) const;
  inline ::llql_proto::DataType* mutable_elem_types(int index);
  inline ::llql_proto::DataType* add_elem_types();
  inline const ::google::protobuf::RepeatedPtrField< ::llql_proto::DataType >&
      elem_types() const;
  inline ::google::protobuf::RepeatedPtrField< ::llql_proto::DataType >*
      mutable_elem_types();

  // @@protoc_insertion_point(class_scope:llql_proto.DataType)
 private:
  inline void set_has_type_info();
  inline void clear_has_type_info();
  inline void set_has_type_info_native();
  inline void clear_has_type_info_native();

  ::google::protobuf::UnknownFieldSet _unknown_fields_;

  ::llql_proto::DataType_TypeInfo* type_info_;
  ::llql_proto::DataType_TypeInfo* type_info_native_;
  ::google::protobuf::RepeatedPtrField< ::llql_proto::DataType > elem_types_;

  mutable int _cached_size_;
  ::google::protobuf::uint32 _has_bits_[(3 + 31) / 32];

  friend void  protobuf_AddDesc_data_2eproto();
  friend void protobuf_AssignDesc_data_2eproto();
  friend void protobuf_ShutdownFile_data_2eproto();

  void InitAsDefaultInstance();
  static DataType* default_instance_;
};
// -------------------------------------------------------------------

class Schema_Col : public ::google::protobuf::Message {
 public:
  Schema_Col();
  virtual ~Schema_Col();

  Schema_Col(const Schema_Col& from);

  inline Schema_Col& operator=(const Schema_Col& from) {
    CopyFrom(from);
    return *this;
  }

  inline const ::google::protobuf::UnknownFieldSet& unknown_fields() const {
    return _unknown_fields_;
  }

  inline ::google::protobuf::UnknownFieldSet* mutable_unknown_fields() {
    return &_unknown_fields_;
  }

  static const ::google::protobuf::Descriptor* descriptor();
  static const Schema_Col& default_instance();

  void Swap(Schema_Col* other);

  // implements Message ----------------------------------------------

  Schema_Col* New() const;
  void CopyFrom(const ::google::protobuf::Message& from);
  void MergeFrom(const ::google::protobuf::Message& from);
  void CopyFrom(const Schema_Col& from);
  void MergeFrom(const Schema_Col& from);
  void Clear();
  bool IsInitialized() const;

  int ByteSize() const;
  bool MergePartialFromCodedStream(
      ::google::protobuf::io::CodedInputStream* input);
  void SerializeWithCachedSizes(
      ::google::protobuf::io::CodedOutputStream* output) const;
  ::google::protobuf::uint8* SerializeWithCachedSizesToArray(::google::protobuf::uint8* output) const;
  int GetCachedSize() const { return _cached_size_; }
  private:
  void SharedCtor();
  void SharedDtor();
  void SetCachedSize(int size) const;
  public:

  ::google::protobuf::Metadata GetMetadata() const;

  // nested types ----------------------------------------------------

  // accessors -------------------------------------------------------

  // required .llql_proto.DataType col_type = 1;
  inline bool has_col_type() const;
  inline void clear_col_type();
  static const int kColTypeFieldNumber = 1;
  inline const ::llql_proto::DataType& col_type() const;
  inline ::llql_proto::DataType* mutable_col_type();
  inline ::llql_proto::DataType* release_col_type();
  inline void set_allocated_col_type(::llql_proto::DataType* col_type);

  // required bool nullable = 2 [default = true];
  inline bool has_nullable() const;
  inline void clear_nullable();
  static const int kNullableFieldNumber = 2;
  inline bool nullable() const;
  inline void set_nullable(bool value);

  // optional string name = 3;
  inline bool has_name() const;
  inline void clear_name();
  static const int kNameFieldNumber = 3;
  inline const ::std::string& name() const;
  inline void set_name(const ::std::string& value);
  inline void set_name(const char* value);
  inline void set_name(const char* value, size_t size);
  inline ::std::string* mutable_name();
  inline ::std::string* release_name();
  inline void set_allocated_name(::std::string* name);

  // @@protoc_insertion_point(class_scope:llql_proto.Schema.Col)
 private:
  inline void set_has_col_type();
  inline void clear_has_col_type();
  inline void set_has_nullable();
  inline void clear_has_nullable();
  inline void set_has_name();
  inline void clear_has_name();

  ::google::protobuf::UnknownFieldSet _unknown_fields_;

  ::llql_proto::DataType* col_type_;
  ::std::string* name_;
  bool nullable_;

  mutable int _cached_size_;
  ::google::protobuf::uint32 _has_bits_[(3 + 31) / 32];

  friend void  protobuf_AddDesc_data_2eproto();
  friend void protobuf_AssignDesc_data_2eproto();
  friend void protobuf_ShutdownFile_data_2eproto();

  void InitAsDefaultInstance();
  static Schema_Col* default_instance_;
};
// -------------------------------------------------------------------

class Schema : public ::google::protobuf::Message {
 public:
  Schema();
  virtual ~Schema();

  Schema(const Schema& from);

  inline Schema& operator=(const Schema& from) {
    CopyFrom(from);
    return *this;
  }

  inline const ::google::protobuf::UnknownFieldSet& unknown_fields() const {
    return _unknown_fields_;
  }

  inline ::google::protobuf::UnknownFieldSet* mutable_unknown_fields() {
    return &_unknown_fields_;
  }

  static const ::google::protobuf::Descriptor* descriptor();
  static const Schema& default_instance();

  void Swap(Schema* other);

  // implements Message ----------------------------------------------

  Schema* New() const;
  void CopyFrom(const ::google::protobuf::Message& from);
  void MergeFrom(const ::google::protobuf::Message& from);
  void CopyFrom(const Schema& from);
  void MergeFrom(const Schema& from);
  void Clear();
  bool IsInitialized() const;

  int ByteSize() const;
  bool MergePartialFromCodedStream(
      ::google::protobuf::io::CodedInputStream* input);
  void SerializeWithCachedSizes(
      ::google::protobuf::io::CodedOutputStream* output) const;
  ::google::protobuf::uint8* SerializeWithCachedSizesToArray(::google::protobuf::uint8* output) const;
  int GetCachedSize() const { return _cached_size_; }
  private:
  void SharedCtor();
  void SharedDtor();
  void SetCachedSize(int size) const;
  public:

  ::google::protobuf::Metadata GetMetadata() const;

  // nested types ----------------------------------------------------

  typedef Schema_Col Col;

  // accessors -------------------------------------------------------

  // repeated .llql_proto.Schema.Col columns = 1;
  inline int columns_size() const;
  inline void clear_columns();
  static const int kColumnsFieldNumber = 1;
  inline const ::llql_proto::Schema_Col& columns(int index) const;
  inline ::llql_proto::Schema_Col* mutable_columns(int index);
  inline ::llql_proto::Schema_Col* add_columns();
  inline const ::google::protobuf::RepeatedPtrField< ::llql_proto::Schema_Col >&
      columns() const;
  inline ::google::protobuf::RepeatedPtrField< ::llql_proto::Schema_Col >*
      mutable_columns();

  // @@protoc_insertion_point(class_scope:llql_proto.Schema)
 private:

  ::google::protobuf::UnknownFieldSet _unknown_fields_;

  ::google::protobuf::RepeatedPtrField< ::llql_proto::Schema_Col > columns_;

  mutable int _cached_size_;
  ::google::protobuf::uint32 _has_bits_[(1 + 31) / 32];

  friend void  protobuf_AddDesc_data_2eproto();
  friend void protobuf_AssignDesc_data_2eproto();
  friend void protobuf_ShutdownFile_data_2eproto();

  void InitAsDefaultInstance();
  static Schema* default_instance_;
};
// ===================================================================


// ===================================================================

// DataType_TypeInfo

// required .llql_proto.Namespace ns = 1;
inline bool DataType_TypeInfo::has_ns() const {
  return (_has_bits_[0] & 0x00000001u) != 0;
}
inline void DataType_TypeInfo::set_has_ns() {
  _has_bits_[0] |= 0x00000001u;
}
inline void DataType_TypeInfo::clear_has_ns() {
  _has_bits_[0] &= ~0x00000001u;
}
inline void DataType_TypeInfo::clear_ns() {
  ns_ = 0;
  clear_has_ns();
}
inline ::llql_proto::Namespace DataType_TypeInfo::ns() const {
  return static_cast< ::llql_proto::Namespace >(ns_);
}
inline void DataType_TypeInfo::set_ns(::llql_proto::Namespace value) {
  assert(::llql_proto::Namespace_IsValid(value));
  set_has_ns();
  ns_ = value;
}

// required int32 type_id = 2;
inline bool DataType_TypeInfo::has_type_id() const {
  return (_has_bits_[0] & 0x00000002u) != 0;
}
inline void DataType_TypeInfo::set_has_type_id() {
  _has_bits_[0] |= 0x00000002u;
}
inline void DataType_TypeInfo::clear_has_type_id() {
  _has_bits_[0] &= ~0x00000002u;
}
inline void DataType_TypeInfo::clear_type_id() {
  type_id_ = 0;
  clear_has_type_id();
}
inline ::google::protobuf::int32 DataType_TypeInfo::type_id() const {
  return type_id_;
}
inline void DataType_TypeInfo::set_type_id(::google::protobuf::int32 value) {
  set_has_type_id();
  type_id_ = value;
}

// required int32 len = 3;
inline bool DataType_TypeInfo::has_len() const {
  return (_has_bits_[0] & 0x00000004u) != 0;
}
inline void DataType_TypeInfo::set_has_len() {
  _has_bits_[0] |= 0x00000004u;
}
inline void DataType_TypeInfo::clear_has_len() {
  _has_bits_[0] &= ~0x00000004u;
}
inline void DataType_TypeInfo::clear_len() {
  len_ = 0;
  clear_has_len();
}
inline ::google::protobuf::int32 DataType_TypeInfo::len() const {
  return len_;
}
inline void DataType_TypeInfo::set_len(::google::protobuf::int32 value) {
  set_has_len();
  len_ = value;
}

// required int32 align = 4;
inline bool DataType_TypeInfo::has_align() const {
  return (_has_bits_[0] & 0x00000008u) != 0;
}
inline void DataType_TypeInfo::set_has_align() {
  _has_bits_[0] |= 0x00000008u;
}
inline void DataType_TypeInfo::clear_has_align() {
  _has_bits_[0] &= ~0x00000008u;
}
inline void DataType_TypeInfo::clear_align() {
  align_ = 0;
  clear_has_align();
}
inline ::google::protobuf::int32 DataType_TypeInfo::align() const {
  return align_;
}
inline void DataType_TypeInfo::set_align(::google::protobuf::int32 value) {
  set_has_align();
  align_ = value;
}

// optional int32 opt1 = 5;
inline bool DataType_TypeInfo::has_opt1() const {
  return (_has_bits_[0] & 0x00000010u) != 0;
}
inline void DataType_TypeInfo::set_has_opt1() {
  _has_bits_[0] |= 0x00000010u;
}
inline void DataType_TypeInfo::clear_has_opt1() {
  _has_bits_[0] &= ~0x00000010u;
}
inline void DataType_TypeInfo::clear_opt1() {
  opt1_ = 0;
  clear_has_opt1();
}
inline ::google::protobuf::int32 DataType_TypeInfo::opt1() const {
  return opt1_;
}
inline void DataType_TypeInfo::set_opt1(::google::protobuf::int32 value) {
  set_has_opt1();
  opt1_ = value;
}

// optional int32 opt2 = 6;
inline bool DataType_TypeInfo::has_opt2() const {
  return (_has_bits_[0] & 0x00000020u) != 0;
}
inline void DataType_TypeInfo::set_has_opt2() {
  _has_bits_[0] |= 0x00000020u;
}
inline void DataType_TypeInfo::clear_has_opt2() {
  _has_bits_[0] &= ~0x00000020u;
}
inline void DataType_TypeInfo::clear_opt2() {
  opt2_ = 0;
  clear_has_opt2();
}
inline ::google::protobuf::int32 DataType_TypeInfo::opt2() const {
  return opt2_;
}
inline void DataType_TypeInfo::set_opt2(::google::protobuf::int32 value) {
  set_has_opt2();
  opt2_ = value;
}

// -------------------------------------------------------------------

// DataType

// required .llql_proto.DataType.TypeInfo type_info = 1;
inline bool DataType::has_type_info() const {
  return (_has_bits_[0] & 0x00000001u) != 0;
}
inline void DataType::set_has_type_info() {
  _has_bits_[0] |= 0x00000001u;
}
inline void DataType::clear_has_type_info() {
  _has_bits_[0] &= ~0x00000001u;
}
inline void DataType::clear_type_info() {
  if (type_info_ != NULL) type_info_->::llql_proto::DataType_TypeInfo::Clear();
  clear_has_type_info();
}
inline const ::llql_proto::DataType_TypeInfo& DataType::type_info() const {
  return type_info_ != NULL ? *type_info_ : *default_instance_->type_info_;
}
inline ::llql_proto::DataType_TypeInfo* DataType::mutable_type_info() {
  set_has_type_info();
  if (type_info_ == NULL) type_info_ = new ::llql_proto::DataType_TypeInfo;
  return type_info_;
}
inline ::llql_proto::DataType_TypeInfo* DataType::release_type_info() {
  clear_has_type_info();
  ::llql_proto::DataType_TypeInfo* temp = type_info_;
  type_info_ = NULL;
  return temp;
}
inline void DataType::set_allocated_type_info(::llql_proto::DataType_TypeInfo* type_info) {
  delete type_info_;
  type_info_ = type_info;
  if (type_info) {
    set_has_type_info();
  } else {
    clear_has_type_info();
  }
}

// optional .llql_proto.DataType.TypeInfo type_info_native = 2;
inline bool DataType::has_type_info_native() const {
  return (_has_bits_[0] & 0x00000002u) != 0;
}
inline void DataType::set_has_type_info_native() {
  _has_bits_[0] |= 0x00000002u;
}
inline void DataType::clear_has_type_info_native() {
  _has_bits_[0] &= ~0x00000002u;
}
inline void DataType::clear_type_info_native() {
  if (type_info_native_ != NULL) type_info_native_->::llql_proto::DataType_TypeInfo::Clear();
  clear_has_type_info_native();
}
inline const ::llql_proto::DataType_TypeInfo& DataType::type_info_native() const {
  return type_info_native_ != NULL ? *type_info_native_ : *default_instance_->type_info_native_;
}
inline ::llql_proto::DataType_TypeInfo* DataType::mutable_type_info_native() {
  set_has_type_info_native();
  if (type_info_native_ == NULL) type_info_native_ = new ::llql_proto::DataType_TypeInfo;
  return type_info_native_;
}
inline ::llql_proto::DataType_TypeInfo* DataType::release_type_info_native() {
  clear_has_type_info_native();
  ::llql_proto::DataType_TypeInfo* temp = type_info_native_;
  type_info_native_ = NULL;
  return temp;
}
inline void DataType::set_allocated_type_info_native(::llql_proto::DataType_TypeInfo* type_info_native) {
  delete type_info_native_;
  type_info_native_ = type_info_native;
  if (type_info_native) {
    set_has_type_info_native();
  } else {
    clear_has_type_info_native();
  }
}

// repeated .llql_proto.DataType elem_types = 3;
inline int DataType::elem_types_size() const {
  return elem_types_.size();
}
inline void DataType::clear_elem_types() {
  elem_types_.Clear();
}
inline const ::llql_proto::DataType& DataType::elem_types(int index) const {
  return elem_types_.Get(index);
}
inline ::llql_proto::DataType* DataType::mutable_elem_types(int index) {
  return elem_types_.Mutable(index);
}
inline ::llql_proto::DataType* DataType::add_elem_types() {
  return elem_types_.Add();
}
inline const ::google::protobuf::RepeatedPtrField< ::llql_proto::DataType >&
DataType::elem_types() const {
  return elem_types_;
}
inline ::google::protobuf::RepeatedPtrField< ::llql_proto::DataType >*
DataType::mutable_elem_types() {
  return &elem_types_;
}

// -------------------------------------------------------------------

// Schema_Col

// required .llql_proto.DataType col_type = 1;
inline bool Schema_Col::has_col_type() const {
  return (_has_bits_[0] & 0x00000001u) != 0;
}
inline void Schema_Col::set_has_col_type() {
  _has_bits_[0] |= 0x00000001u;
}
inline void Schema_Col::clear_has_col_type() {
  _has_bits_[0] &= ~0x00000001u;
}
inline void Schema_Col::clear_col_type() {
  if (col_type_ != NULL) col_type_->::llql_proto::DataType::Clear();
  clear_has_col_type();
}
inline const ::llql_proto::DataType& Schema_Col::col_type() const {
  return col_type_ != NULL ? *col_type_ : *default_instance_->col_type_;
}
inline ::llql_proto::DataType* Schema_Col::mutable_col_type() {
  set_has_col_type();
  if (col_type_ == NULL) col_type_ = new ::llql_proto::DataType;
  return col_type_;
}
inline ::llql_proto::DataType* Schema_Col::release_col_type() {
  clear_has_col_type();
  ::llql_proto::DataType* temp = col_type_;
  col_type_ = NULL;
  return temp;
}
inline void Schema_Col::set_allocated_col_type(::llql_proto::DataType* col_type) {
  delete col_type_;
  col_type_ = col_type;
  if (col_type) {
    set_has_col_type();
  } else {
    clear_has_col_type();
  }
}

// required bool nullable = 2 [default = true];
inline bool Schema_Col::has_nullable() const {
  return (_has_bits_[0] & 0x00000002u) != 0;
}
inline void Schema_Col::set_has_nullable() {
  _has_bits_[0] |= 0x00000002u;
}
inline void Schema_Col::clear_has_nullable() {
  _has_bits_[0] &= ~0x00000002u;
}
inline void Schema_Col::clear_nullable() {
  nullable_ = true;
  clear_has_nullable();
}
inline bool Schema_Col::nullable() const {
  return nullable_;
}
inline void Schema_Col::set_nullable(bool value) {
  set_has_nullable();
  nullable_ = value;
}

// optional string name = 3;
inline bool Schema_Col::has_name() const {
  return (_has_bits_[0] & 0x00000004u) != 0;
}
inline void Schema_Col::set_has_name() {
  _has_bits_[0] |= 0x00000004u;
}
inline void Schema_Col::clear_has_name() {
  _has_bits_[0] &= ~0x00000004u;
}
inline void Schema_Col::clear_name() {
  if (name_ != &::google::protobuf::internal::kEmptyString) {
    name_->clear();
  }
  clear_has_name();
}
inline const ::std::string& Schema_Col::name() const {
  return *name_;
}
inline void Schema_Col::set_name(const ::std::string& value) {
  set_has_name();
  if (name_ == &::google::protobuf::internal::kEmptyString) {
    name_ = new ::std::string;
  }
  name_->assign(value);
}
inline void Schema_Col::set_name(const char* value) {
  set_has_name();
  if (name_ == &::google::protobuf::internal::kEmptyString) {
    name_ = new ::std::string;
  }
  name_->assign(value);
}
inline void Schema_Col::set_name(const char* value, size_t size) {
  set_has_name();
  if (name_ == &::google::protobuf::internal::kEmptyString) {
    name_ = new ::std::string;
  }
  name_->assign(reinterpret_cast<const char*>(value), size);
}
inline ::std::string* Schema_Col::mutable_name() {
  set_has_name();
  if (name_ == &::google::protobuf::internal::kEmptyString) {
    name_ = new ::std::string;
  }
  return name_;
}
inline ::std::string* Schema_Col::release_name() {
  clear_has_name();
  if (name_ == &::google::protobuf::internal::kEmptyString) {
    return NULL;
  } else {
    ::std::string* temp = name_;
    name_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
    return temp;
  }
}
inline void Schema_Col::set_allocated_name(::std::string* name) {
  if (name_ != &::google::protobuf::internal::kEmptyString) {
    delete name_;
  }
  if (name) {
    set_has_name();
    name_ = name;
  } else {
    clear_has_name();
    name_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  }
}

// -------------------------------------------------------------------

// Schema

// repeated .llql_proto.Schema.Col columns = 1;
inline int Schema::columns_size() const {
  return columns_.size();
}
inline void Schema::clear_columns() {
  columns_.Clear();
}
inline const ::llql_proto::Schema_Col& Schema::columns(int index) const {
  return columns_.Get(index);
}
inline ::llql_proto::Schema_Col* Schema::mutable_columns(int index) {
  return columns_.Mutable(index);
}
inline ::llql_proto::Schema_Col* Schema::add_columns() {
  return columns_.Add();
}
inline const ::google::protobuf::RepeatedPtrField< ::llql_proto::Schema_Col >&
Schema::columns() const {
  return columns_;
}
inline ::google::protobuf::RepeatedPtrField< ::llql_proto::Schema_Col >*
Schema::mutable_columns() {
  return &columns_;
}


// @@protoc_insertion_point(namespace_scope)

}  // namespace llql_proto

#ifndef SWIG
namespace google {
namespace protobuf {

template <>
inline const EnumDescriptor* GetEnumDescriptor< ::llql_proto::DataType_BuiltinType>() {
  return ::llql_proto::DataType_BuiltinType_descriptor();
}

}  // namespace google
}  // namespace protobuf
#endif  // SWIG

// @@protoc_insertion_point(global_scope)

#endif  // PROTOBUF_data_2eproto__INCLUDED
