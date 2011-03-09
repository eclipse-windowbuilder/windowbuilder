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
package org.eclipse.wb.tests.designer.core.model;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.core.model.association.AssociationTests;
import org.eclipse.wb.tests.designer.core.model.description.DescriptionTests;
import org.eclipse.wb.tests.designer.core.model.generic.GenericTests;
import org.eclipse.wb.tests.designer.core.model.nonvisual.NonVisualBeansGefTest;
import org.eclipse.wb.tests.designer.core.model.nonvisual.NonVisualBeansTest;
import org.eclipse.wb.tests.designer.core.model.operations.AddTest;
import org.eclipse.wb.tests.designer.core.model.operations.DeleteTest;
import org.eclipse.wb.tests.designer.core.model.parser.ParserTests;
import org.eclipse.wb.tests.designer.core.model.property.PropertiesTests;
import org.eclipse.wb.tests.designer.core.model.util.UtilTests;
import org.eclipse.wb.tests.designer.core.model.variables.VariablesTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author scheglov_ke
 */
public class ModelTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.core.model");
    suite.addTest(createSingleSuite(ObjectInfoTest.class));
    suite.addTest(createSingleSuite(DefaultObjectPresentationTest.class));
    suite.addTest(createSingleSuite(ObjectReferenceInfoTest.class));
    suite.addTest(createSingleSuite(ClassLoadingTest.class));
    suite.addTest(DescriptionTests.suite());
    suite.addTest(createSingleSuite(JavaInfoTest.class));
    suite.addTest(createSingleSuite(DefaultJavaInfoPresentationTest.class));
    suite.addTest(createSingleSuite(AbstractComponentTest.class));
    suite.addTest(UtilTests.suite());
    suite.addTest(AssociationTests.suite());
    suite.addTest(createSingleSuite(AddTest.class));
    suite.addTest(createSingleSuite(DeleteTest.class));
    suite.addTest(PropertiesTests.suite());
    suite.addTest(ParserTests.suite());
    suite.addTest(VariablesTests.suite());
    suite.addTest(GenericTests.suite());
    suite.addTest(createSingleSuite(NonVisualBeansTest.class));
    suite.addTest(createSingleSuite(NonVisualBeansGefTest.class));
    suite.addTest(createSingleSuite(ArrayObjectTest.class));
    suite.addTest(createSingleSuite(WrapperInfoTest.class));
    suite.addTest(createSingleSuite(EllipsisObjectInfoTest.class));
    return suite;
  }
}
