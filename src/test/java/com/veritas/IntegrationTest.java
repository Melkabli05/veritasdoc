package com.veritas;

import com.veritas.config.AsyncSyncConfiguration;
import com.veritas.config.EmbeddedElasticsearch;
import com.veritas.config.EmbeddedKafka;
import com.veritas.config.EmbeddedRedis;
import com.veritas.config.EmbeddedSQL;
import com.veritas.config.JacksonConfiguration;
import com.veritas.config.TestSecurityConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = { VeritasdocApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class, TestSecurityConfiguration.class }
)
@EmbeddedRedis
@EmbeddedElasticsearch
@EmbeddedSQL
@EmbeddedKafka
public @interface IntegrationTest {
}
