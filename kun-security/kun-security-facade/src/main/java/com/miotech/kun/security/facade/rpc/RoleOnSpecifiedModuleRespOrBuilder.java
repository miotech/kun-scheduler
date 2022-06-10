// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: user_role_scope.proto

package com.miotech.kun.security.facade.rpc;

public interface RoleOnSpecifiedModuleRespOrBuilder extends
    // @@protoc_insertion_point(interface_extends:RoleOnSpecifiedModuleResp)
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
   * <code>string module = 2;</code>
   * @return The module.
   */
  java.lang.String getModule();
  /**
   * <code>string module = 2;</code>
   * @return The bytes for module.
   */
  com.google.protobuf.ByteString
      getModuleBytes();

  /**
   * <code>repeated string rolenames = 3;</code>
   * @return A list containing the rolenames.
   */
  java.util.List<java.lang.String>
      getRolenamesList();
  /**
   * <code>repeated string rolenames = 3;</code>
   * @return The count of rolenames.
   */
  int getRolenamesCount();
  /**
   * <code>repeated string rolenames = 3;</code>
   * @param index The index of the element to return.
   * @return The rolenames at the given index.
   */
  java.lang.String getRolenames(int index);
  /**
   * <code>repeated string rolenames = 3;</code>
   * @param index The index of the value to return.
   * @return The bytes of the rolenames at the given index.
   */
  com.google.protobuf.ByteString
      getRolenamesBytes(int index);
}
