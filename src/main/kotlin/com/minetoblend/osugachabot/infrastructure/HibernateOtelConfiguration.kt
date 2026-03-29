package com.minetoblend.osugachabot.infrastructure

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.jdbc.datasource.JdbcTelemetry
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Role
import org.springframework.beans.factory.config.BeanDefinition
import javax.sql.DataSource

@Configuration
class HibernateOtelConfiguration {

    companion object {
        @JvmStatic
        @Bean
        @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
        fun jdbcOtelBeanPostProcessor(openTelemetryProvider: ObjectProvider<OpenTelemetry>): BeanPostProcessor {
            return object : BeanPostProcessor {
                private val telemetry by lazy {
                    JdbcTelemetry.builder(openTelemetryProvider.getObject()).build()
                }

                override fun postProcessAfterInitialization(bean: Any, beanName: String): Any =
                    if (bean is DataSource) telemetry.wrap(bean) else bean
            }
        }
    }
}
