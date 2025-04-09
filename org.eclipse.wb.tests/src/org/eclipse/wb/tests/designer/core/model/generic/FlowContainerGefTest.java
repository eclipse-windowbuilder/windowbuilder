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

import org.eclipse.wb.internal.core.model.generic.FlowContainer;

/**
 * Tests for "flow container" support, such as {@link FlowContainer} interface.
 *
 * @author scheglov_ke
 */
public class FlowContainerGefTest extends FlowContainerAbstractGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void prepareFlowPanel() throws Exception {
		FlowContainerModelTest.prepareFlowPanel();
	}
}
