package com.increff.ironic.pos.service;

import com.increff.ironic.pos.spring.AbstractUnitTest;
import com.increff.ironic.pos.spring.AbstractUnitTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class AboutAppServiceTest extends AbstractUnitTest {

    @Autowired
    private AboutAppService service;

    @Test
    public void testServiceApis() {
        assertEquals("Point of Sale Application", service.getName());
        assertEquals("1.0", service.getVersion());
    }

}
