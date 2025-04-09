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
package org.eclipse.wb.tests.designer.core.model.generic;

import org.eclipse.wb.internal.core.model.generic.SimpleContainer;

/**
 * Tests for "simple container" support, such as {@link SimpleContainer} interface.
 *
 * @author scheglov_ke
 */
public class SimpleContainerGefTest extends SimpleContainerAbstractGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void prepareSimplePanel() throws Exception {
		SimpleContainerModelTest.prepareSimplePanel();
	}
}
