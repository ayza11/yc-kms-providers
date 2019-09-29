package yandex.cloud.kms.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.*;

public class DefaultObjectMapper extends ObjectMapper {
    public DefaultObjectMapper() {
        super();
        setTimeZone(TimeZone.getDefault());
        registerModule(new Jdk8Module());
        registerModule(new JavaTimeModule());
        registerModule(new SimpleModule()
                .addAbstractTypeMapping(Set.class, LinkedHashSet.class)
                .addAbstractTypeMapping(Map.class, LinkedHashMap.class)
                .addAbstractTypeMapping(List.class, ArrayList.class)
        );
        setSerializationInclusion(JsonInclude.Include.NON_NULL);
        configure(SerializationFeature.INDENT_OUTPUT, false);
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
        configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
    }
}
