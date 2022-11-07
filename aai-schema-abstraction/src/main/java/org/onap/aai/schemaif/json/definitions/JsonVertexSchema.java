/**
 * ﻿============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2019 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2019 Amdocs
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.aai.schemaif.json.definitions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.Map;

import org.onap.aai.schemaif.SchemaProviderException;

public class JsonVertexSchema {
    private static final Gson gson = new GsonBuilder().create();

    private String name;
    private String description;
    private List<JsonPropertySchema> properties;
    private Map<String, String> annotations;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<JsonPropertySchema> getProperties() {
        return properties;
    }

    public void setProperties(List<JsonPropertySchema> properties) {
        this.properties = properties;
    }

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<String, String> annotations) {
        this.annotations = annotations;
    }

    public void validate() throws SchemaProviderException {
        if ((getName() == null) || (getName().isEmpty())) {
            throw new SchemaProviderException("Node definition missing a name");
        }

        if (getProperties() != null) {
            for (JsonPropertySchema propSchema : getProperties()) {
                propSchema.validate();
            }
        }
    }

    public String toJson() {
        return gson.toJson(this);
    }

    public static JsonVertexSchema fromJson(String json) {
        return gson.fromJson(json, JsonVertexSchema.class);
    }

    @Override
    public String toString() {
        return "JsonVertexSchema [name=" + name + ", description=" + description + ", properties=" + properties
                + ", annotations=" + annotations + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((annotations == null) ? 0 : annotations.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JsonVertexSchema other = (JsonVertexSchema) obj;
        if (annotations == null) {
            if (other.annotations != null)
                return false;
        } else if (!annotations.equals(other.annotations))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
            return false;
        return true;
    }

}
