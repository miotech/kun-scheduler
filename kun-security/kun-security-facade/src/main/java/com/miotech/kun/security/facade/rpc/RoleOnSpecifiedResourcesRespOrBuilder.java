// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: user_role_scope.proto

package com.miotech.kun.security.facade.rpc;

public interface RoleOnSpecifiedResourcesRespOrBuilder extends
    // @@protoc_insertion_point(interface_extends:RoleOnSpecifiedResourcesResp)
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
   * <code>repeated .ScopeRole scopeRoles = 3;</code>
   */
  java.util.List<com.miotech.kun.security.facade.rpc.ScopeRole> 
      getScopeRolesList();
  /**
   * <code>repeated .ScopeRole scopeRoles = 3;</code>
   */
  com.miotech.kun.security.facade.rpc.ScopeRole getScopeRoles(int index);
  /**
   * <code>repeated .ScopeRole scopeRoles = 3;</code>
   */
  int getScopeRolesCount();
  /**
   * <code>repeated .ScopeRole scopeRoles = 3;</code>
   */
  java.util.List<? extends com.miotech.kun.security.facade.rpc.ScopeRoleOrBuilder> 
      getScopeRolesOrBuilderList();
  /**
   * <code>repeated .ScopeRole scopeRoles = 3;</code>
   */
  com.miotech.kun.security.facade.rpc.ScopeRoleOrBuilder getScopeRolesOrBuilder(
      int index);
}