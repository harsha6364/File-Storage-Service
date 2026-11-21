# File-Storage-Service



\# User Document Storage Service



A backend application developed using Java and Spring Boot that allows users to upload, search, and download documents using Amazon S3.



\## 1. Project Overview



The User Document Storage Service is a REST API application developed as part of the FreightFox Backend Assignment.



The application uses a single Amazon S3 bucket to store documents belonging to different users. Each user has a separate folder (prefix) inside the bucket.



Users can upload documents, search for files by filename, and download their documents through REST APIs.



\## 2. Project Links



\- \*\*GitHub Repository:\*\* https://github.com/harsha6364/File-Storage-Service

\- \*\*Live Application:\*\* http://100.63.112.142:8080/api/v1/files

\- \*\*Swagger Documentation:\*\* http://100.63.112.142:8080/swagger-ui/index.html#/

\- \*\*Loom Demo Video:\*\* https://www.loom.com/share/300e08a1fb04417db882fea9106e4714



\## 3. Features



\- Upload documents to Amazon S3.

\- Search documents by filename.

\- Download documents using the filename.

\- Store each user's documents under a separate S3 prefix.

\- Validate user names, filenames, and uploaded files.

\- Handle application and AWS-related exceptions.

\- Test REST APIs using Swagger UI and Postman.



\## 4. Technology Stack



\- \*\*Programming Language:\*\* Java

\- \*\*Framework:\*\* Spring Boot

\- \*\*Cloud Storage:\*\* Amazon S3

\- \*\*AWS SDK:\*\* AWS SDK for Java 2.x

\- \*\*Build Tool:\*\* Maven

\- \*\*API Documentation:\*\* Swagger / OpenAPI

\- \*\*Testing:\*\* JUnit 5 and Mockito



\## 5. Project Structure



```text

src

&#x20;├── main

&#x20;│   ├── java/com/freightfox/filestorage

&#x20;│   │   ├── config

&#x20;│   │   ├── controller

&#x20;│   │   ├── dto

&#x20;│   │   ├── exception

&#x20;│   │   ├── response

&#x20;│   │   ├── service

&#x20;│   │   │   └── impl

&#x20;│   │   └── FileStorageServiceApplication.java

&#x20;│   └── resources

&#x20;│       └── application.properties

&#x20;│

&#x20;└── test

&#x20;    └── java/com/freightfox/filestorage

&#x20;        └── service/impl

```



\## 6. AWS S3 Configuration



1\. Create an Amazon S3 bucket.

2\. Configure the AWS region.

3\. Create an IAM user with the required S3 permissions.

4\. Configure AWS credentials using the AWS CLI or environment variables.

5\. Update the bucket name in `application.properties`.



Example configuration:



```properties

spring.application.name=FileStorageService



aws.s3.bucket-name=your-bucket-name



spring.servlet.multipart.max-file-size=10MB

spring.servlet.multipart.max-request-size=12MB

```



\*\*Note:\*\* AWS credentials should be configured securely and must not be committed to the GitHub repository.



\## 7. API Endpoints



Base URL:



```text

http://localhost:8080/api/v1/files

```



| HTTP Method | Endpoint | Description |

|---|---|---|

| POST | `/upload` | Upload a document |

| GET | `/search` | Search documents by filename |

| GET | `/download` | Download a document |



\### 7.1 Upload Document



\*\*Endpoint:\*\* `POST /api/v1/files/upload`



\*\*Request Type:\*\* `multipart/form-data`



Parameters:

\- `userName` – Name of the user.

\- `file` – Document to upload.



\### 7.2 Search Documents



\*\*Endpoint:\*\* `GET /api/v1/files/search`



Example:



```text

/api/v1/files/search?userName=sandy\&fileName=sample

```



Searches for files matching the given filename within the specified user's folder.



\### 7.3 Download Document



\*\*Endpoint:\*\* `GET /api/v1/files/download`



Example:



```text

/api/v1/files/download?userName=sandy\&fileName=sample.txt

```



Downloads the requested document from Amazon S3.



\## 8. How to Run the Project Locally



\*\*Step 1:\*\* Clone the repository.



```bash

git clone https://github.com/harsha6364/File-Storage-Service.git

```



\*\*Step 2:\*\* Open the project in your preferred IDE.



\*\*Step 3:\*\* Configure AWS credentials and update the S3 bucket name in `application.properties`.



\*\*Step 4:\*\* Run the application.



```bash

mvn spring-boot:run

```



\*\*Step 5:\*\* Open Swagger UI to access and test the APIs.



```text

http://localhost:8080/swagger-ui/index.html

```



\## 9. Running Tests



Run the following command to execute the test cases:



```bash

mvn clean test

```



Unit tests are implemented using JUnit 5 and Mockito for upload, search, and download operations.



\*\*Test Results:\*\* 24 tests passed successfully.



\## 10. Assignment Submission



This project was developed as part of the FreightFox Backend Assignment.



The application demonstrates REST API development, Amazon S3 integration, file handling, exception handling, and unit testing using Java and Spring Boot.



\*\*Author:\*\* C Harsha

