package com.drajer.ecr.receiver.lambda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class ECRFHIRReceiverLambdaFunctionHandler
		implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {

		LambdaLogger logger = context.getLogger();
		APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent();

		// URL where the request will be forwarded
		String httpPostUrl = System.getenv("HTTP_POST_URL");

		if (httpPostUrl == null) {
			throw new RuntimeException("HTTP_POST_URL Environment variable not configured");
		}
		logger.log("HTTP Post URL " + httpPostUrl);

		try {

			Map<String, String> headers = request.getHeaders();
			String authHeader = headers.get("Authorization");

			logger.log("Token: " + authHeader);

			// Create a instance of httpClient and forward the request
			DefaultHttpClient httpClient = new DefaultHttpClient();

			// Add content type as application / json
			HttpPost postRequest = new HttpPost(httpPostUrl);
			postRequest.addHeader("accept", "application/json");
			StringEntity input = new StringEntity(request.getBody());
			input.setContentType("application/json");
			postRequest.setEntity(input);
			postRequest.addHeader("Authorization", authHeader);

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
			}finally {
				logger.log("Closing HTTP Connection to "+httpPostUrl);
				httpClient.getConnectionManager().shutdown();
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
			logger.log(outputStr.toString());
		} catch (ClientProtocolException e) {
			logger.log("Failed with ClientProtocolException "+e.getMessage() );
			throw new RuntimeException("Failed with ClientProtocolException: " + e.getMessage());
		} catch (IOException e) {
			logger.log("Failed with IOException "+e.getMessage());
			throw new RuntimeException("Failed with IOException: " + e.getMessage());
		} finally {
//			reader.close();
//			writer.close();
		}
		return apiGatewayProxyResponseEvent;

	}

}
