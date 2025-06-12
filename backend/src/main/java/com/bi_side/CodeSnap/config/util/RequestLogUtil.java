package com.bi_side.CodeSnap.config.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestLogUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String buildRequestJson(Object[] args) {
        List<Object> serializableArgs = new ArrayList<>();
        List<Map<String, Object>> uploadedFiles = new ArrayList<>();

        for (Object arg : args) {
            if (arg instanceof org.springframework.web.multipart.MultipartFile) {
                uploadedFiles.add(fileInfo((org.springframework.web.multipart.MultipartFile) arg));
            } else if (arg instanceof org.springframework.web.multipart.MultipartFile[]) {
                for (org.springframework.web.multipart.MultipartFile file : (org.springframework.web.multipart.MultipartFile[]) arg) {
                    uploadedFiles.add(fileInfo(file));
                }
            } else if (arg instanceof List && !((List<?>) arg).isEmpty()
                    && ((List<?>) arg).get(0) instanceof org.springframework.web.multipart.MultipartFile) {
                for (org.springframework.web.multipart.MultipartFile file : (List<org.springframework.web.multipart.MultipartFile>) arg) {
                    uploadedFiles.add(fileInfo(file));
                }
            } else if (isRequestBodyCandidate(arg)) {
                serializableArgs.add(arg);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("params", serializableArgs);
        result.put("files", uploadedFiles);

        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"error\": \"직렬화 실패\"}";
        }
    }

    private static Map<String, Object> fileInfo(org.springframework.web.multipart.MultipartFile file) {
        Map<String, Object> info = new HashMap<>();
        info.put("filename", file.getOriginalFilename());
        info.put("size", file.getSize());
        info.put("contentType", file.getContentType());
        return info;
    }

    private static boolean isRequestBodyCandidate(Object arg) {
        return !(arg instanceof String)
                && !(arg instanceof Number)
                && !(arg instanceof Boolean)
                && !(arg instanceof jakarta.servlet.http.HttpServletRequest)
                && !(arg instanceof jakarta.servlet.http.HttpServletResponse)
                && !(arg instanceof org.springframework.web.multipart.MultipartFile)
                && !(arg instanceof org.springframework.web.multipart.MultipartFile[])
                ;
    }
}
