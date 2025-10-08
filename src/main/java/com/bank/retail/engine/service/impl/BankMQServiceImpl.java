package com.bank.retail.engine.service.impl;

import com.bank.retail.engine.service.BankMQService;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Hashtable;

@Service
public class BankMQServiceImpl implements BankMQService {

    private static final Logger logger = LoggerFactory.getLogger(BankMQService.class);

    @Value("${app.mq.queueManager}")
    private String queueManager;

    @Value("${app.mq.host}")
    private String host;

    @Value("${app.mq.port}")
    private int port;

    @Value("${app.mq.channel}")
    private String channel;

    @Value("${app.mq.requestQueue}")
    private String requestQueueName;

    @Value("${app.mq.replyQueue}")
    private String replyQueueName;

    @Value("${app.mq.userId}")
    private String userId;

    @Value("${app.mq.password}")
    private String password;

    @Value("${app.mq.timeout}")
    private int timeout;

    /**
     * Sends XML to MQ and receives response.
     * First attempts to connect to MQ, falls back to mock response if MQ is unavailable.
     *
     * @param xmlInput the XML to send to MQ
     * @return XML response from MQ or mock response
     */
    public String sendToMQ(String xmlInput) {
        logger.info("Sending XML to bank MQ service");
        logger.debug("Input XML: {}", xmlInput);

        try {
            logger.info("Attempting to connect to MQ");
            String mqResponse = sendToMQInternal(xmlInput);
            logger.info("Successfully received response from MQ");
            logger.debug("MQ Response XML: {}", mqResponse);
            return mqResponse;

        } catch (Exception mqException) {
            logger.warn("MQ connection failed: {}", mqException.getMessage());
            logger.info("Falling back to mock response generation");

            try {
                String mockResponse = generateMockMQResponse(xmlInput);
                logger.info("Generated mock response as fallback");
                logger.debug("Mock Response XML: {}", mockResponse);
                return mockResponse;

            } catch (Exception mockException) {
                logger.error("Both MQ and mock response generation failed", mockException);
                throw new RuntimeException("MQ processing failed and mock fallback unavailable", mockException);
            }
        }
    }

    /**
     * Internal method to send XML message to MQ and receive response.
     *
     * @param xmlInput the XML message to send
     * @return XML response from MQ
     * @throws Exception if MQ connection or communication fails
     */
    private String sendToMQInternal(String xmlInput) throws Exception {
        MQQueueManager qMgr = null;
        MQQueue requestQueue = null;
        MQQueue replyQueue = null;

        try {
            logger.info("Attempting to connect to MQ: {} on {}:{}", queueManager, host, port);

            Hashtable<String, Object> props = new Hashtable<>();
            props.put("hostname", host);
            props.put("port", port);
            props.put("channel", channel);
            props.put("userID", userId);
            props.put("password", password);

            qMgr = new MQQueueManager(queueManager, props);
            logger.info("Successfully connected to MQ: {}", queueManager);

            int openOptions = CMQC.MQOO_OUTPUT;
            requestQueue = qMgr.accessQueue(requestQueueName, openOptions);
            replyQueue = qMgr.accessQueue(replyQueueName, CMQC.MQOO_INPUT_AS_Q_DEF | CMQC.MQOO_FAIL_IF_QUIESCING);

            logger.info("Opened queues - Request: {}, Reply: {}", requestQueueName, replyQueueName);

            MQMessage requestMessage = new MQMessage();
            requestMessage.format = CMQC.MQFMT_STRING;
            requestMessage.replyToQueueName = replyQueueName;
            requestMessage.writeString(xmlInput);

            MQPutMessageOptions pmo = new MQPutMessageOptions();
            requestQueue.put(requestMessage, pmo);

            byte[] requestMessageId = requestMessage.messageId;
            logger.info("Message sent successfully to queue: {} with messageId: {}", requestQueueName, requestMessageId);

            MQMessage replyMessage = new MQMessage();
            MQGetMessageOptions gmo = new MQGetMessageOptions();
            gmo.options = CMQC.MQGMO_WAIT | CMQC.MQGMO_FAIL_IF_QUIESCING;
            gmo.waitInterval = timeout;
            gmo.matchOptions = CMQC.MQMO_MATCH_CORREL_ID;
            replyMessage.correlationId = requestMessageId;

            logger.info("Waiting for reply message with correlationId: {}", requestMessageId);
            replyQueue.get(replyMessage, gmo);

            String responseText = replyMessage.readStringOfByteLength(replyMessage.getDataLength());
            logger.info("Received reply message with correlationId: {}, messageId: {}", 
                       replyMessage.correlationId, replyMessage.messageId);
            logger.debug("Reply message content: {}", responseText);

            return responseText;

        } catch (MQException e) {
            logger.error("MQ Exception occurred: {}", e.getMessage(), e);
            throw new Exception("MQ communication failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during MQ communication: {}", e.getMessage(), e);
            throw new Exception("MQ communication failed: " + e.getMessage(), e);
        } finally {
            try {
                if (requestQueue != null) {
                    requestQueue.close();
                }
                if (replyQueue != null) {
                    replyQueue.close();
                }
                if (qMgr != null) {
                    qMgr.disconnect();
                    logger.info("Disconnected from MQ");
                }
            } catch (MQException e) {
                logger.warn("Error during MQ cleanup: {}", e.getMessage());
            }
        }
    }

    /**
     * Generates a simple, common mock XML response.
     * This simulates what would come back from a real MQ service.
     */
    private String generateMockMQResponse(String xmlInput) {
        // try {
        //     Thread.sleep(5 + random.nextInt(5));
        // } catch (InterruptedException e) {
        //     Thread.currentThread().interrupt();
        //     logger.warn("Mock response generation interrupted", e);
        // }

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<NS1:eAI_MESSAGE xmlns:NS1=\"urn:esbbank.com/gbo/xml/schemas/v1_0/\">" +
               "<NS1:eAI_HEADER>" +
               "<NS1:serviceName>MOCK_SERVICE</NS1:serviceName>" +
               "<NS1:returnCode>000000</NS1:returnCode>" +
               "<NS1:returnMessage>Mock response generated</NS1:returnMessage>" +
               "</NS1:eAI_HEADER>" +
               "<NS1:eAI_BODY>" +
               "<NS1:eAI_REPLY>" +
               "<NS1:mockData>" +
               "<NS1:result>SUCCESS</NS1:result>" +
               "<NS1:timestamp>" + System.currentTimeMillis() + "</NS1:timestamp>" +
               "<NS1:processedBy>BankMQService</NS1:processedBy>" +
               "<NS1:fallbackReason>MQ connection unavailable</NS1:fallbackReason>" +
               "</NS1:mockData>" +
               "</NS1:eAI_REPLY>" +
               "</NS1:eAI_BODY>" +
               "</NS1:eAI_MESSAGE>";
    }
}