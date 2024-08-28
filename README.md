# ecr-fhir-receiver-lambda
ECR FHIR receiver Lambda function

Prerequisites:
1.	Java 17 or Higher
2.	AWS SDK - STS or Eclipse
3.	AWS Account
4.	Maven 3.3.x
5.	GIT

## Clone the Repository

Clone the repository using the below command in command prompt

```git clone https://github.com/drajer-health/ecr-fhir-receiver-lambda.git```

## Create Build:
Import Project as Maven Project
Build ECR FHIR Receiver AWS Lambda Service:

Navigate to  ecr-fhir-receiver-lambda  directory `..../ ` and run Maven build to build lambda jar file.

```
$ mvn clean install

```

This will generate a war file under target/ecr-fhir-receiver-lambda.jar.

## Deploy eCR FHIR Receiver Lambda:

Login to your AWS Account

1) Click on Services then select Lambda

2) Click on Create Function

3) Select "Author from Scratch" option

4) Enter:

```
Function Name: ecrFHIRReceiverLambda
Runtime: Java 17
Permissions: Create a new role with basic Lambda permissions or select your organization specific security

```
5) Click on "Create Function"

## At this point Lambda function would be created, navigate to the newly created function and configure the lambda function and environment variable.

1) Under the "Code" tab select "Upload from"

2) Select .zip or .jar file option

3) Click upload and navigate to your local workspace target folder and select ecr-fhir-receiver-lambda.jar and click "Save"  

4) Click on "Edit" on "Runtime Settings"

5) Enter below value for Handler

```
com.drajer.ecr.receiver.lambda.ECRFHIRReceiverLambdaFunctionHandler

```

6) Click "Save"

7) Click on "Configuration" tab and then "Environment Variables"

8) Click on "Edit" to add new environment variable

9) Click on "Add new environment variable"

10) Enter

```
Key: HTTP_POST_URL
Value: <<THIS SHOULD BE YOUR FHIR SERVER URL FOR THE REQUEST TO BE FORWARDED>>

```

### Optional Test the Lambda Function

1) Click on "Test" tab

2) Use the test/resource/ecrFHIRReceiverTestData.json data to test newly created Lambda Function


## At this point Lambda function is configured and ready to use, next step is to configure API Gateway

## Configuring API Gateway

1) Click on Services and search for "API Gateway"

2) Click on "Create New"

3) Click on "Build" on Rest API option

4) Enter

```
Choose Protocol: REST
Create New API: New API
Settings:
	API Name: $process-message
	Description: eCR FHIR Receiver Lambda function to forward ecr data to FHIR service
	EndPoint Type: Regional or based on your organizational needs

```
5) Click Create API

6) Click on "Actions -> Create Resource"

7) Enter

```
Resource Name*: $process-message
Resource Path*: $process-message

```
8) Click "Create Resource"

9) Select $process-message and then click "Actions -> Create Method"

10) Select "ANY" and click on tick mark

11) Enter

```
	Integration Type: Lambda Function
	Use Lambda Proxy Integration: Checked 
	Lambda Function: ecrFHIRReceiverLambda

```
12) Click "Save"

13) Click "Ok" to add permissions for lambda and API gateway

14) Click "Actions -> Deploy API"

15) Select "Deployment Stage"

16) Enter

```
	Stage Name*: dev
	Stage Description: Development Environment
	Deployment Description: Development Environment

```

17) Click "Deploy"

18) API Gateway is configured !!!!

NOTE: Make a note of API Gateway end point and use ECRFHIRReceiverLambdaHttpClient.java to run the test
