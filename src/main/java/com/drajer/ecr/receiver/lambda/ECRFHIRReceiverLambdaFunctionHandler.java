package com.drajer.ecr.receiver.lambda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.xray.contexts.LambdaSegmentContext;
import com.amazonaws.xray.entities.TraceHeader;
public class ECRFHIRReceiverLambdaFunctionHandler
		implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {

		LambdaLogger logger = context.getLogger();
		APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent();
		

		TraceHeader tHeader = LambdaSegmentContext.getTraceHeaderFromEnvironment();
		String traceId = tHeader.getRootTraceId().toString();
		
		logger.log("tHeader.getRootTraceId() ::::"+traceId);
		// URL where the request will be forwarded
		String httpPostUrl = System.getenv("HTTP_POST_URL");

		if (httpPostUrl == null) {
			throw new RuntimeException("HTTP_POST_URL Environment variable not configured");
		}
		logger.log("HTTP Post URL " + httpPostUrl);
		// Create a instance of httpClient and forward the request
//		DefaultHttpClient httpClient = new DefaultHttpClient();

		int timeout = 15;
		RequestConfig config = RequestConfig.custom()
		  .setConnectTimeout(timeout * 1000)
		  .setConnectionRequestTimeout(timeout * 1000)
		  .setSocketTimeout(timeout * 1000).build();
		CloseableHttpClient httpClient = 
		  HttpClientBuilder.create().setDefaultRequestConfig(config).build();		
		try {
			
			logger.log("Headers "+request.getHeaders());
		//	logger.log("body " + request.getBody());

			Map<String, String> headers = request.getHeaders();
			String authHeader = "";
			if (headers != null && ! headers.isEmpty()) {
				 authHeader = headers.get("Authorization");
			}else {
				logger.log("No headers" );
			}

			logger.log("Token: " + authHeader);

			// Add content type as application / json
			HttpPost postRequest = new HttpPost(httpPostUrl);
			postRequest.addHeader("accept", "application/json");
			StringEntity input = new StringEntity(request.getBody());
			input.setContentType("application/json");
			postRequest.setEntity(input);
			postRequest.addHeader("Authorization", authHeader);
			postRequest.addHeader("persistenceId", traceId);

			logger.log("Forwarding the request to FHIR Validator with Auth Header ");
			logger.log("Request Body Content Size "+input.getContentLength());

			// logger.log(inputStrBuilder.toString());

			HttpResponse response = null;
			try {
				logger.log("Making the HTTP Post to "+httpPostUrl );
				response = httpClient.execute(postRequest);
				logger.log("HTTP Post completed " );
			}catch(Exception e) {
				logger.log(" In HTTP Post Exception "+e.getLocalizedMessage());
				e.printStackTrace();
			}
			
			// Check return status and throw Runtime exception for return code != 200
			if (response !=null && response.getStatusLine().getStatusCode() != 200) {
				logger.log("Post Message failed with Code: " +response.getStatusLine().getStatusCode());
				logger.log("Post Message failed reason: " +response.getStatusLine().getReasonPhrase());
				logger.log("Post Message response body: "+response.toString());
				
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}
			StringBuilder outputStr = new StringBuilder();
			
			if (response !=null ) {
				BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
				String output;
				logger.log("Response from FHIR Validator .... ");
				// Write the response back to invoking program
				while ((output = br.readLine()) != null) {
					outputStr.append(output);
				}
				br.close();
			}			
			apiGatewayProxyResponseEvent.setBody(outputStr.toString());
		//	logger.log(outputStr.toString());
		} catch (ClientProtocolException e) {
			logger.log("Failed with ClientProtocolException "+e.getMessage() );
			throw new RuntimeException("Failed with ClientProtocolException: " + e.getMessage());
		} catch (IOException e) {
			logger.log("Failed with IOException "+e.getMessage());
			throw new RuntimeException("Failed with IOException: " + e.getMessage());
		} finally {
//			reader.close();
//			writer.close();
				logger.log("Closing HTTP Connection to "+httpPostUrl);
				try {
					httpClient.close();
				} catch (IOException e) {
					logger.log("Failed with close connection "+e.getMessage() );
				}
	
		}
		return apiGatewayProxyResponseEvent;

	}

}
