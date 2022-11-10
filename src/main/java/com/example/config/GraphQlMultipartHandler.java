package com.example.config;

import com.example.config.mapper.MultipartVariableMapper;
import com.example.request.MultipartGraphQlRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.graphql.server.WebGraphQlHandler;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.IdGenerator;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.AbstractMultipartHttpServletRequest;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * WebFlux.fn Handler for GraphQL over HTTP requests.
 */
@Slf4j
@Component
public class GraphQlMultipartHandler {

    /**
     * for generating universally unique identifiers ({@link UUID UUIDs}).
     */
    private final IdGenerator idGenerator = new AlternativeJdkIdGenerator();

    private final WebGraphQlHandler graphQlHandler;

    private final ObjectMapper objectMapper;

    public static final List<MediaType> SUPPORTED_RESPONSE_MEDIA_TYPES =
            Arrays.asList(MediaType.APPLICATION_GRAPHQL, MediaType.APPLICATION_JSON);

    public GraphQlMultipartHandler(@NonNull WebGraphQlHandler graphQlHandler, @NonNull ObjectMapper objectMapper) {
        this.graphQlHandler = graphQlHandler;
        this.objectMapper = objectMapper;
    }

    /**
     * The operation type is either query, mutation, or subscription and describes what type of operation you're intending to do.
     *
     * @param serverRequest ServerRequest
     * @return ServerResponse
     */
    public ServerResponse handleRequest(ServerRequest serverRequest) {
        var operation = serverRequest.param("operations");
        var inputQuery = readJson(operation, new TypeReference<Map<String, Object>>() {});

        final Map<String, Object> queryVariables = getFromMapOrEmpty(inputQuery, "variables");

        Optional<String> mapParam = serverRequest.param("map");
        Map<String, MultipartFile> fileParams = readMultipartBody(serverRequest);
        Map<String, List<String>> fileMapInput = readJson(mapParam, new TypeReference<>() {});

        fileMapInput.forEach((String fileKey, List<String> objectPaths) -> {
            MultipartFile file = fileParams.get(fileKey);
            if (file != null) {
                objectPaths.forEach(objectPath -> MultipartVariableMapper.mapVariable(
                        objectPath,
                        queryVariables,
                        file
                ));
            }
        });

        return ServerResponse.async(processRequest(serverRequest, inputQuery, queryVariables));
    }

    private Mono<ServerResponse> processRequest(final ServerRequest serverRequest,
                                                final Map<String, Object> inputQuery,
                                                final Map<String, Object> queryVariables) {

        final Map<String, Object> extensions = getFromMapOrEmpty(inputQuery, "extensions");
        var query = (String) inputQuery.get("query");
        var operationName = (String) inputQuery.get("operationName");
        var graphQlRequest = new MultipartGraphQlRequest(
                query,
                operationName,
                queryVariables,
                extensions,
                serverRequest.uri(), serverRequest.headers().asHttpHeaders(),
                this.idGenerator.generateId().toString(), LocaleContextHolder.getLocale());

        log.debug("Executing: " + graphQlRequest);

        return this.graphQlHandler.handleRequest(graphQlRequest)
                .map(response -> {
                    log.debug("Execution complete");

                    var builder = ServerResponse.ok();
                    builder.headers(headers -> headers.putAll(response.getResponseHeaders()));
                    builder.contentType(selectResponseMediaType(serverRequest));

                    return builder.body(response.toMap());
                });
    }

    @SuppressWarnings("unchecked")
    private <T> T readJson(final Optional<String> string, final TypeReference<T> t) {
        if (string.isPresent()) {
            try {
                return objectMapper.readValue(string.get(), t);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return (T) new HashMap<String, Object>();
    }

    private static Map<String, MultipartFile> readMultipartBody(final ServerRequest request) {
        try {
            var abstractMultipartHttpServletRequest = (AbstractMultipartHttpServletRequest) request.servletRequest();
            return abstractMultipartHttpServletRequest.getFileMap();
        } catch (RuntimeException ex) {
            throw new ServerWebInputException("Error while reading request parts", null, ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getFromMapOrEmpty(Map<String, Object> input, String key) {
        if (input.containsKey(key)) {
            return (Map<String, Object>)input.get(key);
        } else {
            return new HashMap<>();
        }
    }

    private static MediaType selectResponseMediaType(final ServerRequest serverRequest) {
        for (var accepted : serverRequest.headers().accept()) {
            if (SUPPORTED_RESPONSE_MEDIA_TYPES.contains(accepted)) {
                return accepted;
            }
        }
        return MediaType.APPLICATION_JSON;
    }
}
