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
package org.eclipse.wb.tests.designer.core.model.property;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.core.model.property.accessor.AccessorsTests;
import org.eclipse.wb.tests.designer.core.model.property.editor.PropertyEditorsTests;
import org.eclipse.wb.tests.designer.core.model.property.table.PropertyTableTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author scheglov_ke
 */
public class PropertiesTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.core.model.property");
    suite.addTest(createSingleSuite(StandardConvertersTest.class));
    suite.addTest(createSingleSuite(PropertyCategoryTest.class));
    suite.addTest(createSingleSuite(PropertyTest.class));
    suite.addTest(createSingleSuite(PropertyManagerTest.class));
    suite.addTest(createSingleSuite(EmptyPropertyTest.class));
    suite.addTest(createSingleSuite(EventsPropertyTest.class));
    suite.addTest(createSingleSuite(ComponentClassPropertyTest.class));
    suite.addTest(createSingleSuite(TabOrderPropertyTest.class));
    suite.addTest(createSingleSuite(ExposePropertySupportTest.class));
    suite.addTest(AccessorsTests.suite());
    suite.addTest(PropertyEditorsTests.suite());
    suite.addTest(PropertyTableTests.suite());
    return suite;
  }
}
