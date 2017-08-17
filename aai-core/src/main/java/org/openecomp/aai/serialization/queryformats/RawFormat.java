/*-
 * ============LICENSE_START=======================================================
 * org.openecomp.aai
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.aai.serialization.queryformats;

import java.util.Iterator;
import java.util.List;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.openecomp.aai.db.props.AAIProperties;
import org.openecomp.aai.introspection.Loader;
import org.openecomp.aai.serialization.db.DBSerializer;
import org.openecomp.aai.serialization.queryformats.exceptions.AAIFormatVertexException;
import org.openecomp.aai.serialization.queryformats.params.Depth;
import org.openecomp.aai.serialization.queryformats.params.NodesOnly;
import org.openecomp.aai.serialization.queryformats.utils.UrlBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class RawFormat implements FormatMapper {
	protected JsonParser parser = new JsonParser();
	protected final DBSerializer serializer;
	protected final Loader loader;
	protected final UrlBuilder urlBuilder;
	protected final int depth;
	protected final boolean nodesOnly;
	protected RawFormat(Builder builder) {
		this.urlBuilder = builder.getUrlBuilder();
		this.loader = builder.getLoader();
		this.serializer = builder.getSerializer();
		this.depth = builder.getDepth();
		this.nodesOnly = builder.isNodesOnly();
	}
	
	@Override
	public JsonObject formatObject(Object input) throws AAIFormatVertexException {
		Vertex v = (Vertex)input;
		JsonObject json = new JsonObject();
		json.addProperty("id", v.id().toString());
		json.addProperty("node-type", v.<String>value(AAIProperties.NODE_TYPE));
		json.addProperty("url", this.urlBuilder.pathed(v));
		json.add("properties", this.createPropertiesObject(v));
		if (!nodesOnly) {
			json.add("related-to", this.createRelationshipObject(v));
		}
		return json;
	}

	@Override
	public int parallelThreshold() {
		return 100;
	}
	
	
	public JsonObject createPropertiesObject(Vertex v) throws AAIFormatVertexException {
		JsonObject json = new JsonObject();
		Iterator<VertexProperty<Object>> iter = v.properties();

		while (iter.hasNext()) {
			VertexProperty<Object> prop = iter.next();
			if (prop.value() instanceof String) {
				json.addProperty(prop.key(), (String)prop.value());
			} else if (prop.value() instanceof Boolean) {
				json.addProperty(prop.key(), (Boolean)prop.value());
			} else if (prop.value() instanceof Number) {
				json.addProperty(prop.key(), (Number)prop.value());
			} else if (prop.value() instanceof List) {
				Gson gson = new Gson();
				String list = gson.toJson(prop.value());

				json.addProperty(prop.key(), list);
			} else {
				//throw exception?
				return null;
			}
		}

		return json;
	}
	protected JsonArray createRelationshipObject(Vertex v) throws AAIFormatVertexException {
		JsonArray jarray = new JsonArray();
		Iterator<Vertex> iter = v.vertices(Direction.BOTH);

		while (iter.hasNext()) {
			Vertex related = iter.next();

			JsonObject json = new JsonObject();
			json.addProperty("id", related.id().toString());
			json.addProperty("node-type", related.<String>value(AAIProperties.NODE_TYPE));
			json.addProperty("url", this.urlBuilder.pathed(related));
			jarray.add(json);
		}

		return jarray;
	}
	
	public static class Builder implements NodesOnly<Builder>, Depth<Builder> {
		
		protected final Loader loader;
		protected final DBSerializer serializer;
		protected final UrlBuilder urlBuilder;
		protected boolean includeUrl = false;
		protected boolean nodesOnly = false;
		protected int depth = 1;
		protected boolean modelDriven = false;
		public Builder(Loader loader, DBSerializer serializer, UrlBuilder urlBuilder) {
			this.loader = loader;
			this.serializer = serializer;
			this.urlBuilder = urlBuilder;
		}
		
		protected Loader getLoader() {
			return this.loader;
		}

		protected DBSerializer getSerializer() {
			return this.serializer;
		}

		protected UrlBuilder getUrlBuilder() {
			return this.urlBuilder;
		}
		
		public Builder includeUrl() {
			this.includeUrl = true;
			return this;
		}
		
		public Builder nodesOnly(Boolean nodesOnly) {
			this.nodesOnly = nodesOnly;
			return this;
		}
		public boolean isNodesOnly() {
			return this.nodesOnly;
		}
		
		public Builder depth(Integer depth) {
			this.depth = depth;
			return this;
		}
		
		public int getDepth() {
			return this.depth;
		}

		public boolean isIncludeUrl() {
			return this.includeUrl;
		}
		
		public Builder modelDriven() {
			this.modelDriven = true;
			return this;
		}
		
		public boolean getModelDriven() {
			return this.modelDriven;
		}
		public RawFormat build() {
			if (modelDriven) {
				return new SimpleFormat(this);
			} else {
				return new RawFormat(this);
			}
		}
	}
}
