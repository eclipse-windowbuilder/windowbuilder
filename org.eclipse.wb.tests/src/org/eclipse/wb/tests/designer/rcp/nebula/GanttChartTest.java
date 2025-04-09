/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.rcp.nebula;

import org.eclipse.wb.internal.rcp.nebula.ganttchart.GanttGroupEditPart;
import org.eclipse.wb.internal.rcp.nebula.ganttchart.GanttGroupInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.nebula.widgets.ganttchart.GanttChart;

import org.junit.Test;

/**
 * Test for {@link GanttChart} items models.
 *
 * @author sablin_aa
 */
public class GanttChartTest extends AbstractNebulaTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * General test {@link GanttGroupInfo} & {@link GanttGroupEditPart}
	 */
	@Test
	public void test_General() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import org.eclipse.nebula.widgets.ganttchart.*;",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    GanttChart chart = new GanttChart(this, SWT.NONE);",
						"    {",
						"      GanttGroup group = new GanttGroup(chart);",
						"    }",
						"  }",
						"}");
		// refresh() also should be successful
		shell.refresh();
		// info
		CompositeInfo gantt = shell.getChildren(CompositeInfo.class).get(0);
		// inner composite info
		CompositeInfo composite = gantt.getChildren(CompositeInfo.class).get(0);
		assertNotNull(composite);
		// group
		// [failed cause no model in wbp-meta] GanttGroup_Info group = table.getChildren(GanttGroup_Info.class).get(0);
	}
}