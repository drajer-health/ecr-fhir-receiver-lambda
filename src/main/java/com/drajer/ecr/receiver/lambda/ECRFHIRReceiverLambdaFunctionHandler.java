package com.drajer.ecr.receiver.lambda;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.util.StringUtils;

public class ECRFHIRReceiverLambdaFunctionHandler implements RequestStreamHandler {

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {

		LambdaLogger logger = context.getLogger();

		// URL where the request will be forwarded 
		String httpPostUrl = System.getenv("HTTP_POST_URL");

		if (StringUtils.isNullOrEmpty(httpPostUrl)) {
			throw new RuntimeException("HTTP_POST_URL Environment variable not configured");
		}
		logger.log("HTTP Post URL " + httpPostUrl);

		// Read the input stream
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, Charset.forName("UTF-8"))));

		try {

			StringBuilder inputStrBuilder = new StringBuilder();
			String inputStr;
			while ((inputStr = reader.readLine()) != null)
				inputStrBuilder.append(inputStr);

			// Create a instance of httpClient and forward the request
			DefaultHttpClient httpClient = new DefaultHttpClient();

			// Add content type as application / json 
			HttpPost postRequest = new HttpPost(httpPostUrl);
			postRequest.addHeader("accept", "application/json");
			StringEntity input = new StringEntity(inputStrBuilder.toString());
			input.setContentType("application/json");
			postRequest.setEntity(input);

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
			writer.write(outputStr.toString());
			logger.log(outputStr.toString());
			br.close();
			httpClient.getConnectionManager().shutdown();

		} catch (ClientProtocolException e) {
			throw new RuntimeException("Failed with ClientProtocolException: " + e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException("Failed with IOException: "+e.getMessage());
		} finally {
			reader.close();
			writer.close();
		}

	}

}
