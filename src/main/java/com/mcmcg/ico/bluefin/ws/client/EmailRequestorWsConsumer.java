package com.mcmcg.ico.bluefin.ws.client;

import javax.xml.ws.BindingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mcmcg.mail.dataobject.MailData;
import com.mcmcg.mail.dataobject.MailDataAttachment;
import com.mcmcg.mail.facade.EmailRequestor;
import com.mcmcg.mail.facade.EmailRequestorService;

@Component
public class EmailRequestorWsConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailRequestorWsConsumer.class);

    @Value("${com.mcmcg.ico.bluefin.ws.wsdl.emailrequestor}")
    private String endpointAddress;

    public void dropMessage(MailData data) {
        clientInstance().dropMessage(data);

        LOGGER.info("Send email with to=[{}] data=[{}]", data.getToAddress(), data);
    }

    public void dropMessageAttachment(MailDataAttachment data) {
        clientInstance().dropMessageAttachment(data);

        LOGGER.info("Send email with attachments to=[{}] data=[{}]", data.getToAddress(), data);
    }

    private EmailRequestor clientInstance() {
        EmailRequestorService service = new EmailRequestorService();
        EmailRequestor client = service.getEmailRequestor();

        BindingProvider bp = (BindingProvider) client;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress);

        LOGGER.debug("Configuring endpoint address [{}]", endpointAddress);

        return client;
    }
}
