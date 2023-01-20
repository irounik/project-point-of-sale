package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.model.data.AboutAppData;
import com.increff.ironic.pos.service.AboutAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AboutAppDto {

    private final AboutAppService service;

    @Autowired
    public AboutAppDto(AboutAppService service) {
        this.service = service;
    }

    public AboutAppData getDetails() {
        AboutAppData data = new AboutAppData();
        data.setName(service.getName());
        data.setVersion(service.getVersion());
        return data;
    }

}