package com.nitesh.sqs.demo.app;

import com.amazonaws.services.sqs.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author nitesh
 */
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
