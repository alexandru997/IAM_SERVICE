package com.post_hub.iam_Service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.post_hub.iam_Service.kafka.model.utils.PostHubService;
import com.post_hub.iam_Service.kafka.model.utils.UtilMessage;
import com.post_hub.iam_Service.model.constants.ApiErrorMessage;
import com.post_hub.iam_Service.model.constants.ApiLogoMessage;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Component
@Validated
@RequiredArgsConstructor
public class MessageProducer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value(value = "${additional.kafka.topic.iam.service.logs}")
    private String logsOutTopic;

    @Value("${kafka.enabled:false}")
    private boolean isKafkaEnabled;

    public void sendLogs(@NotNull @Valid UtilMessage message) {
        if (!isKafkaEnabled) {
            log.trace(ApiLogoMessage.KAFKA_DISABLED.getValue(), message);
            return;
        }
        try {
            message.setService(PostHubService.IAM_SERVICE);
            String messageJson = objectMapper.writeValueAsString(message);
            log.debug(ApiLogoMessage.KAFKA_SENDING.getValue(), messageJson);

            kafkaTemplate.send(logsOutTopic, messageJson).get();
            log.debug(ApiLogoMessage.KAFKA_SENT.getValue(), message.getActionType(), logsOutTopic, messageJson);
        } catch (Exception cause) {
            log.error(ApiErrorMessage.KAFKA_SEND_FAILED.getMessage(), cause);
        }
    }
}
