package com.mcmcg.ico.bluefin.ws.client;

import javax.xml.ws.BindingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mcmcg.paymentprocessor.PayementRequests;
import com.mcmcg.paymentprocessor.PaymentProcessingRequest;
import com.mcmcg.paymentprocessor.PaymentService;

@Component
public class PaymentRequestorWsConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentRequestorWsConsumer.class);

    @Value("${com.mcmcg.ico.bluefin.ws.wsdl.paymentrequestor}")
    private String endpointAddress;

    public void paymentRequest(PaymentProcessingRequest data) {
        clientInstance().paymentRequest(data);
        // PaymentProcessingResponse

        LOGGER.info("Generating payment data=[{}]", data);
    }

    private PaymentService clientInstance() {
        PayementRequests service = new PayementRequests();
        PaymentService client = service.getPayementRequestsSOAP();

        BindingProvider bp = (BindingProvider) client;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress);

        LOGGER.debug("Configuring endpoint address [{}]", endpointAddress);

        return client;
    }
}
