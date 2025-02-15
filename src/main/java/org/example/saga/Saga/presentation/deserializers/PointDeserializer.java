package org.example.saga.Saga.presentation.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.io.IOException;


public class PointDeserializer extends JsonDeserializer<Point> {
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Override
    public Point deserialize(JsonParser jsonParser, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        // Ожидаем формат GeoJSON: {"type": "Point", "coordinates": [x, y]}
        if (node.has("type") && "Point".equalsIgnoreCase(node.get("type").asText())
                && node.has("coordinates") && node.get("coordinates").isArray()) {
            JsonNode coordinates = node.get("coordinates");
            double x = coordinates.get(0).asDouble();
            double y = coordinates.get(1).asDouble();
            return geometryFactory.createPoint(new Coordinate(x, y));
        }

        // Если формат не соответствует, можно выбросить исключение или вернуть null
        throw new IOException("Неверный формат точки: " + node.toString());
    }
}