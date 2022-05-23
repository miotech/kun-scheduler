// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: user_role_scope.proto

package com.miotech.kun.security.facade.rpc;

/**
 * Protobuf type {@code UpdateScopeOnSpecifiedRoleReq}
 */
public final class UpdateScopeOnSpecifiedRoleReq extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:UpdateScopeOnSpecifiedRoleReq)
    UpdateScopeOnSpecifiedRoleReqOrBuilder {
private static final long serialVersionUID = 0L;
  // Use UpdateScopeOnSpecifiedRoleReq.newBuilder() to construct.
  private UpdateScopeOnSpecifiedRoleReq(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private UpdateScopeOnSpecifiedRoleReq() {
    username_ = "";
    rolename_ = "";
    module_ = "";
    sourceSystemIds_ = com.google.protobuf.LazyStringArrayList.EMPTY;
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new UpdateScopeOnSpecifiedRoleReq();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private UpdateScopeOnSpecifiedRoleReq(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    int mutable_bitField0_ = 0;
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 10: {
            java.lang.String s = input.readStringRequireUtf8();

            username_ = s;
            break;
          }
          case 18: {
            java.lang.String s = input.readStringRequireUtf8();

            rolename_ = s;
            break;
          }
          case 26: {
            java.lang.String s = input.readStringRequireUtf8();

            module_ = s;
            break;
          }
          case 34: {
            java.lang.String s = input.readStringRequireUtf8();
            if (!((mutable_bitField0_ & 0x00000001) != 0)) {
              sourceSystemIds_ = new com.google.protobuf.LazyStringArrayList();
              mutable_bitField0_ |= 0x00000001;
            }
            sourceSystemIds_.add(s);
            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      if (((mutable_bitField0_ & 0x00000001) != 0)) {
        sourceSystemIds_ = sourceSystemIds_.getUnmodifiableView();
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.miotech.kun.security.facade.rpc.SecurityProto.internal_static_UpdateScopeOnSpecifiedRoleReq_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.miotech.kun.security.facade.rpc.SecurityProto.internal_static_UpdateScopeOnSpecifiedRoleReq_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq.class, com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq.Builder.class);
  }

  public static final int USERNAME_FIELD_NUMBER = 1;
  private volatile java.lang.Object username_;
  /**
   * <code>string username = 1;</code>
   * @return The username.
   */
  @java.lang.Override
  public java.lang.String getUsername() {
    java.lang.Object ref = username_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      username_ = s;
      return s;
    }
  }
  /**
   * <code>string username = 1;</code>
   * @return The bytes for username.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getUsernameBytes() {
    java.lang.Object ref = username_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      username_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int ROLENAME_FIELD_NUMBER = 2;
  private volatile java.lang.Object rolename_;
  /**
   * <code>string rolename = 2;</code>
   * @return The rolename.
   */
  @java.lang.Override
  public java.lang.String getRolename() {
    java.lang.Object ref = rolename_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      rolename_ = s;
      return s;
    }
  }
  /**
   * <code>string rolename = 2;</code>
   * @return The bytes for rolename.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getRolenameBytes() {
    java.lang.Object ref = rolename_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      rolename_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int MODULE_FIELD_NUMBER = 3;
  private volatile java.lang.Object module_;
  /**
   * <code>string module = 3;</code>
   * @return The module.
   */
  @java.lang.Override
  public java.lang.String getModule() {
    java.lang.Object ref = module_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      module_ = s;
      return s;
    }
  }
  /**
   * <code>string module = 3;</code>
   * @return The bytes for module.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getModuleBytes() {
    java.lang.Object ref = module_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      module_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int SOURCESYSTEMIDS_FIELD_NUMBER = 4;
  private com.google.protobuf.LazyStringList sourceSystemIds_;
  /**
   * <code>repeated string sourceSystemIds = 4;</code>
   * @return A list containing the sourceSystemIds.
   */
  public com.google.protobuf.ProtocolStringList
      getSourceSystemIdsList() {
    return sourceSystemIds_;
  }
  /**
   * <code>repeated string sourceSystemIds = 4;</code>
   * @return The count of sourceSystemIds.
   */
  public int getSourceSystemIdsCount() {
    return sourceSystemIds_.size();
  }
  /**
   * <code>repeated string sourceSystemIds = 4;</code>
   * @param index The index of the element to return.
   * @return The sourceSystemIds at the given index.
   */
  public java.lang.String getSourceSystemIds(int index) {
    return sourceSystemIds_.get(index);
  }
  /**
   * <code>repeated string sourceSystemIds = 4;</code>
   * @param index The index of the value to return.
   * @return The bytes of the sourceSystemIds at the given index.
   */
  public com.google.protobuf.ByteString
      getSourceSystemIdsBytes(int index) {
    return sourceSystemIds_.getByteString(index);
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(username_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, username_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(rolename_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, rolename_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(module_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, module_);
    }
    for (int i = 0; i < sourceSystemIds_.size(); i++) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 4, sourceSystemIds_.getRaw(i));
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(username_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, username_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(rolename_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, rolename_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(module_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, module_);
    }
    {
      int dataSize = 0;
      for (int i = 0; i < sourceSystemIds_.size(); i++) {
        dataSize += computeStringSizeNoTag(sourceSystemIds_.getRaw(i));
      }
      size += dataSize;
      size += 1 * getSourceSystemIdsList().size();
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq)) {
      return super.equals(obj);
    }
    com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq other = (com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq) obj;

    if (!getUsername()
        .equals(other.getUsername())) return false;
    if (!getRolename()
        .equals(other.getRolename())) return false;
    if (!getModule()
        .equals(other.getModule())) return false;
    if (!getSourceSystemIdsList()
        .equals(other.getSourceSystemIdsList())) return false;
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + USERNAME_FIELD_NUMBER;
    hash = (53 * hash) + getUsername().hashCode();
    hash = (37 * hash) + ROLENAME_FIELD_NUMBER;
    hash = (53 * hash) + getRolename().hashCode();
    hash = (37 * hash) + MODULE_FIELD_NUMBER;
    hash = (53 * hash) + getModule().hashCode();
    if (getSourceSystemIdsCount() > 0) {
      hash = (37 * hash) + SOURCESYSTEMIDS_FIELD_NUMBER;
      hash = (53 * hash) + getSourceSystemIdsList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code UpdateScopeOnSpecifiedRoleReq}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:UpdateScopeOnSpecifiedRoleReq)
      com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReqOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.miotech.kun.security.facade.rpc.SecurityProto.internal_static_UpdateScopeOnSpecifiedRoleReq_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.miotech.kun.security.facade.rpc.SecurityProto.internal_static_UpdateScopeOnSpecifiedRoleReq_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq.class, com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq.Builder.class);
    }

    // Construct using com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      username_ = "";

      rolename_ = "";

      module_ = "";

      sourceSystemIds_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.miotech.kun.security.facade.rpc.SecurityProto.internal_static_UpdateScopeOnSpecifiedRoleReq_descriptor;
    }

    @java.lang.Override
    public com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq getDefaultInstanceForType() {
      return com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq.getDefaultInstance();
    }

    @java.lang.Override
    public com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq build() {
      com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq buildPartial() {
      com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq result = new com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq(this);
      int from_bitField0_ = bitField0_;
      result.username_ = username_;
      result.rolename_ = rolename_;
      result.module_ = module_;
      if (((bitField0_ & 0x00000001) != 0)) {
        sourceSystemIds_ = sourceSystemIds_.getUnmodifiableView();
        bitField0_ = (bitField0_ & ~0x00000001);
      }
      result.sourceSystemIds_ = sourceSystemIds_;
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq) {
        return mergeFrom((com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq other) {
      if (other == com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq.getDefaultInstance()) return this;
      if (!other.getUsername().isEmpty()) {
        username_ = other.username_;
        onChanged();
      }
      if (!other.getRolename().isEmpty()) {
        rolename_ = other.rolename_;
        onChanged();
      }
      if (!other.getModule().isEmpty()) {
        module_ = other.module_;
        onChanged();
      }
      if (!other.sourceSystemIds_.isEmpty()) {
        if (sourceSystemIds_.isEmpty()) {
          sourceSystemIds_ = other.sourceSystemIds_;
          bitField0_ = (bitField0_ & ~0x00000001);
        } else {
          ensureSourceSystemIdsIsMutable();
          sourceSystemIds_.addAll(other.sourceSystemIds_);
        }
        onChanged();
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private java.lang.Object username_ = "";
    /**
     * <code>string username = 1;</code>
     * @return The username.
     */
    public java.lang.String getUsername() {
      java.lang.Object ref = username_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        username_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string username = 1;</code>
     * @return The bytes for username.
     */
    public com.google.protobuf.ByteString
        getUsernameBytes() {
      java.lang.Object ref = username_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        username_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string username = 1;</code>
     * @param value The username to set.
     * @return This builder for chaining.
     */
    public Builder setUsername(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      username_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string username = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearUsername() {
      
      username_ = getDefaultInstance().getUsername();
      onChanged();
      return this;
    }
    /**
     * <code>string username = 1;</code>
     * @param value The bytes for username to set.
     * @return This builder for chaining.
     */
    public Builder setUsernameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      username_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object rolename_ = "";
    /**
     * <code>string rolename = 2;</code>
     * @return The rolename.
     */
    public java.lang.String getRolename() {
      java.lang.Object ref = rolename_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        rolename_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string rolename = 2;</code>
     * @return The bytes for rolename.
     */
    public com.google.protobuf.ByteString
        getRolenameBytes() {
      java.lang.Object ref = rolename_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        rolename_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string rolename = 2;</code>
     * @param value The rolename to set.
     * @return This builder for chaining.
     */
    public Builder setRolename(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      rolename_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string rolename = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearRolename() {
      
      rolename_ = getDefaultInstance().getRolename();
      onChanged();
      return this;
    }
    /**
     * <code>string rolename = 2;</code>
     * @param value The bytes for rolename to set.
     * @return This builder for chaining.
     */
    public Builder setRolenameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      rolename_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object module_ = "";
    /**
     * <code>string module = 3;</code>
     * @return The module.
     */
    public java.lang.String getModule() {
      java.lang.Object ref = module_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        module_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string module = 3;</code>
     * @return The bytes for module.
     */
    public com.google.protobuf.ByteString
        getModuleBytes() {
      java.lang.Object ref = module_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        module_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string module = 3;</code>
     * @param value The module to set.
     * @return This builder for chaining.
     */
    public Builder setModule(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      module_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string module = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearModule() {
      
      module_ = getDefaultInstance().getModule();
      onChanged();
      return this;
    }
    /**
     * <code>string module = 3;</code>
     * @param value The bytes for module to set.
     * @return This builder for chaining.
     */
    public Builder setModuleBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      module_ = value;
      onChanged();
      return this;
    }

    private com.google.protobuf.LazyStringList sourceSystemIds_ = com.google.protobuf.LazyStringArrayList.EMPTY;
    private void ensureSourceSystemIdsIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        sourceSystemIds_ = new com.google.protobuf.LazyStringArrayList(sourceSystemIds_);
        bitField0_ |= 0x00000001;
       }
    }
    /**
     * <code>repeated string sourceSystemIds = 4;</code>
     * @return A list containing the sourceSystemIds.
     */
    public com.google.protobuf.ProtocolStringList
        getSourceSystemIdsList() {
      return sourceSystemIds_.getUnmodifiableView();
    }
    /**
     * <code>repeated string sourceSystemIds = 4;</code>
     * @return The count of sourceSystemIds.
     */
    public int getSourceSystemIdsCount() {
      return sourceSystemIds_.size();
    }
    /**
     * <code>repeated string sourceSystemIds = 4;</code>
     * @param index The index of the element to return.
     * @return The sourceSystemIds at the given index.
     */
    public java.lang.String getSourceSystemIds(int index) {
      return sourceSystemIds_.get(index);
    }
    /**
     * <code>repeated string sourceSystemIds = 4;</code>
     * @param index The index of the value to return.
     * @return The bytes of the sourceSystemIds at the given index.
     */
    public com.google.protobuf.ByteString
        getSourceSystemIdsBytes(int index) {
      return sourceSystemIds_.getByteString(index);
    }
    /**
     * <code>repeated string sourceSystemIds = 4;</code>
     * @param index The index to set the value at.
     * @param value The sourceSystemIds to set.
     * @return This builder for chaining.
     */
    public Builder setSourceSystemIds(
        int index, java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  ensureSourceSystemIdsIsMutable();
      sourceSystemIds_.set(index, value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string sourceSystemIds = 4;</code>
     * @param value The sourceSystemIds to add.
     * @return This builder for chaining.
     */
    public Builder addSourceSystemIds(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  ensureSourceSystemIdsIsMutable();
      sourceSystemIds_.add(value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string sourceSystemIds = 4;</code>
     * @param values The sourceSystemIds to add.
     * @return This builder for chaining.
     */
    public Builder addAllSourceSystemIds(
        java.lang.Iterable<java.lang.String> values) {
      ensureSourceSystemIdsIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(
          values, sourceSystemIds_);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string sourceSystemIds = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearSourceSystemIds() {
      sourceSystemIds_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string sourceSystemIds = 4;</code>
     * @param value The bytes of the sourceSystemIds to add.
     * @return This builder for chaining.
     */
    public Builder addSourceSystemIdsBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      ensureSourceSystemIdsIsMutable();
      sourceSystemIds_.add(value);
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:UpdateScopeOnSpecifiedRoleReq)
  }

  // @@protoc_insertion_point(class_scope:UpdateScopeOnSpecifiedRoleReq)
  private static final com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq();
  }

  public static com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<UpdateScopeOnSpecifiedRoleReq>
      PARSER = new com.google.protobuf.AbstractParser<UpdateScopeOnSpecifiedRoleReq>() {
    @java.lang.Override
    public UpdateScopeOnSpecifiedRoleReq parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new UpdateScopeOnSpecifiedRoleReq(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<UpdateScopeOnSpecifiedRoleReq> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<UpdateScopeOnSpecifiedRoleReq> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

