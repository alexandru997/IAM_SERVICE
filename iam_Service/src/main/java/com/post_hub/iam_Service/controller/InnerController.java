package com.post_hub.iam_Service.controller;

import com.post_hub.iam_Service.config.SwaggerInnerKey;
import com.post_hub.iam_Service.model.constants.ApiLogoMessage;
import com.post_hub.iam_Service.utils.APIUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/iamServiceInner")
@RequiredArgsConstructor
public class InnerController {
    @SwaggerInnerKey
    @GetMapping("/healthCheck")
    @Operation(summary = "HealthCheck for service")
    public ResponseEntity<Void> healthCheck() {
        log.trace(ApiLogoMessage.NAME_OF_CURRENT_METHOD.getValue(), APIUtils.getMethodName());

        return ResponseEntity.ok().build();
    }
}
