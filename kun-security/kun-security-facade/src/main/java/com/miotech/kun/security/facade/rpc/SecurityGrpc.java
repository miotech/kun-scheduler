package com.miotech.kun.security.facade.rpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.45.1)",
    comments = "Source: user_role_scope.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class SecurityGrpc {

  private SecurityGrpc() {}

  public static final String SERVICE_NAME = "Security";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleReq,
      com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleResp> getFindUsersOnSpecifiedRoleMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "findUsersOnSpecifiedRole",
      requestType = com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleReq.class,
      responseType = com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleResp.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleReq,
      com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleResp> getFindUsersOnSpecifiedRoleMethod() {
    io.grpc.MethodDescriptor<com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleReq, com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleResp> getFindUsersOnSpecifiedRoleMethod;
    if ((getFindUsersOnSpecifiedRoleMethod = SecurityGrpc.getFindUsersOnSpecifiedRoleMethod) == null) {
      synchronized (SecurityGrpc.class) {
        if ((getFindUsersOnSpecifiedRoleMethod = SecurityGrpc.getFindUsersOnSpecifiedRoleMethod) == null) {
          SecurityGrpc.getFindUsersOnSpecifiedRoleMethod = getFindUsersOnSpecifiedRoleMethod =
              io.grpc.MethodDescriptor.<com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleReq, com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleResp>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "findUsersOnSpecifiedRole"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleReq.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleResp.getDefaultInstance()))
              .setSchemaDescriptor(new SecurityMethodDescriptorSupplier("findUsersOnSpecifiedRole"))
              .build();
        }
      }
    }
    return getFindUsersOnSpecifiedRoleMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleReq,
      com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleResp> getFindRoleOnSpecifiedModuleMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "findRoleOnSpecifiedModule",
      requestType = com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleReq.class,
      responseType = com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleResp.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleReq,
      com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleResp> getFindRoleOnSpecifiedModuleMethod() {
    io.grpc.MethodDescriptor<com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleReq, com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleResp> getFindRoleOnSpecifiedModuleMethod;
    if ((getFindRoleOnSpecifiedModuleMethod = SecurityGrpc.getFindRoleOnSpecifiedModuleMethod) == null) {
      synchronized (SecurityGrpc.class) {
        if ((getFindRoleOnSpecifiedModuleMethod = SecurityGrpc.getFindRoleOnSpecifiedModuleMethod) == null) {
          SecurityGrpc.getFindRoleOnSpecifiedModuleMethod = getFindRoleOnSpecifiedModuleMethod =
              io.grpc.MethodDescriptor.<com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleReq, com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleResp>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "findRoleOnSpecifiedModule"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleReq.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleResp.getDefaultInstance()))
              .setSchemaDescriptor(new SecurityMethodDescriptorSupplier("findRoleOnSpecifiedModule"))
              .build();
        }
      }
    }
    return getFindRoleOnSpecifiedModuleMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesReq,
      com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesResp> getFindRoleOnSpecifiedResourcesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "findRoleOnSpecifiedResources",
      requestType = com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesReq.class,
      responseType = com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesResp.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesReq,
      com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesResp> getFindRoleOnSpecifiedResourcesMethod() {
    io.grpc.MethodDescriptor<com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesReq, com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesResp> getFindRoleOnSpecifiedResourcesMethod;
    if ((getFindRoleOnSpecifiedResourcesMethod = SecurityGrpc.getFindRoleOnSpecifiedResourcesMethod) == null) {
      synchronized (SecurityGrpc.class) {
        if ((getFindRoleOnSpecifiedResourcesMethod = SecurityGrpc.getFindRoleOnSpecifiedResourcesMethod) == null) {
          SecurityGrpc.getFindRoleOnSpecifiedResourcesMethod = getFindRoleOnSpecifiedResourcesMethod =
              io.grpc.MethodDescriptor.<com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesReq, com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesResp>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "findRoleOnSpecifiedResources"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesReq.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesResp.getDefaultInstance()))
              .setSchemaDescriptor(new SecurityMethodDescriptorSupplier("findRoleOnSpecifiedResources"))
              .build();
        }
      }
    }
    return getFindRoleOnSpecifiedResourcesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq,
      com.google.protobuf.Empty> getAddScopeOnSpecifiedRoleMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "addScopeOnSpecifiedRole",
      requestType = com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq,
      com.google.protobuf.Empty> getAddScopeOnSpecifiedRoleMethod() {
    io.grpc.MethodDescriptor<com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq, com.google.protobuf.Empty> getAddScopeOnSpecifiedRoleMethod;
    if ((getAddScopeOnSpecifiedRoleMethod = SecurityGrpc.getAddScopeOnSpecifiedRoleMethod) == null) {
      synchronized (SecurityGrpc.class) {
        if ((getAddScopeOnSpecifiedRoleMethod = SecurityGrpc.getAddScopeOnSpecifiedRoleMethod) == null) {
          SecurityGrpc.getAddScopeOnSpecifiedRoleMethod = getAddScopeOnSpecifiedRoleMethod =
              io.grpc.MethodDescriptor.<com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "addScopeOnSpecifiedRole"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new SecurityMethodDescriptorSupplier("addScopeOnSpecifiedRole"))
              .build();
        }
      }
    }
    return getAddScopeOnSpecifiedRoleMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq,
      com.google.protobuf.Empty> getDeleteScopeOnSpecifiedRoleMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "deleteScopeOnSpecifiedRole",
      requestType = com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq,
      com.google.protobuf.Empty> getDeleteScopeOnSpecifiedRoleMethod() {
    io.grpc.MethodDescriptor<com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq, com.google.protobuf.Empty> getDeleteScopeOnSpecifiedRoleMethod;
    if ((getDeleteScopeOnSpecifiedRoleMethod = SecurityGrpc.getDeleteScopeOnSpecifiedRoleMethod) == null) {
      synchronized (SecurityGrpc.class) {
        if ((getDeleteScopeOnSpecifiedRoleMethod = SecurityGrpc.getDeleteScopeOnSpecifiedRoleMethod) == null) {
          SecurityGrpc.getDeleteScopeOnSpecifiedRoleMethod = getDeleteScopeOnSpecifiedRoleMethod =
              io.grpc.MethodDescriptor.<com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "deleteScopeOnSpecifiedRole"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new SecurityMethodDescriptorSupplier("deleteScopeOnSpecifiedRole"))
              .build();
        }
      }
    }
    return getDeleteScopeOnSpecifiedRoleMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static SecurityStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SecurityStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SecurityStub>() {
        @java.lang.Override
        public SecurityStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SecurityStub(channel, callOptions);
        }
      };
    return SecurityStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static SecurityBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SecurityBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SecurityBlockingStub>() {
        @java.lang.Override
        public SecurityBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SecurityBlockingStub(channel, callOptions);
        }
      };
    return SecurityBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static SecurityFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SecurityFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SecurityFutureStub>() {
        @java.lang.Override
        public SecurityFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SecurityFutureStub(channel, callOptions);
        }
      };
    return SecurityFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class SecurityImplBase implements io.grpc.BindableService {

    /**
     */
    public void findUsersOnSpecifiedRole(com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleReq request,
        io.grpc.stub.StreamObserver<com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleResp> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getFindUsersOnSpecifiedRoleMethod(), responseObserver);
    }

    /**
     */
    public void findRoleOnSpecifiedModule(com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleReq request,
        io.grpc.stub.StreamObserver<com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleResp> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getFindRoleOnSpecifiedModuleMethod(), responseObserver);
    }

    /**
     */
    public void findRoleOnSpecifiedResources(com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesReq request,
        io.grpc.stub.StreamObserver<com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesResp> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getFindRoleOnSpecifiedResourcesMethod(), responseObserver);
    }

    /**
     */
    public void addScopeOnSpecifiedRole(com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAddScopeOnSpecifiedRoleMethod(), responseObserver);
    }

    /**
     */
    public void deleteScopeOnSpecifiedRole(com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteScopeOnSpecifiedRoleMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getFindUsersOnSpecifiedRoleMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleReq,
                com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleResp>(
                  this, METHODID_FIND_USERS_ON_SPECIFIED_ROLE)))
          .addMethod(
            getFindRoleOnSpecifiedModuleMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleReq,
                com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleResp>(
                  this, METHODID_FIND_ROLE_ON_SPECIFIED_MODULE)))
          .addMethod(
            getFindRoleOnSpecifiedResourcesMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesReq,
                com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesResp>(
                  this, METHODID_FIND_ROLE_ON_SPECIFIED_RESOURCES)))
          .addMethod(
            getAddScopeOnSpecifiedRoleMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq,
                com.google.protobuf.Empty>(
                  this, METHODID_ADD_SCOPE_ON_SPECIFIED_ROLE)))
          .addMethod(
            getDeleteScopeOnSpecifiedRoleMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq,
                com.google.protobuf.Empty>(
                  this, METHODID_DELETE_SCOPE_ON_SPECIFIED_ROLE)))
          .build();
    }
  }

  /**
   */
  public static final class SecurityStub extends io.grpc.stub.AbstractAsyncStub<SecurityStub> {
    private SecurityStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SecurityStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SecurityStub(channel, callOptions);
    }

    /**
     */
    public void findUsersOnSpecifiedRole(com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleReq request,
        io.grpc.stub.StreamObserver<com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleResp> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getFindUsersOnSpecifiedRoleMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void findRoleOnSpecifiedModule(com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleReq request,
        io.grpc.stub.StreamObserver<com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleResp> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getFindRoleOnSpecifiedModuleMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void findRoleOnSpecifiedResources(com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesReq request,
        io.grpc.stub.StreamObserver<com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesResp> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getFindRoleOnSpecifiedResourcesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void addScopeOnSpecifiedRole(com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAddScopeOnSpecifiedRoleMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteScopeOnSpecifiedRole(com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteScopeOnSpecifiedRoleMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class SecurityBlockingStub extends io.grpc.stub.AbstractBlockingStub<SecurityBlockingStub> {
    private SecurityBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SecurityBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SecurityBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleResp findUsersOnSpecifiedRole(com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleReq request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getFindUsersOnSpecifiedRoleMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleResp findRoleOnSpecifiedModule(com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleReq request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getFindRoleOnSpecifiedModuleMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesResp findRoleOnSpecifiedResources(com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesReq request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getFindRoleOnSpecifiedResourcesMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty addScopeOnSpecifiedRole(com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAddScopeOnSpecifiedRoleMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty deleteScopeOnSpecifiedRole(com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteScopeOnSpecifiedRoleMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class SecurityFutureStub extends io.grpc.stub.AbstractFutureStub<SecurityFutureStub> {
    private SecurityFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SecurityFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SecurityFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleResp> findUsersOnSpecifiedRole(
        com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleReq request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getFindUsersOnSpecifiedRoleMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleResp> findRoleOnSpecifiedModule(
        com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleReq request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getFindRoleOnSpecifiedModuleMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesResp> findRoleOnSpecifiedResources(
        com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesReq request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getFindRoleOnSpecifiedResourcesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> addScopeOnSpecifiedRole(
        com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAddScopeOnSpecifiedRoleMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteScopeOnSpecifiedRole(
        com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteScopeOnSpecifiedRoleMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_FIND_USERS_ON_SPECIFIED_ROLE = 0;
  private static final int METHODID_FIND_ROLE_ON_SPECIFIED_MODULE = 1;
  private static final int METHODID_FIND_ROLE_ON_SPECIFIED_RESOURCES = 2;
  private static final int METHODID_ADD_SCOPE_ON_SPECIFIED_ROLE = 3;
  private static final int METHODID_DELETE_SCOPE_ON_SPECIFIED_ROLE = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final SecurityImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(SecurityImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_FIND_USERS_ON_SPECIFIED_ROLE:
          serviceImpl.findUsersOnSpecifiedRole((com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleReq) request,
              (io.grpc.stub.StreamObserver<com.miotech.kun.security.facade.rpc.UsersOnSpecifiedRoleResp>) responseObserver);
          break;
        case METHODID_FIND_ROLE_ON_SPECIFIED_MODULE:
          serviceImpl.findRoleOnSpecifiedModule((com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleReq) request,
              (io.grpc.stub.StreamObserver<com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleResp>) responseObserver);
          break;
        case METHODID_FIND_ROLE_ON_SPECIFIED_RESOURCES:
          serviceImpl.findRoleOnSpecifiedResources((com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesReq) request,
              (io.grpc.stub.StreamObserver<com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesResp>) responseObserver);
          break;
        case METHODID_ADD_SCOPE_ON_SPECIFIED_ROLE:
          serviceImpl.addScopeOnSpecifiedRole((com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_DELETE_SCOPE_ON_SPECIFIED_ROLE:
          serviceImpl.deleteScopeOnSpecifiedRole((com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class SecurityBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    SecurityBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.miotech.kun.security.facade.rpc.SecurityProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Security");
    }
  }

  private static final class SecurityFileDescriptorSupplier
      extends SecurityBaseDescriptorSupplier {
    SecurityFileDescriptorSupplier() {}
  }

  private static final class SecurityMethodDescriptorSupplier
      extends SecurityBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    SecurityMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (SecurityGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new SecurityFileDescriptorSupplier())
              .addMethod(getFindUsersOnSpecifiedRoleMethod())
              .addMethod(getFindRoleOnSpecifiedModuleMethod())
              .addMethod(getFindRoleOnSpecifiedResourcesMethod())
              .addMethod(getAddScopeOnSpecifiedRoleMethod())
              .addMethod(getDeleteScopeOnSpecifiedRoleMethod())
              .build();
        }
      }
    }
    return result;
  }
}
