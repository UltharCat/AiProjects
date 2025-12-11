package com.ai.alibaba.client;

import com.ai.alibaba.dto.OrderDTO;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange("/functionCalling")
public interface AiWebClient {

    @GetExchange("/findAllOrder")
    List<OrderDTO> findAllOrder();

}
