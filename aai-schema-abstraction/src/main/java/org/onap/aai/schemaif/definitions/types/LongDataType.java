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

package org.onap.aai.schemaif.definitions.types;

public class LongDataType extends DataType {
    public LongDataType() {
        super(Type.LONG);
    }

    @Override
    public Object validateValue(String value) {
        // TODO: In Tosca, you can impose constraints such as min/max value.
        // In future we can add this type of validation
        Long longInt;
        try {
            longInt = Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }

        return longInt;
    }
}
