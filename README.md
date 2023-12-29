To learn about Streaming systems and Redis PubSub implementation, check repo: https://github.com/nickskewl/RedisPubSubDemo

**Queue Demo (AWS SQS):**  
To implement the queue using AWS SQS and perform operations on it, we need to do below 5 steps:
1. Create a free tier account on AWS - 'https://console.aws.amazon.com'.
2. Install AWS cli and configure credentials
   - Install AWS cli: https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html
   - Configure credentials on local machine to access aws:
     1. Go to Account -> Security Credentials -> Access mgmt -> users -> Create user -> provide a username -> next -> select Attach policies directly -> search and select policy 'AmazonSQSFullAccess' -> Next -> create user.
     2. Open created user -> Security credentials -> Create Access Key -> select CLI -> next -> create.
     3. You will get 'Access Key' and 'Secret access key', copy these keys.
     4. Go to Terminal -> type 'aws configure' and provide above keys to generate and save the credentials in your machine (Ex: For mac, it will be saved in ~./aws folder).
3. Create a queue
    ```
   private final AmazonSQS sqsClient = AmazonSQSClientBuilder.defaultClient();

    public void createQueue(String name) {
        try {
            sqsClient.createQueue(name);
        } catch (AmazonSQSException e) {
            if (!e.getErrorCode().equals("QueueAlreadyExists")) {
                throw e;
            }
        }
    }
   ```
4. Publish a message in queue
    ```
   public void publishMessage(String queueName, MessageBody messageBody) {
        String queueUrl = sqsClient.getQueueUrl(queueName).getQueueUrl();
        SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(messageBody.toString())
                .withDelaySeconds(5);

        sqsClient.sendMessage(sendMessageRequest);
    }
   ```
5. Read the message from queue
    ```
    public List<Message> getMessage(String queueName) {
        String queueUrl = sqsClient.getQueueUrl(queueName).getQueueUrl();
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                .withQueueUrl(queueUrl)
                .withMaxNumberOfMessages(1);

        List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).getMessages();
        for (Message m : messages) {
            sqsClient.deleteMessage(queueUrl, m.getReceiptHandle());
        }
        return messages;
    }
   ```

**To write above code pieces all together**, create a spring boot project using https://start.spring.io/ with below configuration:

- Maven Project
- Jar packaging
- Java 17
- Dependencies:
  1. spring-boot-starter-web
  2. lombok
  3. devtools
  4. aws-java-sdk-sqs
        ```
     <dependency>
            <groupId>com.amazonaws</groupId>
            <artifactId>aws-java-sdk-sqs</artifactId>
            <version>1.12.627</version>
        </dependency>
     ```
     
Import this project in intellij and create below 3 files:
1. MessageBody class:
    - Message object that we want to send to queue.
   ```
   @Data
   public class MessageBody {
    String name;
    int age;
    }
   ```
2. Create 'AwsSqsService' class which defines method to perform operation on SQS.
    ```
   @Service
   public class AwsSqsService {

    private final AmazonSQS sqsClient = AmazonSQSClientBuilder.defaultClient();

    public void createQueue(String name) {
        try {
            sqsClient.createQueue(name);
        } catch (AmazonSQSException e) {
            if (!e.getErrorCode().equals("QueueAlreadyExists")) {
                throw e;
            }
        }
    }

    public List<String> getQueues() {
        return sqsClient.listQueues().getQueueUrls();
    }

    public void publishMessage(String queueName, MessageBody messageBody) {
        String queueUrl = sqsClient.getQueueUrl(queueName).getQueueUrl();
        SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(messageBody.toString())
                .withDelaySeconds(5);

        sqsClient.sendMessage(sendMessageRequest);
    }

    public List<Message> getMessage(String queueName) {
        String queueUrl = sqsClient.getQueueUrl(queueName).getQueueUrl();
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                .withQueueUrl(queueUrl)
                .withMaxNumberOfMessages(1);

        List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).getMessages();
        for (Message m : messages) {
            sqsClient.deleteMessage(queueUrl, m.getReceiptHandle());
        }
        return messages;
    }
    }
   ```
3. Create Controller to expose the APIs to trigger the operation on SQS:
    ```
   @RestController
    @RequestMapping("/queue")
    public class ApiAwsController {

    @Autowired
    AwsSqsService awsSqsService;

    @PostMapping("/create/{queueName}")
    public void createQueue(@PathVariable String queueName) {
        awsSqsService.createQueue(queueName);
    }

    @GetMapping
    public List<String> getQueues() {
        return awsSqsService.getQueues();
    }

    @PostMapping("/{queueName}/message")
    public ResponseEntity<String> sendMessage(@PathVariable String queueName, @RequestBody MessageBody messageBody) {
        awsSqsService.publishMessage(queueName, messageBody);
        return ResponseEntity.created(null).build();
    }

    @GetMapping("{queueName}/message")
    public List<Message> getMessage(@PathVariable String queueName) {
        return awsSqsService.getMessage(queueName);
    }
    }
   ```
    
4. Run the Spring boot application and make the API calls to create a queue, to publish the message and consume it.

**References:**  
1. https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-basic-architecture.html
2. https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/creating-sqs-standard-queues.html
3. https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-sqs-message-queues.html  
4. https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-sqs-messages.html
