/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.regression.resultset.querytype;

import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.scopetest.EPAssertionUtil;
import com.espertech.esper.client.scopetest.SupportUpdateListener;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.client.time.CurrentTimeSpanEvent;
import com.espertech.esper.supportregression.bean.SupportBean;
import com.espertech.esper.supportregression.bean.SupportBean_S0;
import com.espertech.esper.supportregression.bean.SupportMarketDataBean;
import com.espertech.esper.supportregression.execution.RegressionExecution;
import com.espertech.esper.supportregression.patternassert.ResultAssertExecution;
import com.espertech.esper.supportregression.patternassert.ResultAssertTestResult;

import static org.junit.Assert.assertFalse;

public class ExecQuerytypeRollupOutputRate implements RegressionExecution {
    public void run(EPServiceProvider epService) throws Exception {
        epService.getEPAdministrator().getConfiguration().addEventType(SupportBean.class);
        epService.getEPAdministrator().getConfiguration().addEventType(SupportBean_S0.class);
        epService.getEPAdministrator().getConfiguration().addEventType("MarketData", SupportMarketDataBean.class);

        runAssertionOutputLast(epService, false, false);
        runAssertionOutputLast(epService, false, true);
        runAssertionOutputLast(epService, true, false);
        runAssertionOutputLast(epService, true, true);

        runAssertionOutputLastSorted(epService, false);
        runAssertionOutputLastSorted(epService, true);

        runAssertionOutputAll(epService, false, false);
        runAssertionOutputAll(epService, false, true);
        runAssertionOutputAll(epService, true, false);
        runAssertionOutputAll(epService, true, true);

        runAssertionOutputAllSorted(epService, false);
        runAssertionOutputAllSorted(epService, true);

        runAssertionOutputDefault(epService, false);
        runAssertionOutputDefault(epService, true);

        runAssertionOutputDefaultSorted(epService, false);
        runAssertionOutputDefaultSorted(epService, true);

        runAssertionOutputFirstHaving(epService, false);
        runAssertionOutputFirstHaving(epService, true);

        runAssertionOutputFirstSorted(epService, false);
        runAssertionOutputFirstSorted(epService, true);

        runAssertionOutputFirst(epService, false);
        runAssertionOutputFirst(epService, true);

        runAssertion3OutputLimitAll(epService, false);
        runAssertion3OutputLimitAll(epService, true);

        runAssertion4OutputLimitLast(epService, false);
        runAssertion4OutputLimitLast(epService, true);

        runAssertion1NoOutputLimit(epService);
        runAssertion2OutputLimitDefault(epService);
        runAssertion5OutputLimitFirst(epService);
        runAssertion6OutputLimitSnapshot(epService);
    }

    private void runAssertion1NoOutputLimit(EPServiceProvider epService) {
        String stmtText = "select symbol, sum(price) " +
                "from MarketData#time(5.5 sec)" +
                "group by rollup(symbol)";
        sendTimer(epService, 0);
        EPStatement stmt = epService.getEPAdministrator().createEPL(stmtText);
        SupportUpdateListener listener = new SupportUpdateListener();
        stmt.addListener(listener);

        String[] fields = new String[]{"symbol", "sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult("NoOutputLimit", null, fields);
        expected.addResultInsRem(200, 1, new Object[][]{{"IBM", 25d}, {null, 25d}}, new Object[][]{{"IBM", null}, {null, null}});
        expected.addResultInsRem(800, 1, new Object[][]{{"MSFT", 9d}, {null, 34d}}, new Object[][]{{"MSFT", null}, {null, 25d}});
        expected.addResultInsRem(1500, 1, new Object[][]{{"IBM", 49d}, {null, 58d}}, new Object[][]{{"IBM", 25d}, {null, 34d}});
        expected.addResultInsRem(1500, 2, new Object[][]{{"YAH", 1d}, {null, 59d}}, new Object[][]{{"YAH", null}, {null, 58d}});
        expected.addResultInsRem(2100, 1, new Object[][]{{"IBM", 75d}, {null, 85d}}, new Object[][]{{"IBM", 49d}, {null, 59d}});
        expected.addResultInsRem(3500, 1, new Object[][]{{"YAH", 3d}, {null, 87d}}, new Object[][]{{"YAH", 1d}, {null, 85d}});
        expected.addResultInsRem(4300, 1, new Object[][]{{"IBM", 97d}, {null, 109d}}, new Object[][]{{"IBM", 75d}, {null, 87d}});
        expected.addResultInsRem(4900, 1, new Object[][]{{"YAH", 6d}, {null, 112d}}, new Object[][]{{"YAH", 3d}, {null, 109d}});
        expected.addResultInsRem(5700, 0, new Object[][]{{"IBM", 72d}, {null, 87d}}, new Object[][]{{"IBM", 97d}, {null, 112d}});
        expected.addResultInsRem(5900, 1, new Object[][]{{"YAH", 7d}, {null, 88d}}, new Object[][]{{"YAH", 6d}, {null, 87d}});
        expected.addResultInsRem(6300, 0, new Object[][]{{"MSFT", null}, {null, 79d}}, new Object[][]{{"MSFT", 9d}, {null, 88d}});
        expected.addResultInsRem(7000, 0, new Object[][]{{"IBM", 48d}, {"YAH", 6d}, {null, 54d}}, new Object[][]{{"IBM", 72d}, {"YAH", 7d}, {null, 79d}});

        ResultAssertExecution execution = new ResultAssertExecution(epService, stmt, listener, expected);
        execution.execute(false);
    }

    private void runAssertion2OutputLimitDefault(EPServiceProvider epService) {
        String stmtText = "select symbol, sum(price) " +
                "from MarketData#time(5.5 sec)" +
                "group by rollup(symbol)" +
                "output every 1 seconds";
        sendTimer(epService, 0);
        EPStatement stmt = epService.getEPAdministrator().createEPL(stmtText);
        SupportUpdateListener listener = new SupportUpdateListener();
        stmt.addListener(listener);

        String[] fields = new String[]{"symbol", "sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult("DefaultOutputLimit", null, fields);
        expected.addResultInsRem(1200, 0,
                new Object[][]{{"IBM", 25d}, {null, 25d}, {"MSFT", 9d}, {null, 34d}},
                new Object[][]{{"IBM", null}, {null, null}, {"MSFT", null}, {null, 25d}});
        expected.addResultInsRem(2200, 0,
                new Object[][]{{"IBM", 49d}, {null, 58d}, {"YAH", 1d}, {null, 59d}, {"IBM", 75d}, {null, 85d}},
                new Object[][]{{"IBM", 25d}, {null, 34d}, {"YAH", null}, {null, 58d}, {"IBM", 49d}, {null, 59d}});
        expected.addResultInsRem(3200, 0, null, null);
        expected.addResultInsRem(4200, 0,
                new Object[][]{{"YAH", 3d}, {null, 87d}},
                new Object[][]{{"YAH", 1d}, {null, 85d}});
        expected.addResultInsRem(5200, 0,
                new Object[][]{{"IBM", 97d}, {null, 109d}, {"YAH", 6d}, {null, 112d}},
                new Object[][]{{"IBM", 75d}, {null, 87d}, {"YAH", 3d}, {null, 109d}});
        expected.addResultInsRem(6200, 0,
                new Object[][]{{"IBM", 72d}, {null, 87d}, {"YAH", 7d}, {null, 88d}},
                new Object[][]{{"IBM", 97d}, {null, 112d}, {"YAH", 6d}, {null, 87d}});
        expected.addResultInsRem(7200, 0,
                new Object[][]{{"MSFT", null}, {null, 79d}, {"IBM", 48d}, {"YAH", 6d}, {null, 54d}},
                new Object[][]{{"MSFT", 9d}, {null, 88d}, {"IBM", 72d}, {"YAH", 7d}, {null, 79d}});

        ResultAssertExecution execution = new ResultAssertExecution(epService, stmt, listener, expected);
        execution.execute(false);
    }

    private void runAssertion3OutputLimitAll(EPServiceProvider epService, boolean hinted) {
        String hint = hinted ? "@Hint('enable_outputlimit_opt') " : "";
        String stmtText = hint + "select symbol, sum(price) " +
                "from MarketData#time(5.5 sec)" +
                "group by rollup(symbol)" +
                "output all every 1 seconds";
        sendTimer(epService, 0);
        EPStatement stmt = epService.getEPAdministrator().createEPL(stmtText);
        SupportUpdateListener listener = new SupportUpdateListener();
        stmt.addListener(listener);

        String[] fields = new String[]{"symbol", "sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult("AllOutputLimit", null, fields);
        expected.addResultInsRem(1200, 0,
                new Object[][]{{"IBM", 25d}, {"MSFT", 9d}, {null, 34d}},
                new Object[][]{{"IBM", null}, {"MSFT", null}, {null, null}});
        expected.addResultInsRem(2200, 0,
                new Object[][]{{"IBM", 75d}, {"MSFT", 9d}, {"YAH", 1d}, {null, 85d}},
                new Object[][]{{"IBM", 25d}, {"MSFT", 9d}, {"YAH", null}, {null, 34d}});
        expected.addResultInsRem(3200, 0,
                new Object[][]{{"IBM", 75d}, {"MSFT", 9d}, {"YAH", 1d}, {null, 85d}},
                new Object[][]{{"IBM", 75d}, {"MSFT", 9d}, {"YAH", 1d}, {null, 85d}});
        expected.addResultInsRem(4200, 0,
                new Object[][]{{"IBM", 75d}, {"MSFT", 9d}, {"YAH", 3d}, {null, 87d}},
                new Object[][]{{"IBM", 75d}, {"MSFT", 9d}, {"YAH", 1d}, {null, 85d}});
        expected.addResultInsRem(5200, 0,
                new Object[][]{{"IBM", 97d}, {"MSFT", 9d}, {"YAH", 6d}, {null, 112d}},
                new Object[][]{{"IBM", 75d}, {"MSFT", 9d}, {"YAH", 3d}, {null, 87d}});
        expected.addResultInsRem(6200, 0,
                new Object[][]{{"IBM", 72d}, {"MSFT", 9d}, {"YAH", 7d}, {null, 88d}},
                new Object[][]{{"IBM", 97d}, {"MSFT", 9d}, {"YAH", 6d}, {null, 112d}});
        expected.addResultInsRem(7200, 0,
                new Object[][]{{"IBM", 48d}, {"MSFT", null}, {"YAH", 6d}, {null, 54d}},
                new Object[][]{{"IBM", 72d}, {"MSFT", 9d}, {"YAH", 7d}, {null, 88d}});

        ResultAssertExecution execution = new ResultAssertExecution(epService, stmt, listener, expected);
        execution.execute(true);
    }

    private void runAssertion4OutputLimitLast(EPServiceProvider epService, boolean hinted) {
        String hint = hinted ? "@Hint('enable_outputlimit_opt') " : "";
        String stmtText = hint + "select symbol, sum(price) " +
                "from MarketData#time(5.5 sec)" +
                "group by rollup(symbol)" +
                "output last every 1 seconds";
        sendTimer(epService, 0);
        EPStatement stmt = epService.getEPAdministrator().createEPL(stmtText);
        SupportUpdateListener listener = new SupportUpdateListener();
        stmt.addListener(listener);

        String[] fields = new String[]{"symbol", "sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult("AllOutputLimit", null, fields);
        expected.addResultInsRem(1200, 0,
                new Object[][]{{"IBM", 25d}, {"MSFT", 9d}, {null, 34d}},
                new Object[][]{{"IBM", null}, {"MSFT", null}, {null, null}});
        expected.addResultInsRem(2200, 0,
                new Object[][]{{"IBM", 75d}, {"YAH", 1d}, {null, 85d}},
                new Object[][]{{"IBM", 25d}, {"YAH", null}, {null, 34d}});
        expected.addResultInsRem(3200, 0, null, null);
        expected.addResultInsRem(4200, 0,
                new Object[][]{{"YAH", 3d}, {null, 87d}},
                new Object[][]{{"YAH", 1d}, {null, 85d}});
        expected.addResultInsRem(5200, 0,
                new Object[][]{{"IBM", 97d}, {"YAH", 6d}, {null, 112d}},
                new Object[][]{{"IBM", 75d}, {"YAH", 3d}, {null, 87d}});
        expected.addResultInsRem(6200, 0,
                new Object[][]{{"IBM", 72d}, {"YAH", 7d}, {null, 88d}},
                new Object[][]{{"IBM", 97d}, {"YAH", 6d}, {null, 112d}});
        expected.addResultInsRem(7200, 0,
                new Object[][]{{"MSFT", null}, {"IBM", 48d}, {"YAH", 6d}, {null, 54d}},
                new Object[][]{{"MSFT", 9d}, {"IBM", 72d}, {"YAH", 7d}, {null, 88d}});

        ResultAssertExecution execution = new ResultAssertExecution(epService, stmt, listener, expected);
        execution.execute(true);
    }

    private void runAssertion5OutputLimitFirst(EPServiceProvider epService) {
        String stmtText = "select symbol, sum(price) " +
                "from MarketData#time(5.5 sec)" +
                "group by rollup(symbol)" +
                "output first every 1 seconds";
        sendTimer(epService, 0);
        EPStatement stmt = epService.getEPAdministrator().createEPL(stmtText);
        SupportUpdateListener listener = new SupportUpdateListener();
        stmt.addListener(listener);

        String[] fields = new String[]{"symbol", "sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult("AllOutputLimit", null, fields);
        expected.addResultInsRem(200, 1, new Object[][]{{"IBM", 25d}, {null, 25d}}, new Object[][]{{"IBM", null}, {null, null}});
        expected.addResultInsRem(800, 1, new Object[][]{{"MSFT", 9d}}, new Object[][]{{"MSFT", null}});
        expected.addResultInsRem(1500, 1, new Object[][]{{"IBM", 49d}, {null, 58d}}, new Object[][]{{"IBM", 25d}, {null, 34d}});
        expected.addResultInsRem(1500, 2, new Object[][]{{"YAH", 1d}}, new Object[][]{{"YAH", null}});
        expected.addResultInsRem(3500, 1, new Object[][]{{"YAH", 3d}, {null, 87d}}, new Object[][]{{"YAH", 1d}, {null, 85d}});
        expected.addResultInsRem(4300, 1, new Object[][]{{"IBM", 97d}}, new Object[][]{{"IBM", 75d}});
        expected.addResultInsRem(4900, 1, new Object[][]{{"YAH", 6d}, {null, 112d}}, new Object[][]{{"YAH", 3d}, {null, 109d}});
        expected.addResultInsRem(5700, 0, new Object[][]{{"IBM", 72d}}, new Object[][]{{"IBM", 97d}});
        expected.addResultInsRem(5900, 1, new Object[][]{{"YAH", 7d}, {null, 88d}}, new Object[][]{{"YAH", 6d}, {null, 87d}});
        expected.addResultInsRem(6300, 0, new Object[][]{{"MSFT", null}}, new Object[][]{{"MSFT", 9d}});
        expected.addResultInsRem(7000, 0, new Object[][]{{"IBM", 48d}, {"YAH", 6d}, {null, 54d}}, new Object[][]{{"IBM", 72d}, {"YAH", 7d}, {null, 79d}});

        ResultAssertExecution execution = new ResultAssertExecution(epService, stmt, listener, expected);
        execution.execute(false);
    }

    private void runAssertion6OutputLimitSnapshot(EPServiceProvider epService) {
        String stmtText = "select symbol, sum(price) " +
                "from MarketData#time(5.5 sec)" +
                "group by rollup(symbol)" +
                "output snapshot every 1 seconds";
        sendTimer(epService, 0);
        EPStatement stmt = epService.getEPAdministrator().createEPL(stmtText);
        SupportUpdateListener listener = new SupportUpdateListener();
        stmt.addListener(listener);

        String[] fields = new String[]{"symbol", "sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult("AllOutputLimit", null, fields);
        expected.addResultInsert(1200, 0, new Object[][]{{"IBM", 25d}, {"MSFT", 9d}, {null, 34.0}});
        expected.addResultInsert(2200, 0, new Object[][]{{"IBM", 75d}, {"MSFT", 9d}, {"YAH", 1d}, {null, 85.0}});
        expected.addResultInsert(3200, 0, new Object[][]{{"IBM", 75d}, {"MSFT", 9d}, {"YAH", 1d}, {null, 85.0}});
        expected.addResultInsert(4200, 0, new Object[][]{{"IBM", 75d}, {"MSFT", 9d}, {"YAH", 3d}, {null, 87.0}});
        expected.addResultInsert(5200, 0, new Object[][]{{"IBM", 97d}, {"MSFT", 9d}, {"YAH", 6d}, {null, 112.0}});
        expected.addResultInsert(6200, 0, new Object[][]{{"MSFT", 9d}, {"IBM", 72d}, {"YAH", 7d}, {null, 88.0}});
        expected.addResultInsert(7200, 0, new Object[][]{{"IBM", 48d}, {"YAH", 6d}, {null, 54.0}});

        ResultAssertExecution execution = new ResultAssertExecution(epService, stmt, listener, expected);
        execution.execute(false);
    }

    private void runAssertionOutputFirstHaving(EPServiceProvider epService, boolean join) {
        String[] fields = "c0,c1,c2".split(",");
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(0));
        SupportUpdateListener listener = new SupportUpdateListener();
        epService.getEPAdministrator().createEPL("@Name('s1')" +
                "select irstream theString as c0, intPrimitive as c1, sum(longPrimitive) as c2 " +
                "from SupportBean#time(3.5 sec) " + (join ? ", SupportBean_S0#lastevent " : "") +
                "group by rollup(theString, intPrimitive) " +
                "having sum(longPrimitive) > 100 " +
                "output first every 1 second").addListener(listener);
        epService.getEPRuntime().sendEvent(new SupportBean_S0(1));

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 10L));
        epService.getEPRuntime().sendEvent(makeEvent("E1", 2, 20L));
        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 30L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(1000));
        epService.getEPRuntime().sendEvent(makeEvent("E2", 1, 40L));
        assertFalse(listener.isInvoked());

        epService.getEPRuntime().sendEvent(makeEvent("E1", 2, 50L));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", null, 110L}, {null, null, 150L}},
                new Object[][]{{"E1", null, 110L}, {null, null, 150L}});

        // pass 1 second
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(2000));
        assertFalse(listener.isInvoked());

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 60L));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", null, 170L}, {null, null, 210L}},
                new Object[][]{{"E1", null, 170L}, {null, null, 210L}});

        // pass 1 second
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(3000));

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 70L));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 170L}, {"E1", null, 240L}, {null, null, 280L}},
                new Object[][]{{"E1", 1, 170L}, {"E1", null, 240L}, {null, null, 280L}});

        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(4000)); // removes the first 3 events
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 130L}, {"E1", null, 180L}, {null, null, 220L}},
                new Object[][]{{"E1", 1, 130L}, {"E1", null, 180L}, {null, null, 220L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 80L));
        assertFalse(listener.isInvoked());

        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(5000)); // removes the second 2 events
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", null, 210L}, {null, null, 210L}},
                new Object[][]{{"E1", null, 210L}, {null, null, 210L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 90L));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 300L}},
                new Object[][]{{"E1", 1, 300L}});

        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(6000)); // removes the third 1 event
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 240L}, {"E1", null, 240L}, {null, null, 240L}},
                new Object[][]{{"E1", 1, 240L}, {"E1", null, 240L}, {null, null, 240L}});
    }

    private void runAssertionOutputFirst(EPServiceProvider epService, boolean join) {
        String[] fields = "c0,c1,c2".split(",");
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(0));
        SupportUpdateListener listener = new SupportUpdateListener();
        epService.getEPAdministrator().createEPL("@Name('s1')" +
                "select irstream theString as c0, intPrimitive as c1, sum(longPrimitive) as c2 " +
                "from SupportBean#time(3.5 sec) " + (join ? ", SupportBean_S0#lastevent " : "") +
                "group by rollup(theString, intPrimitive) " +
                "output first every 1 second").addListener(listener);
        epService.getEPRuntime().sendEvent(new SupportBean_S0(1));

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 10L));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 10L}, {"E1", null, 10L}, {null, null, 10L}},
                new Object[][]{{"E1", 1, null}, {"E1", null, null}, {null, null, null}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 2, 20L));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 2, 20L}},
                new Object[][]{{"E1", 2, null}});

        // pass 1 second
        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 30L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(1000));
        assertFalse(listener.isInvoked());

        epService.getEPRuntime().sendEvent(makeEvent("E2", 1, 40L));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E2", 1, 40L}, {"E2", null, 40L}, {null, null, 100L}},
                new Object[][]{{"E2", 1, null}, {"E2", null, null}, {null, null, 60L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 2, 50L));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 2, 70L}, {"E1", null, 110L}},
                new Object[][]{{"E1", 2, 20L}, {"E1", null, 60L}});

        // pass 1 second
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(2000));
        assertFalse(listener.isInvoked());

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 60L));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 100L}, {"E1", null, 170L}, {null, null, 210L}},
                new Object[][]{{"E1", 1, 40L}, {"E1", null, 110L}, {null, null, 150L}});

        // pass 1 second
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(3000));

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 70L));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 170L}, {"E1", null, 240L}, {null, null, 280L}},
                new Object[][]{{"E1", 1, 100L}, {"E1", null, 170L}, {null, null, 210L}});

        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(4000)); // removes the first 3 events
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 130L}, {"E1", 2, 50L}, {"E1", null, 180L}, {null, null, 220L}},
                new Object[][]{{"E1", 1, 170L}, {"E1", 2, 70L}, {"E1", null, 240L}, {null, null, 280L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 80L));
        assertFalse(listener.isInvoked());

        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(5000)); // removes the second 2 events
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{
                        {"E2", 1, null}, {"E1", 2, null}, {"E2", null, null},
                        {"E1", null, 210L}, {null, null, 210L}},
                new Object[][]{
                        {"E2", 1, 40L}, {"E1", 2, 50L}, {"E2", null, 40L},
                        {"E1", null, 260L}, {null, null, 300L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 90L));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 300L}},
                new Object[][]{{"E1", 1, 210L}});

        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(6000)); // removes the third 1 event
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 240L}, {"E1", null, 240L}, {null, null, 240L}},
                new Object[][]{{"E1", 1, 300L}, {"E1", null, 300L}, {null, null, 300L}});

        epService.getEPAdministrator().destroyAllStatements();
    }

    private void runAssertionOutputFirstSorted(EPServiceProvider epService, boolean join) {
        String[] fields = "c0,c1,c2".split(",");
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(0));
        SupportUpdateListener listener = new SupportUpdateListener();
        epService.getEPAdministrator().createEPL("@Name('s1')" +
                "select irstream theString as c0, intPrimitive as c1, sum(longPrimitive) as c2 " +
                "from SupportBean#time(3.5 sec) " + (join ? ", SupportBean_S0#lastevent " : "") +
                "group by rollup(theString, intPrimitive) " +
                "output first every 1 second " +
                "order by theString, intPrimitive").addListener(listener);
        epService.getEPRuntime().sendEvent(new SupportBean_S0(1));

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 10L));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{null, null, 10L}, {"E1", null, 10L}, {"E1", 1, 10L}},
                new Object[][]{{null, null, null}, {"E1", null, null}, {"E1", 1, null}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 2, 20L));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 2, 20L}},
                new Object[][]{{"E1", 2, null}});

        // pass 1 second
        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 30L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(1000));
        assertFalse(listener.isInvoked());

        epService.getEPRuntime().sendEvent(makeEvent("E2", 1, 40L));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{null, null, 100L}, {"E2", null, 40L}, {"E2", 1, 40L}},
                new Object[][]{{null, null, 60L}, {"E2", null, null}, {"E2", 1, null}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 2, 50L));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", null, 110L}, {"E1", 2, 70L}},
                new Object[][]{{"E1", null, 60L}, {"E1", 2, 20L}});

        // pass 1 second
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(2000));
        assertFalse(listener.isInvoked());

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 60L));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{null, null, 210L}, {"E1", null, 170L}, {"E1", 1, 100L}},
                new Object[][]{{null, null, 150L}, {"E1", null, 110L}, {"E1", 1, 40L}});

        // pass 1 second
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(3000));

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 70L));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{null, null, 280L}, {"E1", null, 240L}, {"E1", 1, 170L}},
                new Object[][]{{null, null, 210L}, {"E1", null, 170L}, {"E1", 1, 100L}});

        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(4000)); // removes the first 3 events
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{null, null, 220L}, {"E1", null, 180L}, {"E1", 1, 130L}, {"E1", 2, 50L}},
                new Object[][]{{null, null, 280L}, {"E1", null, 240L}, {"E1", 1, 170L}, {"E1", 2, 70L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 80L));
        assertFalse(listener.isInvoked());

        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(5000)); // removes the second 2 events
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{null, null, 210L}, {"E1", null, 210L}, {"E1", 2, null},
                    {"E2", null, null}, {"E2", 1, null}},
                new Object[][]{{null, null, 300L}, {"E1", null, 260L}, {"E1", 2, 50L},
                    {"E2", null, 40L}, {"E2", 1, 40L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 90L));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 300L}},
                new Object[][]{{"E1", 1, 210L}});

        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(6000)); // removes the third 1 event
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{null, null, 240L}, {"E1", null, 240L}, {"E1", 1, 240L}},
                new Object[][]{{null, null, 300L}, {"E1", null, 300L}, {"E1", 1, 300L}});

        epService.getEPAdministrator().destroyAllStatements();
    }

    private void runAssertionOutputDefault(EPServiceProvider epService, boolean join) {
        String[] fields = "c0,c1,c2".split(",");
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(0));
        SupportUpdateListener listener = new SupportUpdateListener();
        epService.getEPAdministrator().createEPL("@Name('s1')" +
                "select irstream theString as c0, intPrimitive as c1, sum(longPrimitive) as c2 " +
                "from SupportBean#time(3.5 sec) " + (join ? ", SupportBean_S0#lastevent " : "") +
                "group by rollup(theString, intPrimitive) " +
                "output every 1 second").addListener(listener);
        epService.getEPRuntime().sendEvent(new SupportBean_S0(1));

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 10L));
        epService.getEPRuntime().sendEvent(makeEvent("E1", 2, 20L));
        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 30L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(1000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{
                        {"E1", 1, 10L}, {"E1", null, 10L}, {null, null, 10L},
                        {"E1", 2, 20L}, {"E1", null, 30L}, {null, null, 30L},
                        {"E1", 1, 40L}, {"E1", null, 60L}, {null, null, 60L}},
                new Object[][]{
                        {"E1", 1, null}, {"E1", null, null}, {null, null, null},
                        {"E1", 2, null}, {"E1", null, 10L}, {null, null, 10L},
                        {"E1", 1, 10L}, {"E1", null, 30L}, {null, null, 30L}});

        epService.getEPRuntime().sendEvent(makeEvent("E2", 1, 40L));
        epService.getEPRuntime().sendEvent(makeEvent("E1", 2, 50L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(2000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{
                        {"E2", 1, 40L}, {"E2", null, 40L}, {null, null, 100L},
                        {"E1", 2, 70L}, {"E1", null, 110L}, {null, null, 150L}},
                new Object[][]{
                        {"E2", 1, null}, {"E2", null, null}, {null, null, 60L},
                        {"E1", 2, 20L}, {"E1", null, 60L}, {null, null, 100L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 60L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(3000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{
                        {"E1", 1, 100L}, {"E1", null, 170L}, {null, null, 210L}},
                new Object[][]{
                        {"E1", 1, 40L}, {"E1", null, 110L}, {null, null, 150L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 70L));    // removes the first 3 events
        epService.getEPRuntime().sendEvent(new CurrentTimeSpanEvent(4000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{
                        {"E1", 1, 170L}, {"E1", null, 240L}, {null, null, 280L},
                        {"E1", 1, 130L}, {"E1", 2, 50L}, {"E1", null, 180L}, {null, null, 220L},
                },
                new Object[][]{
                        {"E1", 1, 100L}, {"E1", null, 170L}, {null, null, 210L},
                        {"E1", 1, 170L}, {"E1", 2, 70L}, {"E1", null, 240L}, {null, null, 280L},
                });

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 80L));    // removes the second 2 events
        epService.getEPRuntime().sendEvent(new CurrentTimeSpanEvent(5000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{
                        {"E1", 1, 210L}, {"E1", null, 260L}, {null, null, 300L},
                        {"E2", 1, null}, {"E1", 2, null}, {"E2", null, null}, {"E1", null, 210L}, {null, null, 210L},
                },
                new Object[][]{
                        {"E1", 1, 130L}, {"E1", null, 180L}, {null, null, 220L},
                        {"E2", 1, 40L}, {"E1", 2, 50L}, {"E2", null, 40L}, {"E1", null, 260L}, {null, null, 300L},
                });

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 90L));    // removes the third 1 event
        epService.getEPRuntime().sendEvent(new CurrentTimeSpanEvent(6000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{
                        {"E1", 1, 300L}, {"E1", null, 300L}, {null, null, 300L},
                        {"E1", 1, 240L}, {"E1", null, 240L}, {null, null, 240L}},
                new Object[][]{
                        {"E1", 1, 210L}, {"E1", null, 210L}, {null, null, 210L},
                        {"E1", 1, 300L}, {"E1", null, 300L}, {null, null, 300L}});

        epService.getEPAdministrator().destroyAllStatements();
    }

    private void runAssertionOutputDefaultSorted(EPServiceProvider epService, boolean join) {
        String[] fields = "c0,c1,c2".split(",");
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(0));
        SupportUpdateListener listener = new SupportUpdateListener();
        epService.getEPAdministrator().createEPL("@Name('s1')" +
                "select irstream theString as c0, intPrimitive as c1, sum(longPrimitive) as c2 " +
                "from SupportBean#time(3.5 sec) " + (join ? ", SupportBean_S0#lastevent " : "") +
                "group by rollup(theString, intPrimitive) " +
                "output every 1 second " +
                "order by theString, intPrimitive").addListener(listener);
        epService.getEPRuntime().sendEvent(new SupportBean_S0(1));

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 10L));
        epService.getEPRuntime().sendEvent(makeEvent("E1", 2, 20L));
        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 30L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(1000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{
                        {null, null, 10L}, {null, null, 30L}, {null, null, 60L},
                        {"E1", null, 10L}, {"E1", null, 30L}, {"E1", null, 60L},
                        {"E1", 1, 10L}, {"E1", 1, 40L}, {"E1", 2, 20L}},
                new Object[][]{
                        {null, null, null}, {null, null, 10L}, {null, null, 30L},
                        {"E1", null, null}, {"E1", null, 10L}, {"E1", null, 30L},
                        {"E1", 1, null}, {"E1", 1, 10L}, {"E1", 2, null}});

        epService.getEPRuntime().sendEvent(makeEvent("E2", 1, 40L));
        epService.getEPRuntime().sendEvent(makeEvent("E1", 2, 50L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(2000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{
                        {null, null, 100L}, {null, null, 150L},
                        {"E1", null, 110L}, {"E1", 2, 70L},
                        {"E2", null, 40L}, {"E2", 1, 40L}},
                new Object[][]{
                        {null, null, 60L}, {null, null, 100L},
                        {"E1", null, 60L}, {"E1", 2, 20L},
                        {"E2", null, null}, {"E2", 1, null}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 60L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(3000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{
                        {null, null, 210L}, {"E1", null, 170L}, {"E1", 1, 100L}},
                new Object[][]{
                        {null, null, 150L}, {"E1", null, 110L}, {"E1", 1, 40L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 70L));    // removes the first 3 events
        epService.getEPRuntime().sendEvent(new CurrentTimeSpanEvent(4000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{
                        {null, null, 280L}, {null, null, 220L},
                        {"E1", null, 240L}, {"E1", null, 180L},
                        {"E1", 1, 170L}, {"E1", 1, 130L}, {"E1", 2, 50L}},
                new Object[][]{
                        {null, null, 210L}, {null, null, 280L},
                        {"E1", null, 170L}, {"E1", null, 240L},
                        {"E1", 1, 100L}, {"E1", 1, 170L}, {"E1", 2, 70L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 80L));    // removes the second 2 events
        epService.getEPRuntime().sendEvent(new CurrentTimeSpanEvent(5000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{
                        {null, null, 300L}, {null, null, 210L},
                        {"E1", null, 260L}, {"E1", null, 210L},
                        {"E1", 1, 210L}, {"E1", 2, null}, {"E2", null, null}, {"E2", 1, null}},
                new Object[][]{
                        {null, null, 220L}, {null, null, 300L},
                        {"E1", null, 180L}, {"E1", null, 260L},
                        {"E1", 1, 130L}, {"E1", 2, 50L}, {"E2", null, 40L}, {"E2", 1, 40L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 90L));    // removes the third 1 event
        epService.getEPRuntime().sendEvent(new CurrentTimeSpanEvent(6000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{
                        {null, null, 300L}, {null, null, 240L},
                        {"E1", null, 300L}, {"E1", null, 240L},
                        {"E1", 1, 300L}, {"E1", 1, 240L}},
                new Object[][]{
                        {null, null, 210L}, {null, null, 300L},
                        {"E1", null, 210L}, {"E1", null, 300L},
                        {"E1", 1, 210L}, {"E1", 1, 300L}});

        epService.getEPAdministrator().destroyAllStatements();
    }

    private void runAssertionOutputAll(EPServiceProvider epService, boolean join, boolean hinted) {

        String hint = hinted ? "@Hint('enable_outputlimit_opt') " : "";
        String[] fields = "c0,c1,c2".split(",");
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(0));
        SupportUpdateListener listener = new SupportUpdateListener();
        epService.getEPAdministrator().createEPL(hint + "@Name('s1')" +
                "select irstream theString as c0, intPrimitive as c1, sum(longPrimitive) as c2 " +
                "from SupportBean#time(3.5 sec) " + (join ? ", SupportBean_S0#lastevent " : "") +
                "group by rollup(theString, intPrimitive) " +
                "output all every 1 second").addListener(listener);
        epService.getEPRuntime().sendEvent(new SupportBean_S0(1));

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 10L));
        epService.getEPRuntime().sendEvent(makeEvent("E1", 2, 20L));
        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 30L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(1000));
        EPAssertionUtil.assertPropsPerRowAnyOrder(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 40L}, {"E1", 2, 20L}, {"E1", null, 60L}, {null, null, 60L}},
                new Object[][]{{"E1", 1, null}, {"E1", 2, null}, {"E1", null, null}, {null, null, null}});

        epService.getEPRuntime().sendEvent(makeEvent("E2", 1, 40L));
        epService.getEPRuntime().sendEvent(makeEvent("E1", 2, 50L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(2000));
        EPAssertionUtil.assertPropsPerRowAnyOrder(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 40L}, {"E1", 2, 70L}, {"E2", 1, 40L}, {"E1", null, 110L}, {"E2", null, 40L}, {null, null, 150L}},
                new Object[][]{{"E1", 1, 40L}, {"E1", 2, 20L}, {"E2", 1, null}, {"E1", null, 60L}, {"E2", null, null}, {null, null, 60L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 60L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(3000));
        EPAssertionUtil.assertPropsPerRowAnyOrder(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 100L}, {"E1", 2, 70L}, {"E2", 1, 40L}, {"E1", null, 170L}, {"E2", null, 40L}, {null, null, 210L}},
                new Object[][]{{"E1", 1, 40L}, {"E1", 2, 70L}, {"E2", 1, 40L}, {"E1", null, 110L}, {"E2", null, 40L}, {null, null, 150L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 70L));    // removes the first 3 events
        epService.getEPRuntime().sendEvent(new CurrentTimeSpanEvent(4000));
        EPAssertionUtil.assertPropsPerRowAnyOrder(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 130L}, {"E1", 2, 50L}, {"E2", 1, 40L}, {"E1", null, 180L}, {"E2", null, 40L}, {null, null, 220L}},
                new Object[][]{{"E1", 1, 100L}, {"E1", 2, 70L}, {"E2", 1, 40L}, {"E1", null, 170L}, {"E2", null, 40L}, {null, null, 210L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 80L));    // removes the second 2 events
        epService.getEPRuntime().sendEvent(new CurrentTimeSpanEvent(5000));
        EPAssertionUtil.assertPropsPerRowAnyOrder(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 210L}, {"E1", 2, null}, {"E2", 1, null}, {"E1", null, 210L}, {"E2", null, null}, {null, null, 210L}},
                new Object[][]{{"E1", 1, 130L}, {"E1", 2, 50L}, {"E2", 1, 40L}, {"E1", null, 180L}, {"E2", null, 40L}, {null, null, 220L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 90L));    // removes the third 1 event
        epService.getEPRuntime().sendEvent(new CurrentTimeSpanEvent(6000));
        EPAssertionUtil.assertPropsPerRowAnyOrder(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 240L}, {"E1", 2, null}, {"E2", 1, null}, {"E1", null, 240L}, {"E2", null, null}, {null, null, 240L}},
                new Object[][]{{"E1", 1, 210L}, {"E1", 2, null}, {"E2", 1, null}, {"E1", null, 210L}, {"E2", null, null}, {null, null, 210L}});

        epService.getEPAdministrator().destroyAllStatements();
    }

    private void runAssertionOutputAllSorted(EPServiceProvider epService, boolean join) {
        String[] fields = "c0,c1,c2".split(",");
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(0));
        SupportUpdateListener listener = new SupportUpdateListener();
        epService.getEPAdministrator().createEPL("@Name('s1')" +
                "select irstream theString as c0, intPrimitive as c1, sum(longPrimitive) as c2 " +
                "from SupportBean#time(3.5 sec) " + (join ? ", SupportBean_S0#lastevent " : "") +
                "group by rollup(theString, intPrimitive) " +
                "output all every 1 second " +
                "order by theString, intPrimitive").addListener(listener);
        epService.getEPRuntime().sendEvent(new SupportBean_S0(1));

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 10L));
        epService.getEPRuntime().sendEvent(makeEvent("E1", 2, 20L));
        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 30L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(1000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{null, null, 60L}, {"E1", null, 60L}, {"E1", 1, 40L}, {"E1", 2, 20L}},
                new Object[][]{{null, null, null}, {"E1", null, null}, {"E1", 1, null}, {"E1", 2, null}});

        epService.getEPRuntime().sendEvent(makeEvent("E2", 1, 40L));
        epService.getEPRuntime().sendEvent(makeEvent("E1", 2, 50L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(2000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{null, null, 150L}, {"E1", null, 110L}, {"E1", 1, 40L}, {"E1", 2, 70L}, {"E2", null, 40L}, {"E2", 1, 40L}},
                new Object[][]{{null, null, 60L}, {"E1", null, 60L}, {"E1", 1, 40L}, {"E1", 2, 20L}, {"E2", null, null}, {"E2", 1, null}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 60L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(3000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{null, null, 210L}, {"E1", null, 170L}, {"E1", 1, 100L}, {"E1", 2, 70L}, {"E2", null, 40L}, {"E2", 1, 40L}},
                new Object[][]{{null, null, 150L}, {"E1", null, 110L}, {"E1", 1, 40L}, {"E1", 2, 70L}, {"E2", null, 40L}, {"E2", 1, 40L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 70L));    // removes the first 3 events
        epService.getEPRuntime().sendEvent(new CurrentTimeSpanEvent(4000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{null, null, 220L}, {"E1", null, 180L}, {"E1", 1, 130L}, {"E1", 2, 50L}, {"E2", null, 40L}, {"E2", 1, 40L}},
                new Object[][]{{null, null, 210L}, {"E1", null, 170L}, {"E1", 1, 100L}, {"E1", 2, 70L}, {"E2", null, 40L}, {"E2", 1, 40L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 80L));    // removes the second 2 events
        epService.getEPRuntime().sendEvent(new CurrentTimeSpanEvent(5000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{null, null, 210L}, {"E1", null, 210L}, {"E1", 1, 210L}, {"E1", 2, null}, {"E2", null, null}, {"E2", 1, null}},
                new Object[][]{{null, null, 220L}, {"E1", null, 180L}, {"E1", 1, 130L}, {"E1", 2, 50L}, {"E2", null, 40L}, {"E2", 1, 40L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 90L));    // removes the third 1 event
        epService.getEPRuntime().sendEvent(new CurrentTimeSpanEvent(6000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{null, null, 240L}, {"E1", null, 240L}, {"E1", 1, 240L}, {"E1", 2, null}, {"E2", null, null}, {"E2", 1, null}},
                new Object[][]{{null, null, 210L}, {"E1", null, 210L}, {"E1", 1, 210L}, {"E1", 2, null}, {"E2", null, null}, {"E2", 1, null}});

        epService.getEPAdministrator().destroyAllStatements();
    }

    private void runAssertionOutputLast(EPServiceProvider epService, boolean hinted, boolean join) {
        String hint = hinted ? "@Hint('enable_outputlimit_opt') " : "";
        String[] fields = "c0,c1,c2".split(",");
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(0));
        SupportUpdateListener listener = new SupportUpdateListener();
        epService.getEPAdministrator().createEPL(hint + "@Name('s1')" +
                "select irstream theString as c0, intPrimitive as c1, sum(longPrimitive) as c2 " +
                "from SupportBean#time(3.5 sec) " + (join ? ", SupportBean_S0#lastevent " : "") +
                "group by rollup(theString, intPrimitive) " +
                "output last every 1 second").addListener(listener);
        epService.getEPRuntime().sendEvent(new SupportBean_S0(1));

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 10L));
        epService.getEPRuntime().sendEvent(makeEvent("E1", 2, 20L));
        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 30L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(1000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 40L}, {"E1", 2, 20L}, {"E1", null, 60L}, {null, null, 60L}},
                new Object[][]{{"E1", 1, null}, {"E1", 2, null}, {"E1", null, null}, {null, null, null}});

        epService.getEPRuntime().sendEvent(makeEvent("E2", 1, 40L));
        epService.getEPRuntime().sendEvent(makeEvent("E1", 2, 50L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(2000));
        EPAssertionUtil.assertPropsPerRowAnyOrder(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E2", 1, 40L}, {"E1", 2, 70L}, {"E2", null, 40L}, {"E1", null, 110L}, {null, null, 150L}},
                new Object[][]{{"E2", 1, null}, {"E1", 2, 20L}, {"E2", null, null}, {"E1", null, 60L}, {null, null, 60L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 60L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(3000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 100L}, {"E1", null, 170L}, {null, null, 210L}},
                new Object[][]{{"E1", 1, 40L}, {"E1", null, 110L}, {null, null, 150L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 70L));
        epService.getEPRuntime().sendEvent(new CurrentTimeSpanEvent(4000)); // removes the first 3 events
        EPAssertionUtil.assertPropsPerRowAnyOrder(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 130L}, {"E1", 2, 50L}, {"E1", null, 180L}, {null, null, 220L}},
                new Object[][]{{"E1", 1, 100L}, {"E1", 2, 70L}, {"E1", null, 170L}, {null, null, 210L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 80L));
        epService.getEPRuntime().sendEvent(new CurrentTimeSpanEvent(5000)); // removes the second 2 events
        EPAssertionUtil.assertPropsPerRowAnyOrder(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 210L}, {"E2", 1, null}, {"E1", 2, null}, {"E1", null, 210L}, {"E2", null, null}, {null, null, 210L}},
                new Object[][]{{"E1", 1, 130L}, {"E2", 1, 40L}, {"E1", 2, 50L}, {"E1", null, 180L}, {"E2", null, 40L}, {null, null, 220L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 90L));
        epService.getEPRuntime().sendEvent(new CurrentTimeSpanEvent(6000)); // removes the third 1 event
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{"E1", 1, 240L}, {"E1", null, 240L}, {null, null, 240L}},
                new Object[][]{{"E1", 1, 210L}, {"E1", null, 210L}, {null, null, 210L}});

        epService.getEPAdministrator().destroyAllStatements();
    }

    private void runAssertionOutputLastSorted(EPServiceProvider epService, boolean join) {

        String[] fields = "c0,c1,c2".split(",");
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(0));
        SupportUpdateListener listener = new SupportUpdateListener();
        epService.getEPAdministrator().createEPL("@Name('s1')" +
                "select irstream theString as c0, intPrimitive as c1, sum(longPrimitive) as c2 " +
                "from SupportBean#time(3.5 sec) " + (join ? ", SupportBean_S0#lastevent " : "") +
                "group by rollup(theString, intPrimitive) " +
                "output last every 1 second " +
                "order by theString, intPrimitive").addListener(listener);
        epService.getEPRuntime().sendEvent(new SupportBean_S0(1));

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 10L));
        epService.getEPRuntime().sendEvent(makeEvent("E1", 2, 20L));
        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 30L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(1000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{null, null, 60L}, {"E1", null, 60L}, {"E1", 1, 40L}, {"E1", 2, 20L}},
                new Object[][]{{null, null, null}, {"E1", null, null}, {"E1", 1, null}, {"E1", 2, null}});

        epService.getEPRuntime().sendEvent(makeEvent("E2", 1, 40L));
        epService.getEPRuntime().sendEvent(makeEvent("E1", 2, 50L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(2000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{null, null, 150L}, {"E1", null, 110L}, {"E1", 2, 70L}, {"E2", null, 40L}, {"E2", 1, 40L}},
                new Object[][]{{null, null, 60L}, {"E1", null, 60L}, {"E1", 2, 20L}, {"E2", null, null}, {"E2", 1, null}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 60L));
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(3000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{null, null, 210L}, {"E1", null, 170L}, {"E1", 1, 100L}},
                new Object[][]{{null, null, 150L}, {"E1", null, 110L}, {"E1", 1, 40L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 70L));    // removes the first 3 events
        epService.getEPRuntime().sendEvent(new CurrentTimeSpanEvent(4000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{null, null, 220L}, {"E1", null, 180L}, {"E1", 1, 130L}, {"E1", 2, 50L}},
                new Object[][]{{null, null, 210L}, {"E1", null, 170L}, {"E1", 1, 100L}, {"E1", 2, 70L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 80L));    // removes the second 2 events
        epService.getEPRuntime().sendEvent(new CurrentTimeSpanEvent(5000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{null, null, 210L}, {"E1", null, 210L}, {"E1", 1, 210L}, {"E1", 2, null}, {"E2", null, null}, {"E2", 1, null}},
                new Object[][]{{null, null, 220L}, {"E1", null, 180L}, {"E1", 1, 130L}, {"E1", 2, 50L}, {"E2", null, 40L}, {"E2", 1, 40L}});

        epService.getEPRuntime().sendEvent(makeEvent("E1", 1, 90L));    // removes the third 1 event
        epService.getEPRuntime().sendEvent(new CurrentTimeSpanEvent(6000));
        EPAssertionUtil.assertPropsPerRow(listener.getAndResetDataListsFlattened(), fields,
                new Object[][]{{null, null, 240L}, {"E1", null, 240L}, {"E1", 1, 240L}},
                new Object[][]{{null, null, 210L}, {"E1", null, 210L}, {"E1", 1, 210L}});

        epService.getEPAdministrator().destroyAllStatements();
    }

    private SupportBean makeEvent(String theString, int intPrimitive, long longPrimitive) {
        SupportBean sb = new SupportBean(theString, intPrimitive);
        sb.setLongPrimitive(longPrimitive);
        return sb;
    }

    private void sendTimer(EPServiceProvider epService, long timeInMSec) {
        CurrentTimeEvent theEvent = new CurrentTimeEvent(timeInMSec);
        EPRuntime runtime = epService.getEPRuntime();
        runtime.sendEvent(theEvent);
    }
}
