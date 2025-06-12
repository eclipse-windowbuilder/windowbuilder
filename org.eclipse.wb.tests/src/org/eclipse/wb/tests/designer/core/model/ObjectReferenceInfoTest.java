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
package org.eclipse.wb.tests.designer.core.model;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.ObjectReferenceInfo;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link ObjectReferenceInfo}.
 *
 * @author scheglov_ke
 */
public class ObjectReferenceInfoTest extends DesignerTestCase {
	/**
	 * Can not create {@link ObjectReferenceInfo} for <code>null</code> {@link ObjectInfo}.
	 */
	@Test
	public void test_notNull() throws Exception {
		try {
			new TestObjectInfo(null);
			fail();
		} catch (Throwable e) {
		}
	}

	/**
	 * {@link ObjectReferenceInfo} redirects invocations to {@link ObjectInfo}.
	 */
	@Test
	public void test_redirect() throws Exception {
		TestObjectInfo object = new TestObjectInfo();
		ObjectReferenceInfo reference = new ObjectReferenceInfo(object);
		assertSame(object, reference.getObject());
		assertSame(object.getBroadcastSupport(), reference.getBroadcastSupport());
		assertSame(object.getPresentation(), reference.getPresentation());
	}
}
