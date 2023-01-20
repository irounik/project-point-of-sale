package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.model.data.AboutAppData;
import com.increff.ironic.pos.spring.AbstractUnitTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AboutAppDtoTest extends AbstractUnitTest {

    @Autowired
    AboutAppDto aboutAppDto;

    @Test
    public void getDetails() {
        AboutAppData aboutAppData = aboutAppDto.getDetails();
        Assert.assertEquals("Point of Sale Application", aboutAppData.getName());
        Assert.assertEquals("1.0", aboutAppData.getVersion());
    }

}