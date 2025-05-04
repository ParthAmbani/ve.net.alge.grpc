//package org.idempiere.grpc.test;
//
//import idempiere.ProductServiceGrpc;
//import idempiere.ProductServiceOuterClass.ProductRequest;
//import idempiere.ProductServiceOuterClass.ProductResponse;
//import io.grpc.ManagedChannel;
//import io.grpc.ManagedChannelBuilder;
//
//public class GrpcClientTest {
//
//    public static void main(String[] args) {
//        // Make sure server is up and running before executing this
//        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
//                .usePlaintext()
//                .build();
//
//        // Block and make sure the request goes through
//        ProductServiceGrpc.ProductServiceBlockingStub stub = ProductServiceGrpc.newBlockingStub(channel);
//
//        ProductRequest request = ProductRequest.newBuilder().setId("12345").build();
//
//        try {
//            ProductResponse response = stub.getProduct(request);
//            System.out.println("Received response: " + response.getName() + ", " + response.getDescription());
//        } catch (Exception e) {
//            System.err.println("Error while calling gRPC service: " + e.getMessage());
//            e.printStackTrace();
//        } finally {
//            channel.shutdownNow();
//        }
//    }
//}
