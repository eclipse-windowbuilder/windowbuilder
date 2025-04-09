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
package org.eclipse.wb.tests.designer.rcp.swing2swt;

import org.eclipse.wb.internal.rcp.swing2swt.Activator;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.core.runtime.FileLocator;

import org.osgi.framework.Bundle;

/**
 * Abstract test for Swing2SWT.
 *
 * @author scheglov_ke
 */
public abstract class AbstractSwing2SwtTest extends RcpModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configureNewProject() throws Exception {
		super.configureNewProject();
		{
			Bundle bundle = Activator.getBundleStatic();
			String path = FileLocator.toFileURL(bundle.getEntry("/swing2swt.jar")).getPath();
			m_testProject.addExternalJar(path);
		}
	}
}