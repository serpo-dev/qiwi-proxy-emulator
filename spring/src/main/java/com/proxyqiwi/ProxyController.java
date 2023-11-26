package com.proxyqiwi.qiwiproxyemulator;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.client.RestTemplate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PathVariable;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProxyController {

    // Подключение БД
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ProxyController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @RequestMapping("/proxy/{partner_name}")
    public ResponseEntity<Map<String, Object>> processPayload(@RequestBody String requestBody,
            @PathVariable("partner_name") String partnerName) {
        try {
            // Загрузка YAML-файла
            Yaml yaml = new Yaml();
            InputStream inputStream = new ClassPathResource("schema.yml").getInputStream();
            Map<String, Object> yamlMap = yaml.load(inputStream);
            Map<String, Object> visaMap = (Map<String, Object>) yamlMap.get("visa");
            Map<String, Object> schema = (Map<String, Object>) visaMap.get("payload");

            // Преобразование JSON-запроса в объект
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonMap;
            try {
                jsonMap = objectMapper.readValue(requestBody, Map.class);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Создание объекта результата
            Map<String, Object> resultMap = new HashMap<>();
            processFields(schema, jsonMap, resultMap, "");

            String resultMapString = resultMap.toString();
            Map<String, Object> cached = findCacвhed(resultMapString, partnerName);
            boolean isCached = (boolean) cached.getOrDefault("is_cached", false);

            if (isCached) {
                Map<String, Object> resultData = (Map<String, Object>) cached.getOrDefault("result_data", null);
                return ResponseEntity.status(HttpStatus.OK).body(resultData);
            } else {
                return ResponseEntity.status(201).body(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private void processFields(Map<String, Object> schema, Map<String, Object> json, Map<String, Object> result,
            String path) {
        for (Map.Entry<String, Object> entry : schema.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Boolean && (Boolean) value) {
                if (json.containsKey(key)) {
                    result.put(path + key, json.get(key));
                }
            } else if (value instanceof Map) {
                processFields((Map<String, Object>) value, (Map<String, Object>) json.get(key), result,
                        path + key + ".");
            }
        }
    }

    // public boolean checkCached(@RequestParam String body, @RequestParam String
    // partner_name) {
    // String sql = "SELECT EXISTS(SELECT 1 FROM cached_reqs WHERE body = ? AND
    // partner_name = ?)";

    // boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, body,
    // partner_name);

    // if (!exists) {
    // String insertSql = "INSERT INTO cached_reqs (body, partner_name) VALUES (?,
    // ?)";
    // jdbcTemplate.update(insertSql, body, partner_name);
    // }

    // return exists;
    // }

    private Map<String, Object> findCacвhed(@RequestParam String body, @RequestParam String partner_name) {
        String sql = "SELECT EXISTS(SELECT 1 FROM cached_reqs WHERE body = ? AND partner_name = ?)";
        boolean exists = (boolean) jdbcTemplate.queryForObject(sql, Boolean.class, body, partner_name);

        if (!exists) {
            return Map.of("is_cached", false, "result_data", null);
        } else {
            String selectSql = "SELECT * FROM cached_res WHERE cached_reqs_id = (SELECT id FROM cached_reqs WHERE body = ? AND partner_name = ?)";
            Map<String, Object> resultData = jdbcTemplate.queryForMap(selectSql, body, partner_name);

            if (resultData != null) {
                return Map.of("is_cached", true, "result_data", resultData);
            } else {
                String deleteSql = "DELETE FROM cached_reqs WHERE body = ? AND partner_name = ?";
                jdbcTemplate.update(deleteSql, body, partner_name);
                return Map.of("is_cached", false, "result_data", null);
            }
        }
    }

    // @GetMapping(value = "/{partner_name}")
    // public Map<String, String> getFormat(@PathVariable("partner_name") String
    // partnerName) throws IOException {
    // Yaml yaml = new Yaml();
    // try (InputStream inputStream = new
    // ClassPathResource("schema.yml").getInputStream()) {
    // Map<String, Map<String, Object>> schema = yaml.load(inputStream);
    // if (schema.containsKey(partnerName)) {
    // String format = (String) schema.get(partnerName).get("format");
    // return Map.of("type", format);
    // }
    // }
    // return null;
    // }

    // public ResponseEntity<String> proxyRequest( @RequestParam("url") String url)
    // {
    // RestTemplate restTemplate = new RestTemplate();
    // ResponseEntity<String> response = restTemplate.getForEntity(url,
    // String.class);
    // return response;
    // }
}