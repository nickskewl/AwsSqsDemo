package com.nitesh.sqs.demo.app;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author nitesh
 */
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
