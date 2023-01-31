package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.PerDaySaleDao;
import com.increff.ironic.pos.pojo.PerDaySale;
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

    private List<PerDaySale> perDaySaleList;

    private LocalDateTime currentDate;

    @Before
    public void setUp() {
        currentDate = LocalDateTime.now();
        perDaySaleList = new LinkedList<>();

        for (int i = 0; i < 5; i++) {
            PerDaySale perDaySale = MockUtils.getMockPerDaySale();
            perDaySale.setDate(currentDate.plusDays(i));
            perDaySaleDao.insert(perDaySale);
            perDaySaleList.add(perDaySale);
        }
    }

    @Test
    public void addPerDaySale() {
        PerDaySale perDaySale = MockUtils.getMockPerDaySale();
        perDaySaleService.add(perDaySale);
        PerDaySale actual = perDaySaleDao.select(perDaySale.getId());
        AssertUtils.assertEqualPerDaySale(perDaySale, actual);
    }

    @Test
    public void getAllPerDaySale() {
        List<PerDaySale> actual = perDaySaleService.getAll();
        List<PerDaySale> expected = perDaySaleList;
        AssertUtils.assertEqualList(expected, actual, AssertUtils::assertEqualPerDaySale);
    }

    @Test
    public void getPerDaySaleBetween() {
        LocalDateTime startDate = currentDate.plusDays(1);
        LocalDateTime endDate = currentDate.plusDays(4);
        List<PerDaySale> actual = perDaySaleService.getPerDaySaleBetween(startDate, endDate);

        List<PerDaySale> expected = perDaySaleList
                .stream()
                .filter(perDaySale -> isBetween(perDaySale, startDate, endDate))
                .collect(Collectors.toList());

        AssertUtils.assertEqualList(expected, actual, AssertUtils::assertEqualPerDaySale);
    }

    private boolean isBetween(PerDaySale perDaySale, LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime date = perDaySale.getDate();
        if (date.isEqual(startDate) || date.isEqual(endDate)) return true;
        return date.isBefore(endDate) && date.isAfter(startDate);
    }

}
