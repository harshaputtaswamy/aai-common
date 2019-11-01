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

import java.text.DecimalFormat;

public class IntDataType extends DataType {    
    public IntDataType() {
        super(Type.INT);
    }

    @Override
    public Object validateValue(String value) {
        // TODO:  In Tosca, you can impose constraints such as min/max value.
        // In future we can add this type of validation
        Integer intValue;
        try {
            intValue = Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
            // There is an edge case where an int value is stored as "x.0" in champ.
            // In that case, we just want to drop the ".0" and treat it as a proper
            // integer
            try {
                DecimalFormat decimalFormat = new DecimalFormat("0.#");
                intValue = Integer.parseInt(decimalFormat.format(Double.valueOf(value)));
            }
            catch (Exception ex)  {
                return null;
            }
        }

        return intValue;
    }
}
