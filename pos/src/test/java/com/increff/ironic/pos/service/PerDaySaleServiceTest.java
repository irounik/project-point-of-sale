package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.PerDaySaleDao;
import com.increff.ironic.pos.pojo.PerDaySalePojo;
import com.increff.ironic.pos.spring.AbstractUnitTest;
import com.increff.ironic.pos.testutils.AssertUtils;
import com.increff.ironic.pos.testutils.MockUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PerDaySaleServiceTest extends AbstractUnitTest {

    @Autowired
    private PerDaySaleService perDaySaleService;

    @Autowired
    public PerDaySaleDao perDaySaleDao;

    private List<PerDaySalePojo> perDaySaleList;

    private LocalDateTime currentDate;

    @Before
    public void setUp() {
        currentDate = LocalDateTime.now();
        perDaySaleList = new LinkedList<>();

        for (int i = 0; i < 5; i++) {
            PerDaySalePojo perDaySalePojo = MockUtils.getMockPerDaySale();
            perDaySalePojo.setDate(currentDate.plusDays(i));
            perDaySaleDao.insert(perDaySalePojo);
            perDaySaleList.add(perDaySalePojo);
        }
    }

    @Test
    public void addPerDaySale() {
        PerDaySalePojo perDaySalePojo = MockUtils.getMockPerDaySale();
        perDaySaleService.add(perDaySalePojo);
        PerDaySalePojo actual = perDaySaleDao.select(perDaySalePojo.getId());
        AssertUtils.assertEqualPerDaySale(perDaySalePojo, actual);
    }

    @Test
    public void getAllPerDaySale() {
        List<PerDaySalePojo> actual = perDaySaleService.getAll();
        List<PerDaySalePojo> expected = perDaySaleList;
        AssertUtils.assertEqualList(expected, actual, AssertUtils::assertEqualPerDaySale);
    }

    @Test
    public void getPerDaySaleBetween() {
        LocalDateTime startDate = currentDate.plusDays(1);
        LocalDateTime endDate = currentDate.plusDays(4);
        List<PerDaySalePojo> actual = perDaySaleService.getPerDaySaleBetween(startDate, endDate);

        List<PerDaySalePojo> expected = perDaySaleList
                .stream()
                .filter(perDaySale -> isBetween(perDaySale, startDate, endDate))
                .collect(Collectors.toList());

        AssertUtils.assertEqualList(expected, actual, AssertUtils::assertEqualPerDaySale);
    }

    private boolean isBetween(PerDaySalePojo perDaySalePojo, LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime date = perDaySalePojo.getDate();
        if (date.isEqual(startDate) || date.isEqual(endDate)) return true;
        return date.isBefore(endDate) && date.isAfter(startDate);
    }

}
