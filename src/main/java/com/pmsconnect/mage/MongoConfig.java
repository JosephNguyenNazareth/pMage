package com.pmsconnect.mage;

import org.bson.Document;
import org.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.core.convert.converter.Converter;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new JSONObjectWriteConverter());
        converters.add(new JSONObjectReadConverter());
        return new MongoCustomConversions(converters);
    }

    static class JSONObjectWriteConverter implements Converter<JSONObject, Document> {
        @Override
        public Document convert(JSONObject source) {
            return new Document(Document.parse(source.toString()));
        }
    }

    static class JSONObjectReadConverter implements Converter<Document, JSONObject> {
        @Override
        public JSONObject convert(Document source) {
            return new JSONObject(source);
        }
    }
}
