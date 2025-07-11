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
package org.eclipse.wb.tests.designer.core.databinding;

import org.eclipse.wb.internal.core.databinding.ui.providers.ObjectsTreeContentProvider;
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.tests.designer.core.model.TestObjectInfo;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.apache.commons.lang3.ArrayUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test for {@link ObjectsTreeContentProvider}.
 *
 * @author lobas_av
 */
public class ObjectsTreeContentProviderTest extends DesignerTestCase {
	@Test
	public void test_input() throws Exception {
		ObjectsTreeContentProvider provider = new ObjectsTreeContentProvider();
		//
		assertSame(ArrayUtils.EMPTY_OBJECT_ARRAY, provider.getElements(null));
		assertSame(ArrayUtils.EMPTY_OBJECT_ARRAY, provider.getElements("test"));
		//
		TestObjectInfo testObject = new TestObjectInfo("testObject");
		//
		Assertions.assertThat(provider.getElements(new Object[]{testObject})).containsOnly(testObject);
		//
		List<TestObjectInfo> input = new ArrayList<>();
		input.add(testObject);
		Assertions.assertThat(provider.getElements(input)).containsOnly(testObject);
		//
		TestObjectInfo childObject = new TestObjectInfo("childObject");
		testObject.addChild(childObject);
		//
		Assertions.assertThat(provider.getElements(testObject)).containsOnly(childObject);
		// not used
		provider.inputChanged(null, null, null);
		provider.dispose();
	}

	@Test
	public void test_getParent() throws Exception {
		ObjectsTreeContentProvider provider = new ObjectsTreeContentProvider();
		//
		assertNull(provider.getParent(null));
		assertNull(provider.getParent("test"));
		//
		TestObjectInfo testObject = new TestObjectInfo("testObject");
		TestObjectInfo childObject = new TestObjectInfo("childObject");
		testObject.addChild(childObject);
		//
		assertNull(provider.getParent(testObject));
		assertSame(testObject, provider.getParent(childObject));
	}

	@Test
	public void test_hasChildren() throws Exception {
		ObjectsTreeContentProvider provider = new ObjectsTreeContentProvider();
		//
		assertFalse(provider.hasChildren(null));
		assertFalse(provider.hasChildren("test"));
		//
		TestObjectInfo testObject = new TestObjectInfo("testObject");
		//
		assertFalse(provider.hasChildren(testObject));
		//
		TestObjectInfo childObject = new TestObjectInfo("childObject");
		testObject.addChild(childObject);
		//
		assertTrue(provider.hasChildren(testObject));
		//
		TestObjectInfo testObjectPresentation = new TestObjectInfo("testObjectPresentation") {
			@Override
			public IObjectPresentation getPresentation() {
				return new DefaultObjectPresentation(this) {
					@Override
					public String getText() throws Exception {
						return "testObjectPresentation";
					}

					@Override
					public boolean isVisible() throws Exception {
						return false;
					}
				};
			}
		};
		testObjectPresentation.addChild(new TestObjectInfo("childObject"));
		//
		assertFalse(provider.hasChildren(testObjectPresentation));
	}

	@Test
	public void test_getChildren() throws Exception {
		ObjectsTreeContentProvider provider = new ObjectsTreeContentProvider();
		//
		assertSame(ArrayUtils.EMPTY_OBJECT_ARRAY, provider.getChildren(null));
		assertSame(ArrayUtils.EMPTY_OBJECT_ARRAY, provider.getChildren("test"));
		//
		TestObjectInfo testObject = new TestObjectInfo("testObject");
		//
		Assertions.assertThat(provider.getChildren(testObject)).isNotNull().isEmpty();
		//
		TestObjectInfo childObject = new TestObjectInfo("childObject");
		testObject.addChild(childObject);
		//
		Assertions.assertThat(provider.getChildren(testObject)).containsOnly(childObject);
	}
}