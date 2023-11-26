package com.proxyqiwi;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.http.HttpStatus;

import org.springframework.web.client.RestTemplate;
import org.apache.coyote.Response;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PathVariable;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;

import org.springframework.http.HttpMethod;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import java.io.*;

@RestController
public class ProxyController {

    @PutMapping("/proxy/{partner_name}")
    public ResponseEntity<String> PutHandler(@RequestBody String requestBody,
            @RequestParam("url") String url,
            @PathVariable("partner_name") String partnerName) {
        String method = "PUT";
        return processPutPayload(method, requestBody, url, partnerName);
    }

    @PostMapping("/proxy/{partner_name}")
    public ResponseEntity<String> PostHadler(@RequestBody String requestBody,
            @RequestParam("url") String url,
            @PathVariable("partner_name") String partnerName) {
        String method = "POST";
        return processPutPayload(method, requestBody, url, partnerName);
    }

    private ResponseEntity<String> processPutPayload(String method, @RequestBody String requestBody,
            @RequestParam("url") String url,
            @PathVariable("partner_name") String partnerName) {

        try {
            // Загрузка YAML-файла
            Yaml yaml = new Yaml();
            InputStream inputStream = new ClassPathResource("schema.yml").getInputStream();
            Map<String, Object> yamlMap = yaml.load(inputStream);
            Map<String, Object> visaMap = (Map<String, Object>) yamlMap.get(partnerName);
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
                String resultData = (String) cached.getOrDefault("result_data", null).toString();
                return ResponseEntity.status(HttpStatus.OK).body(resultData);
            } else {
                ResponseEntity<String> response = makeRequest(url, method, requestBody);
                String partner = partnerName;
                String req_method = method;
                String req_body = resultMapString;
                String res_body = response.getBody().toString();
                int res_status = response.getStatusCodeValue();

                System.out.println(partner);
                System.out.println(req_method);
                System.out.println(req_body);
                System.out.println(res_body);
                System.out.println(res_status);

                FileWriter writer = null;
                try {
                    // Чтение содержимого файла
                    writer = new FileWriter("./src/main/resources/cached.txt", true); // true означает, что запись будет
                                                                                      // добавлена в конец
                    String newLine = partner + "|" + req_method + "|" + req_body + "|" + res_body + "|" + res_status
                            + "\n";
                    writer.write(newLine);

                    System.out.println("Переменные успешно записаны в файл.");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                return ResponseEntity.status(res_status).body(res_body);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
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

    private Map<String, Object> findCached(@RequestParam String body, String method,
            @RequestParam String partner_name) {
        List<List<String>> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("./src/main/resources/cached.txt"))) {
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
    }

    private ResponseEntity<String> makeRequest(String req_url, String req_method, String req_body) {
        System.out.println("URL: " + req_url);

        RestTemplate restTemplate = new RestTemplate();

        // Сохраняем метод запроса в локальную переменную
        HttpMethod method;
        switch (req_method) {
            case "PUT":
                method = HttpMethod.PUT;
                break;
            case "POST":
                method = HttpMethod.POST;
                break;
            default:
                method = HttpMethod.GET;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(req_body, headers);
        ResponseEntity<String> response = restTemplate.exchange(req_url, method, requestEntity, String.class);

        return response;
    }
}