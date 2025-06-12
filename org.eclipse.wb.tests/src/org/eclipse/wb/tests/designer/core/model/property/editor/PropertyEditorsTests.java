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
package org.eclipse.wb.tests.designer.core.model.property.editor;

import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests for {@link PropertyEditor}'s.
 *
 * @author scheglov_ke
 */
@Suite
@SelectClasses({
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
