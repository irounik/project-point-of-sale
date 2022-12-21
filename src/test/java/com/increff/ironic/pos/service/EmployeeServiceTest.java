package com.increff.ironic.pos.service;

import static org.junit.Assert.assertEquals;

import com.increff.ironic.pos.pojo.Employee;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class EmployeeServiceTest extends AbstractUnitTest {

	@Autowired
	private EmployeeService service;

	@Test
	public void testAdd() throws ApiException {
		Employee p = new Employee();
		p.setName(" Romil Jain ");
		service.add(p);
	}

	@Test
	public void testNormalize() {
		Employee p = new Employee();
		p.setName(" Romil Jain ");
		EmployeeService.normalize(p);
		assertEquals("romil jain", p.getName());
	}

}
