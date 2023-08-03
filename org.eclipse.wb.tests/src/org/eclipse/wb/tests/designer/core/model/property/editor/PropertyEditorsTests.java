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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for {@link PropertyEditor}'s.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		// primitives
		BooleanPropertyEditorTest.class,
		IntegerPropertyEditorTest.class,
		// arrays
		IntegerArrayPropertyEditorTest.class,
		// wrappers
		BooleanObjectPropertyEditorTest.class,
		ByteObjectPropertyEditorTest.class,
		ShortObjectPropertyEditorTest.class,
		IntegerObjectPropertyEditorTest.class,
		LongObjectPropertyEditorTest.class,
		DoubleObjectPropertyEditorTest.class,
		// other
		StaticFieldPropertyEditorTest.class,
		EnumCustomPropertyEditorTest.class,
		EnumPropertyEditorTest.class,
		InnerClassPropertyEditorTest.class,
		ConstantSelectionPropertyEditorTest.class,
		StringsAddPropertyTest.class,
		LocalePropertyEditorTest.class,
		StylePropertyEditorTest.class,
		StylePropertyEditorLongTest.class,
		DisplayExpressionPropertyEditorTest.class,
		StringListPropertyEditorTest.class,
		InstanceListPropertyEditorTest.class,
		ExpressionListPropertyEditorTest.class,
		InstanceObjectPropertyEditorTest.class,
		ObjectPropertyEditorTest.class,
		DatePropertyEditorTest.class,
		EnumerationValuesPropertyEditorTest.class
})
public class PropertyEditorsTests {
}
