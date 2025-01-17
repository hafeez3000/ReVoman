package com.salesforce.revoman.integration.core.pq.adapters;

import static com.salesforce.revoman.input.json.JsonReaderUtils.anyMapR;
import static com.salesforce.revoman.input.json.JsonReaderUtils.listR;
import static com.salesforce.revoman.input.json.JsonReaderUtils.nextString;
import static com.salesforce.revoman.input.json.JsonReaderUtils.objR;
import static com.salesforce.revoman.input.json.JsonReaderUtils.readProps;
import static com.salesforce.revoman.input.json.JsonReaderUtils.skipValue;
import static com.salesforce.revoman.input.json.JsonWriterUtils.listW;
import static com.salesforce.revoman.input.json.JsonWriterUtils.objW;
import static com.salesforce.revoman.input.json.JsonWriterUtils.string;
import static com.salesforce.revoman.input.json.JsonWriterUtils.writeProps;

import com.salesforce.revoman.integration.core.pq.connect.ConnectInputRepresentationWithGraph;
import com.salesforce.revoman.integration.core.pq.connect.ObjectGraphInputRepresentation;
import com.salesforce.revoman.integration.core.pq.connect.ObjectInputRepresentationMap;
import com.salesforce.revoman.integration.core.pq.connect.ObjectWithReferenceInputRepresentation;
import com.salesforce.revoman.integration.core.pq.connect.ObjectWithReferenceInputRepresentationList;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import io.vavr.control.Try;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeanUtils;

public class ConnectInputRepWithGraphAdapter<T extends ConnectInputRepresentationWithGraph>
    extends JsonAdapter<T> {
  private final Class<T> type;

  private final JsonAdapter<Object> dynamicJsonAdapter;

  private ConnectInputRepWithGraphAdapter(Class<T> type, JsonAdapter<Object> dynamicJsonAdapter) {
    this.type = type;
    this.dynamicJsonAdapter = dynamicJsonAdapter;
  }

  public static <T extends ConnectInputRepresentationWithGraph> Factory adapter(
      final Class<T> type) {
    return new Factory() {
      @Override
      public @Nullable JsonAdapter<?> create(
          @NotNull Type requestedType,
          @NotNull Set<? extends Annotation> annotations,
          @NotNull Moshi moshi) {
        if (type != requestedType) {
          return null;
        }
        return new ConnectInputRepWithGraphAdapter<>(type, moshi.adapter(Object.class));
      }
    };
  }

  @Override
  public T fromJson(@NotNull JsonReader reader) {
    return objR(
        () -> BeanUtils.instantiateClass(type),
        reader,
        (pqir, key1) -> {
          if (key1.equals("graph")) {
            pqir.setGraph(
                objR(
                    ObjectGraphInputRepresentation::new,
                    reader,
                    (ogi, key2) -> {
                      switch (key2) {
                        case "graphId":
                          ogi.setGraphId(nextString(reader));
                          break;
                        case "records":
                          final var oripl = new ObjectWithReferenceInputRepresentationList();
                          ogi.setRecords(oripl);
                          oripl.setRecordsList(
                              listR(
                                  ObjectWithReferenceInputRepresentation::new,
                                  reader,
                                  (orir, key3) -> {
                                    switch (key3) {
                                      case "referenceId":
                                        orir.setReferenceId(nextString(reader));
                                        break;
                                      case "record":
                                        final var oirm = new ObjectInputRepresentationMap();
                                        orir.setRecord(oirm);
                                        oirm.setRecordBody(anyMapR(reader));
                                        break;
                                      default:
                                        skipValue(reader);
                                    }
                                  }));
                          break;
                        default:
                          skipValue(reader);
                      }
                    }));
          } else {
            readProps(reader, type, pqir, key1);
          }
        });
  }

  @Override
  public void toJson(@NotNull JsonWriter writer, T connectInputRepWithGraph) {
    objW(
        connectInputRepWithGraph,
        writer,
        cirwg -> {
          writeProps(
              writer,
              type,
              cirwg,
              Set.of(ObjectGraphInputRepresentation.class),
              dynamicJsonAdapter);
          objW(
              "graph",
              cirwg.getGraph(),
              writer,
              graph -> {
                string("graphId", graph.getGraphId(), writer);
                listW(
                    "records",
                    graph.getRecords().getRecordsList(),
                    writer,
                    recd ->
                        objW(
                            recd,
                            writer,
                            rec -> {
                              string("referenceId", rec.getReferenceId(), writer);
                              Try.run(
                                  () -> {
                                    writer.name("record");
                                    dynamicJsonAdapter.toJson(
                                        writer, rec.getRecord().getRecordBody());
                                  });
                            }));
              });
        });
  }
}
