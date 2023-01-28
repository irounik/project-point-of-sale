package com.increff.ironic.pos.util;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;

public class NormalizationUtilTest {

    @Test
    public void testNormalizeString() {
        String expected = "perfectly normalized";
        String actual = NormalizationUtil.normalize("  Perfectly Normalized ");
        assertEquals(expected, actual);
    }

    @Test
    public void testNormalizeDouble() {
        Assert.assertThat(NormalizationUtil.normalize(12.003), is(12.0));
        Assert.assertThat(NormalizationUtil.normalize(12.9999), is(12.99));
        Assert.assertThat(NormalizationUtil.normalize(1223.999), is(1223.99));
        Assert.assertThat(NormalizationUtil.normalize(12.899), is(12.89));
        Assert.assertThat(NormalizationUtil.normalize(0.899), is(0.89));
    }

}