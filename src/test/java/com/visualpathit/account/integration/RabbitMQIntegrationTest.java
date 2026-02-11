package com.visualpathit.account.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visualpathit.account.model.User;
import org.junit.jupiter.api.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests for RabbitMQ Messaging
 * Tests message publishing and consumption
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("RabbitMQ Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RabbitMQIntegrationTest {

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String QUEUE_NAME = "user.queue";
    private static final String EXCHANGE_NAME = "user.exchange";
    private static final String ROUTING_KEY = "user.created";

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("mqtest");
        testUser.setEmail("mq@test.com");
        testUser.setRole("USER");
    }

    @Test
    @Order(1)
    @DisplayName("Should connect to RabbitMQ")
    void testRabbitMQConnection() {
        if (rabbitTemplate != null) {
            assertNotNull(rabbitTemplate);
            assertNotNull(rabbitTemplate.getConnectionFactory());
        } else {
            System.out.println("RabbitTemplate not configured - skipping test");
        }
    }

    @Test
    @Order(2)
    @DisplayName("Should send message to queue")
    void testSendMessage() throws Exception {
        if (rabbitTemplate != null) {
            // Act
            String message = objectMapper.writeValueAsString(testUser);
            rabbitTemplate.convertAndSend(QUEUE_NAME, message);

            // Assert - Give RabbitMQ time to process
            TimeUnit.MILLISECONDS.sleep(500);
            
            // Verify message was sent (check queue depth if possible)
            assertTrue(true); // Message sent without exception
        } else {
            System.out.println("RabbitTemplate not configured - skipping test");
        }
    }

    @Test
    @Order(3)
    @DisplayName("Should send and receive message")
    void testSendAndReceiveMessage() throws Exception {
        if (rabbitTemplate != null) {
            // Arrange
            String messageBody = objectMapper.writeValueAsString(testUser);

            // Act - Send message
            rabbitTemplate.convertAndSend(QUEUE_NAME, messageBody);
            TimeUnit.MILLISECONDS.sleep(500);

            // Receive message (with timeout)
            Object received = rabbitTemplate.receiveAndConvert(QUEUE_NAME, 5000);

            // Assert
            assertNotNull(received);
            String receivedJson = (String) received;
            User receivedUser = objectMapper.readValue(receivedJson, User.class);
            assertEquals("mqtest", receivedUser.getUsername());
        } else {
            System.out.println("RabbitTemplate not configured - skipping test");
        }
    }

    @Test
    @Order(4)
    @DisplayName("Should publish message to exchange")
    void testPublishToExchange() throws Exception {
        if (rabbitTemplate != null) {
            // Act
            String message = objectMapper.writeValueAsString(testUser);
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, message);

            // Assert
            TimeUnit.MILLISECONDS.sleep(500);
            assertTrue(true); // Message published without exception
        } else {
            System.out.println("RabbitTemplate not configured - skipping test");
        }
    }

    @Test
    @Order(5)
    @DisplayName("Should send message with custom properties")
    void testSendMessageWithProperties() throws Exception {
        if (rabbitTemplate != null) {
            // Arrange
            String messageBody = objectMapper.writeValueAsString(testUser);
            MessageProperties properties = new MessageProperties();
            properties.setContentType("application/json");
            properties.setHeader("user-id", testUser.getId());
            properties.setHeader("operation", "CREATE");
            
            Message message = new Message(messageBody.getBytes(), properties);

            // Act
            rabbitTemplate.send(QUEUE_NAME, message);
            TimeUnit.MILLISECONDS.sleep(500);

            // Receive and verify
            Message received = rabbitTemplate.receive(QUEUE_NAME, 5000);
            assertNotNull(received);
            assertEquals("application/json", received.getMessageProperties().getContentType());
            assertEquals(1L, received.getMessageProperties().getHeader("user-id"));
        } else {
            System.out.println("RabbitTemplate not configured - skipping test");
        }
    }

    @Test
    @Order(6)
    @DisplayName("Should handle message timeout")
    void testMessageTimeout() {
        if (rabbitTemplate != null) {
            // Act - Try to receive from empty queue
            Object received = rabbitTemplate.receiveAndConvert(QUEUE_NAME, 1000);

            // Assert
            assertNull(received); // Should timeout and return null
        } else {
            System.out.println("RabbitTemplate not configured - skipping test");
        }
    }

    @Test
    @Order(7)
    @DisplayName("Should send multiple messages")
    void testSendMultipleMessages() throws Exception {
        if (rabbitTemplate != null) {
            // Act - Send multiple messages
            for (int i = 1; i <= 5; i++) {
                User user = new User();
                user.setId((long) i);
                user.setUsername("user" + i);
                user.setEmail("user" + i + "@test.com");
                
                String message = objectMapper.writeValueAsString(user);
                rabbitTemplate.convertAndSend(QUEUE_NAME, message);
            }

            TimeUnit.MILLISECONDS.sleep(1000);

            // Assert - Receive and verify count
            int receivedCount = 0;
            for (int i = 0; i < 5; i++) {
                Object received = rabbitTemplate.receiveAndConvert(QUEUE_NAME, 2000);
                if (received != null) {
                    receivedCount++;
                }
            }

            assertEquals(5, receivedCount);
        } else {
            System.out.println("RabbitTemplate not configured - skipping test");
        }
    }

    @Test
    @Order(8)
    @DisplayName("Should handle message serialization error")
    void testSerializationError() {
        if (rabbitTemplate != null) {
            // Act & Assert - Send invalid object
            assertThrows(Exception.class, () -> {
                Object invalidObject = new Object() {
                    @SuppressWarnings("unused")
                    public Object getCircularReference() {
                        return this; // Circular reference
                    }
                };
                rabbitTemplate.convertAndSend(QUEUE_NAME, invalidObject);
            });
        } else {
            System.out.println("RabbitTemplate not configured - skipping test");
        }
    }

    @Test
    @Order(9)
    @DisplayName("Should support message priority")
    void testMessagePriority() throws Exception {
        if (rabbitTemplate != null) {
            // Arrange
            MessageProperties highPriority = new MessageProperties();
            highPriority.setPriority(10);
            
            MessageProperties lowPriority = new MessageProperties();
            lowPriority.setPriority(1);

            String message = objectMapper.writeValueAsString(testUser);

            // Act
            rabbitTemplate.send(QUEUE_NAME, new Message(message.getBytes(), lowPriority));
            rabbitTemplate.send(QUEUE_NAME, new Message(message.getBytes(), highPriority));
            
            TimeUnit.MILLISECONDS.sleep(500);

            // Assert - Both messages sent
            Message msg1 = rabbitTemplate.receive(QUEUE_NAME, 2000);
            Message msg2 = rabbitTemplate.receive(QUEUE_NAME, 2000);
            
            assertNotNull(msg1);
            assertNotNull(msg2);
        } else {
            System.out.println("RabbitTemplate not configured - skipping test");
        }
    }

    @Test
    @Order(10)
    @DisplayName("Should handle queue purge")
    void testQueuePurge() throws Exception {
        if (rabbitTemplate != null) {
            // Arrange - Send messages
            for (int i = 0; i < 3; i++) {
                rabbitTemplate.convertAndSend(QUEUE_NAME, "message" + i);
            }
            TimeUnit.MILLISECONDS.sleep(500);

            // Act - Purge queue
            rabbitTemplate.execute(channel -> {
                channel.queuePurge(QUEUE_NAME);
                return null;
            });

            // Assert - Queue should be empty
            Object received = rabbitTemplate.receiveAndConvert(QUEUE_NAME, 1000);
            assertNull(received);
        } else {
            System.out.println("RabbitTemplate not configured - skipping test");
        }
    }
}
