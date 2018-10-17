/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.helpers.tests;

import android.platform.helpers.HelperAccessor;
import android.platform.helpers.ICalendarHelper;
import androidx.test.runner.AndroidJUnit4;

import com.android.helpers.CpuUsageHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Android Unit tests for {@link CpuUsageHelperTest}.
 *
 * To run:
 * atest CollectorsHelperTest:com.android.helpers.tests.CpuUsageHelperTest
 */
@RunWith(AndroidJUnit4.class)
public class CpuUsageHelperTest {

    // Kill the calendar app.
    private static final String KILL_TEST_APP_CMD = "am force-stop com.google.android.calendar";
    // Key prefix used for cpu usage by frequency index.
    private static final String CPU_USAGE_FREQ_PREFIX = "cpu_usage_freq";
    // Key prefix used for cpu usage by package name or uid
    private static final String CPU_USAGE_PKG_UID_PREFIX = "cpu_usage_pkg_or_uid";
    // Key used for total CPU usage
    private static final String TOTAL_CPU_USAGE = "total_cpu_usage";
    // Key used for total CPU usage in frequency buckets
    private static final String TOTAL_CPU_USAGE_FREQ = "total_cpu_usage_freq";

    private CpuUsageHelper mCpuUsageHelper = new CpuUsageHelper();
    private HelperAccessor<ICalendarHelper> mHelper =
            new HelperAccessor<>(ICalendarHelper.class);

    @Before
    public void setUp() {
        mCpuUsageHelper = new CpuUsageHelper();
    }

    /**
     * Test successfull cpu usage config.
     */
    @Test
    public void testCpuUsageConfig() throws Exception {
        assertTrue(mCpuUsageHelper.startCollecting());
        assertTrue(mCpuUsageHelper.stopCollecting());
    }

    /**
     * Test cpu usage metrics are collected.
     */
    @Test
    public void testCpuUsageMetrics() throws Exception {
        assertTrue(mCpuUsageHelper.startCollecting());
        mHelper.get().open();
        Map<String, Long> cpuUsage = mCpuUsageHelper.getMetrics();
        assertTrue(cpuUsage.size() > 0);
        assertTrue(mCpuUsageHelper.stopCollecting());
        mHelper.get().exit();
    }

    /**
     * Test that at least one cpu usage per pkg or uid and per preq index is collected,
     * the total usage is collected, and that
     * the total usage is indeed the sum of the per pkg/uid and frequency usage, respectively.
     */
    @Test
    public void testCpuUsageMetricsKey() throws Exception {
        // Variables to verify existence of collected metrics.
        boolean isFreqIndexPresent = false;
        boolean isPkgorUidPresent = false;
        boolean isFreqUsed = false;
        boolean isUIDUsed = false;
        boolean isTotalCpuUsageEntryPresent = false;
        boolean isTotalCpuUsageValuePresent = false;
        boolean isTotalCpuFreqEntryPresent = false;
        boolean isTotalCpuFreqValuePresent = false;
        assertTrue(mCpuUsageHelper.startCollecting());
        mHelper.get().open();
        // Variables to Verify that the reported usage does sum up to the reported total usage.
        Long sumCpuUsage = 0L;
        Long sumCpuUsageFreq = 0L;
        Long reportedTotalCpuUsage = 0L;
        Long reportedTotalCpuUsageFreq = 0L;
        for (Map.Entry<String, Long> cpuUsageEntry : mCpuUsageHelper.getMetrics().entrySet()) {
            if (cpuUsageEntry.getKey().startsWith(CPU_USAGE_FREQ_PREFIX)) {
                isFreqIndexPresent = true;
                if (cpuUsageEntry.getValue() > 0) {
                    isFreqUsed = true;
                }
                sumCpuUsageFreq += cpuUsageEntry.getValue();
            }
            if (cpuUsageEntry.getKey().startsWith(CPU_USAGE_PKG_UID_PREFIX)) {
                isPkgorUidPresent = true;
                if (cpuUsageEntry.getValue() > 0) {
                    isUIDUsed = true;
                }
                sumCpuUsage += cpuUsageEntry.getValue();
            }
            if (cpuUsageEntry.getKey().equals(TOTAL_CPU_USAGE_FREQ)) {
                isTotalCpuFreqEntryPresent = true;
                if (cpuUsageEntry.getValue() > 0) {
                    isTotalCpuFreqValuePresent = true;
                }
                reportedTotalCpuUsageFreq = cpuUsageEntry.getValue();
            }
            if (cpuUsageEntry.getKey().equals(TOTAL_CPU_USAGE)) {
                isTotalCpuUsageEntryPresent = true;
                if (cpuUsageEntry.getValue() > 0) {
                    isTotalCpuUsageValuePresent = true;
                }
                reportedTotalCpuUsage = cpuUsageEntry.getValue();
            }
        }
        assertTrue(isFreqIndexPresent && isFreqUsed);
        assertTrue(isPkgorUidPresent && isUIDUsed);
        assertTrue(isTotalCpuUsageEntryPresent && isTotalCpuUsageValuePresent);
        assertTrue(isTotalCpuFreqEntryPresent && isTotalCpuFreqValuePresent);
        assertEquals(sumCpuUsageFreq, reportedTotalCpuUsageFreq);
        assertEquals(sumCpuUsage, reportedTotalCpuUsage);
        assertTrue(mCpuUsageHelper.stopCollecting());
        mHelper.get().exit();
    }

}

