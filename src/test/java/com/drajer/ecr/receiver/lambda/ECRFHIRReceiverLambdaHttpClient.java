package com.drajer.ecr.receiver.lambda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class ECRFHIRReceiverLambdaHttpClient {

	public static void main(String[] args) {
		try {

			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost("https://xxxxxxx.xxxxxxxx.amazonaws.com/dev/$process-message");

			postRequest.addHeader("accept", "application/json");

			StringEntity input = new StringEntity(JSON_INPUT_STRING);
			input.setContentType("application/json");
			postRequest.setEntity(input);

			HttpResponse response = httpClient.execute(postRequest);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			String output;
			System.out.println("Output from Http Client .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			httpClient.getConnectionManager().shutdown();

		} catch (ClientProtocolException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	private static final String JSON_INPUT_STRING = "{\n" + "  \"resourceType\": \"Bundle\",\n"
			+ "  \"id\": \"bundle-example\",\n" + "  \"meta\": {\n" + "    \"lastUpdated\": \"2014-08-18T01:43:30Z\"\n"
			+ "  },\n" + "  \"type\": \"searchset\",\n" + "  \"total\": 3,\n" + "  \"link\": [\n" + "    {\n"
			+ "      \"relation\": \"self\",\n"
			+ "      \"url\": \"https://example.com/base/MedicationRequest?patient=347&_include=MedicationRequest.medication&_count=2\"\n"
			+ "    },\n" + "    {\n" + "      \"relation\": \"next\",\n"
			+ "      \"url\": \"https://example.com/base/MedicationRequest?patient=347&searchId=ff15fd40-ff71-4b48-b366-09c706bed9d0&page=2\"\n"
			+ "    }\n" + "  ],\n" + "  \"entry\": [\n" + "    {\n"
			+ "      \"fullUrl\": \"https://example.com/base/MedicationRequest/3123\",\n" + "      \"resource\": {\n"
			+ "        \"resourceType\": \"MedicationRequest\",\n" + "        \"id\": \"3123\",\n"
			+ "        \"text\": {\n" + "          \"status\": \"generated\",\n"
			+ "          \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\"><p><b>Generated Narrative with Details</b></p><p><b>id</b>: 3123</p><p><b>status</b>: unknown</p><p><b>intent</b>: order</p><p><b>medication</b>: <a>Medication/example</a></p><p><b>subject</b>: <a>Patient/347</a></p></div>\"\n"
			+ "        },\n" + "        \"status\": \"unknown\",\n" + "        \"intent\": \"order\",\n"
			+ "        \"medicationReference\": {\n" + "          \"reference\": \"Medication/example\"\n"
			+ "        },\n" + "        \"subject\": {\n" + "          \"reference\": \"Patient/347\"\n" + "        }\n"
			+ "      },\n" + "      \"search\": {\n" + "        \"mode\": \"match\",\n" + "        \"score\": 1\n"
			+ "      }\n" + "    },\n" + "    {\n"
			+ "      \"fullUrl\": \"https://example.com/base/Medication/example\",\n" + "      \"resource\": {\n"
			+ "        \"resourceType\": \"Medication\",\n" + "        \"id\": \"example\",\n" + "        \"text\": {\n"
			+ "          \"status\": \"generated\",\n"
			+ "          \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\"><p><b>Generated Narrative with Details</b></p><p><b>id</b>: example</p></div>\"\n"
			+ "        }\n" + "      },\n" + "      \"search\": {\n" + "        \"mode\": \"include\"\n" + "      }\n"
			+ "    }\n" + "  ]\n" + "}";

}