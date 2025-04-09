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
package org.eclipse.wb.tests.designer.databinding.rcp.model;

import org.eclipse.wb.internal.core.databinding.model.reference.CompoundReferenceProvider;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author lobas_av
 *
 */
public class ReferenceProvidersTest extends Assert {
	@Test
	public void test_StringReferenceProvider() throws Exception {
		StringReferenceProvider referenceProvider = new StringReferenceProvider("test");
		assertEquals("test", referenceProvider.getReference());
	}

	@Test
	public void test_CompoundReferenceProvider() throws Exception {
		CompoundReferenceProvider referenceProvider =
				new CompoundReferenceProvider(new StringReferenceProvider("test"), ".test");
		assertEquals("test.test", referenceProvider.getReference());
	}
}