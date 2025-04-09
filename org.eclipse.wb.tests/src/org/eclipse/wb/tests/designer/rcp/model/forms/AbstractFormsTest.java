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
package org.eclipse.wb.tests.designer.rcp.model.forms;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import junit.framework.TestCase;

/**
 * Abstract {@link TestCase} for all "Forms API" tests.
 *
 * @author scheglov_ke
 */
public abstract class AbstractFormsTest extends RcpModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the <code>FormToolkit</code> instance from last parsed {@link JavaInfo}.
	 */
	protected final InstanceFactoryInfo getToolkit() throws Exception {
		return InstanceFactoryInfo.getFactories(
				m_lastParseInfo,
				m_lastLoader.loadClass("org.eclipse.ui.forms.widgets.FormToolkit")).get(0);
	}
}