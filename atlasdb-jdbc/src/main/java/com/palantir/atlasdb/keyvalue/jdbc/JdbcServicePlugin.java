package com.palantir.atlasdb.keyvalue.jdbc;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.atlasdb.keyvalue.api.KeyValueService;
import com.palantir.atlasdb.keyvalue.remoting.RemotingKeyValueService;
import com.palantir.atlasdb.spi.AtlasDbServerEnvironment;
import com.palantir.atlasdb.spi.AtlasDbServicePlugin;

@JsonDeserialize(as = ImmutableJdbcServicePlugin.class)
@JsonSerialize(as = ImmutableJdbcServicePlugin.class)
@JsonTypeName(JdbcKeyValueConfiguration.TYPE)
@Value.Immutable
public abstract class JdbcServicePlugin implements AtlasDbServicePlugin {
    @Override
    public String type() {
        return JdbcKeyValueConfiguration.TYPE;
    }

    @Override
    public void registerServices(AtlasDbServerEnvironment environment) {
        KeyValueService kvs = new JdbcAtlasDbFactory().createRawKeyValueService(getConfig());
        kvs.initializeFromFreshInstance();
        RemotingKeyValueService.registerKeyValueWithEnvironment(kvs, environment);
    }

    public abstract JdbcKeyValueConfiguration getConfig();

}
