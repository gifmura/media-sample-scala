package services.amazon

import java.io.File

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.{
  PutObjectRequest,
  PutObjectResult,
  S3ObjectInputStream
}
import com.amazonaws.{ClientConfiguration, Protocol}
import com.google.inject.Singleton
import javax.inject.Inject
import play.api.Configuration

@Singleton
class S3Service @Inject()(config: Configuration) {

  private val accessKey = config.get[String]("aws.s3.accessKey")
  private val secretKey = config.get[String]("aws.s3.secretKey")
  private val bucketName = config.get[String]("aws.s3.bucketName")
  private val serviceEndpoint = config.get[String]("aws.s3.serviceEndpoint")
  private val regionName = config.get[String]("aws.s3.regionName")

  def downloadS3(objectKey: String): S3ObjectInputStream = {

    val s3Client = getClient(bucketName)

    val s3Object = s3Client.getObject(bucketName, objectKey)

    s3Object.getObjectContent
  }

  def uploadS3(tmpFile: File): PutObjectResult = {

    val s3Client = getClient(bucketName)
    val objectKey = tmpFile.getName

    s3Client.putObject(new PutObjectRequest(bucketName, objectKey, tmpFile))
  }

  private def getClient(bucketName: String) = {

    val credentials = new BasicAWSCredentials(accessKey, secretKey)

    val clientConfig = new ClientConfiguration()
    clientConfig.setProtocol(Protocol.HTTPS)
    clientConfig.setConnectionTimeout(10000)

    val endpointConfiguration =
      new EndpointConfiguration(serviceEndpoint, regionName)

    val s3Client = AmazonS3ClientBuilder
      .standard()
      .withCredentials(new AWSStaticCredentialsProvider(credentials))
      .withClientConfiguration(clientConfig)
      .withEndpointConfiguration(endpointConfiguration)
      .build()

    s3Client
  }
}
