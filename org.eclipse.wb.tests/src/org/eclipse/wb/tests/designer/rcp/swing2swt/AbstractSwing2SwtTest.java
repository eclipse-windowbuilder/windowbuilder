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