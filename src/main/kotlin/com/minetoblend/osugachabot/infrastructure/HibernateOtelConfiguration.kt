package com.minetoblend.osugachabot.infrastructure

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.jdbc.datasource.JdbcTelemetry
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class HibernateOtelConfiguration {

    @Bean
    fun jdbcOtelBeanPostProcessor(openTelemetry: OpenTelemetry): BeanPostProcessor {
        val telemetry = JdbcTelemetry.builder(openTelemetry).build()
        return object : BeanPostProcessor {
            override fun postProcessAfterInitialization(bean: Any, beanName: String): Any =
                if (bean is DataSource) telemetry.wrap(bean) else bean
        }
    }
}
