package org.xbib.elasticsearch.index.query;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;

import java.io.IOException;

public class PayloadTermQueryBuilder extends QueryBuilder {

    private final String name;

    private final Object value;

    private String queryName;

    /**
     * Constructs a new term query.
     *
     * @param name  The name of the field
     * @param value The value of the term
     */
    public PayloadTermQueryBuilder(String name, String value) {
        this(name, (Object) value);
    }

    /**
     * Constructs a new term query.
     *
     * @param name  The name of the field
     * @param value The value of the term
     */
    public PayloadTermQueryBuilder(String name, int value) {
        this(name, (Object) value);
    }

    /**
     * Constructs a new term query.
     *
     * @param name  The name of the field
     * @param value The value of the term
     */
    public PayloadTermQueryBuilder(String name, long value) {
        this(name, (Object) value);
    }

    /**
     * Constructs a new term query.
     *
     * @param name  The name of the field
     * @param value The value of the term
     */
    public PayloadTermQueryBuilder(String name, float value) {
        this(name, (Object) value);
    }

    /**
     * Constructs a new term query.
     *
     * @param name  The name of the field
     * @param value The value of the term
     */
    public PayloadTermQueryBuilder(String name, double value) {
        this(name, (Object) value);
    }

    /**
     * Constructs a new term query.
     *
     * @param name  The name of the field
     * @param value The value of the term
     */
    public PayloadTermQueryBuilder(String name, boolean value) {
        this(name, (Object) value);
    }

    /**
     * Constructs a new term query.
     *
     * @param name  The name of the field
     * @param value The value of the term
     */
    public PayloadTermQueryBuilder(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public PayloadTermQueryBuilder queryName(String queryName) {
        this.queryName = queryName;
        return this;
    }

    @Override
    public void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(PayloadTermQueryParser.NAME);
        if (queryName == null) {
            builder.field(name, value);
        } else {
            builder.startObject(name);
            builder.field("value", value);
            if (queryName != null) {
                builder.field("_name", queryName);
            }
            builder.endObject();
        }
        builder.endObject();
    }
}
