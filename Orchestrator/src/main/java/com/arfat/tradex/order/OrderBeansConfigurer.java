package com.arfat.tradex.order;

import com.arfat.tradex.persistence.Persistence;
import com.arfat.tradex.persistence.StateMachine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderBeansConfigurer {

    @Bean
    Persistence persistence() {
        return new StateMachine();
    }

    @Bean
    OrderService orderService(Persistence persistence) {
        return new DefaultOrderService(persistence);
    }
}
