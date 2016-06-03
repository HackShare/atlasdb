/**
 * Copyright 2015 Palantir Technologies
 *
 * Licensed under the BSD-3 License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.atlasdb.keyvalue.dbkvs;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.atlasdb.AtlasDbConstants;
import com.palantir.atlasdb.keyvalue.api.TableReference;

@JsonDeserialize(as = ImmutableDbSharedConfig.class)
@JsonSerialize(as = ImmutableDbSharedConfig.class)
@Value.Immutable
public abstract class DbSharedConfig {

    @Value.Default
    public TableReference metadataTable() {
        return AtlasDbConstants.METADATA_TABLE;
    }

    @Value.Default
    public String tablePrefix() {
        return "a_";
    }

    @Value.Default
    public int poolSize() {
        return 64;
    }

    @Value.Default
    public int fetchBatchSize() {
        return 256;
    }

    @Value.Default
    public int mutationBatchCount() {
        return 1000;
    }

    @Value.Default
    public int mutationBatchSizeBytes() {
        return 2 * 1024 * 1024;
    }

}
