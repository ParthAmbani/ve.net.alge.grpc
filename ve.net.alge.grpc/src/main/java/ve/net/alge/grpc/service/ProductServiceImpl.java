
package ve.net.alge.grpc.service;

import org.idempiere.grpc.ProductServiceGrpc;
import org.idempiere.grpc.ProductServiceOuterClass.ProductRequest;
import org.idempiere.grpc.ProductServiceOuterClass.ProductResponse;

import io.grpc.stub.StreamObserver;

public class ProductServiceImpl extends ProductServiceGrpc.ProductServiceImplBase {
	@Override
	public void getProduct(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
	    try {
	        // Simulate response generation (replace with actual logic)
	        ProductResponse response = ProductResponse.newBuilder()
	                .setProductId(request.getId())
	                .setName("Sample Product")
	                .setDescription("This is a sample product.")
	                .build();
	        responseObserver.onNext(response);
	        responseObserver.onCompleted();
	    } catch (Exception e) {
	    	System.err.println("Error processing request: " + e.getMessage());
	        e.printStackTrace();
	        responseObserver.onError(io.grpc.Status.INTERNAL.withDescription("Server error").withCause(e).asRuntimeException());
	   }
	}

}

