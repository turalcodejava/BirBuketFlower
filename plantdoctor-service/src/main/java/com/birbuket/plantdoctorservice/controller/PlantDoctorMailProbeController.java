package com.birbuket.plantdoctorservice.controller;

import com.birbuket.plantdoctorservice.mail.AgronomistReplyMailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/plantdoctor/internal")
@ConditionalOnProperty(prefix = "plantdoctor.mail", name = "probe-endpoint-enabled", havingValue = "true")
@RequiredArgsConstructor
public class PlantDoctorMailProbeController {

    private final AgronomistReplyMailSender agronomistReplyMailSender;

    /**
     * Lokal SMTP yoxlaması: {@code POST .../mail-probe?to=siz@gmail.com}<br>
     * Mailpitdə ünvan hər hansı ola bilər — UI {@code http://localhost:8025}
     */
    @PostMapping("/mail-probe")
    public ResponseEntity<Map<String, Object>> mailProbe(@RequestParam("to") String to) {
        String error = agronomistReplyMailSender.trySendProbeEmail(to);
        if (error == null) {
            return ResponseEntity.ok(Map.of("ok", true));
        }
        return ResponseEntity.badRequest().body(Map.of("ok", false, "error", error));
    }
}
