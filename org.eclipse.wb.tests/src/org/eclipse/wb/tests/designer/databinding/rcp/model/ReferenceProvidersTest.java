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