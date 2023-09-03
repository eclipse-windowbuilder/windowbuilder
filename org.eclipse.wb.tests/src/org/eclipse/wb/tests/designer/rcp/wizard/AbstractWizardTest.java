/*******************************************************************************
 * Copyright (c) 2023 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.rcp.wizard;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.wizards.project.NewProjectWizard;
import org.eclipse.wb.tests.designer.editor.DesignerEditorTestCase;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;

import org.junit.Before;

public abstract class AbstractWizardTest extends DesignerEditorTestCase {
	protected IPackageFragment m_packageFragment;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		if (m_testProject == null) {
			do_projectCreate();
			ReflectionUtils.getMethod(NewProjectWizard.class, "addRequiredLibraries", IJavaProject.class) //
					.invoke(null, m_javaProject);
		}
		m_packageFragment = m_testProject.getPackage("test");
	}
}
