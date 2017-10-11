/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */
package org.onap.aai.serialization.queryformats;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.onap.aai.db.props.AAIProperties;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.Introspector;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.exceptions.AAIUnknownObjectException;
import org.onap.aai.serialization.db.DBSerializer;
import org.onap.aai.serialization.queryformats.exceptions.AAIFormatVertexException;
import org.onap.aai.serialization.queryformats.params.Depth;
import org.onap.aai.serialization.queryformats.params.NodesOnly;
import org.onap.aai.serialization.queryformats.utils.UrlBuilder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Resource implements FormatMapper {

	private final Loader loader;
	private final DBSerializer serializer;
	private final JsonParser parser;
	private final UrlBuilder urlBuilder;
	private final boolean includeUrl;
	private final boolean nodesOnly;
	private final int depth;
	private Resource (Builder builder) {
		this.parser = new JsonParser();
		this.loader = builder.getLoader();
		this.serializer = builder.getSerializer();
		this.urlBuilder = builder.getUrlBuilder();
		this.includeUrl = builder.isIncludeUrl();
		this.nodesOnly = builder.isNodesOnly();
		this.depth = builder.getDepth();
	}

	@Override
	public JsonObject formatObject(Object input) throws AAIFormatVertexException {
		Vertex v = (Vertex)input;
		JsonObject json = new JsonObject();
		
		if (this.includeUrl) {
			json.addProperty("url", this.urlBuilder.pathed(v));
		}
		json.add(v.<String>property(AAIProperties.NODE_TYPE)
											.orElse(null), this.vertexToJsonObject(v));
		
		return json;
	}

	protected JsonObject vertexToJsonObject(Vertex v) throws AAIFormatVertexException {
		try {
			final Introspector obj = getLoader().introspectorFromName(
										v.<String>property(AAIProperties.NODE_TYPE)
											.orElse(null)
									 );

			final List<Vertex> wrapper = new ArrayList<>();

			wrapper.add(v);

			try {
				getSerializer().dbToObject(wrapper, obj, this.depth, this.nodesOnly, "false");
			} catch (AAIException | UnsupportedEncodingException  e) {
				throw new AAIFormatVertexException("Failed to format vertex - error while serializing: " + e.getMessage(), e);
			}

			final String json = obj.marshal(false);

			return getParser().parse(json).getAsJsonObject();
		} catch (AAIUnknownObjectException e) {
			throw new AAIFormatVertexException("Failed to format vertex - unknown object", e);
		}
	}

	@Override
	public int parallelThreshold() {
		return 20;
	}

	private Loader getLoader() { return loader; }
	private DBSerializer getSerializer() { return serializer; }
	private JsonParser getParser() { return parser; }
	
	public static class Builder implements NodesOnly<Builder>, Depth<Builder> {
		
		private final Loader loader;
		private final DBSerializer serializer;
		private final UrlBuilder urlBuilder;
		private boolean includeUrl = false;
		private boolean nodesOnly = false;
		private int depth = 1;
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
		
		public Resource build() {
			return new Resource(this);
		}
	}
}