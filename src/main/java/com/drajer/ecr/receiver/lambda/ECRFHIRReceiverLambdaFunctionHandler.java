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

			logger.log("Forwarding the request to FHIR Validator ");

			// logger.log(inputStrBuilder.toString());

			HttpResponse response = httpClient.execute(postRequest);

			// Check return status and throw Runtime exception for return code != 200
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			String output;
			StringBuilder outputStr = new StringBuilder();
			logger.log("Response from FHIR Validator .... ");
			// Write the response back to invoking program
			while ((output = br.readLine()) != null) {
				outputStr.append(output);
			}
			apiGatewayProxyResponseEvent.setBody(outputStr.toString());
			logger.log(outputStr.toString());
			br.close();
			httpClient.getConnectionManager().shutdown();

		} catch (ClientProtocolException e) {
			throw new RuntimeException("Failed with ClientProtocolException: " + e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException("Failed with IOException: " + e.getMessage());
		} finally {
//			reader.close();
//			writer.close();
		}
		return apiGatewayProxyResponseEvent;

	}

}
