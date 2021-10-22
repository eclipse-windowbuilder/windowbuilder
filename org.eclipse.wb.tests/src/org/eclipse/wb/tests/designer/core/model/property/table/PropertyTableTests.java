/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.model.property.table;

import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for {@link PropertyTable}.
 *
 * @author scheglov_ke
 */
public class PropertyTableTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.core.model.property.table");
    suite.addTest(createSingleSuite(PropertyTableTest.class));
    //suite.addTest(createSingleSuite(PropertyTableTooltipTest.class));
    //suite.addTest(createSingleSuite(PropertyTableEditorsTest.class));
    return suite;
  }
}
