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
package org.eclipse.wb.tests.designer.core.model.property.editor;

import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for {@link PropertyEditor}'s.
 *
 * @author scheglov_ke
 */
public class PropertyEditorsTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.core.model.property.editor");
		// primitives
		suite.addTest(createSingleSuite(BooleanPropertyEditorTest.class));
		suite.addTest(createSingleSuite(IntegerPropertyEditorTest.class));
		// arrays
		suite.addTest(createSingleSuite(IntegerArrayPropertyEditorTest.class));
		// wrappers
		suite.addTest(createSingleSuite(BooleanObjectPropertyEditorTest.class));
		suite.addTest(createSingleSuite(ByteObjectPropertyEditorTest.class));
		suite.addTest(createSingleSuite(ShortObjectPropertyEditorTest.class));
		suite.addTest(createSingleSuite(IntegerObjectPropertyEditorTest.class));
		suite.addTest(createSingleSuite(LongObjectPropertyEditorTest.class));
		suite.addTest(createSingleSuite(DoubleObjectPropertyEditorTest.class));
		// other
		suite.addTest(createSingleSuite(StaticFieldPropertyEditorTest.class));
		suite.addTest(createSingleSuite(EnumCustomPropertyEditorTest.class));
		suite.addTest(createSingleSuite(EnumPropertyEditorTest.class));
		suite.addTest(createSingleSuite(InnerClassPropertyEditorTest.class));
		suite.addTest(createSingleSuite(ConstantSelectionPropertyEditorTest.class));
		suite.addTest(createSingleSuite(StringsAddPropertyTest.class));
		suite.addTest(createSingleSuite(LocalePropertyEditorTest.class));
		suite.addTest(createSingleSuite(StylePropertyEditorTest.class));
		suite.addTest(createSingleSuite(StylePropertyEditorLongTest.class));
		suite.addTest(createSingleSuite(DisplayExpressionPropertyEditorTest.class));
		suite.addTest(createSingleSuite(StringListPropertyEditorTest.class));
		suite.addTest(createSingleSuite(InstanceListPropertyEditorTest.class));
		suite.addTest(createSingleSuite(ExpressionListPropertyEditorTest.class));
		suite.addTest(createSingleSuite(InstanceObjectPropertyEditorTest.class));
		suite.addTest(createSingleSuite(ObjectPropertyEditorTest.class));
		suite.addTest(createSingleSuite(DatePropertyEditorTest.class));
		suite.addTest(createSingleSuite(EnumerationValuesPropertyEditorTest.class));
		return suite;
	}
}
