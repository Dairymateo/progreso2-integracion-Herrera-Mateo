package edu.udla.integracion.progreso2.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public FanoutExchange appointmentsExchange() {
        return ExchangeBuilder.fanoutExchange("appointments.events").durable(true).build();
    }

    @Bean
    public Queue billingQueue() {
        return QueueBuilder.durable("billing.queue").build();
    }

    @Bean
    public DirectExchange billingExchange() {
        return ExchangeBuilder.directExchange("billing.exchange").durable(true).build();
    }

    @Bean
    public Binding billingBinding(Queue billingQueue, DirectExchange billingExchange) {
        return BindingBuilder.bind(billingQueue).to(billingExchange).with("billing.routing.key");
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable("notifications.queue").build();
    }

    @Bean
    public Queue analyticsQueue() {
        return QueueBuilder.durable("analytics.queue").build();
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsQueue, FanoutExchange appointmentsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(appointmentsExchange);
    }

    @Bean
    public Binding analyticsBinding(Queue analyticsQueue, FanoutExchange appointmentsExchange) {
        return BindingBuilder.bind(analyticsQueue).to(appointmentsExchange);
    }
}
