package org.everit.json.schema;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.everit.json.schema.internal.JSONPrinter;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Enum schema validator.
 */
public class EnumSchema extends Schema {

    static Object toJavaValue(Object orig) {
        if (orig instanceof JSONArray) {
            return ((JSONArray) orig).toList();
        } else if (orig instanceof JSONObject) {
            return ((JSONObject) orig).toMap();
        } else {
            return orig;
        }
    }

    static Set<Object> toJavaValues(Set<Object> orgJsons) {
        return orgJsons.stream().map(EnumSchema::toJavaValue).collect(toSet());
    }

    /**
     * Builder class for {@link EnumSchema}.
     */
    public static class Builder extends Schema.Builder<EnumSchema> {

        private Set<Object> possibleValues = new HashSet<>();

        @Override
        public EnumSchema build() {
            return new EnumSchema(this);
        }

        public Builder possibleValue(final Object possibleValue) {
            possibleValues.add(possibleValue);
            return this;
        }

        public Builder possibleValues(final Set<Object> possibleValues) {
            this.possibleValues = possibleValues;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final Set<Object> possibleValues;

    public EnumSchema(final Builder builder) {
        super(builder);
        possibleValues = Collections.unmodifiableSet(toJavaValues(builder.possibleValues));
    }

    public Set<Object> getPossibleValues() {
        return possibleValues;
    }

    @Override
    public void validate(final Object subject) {
        Object effectiveSubject = toJavaValue(subject);
        possibleValues
                .stream()
                .filter(val -> ObjectComparator.deepEquals(val, effectiveSubject))
                .findAny()
                .orElseThrow(
                        () -> failure(format("%s is not a valid enum value", subject), "enum"));
    }

    @Override
    void describePropertiesTo(final JSONPrinter writer) {
        writer.key("type");
        writer.value("enum");
        writer.key("enum");
        writer.array();
        possibleValues.forEach(writer::value);
        writer.endArray();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof EnumSchema) {
            EnumSchema that = (EnumSchema) o;
            return that.canEqual(this) &&
                    Objects.equals(possibleValues, that.possibleValues) &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), possibleValues);
    }

    @Override
    protected boolean canEqual(final Object other) {
        return other instanceof EnumSchema;
    }

}
