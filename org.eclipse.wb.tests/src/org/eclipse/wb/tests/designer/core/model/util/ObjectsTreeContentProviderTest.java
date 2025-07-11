/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.tests.designer.core.model.util;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.util.ObjectsTreeContentProvider;
import org.eclipse.wb.tests.designer.core.model.TestObjectInfo;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.jface.viewers.ITreeContentProvider;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

/**
 * Test for {@link ObjectsTreeContentProvider}.
 *
 * @author scheglov_ke
 */
public class ObjectsTreeContentProviderTest extends DesignerTestCase {
	@Test
	public void test() throws Exception {
		final TestObjectInfo parent = new TestObjectInfo("parent");
		final TestObjectInfo child_1 = new TestObjectInfo("child_1");
		final TestObjectInfo child_2 = new TestObjectInfo("child_2");
		parent.addChild(child_1);
		parent.addChild(child_2);
		// prepare ITreeContentProvider
		Predicate<ObjectInfo> predicate = t -> t != child_2;
		ITreeContentProvider contentProvider = new ObjectsTreeContentProvider(predicate);
		// check ITreeContentProvider
		Assertions.assertThat(contentProvider.getElements(new Object[]{parent})).containsOnly(parent);
		Assertions.assertThat(contentProvider.getElements(parent)).containsOnly(child_1);
		assertTrue(contentProvider.hasChildren(parent));
		assertFalse(contentProvider.hasChildren(child_1));
		Assertions.assertThat(contentProvider.getChildren(parent)).containsOnly(child_1);
		assertSame(parent, contentProvider.getParent(child_1));
		// no implementation
		contentProvider.inputChanged(null, null, parent);
		contentProvider.dispose();
	}
}
