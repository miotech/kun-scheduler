// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: user_role_scope.proto

package com.miotech.kun.security.facade.rpc;

public interface UpdateScopeOnSpecifiedRoleReqOrBuilder extends
    // @@protoc_insertion_point(interface_extends:UpdateScopeOnSpecifiedRoleReq)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string username = 1;</code>
   * @return The username.
   */
  java.lang.String getUsername();
  /**
   * <code>string username = 1;</code>
   * @return The bytes for username.
   */
  com.google.protobuf.ByteString
      getUsernameBytes();

  /**
   * <code>string rolename = 2;</code>
   * @return The rolename.
   */
  java.lang.String getRolename();
  /**
   * <code>string rolename = 2;</code>
   * @return The bytes for rolename.
   */
  com.google.protobuf.ByteString
      getRolenameBytes();

  /**
   * <code>string module = 3;</code>
   * @return The module.
   */
  java.lang.String getModule();
  /**
   * <code>string module = 3;</code>
   * @return The bytes for module.
   */
  com.google.protobuf.ByteString
      getModuleBytes();

  /**
   * <code>repeated string sourceSystemIds = 4;</code>
   * @return A list containing the sourceSystemIds.
   */
  java.util.List<java.lang.String>
      getSourceSystemIdsList();
  /**
   * <code>repeated string sourceSystemIds = 4;</code>
   * @return The count of sourceSystemIds.
   */
  int getSourceSystemIdsCount();
  /**
   * <code>repeated string sourceSystemIds = 4;</code>
   * @param index The index of the element to return.
   * @return The sourceSystemIds at the given index.
   */
  java.lang.String getSourceSystemIds(int index);
  /**
   * <code>repeated string sourceSystemIds = 4;</code>
   * @param index The index of the value to return.
   * @return The bytes of the sourceSystemIds at the given index.
   */
  com.google.protobuf.ByteString
      getSourceSystemIdsBytes(int index);
}
