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
package org.eclipse.wb.tests.designer.XML.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * XML {@link Property} tests.
 *
 * @author scheglov_ke
 */
public class PropertyTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.xml.model.property");
    suite.addTest(createSingleSuite(XmlPropertyTest.class));
    suite.addTest(createSingleSuite(EmptyXmlPropertyTest.class));
    suite.addTest(createSingleSuite(XmlAttributePropertyTest.class));
    suite.addTest(createSingleSuite(PropertyTest.class));
    suite.addTest(createSingleSuite(StaticFieldPropertyEditorTest.class));
    suite.addTest(createSingleSuite(EnumPropertyEditorTest.class));
    suite.addTest(createSingleSuite(StringArrayPropertyEditorTest.class));
    suite.addTest(createSingleSuite(EventsPropertyTest.class));
    return suite;
  }
}