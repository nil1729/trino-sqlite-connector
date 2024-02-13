/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.trino.plugin.sqlite;

import com.google.common.collect.ImmutableMap;
import io.airlift.log.Level;
import io.airlift.log.Logger;
import io.airlift.log.Logging;
import io.trino.Session;
import io.trino.testing.DistributedQueryRunner;
import io.trino.testing.QueryRunner;

import java.util.Map;

import static io.trino.testing.TestingSession.testSessionBuilder;

public class SqliteQueryRunner
{
    private SqliteQueryRunner() {}

    public static QueryRunner createQueryRunner()
            throws Exception
    {
        Session defaultSession = testSessionBuilder()
                .setCatalog("sqlite")
                .setSchema("main")
                .build();

        Map<String, String> extraProperties = ImmutableMap.<String, String>builder()
                .put("http-server.http.port", "8080")
                .buildOrThrow();
        QueryRunner queryRunner = DistributedQueryRunner.builder(defaultSession)
                .setExtraProperties(extraProperties)
                .setNodeCount(1)
                .build();
        queryRunner.installPlugin(new SqlitePlugin());

        Map<String, String> connectorProperties = Map.of(
                "connection-url", "jdbc:sqlite:sqlite.db",
                "connection-user", "test",
                "connection-password", "");
        queryRunner.createCatalog(
                "sqlite",
                "sqlite",
                connectorProperties);

        return queryRunner;
    }

    public static void main(String[] args)
            throws Exception
    {
        Logging logger = Logging.initialize();
        logger.setLevel("io.trino.plugin.sqlite", Level.DEBUG);
        logger.setLevel("io.trino", Level.INFO);

        QueryRunner queryRunner = createQueryRunner();

        Logger log = Logger.get(SqliteQueryRunner.class);
        log.info("======== SERVER STARTED ========");
        log.info("\n====\n%s\n====", ((DistributedQueryRunner) queryRunner).getCoordinator().getBaseUrl());
    }
}
