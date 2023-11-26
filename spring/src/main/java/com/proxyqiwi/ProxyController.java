package com.proxyqiwi.qiwiproxyemulator;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import java.util.List;
import java.util.Map;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.jdbc.core.JdbcTemplate;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProxyController {

    // // Подключение БД
    // private final JdbcTemplate jdbcTemplate;

    // @Autowired
    // public ProxyController(JdbcTemplate jdbcTemplate) {
    // this.jdbcTemplate = jdbcTemplate;
    // }

    @PostMapping("/proxy/{partner_name}")
    public ResponseEntity<Map<String, Object>> processPostPayload(@RequestBody String requestBody,
            @PathVariable("partner_name") String partnerName) {
        String method = "POST";

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
            Map<String, Object> cached = findCached(resultMapString, method, partnerName);
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

    @PutMapping("/proxy/{partner_name}")
    public ResponseEntity<Map<String, Object>> processPutPayload(@RequestBody String requestBody,
            @PathVariable("partner_name") String partnerName) {
        String method = "PUT";

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
            Map<String, Object> cached = findCached(resultMapString, method, partnerName);
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

    private Map<String, Object> findCached(@RequestParam String body, String method,
            @RequestParam String partner_name) {
        List<List<String>> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("cached.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] substrings = line.split("\\|");
                List<String> subList = new ArrayList<>();
                for (String substring : substrings) {
                    subList.add(substring.trim());
                }
                result.add(subList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (List<String> sublist : result) {
            if (sublist.size() >= 5) {
                String cachedBody = sublist.get(3);
                String statusCode = sublist.get(4);
                String cachedMethod = sublist.get(1);
                String cachedPartner = sublist.get(0);

                if (cachedBody.equals(body) && cachedMethod.equals(method) && cachedPartner.equals(partner_name)) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("body", cachedBody);
                    data.put("status_code", statusCode);

                    Map<String, Object> response = new HashMap<>();
                    response.put("is_cached", true);
                    response.put("data", data);

                    return response;
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("is_cached", false);
        response.put("data", null);

        return response;

        // String sql = "SELECT EXISTS(SELECT 1 FROM cached_reqs WHERE body = ? AND
        // partner_name = ?)";
        // boolean exists = (boolean) jdbcTemplate.queryForObject(sql, Boolean.class,
        // body, partner_name);

        // if (!exists) {
        // return Map.of("is_cached", false, "result_data", null);
        // } else {
        // String selectSql = "SELECT * FROM cached_res WHERE cached_reqs_id = (SELECT
        // id FROM cached_reqs WHERE body = ? AND partner_name = ?)";
        // Map<String, Object> resultData = jdbcTemplate.queryForMap(selectSql, body,
        // partner_name);

        // if (resultData != null) {
        // return Map.of("is_cached", true, "result_data", resultData);
        // } else {
        // String deleteSql = "DELETE FROM cached_reqs WHERE body = ? AND partner_name =
        // ?";
        // jdbcTemplate.update(deleteSql, body, partner_name);
        // return Map.of("is_cached", false, "result_data", null);
        // }
        // }
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

    // private final RestTemplate restTemplate;

    // public ProxyController(RestTemplate restTemplate) {
    // this.restTemplate = restTemplate;
    // }

    // @RequestMapping("/proxy")
    // public ResponseEntity<String> proxyRequest(@RequestParam("url") String url) {
    // RestTemplate restTemplate = new RestTemplate();
    // ResponseEntity<String> response = restTemplate.getForEntity(url,
    // String.class);
    // return response;
    // }

    @RequestMapping("/proxy")
    public ResponseEntity<String> proxyRequest(@RequestParam("url") String url, @RequestBody String body) {
        System.out.println("URL: " + url);

        RestTemplate restTemplate = new RestTemplate();
        HttpMethod method = HttpMethod.GET; // Сохраняем метод запроса в локальную переменную

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

        // Выполняем запрос на сервер и получаем ответ
        ResponseEntity<String> response = restTemplate.exchange(url, method, requestEntity, String.class);

        // Сохраняем тело ответа и статус код в локальные переменные
        String responseBody = response.getBody();
        int statusCode = response.getStatusCodeValue();

        // Выводим все четыре переменные в командную строку
        System.out.println("Метод запроса: " + method);
        System.out.println("Тело запроса: " + body);
        System.out.println("Тело ответа: " + responseBody);
        System.out.println("Статус код: " + statusCode);

        // Возвращаем полученный ответ клиенту
        return response;
    }
}