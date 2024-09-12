/*******************************************************************************
 * Copyright (c) 2024 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.swtbot.designer;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.wizards.project.NewProjectWizard;
import org.eclipse.wb.tests.designer.core.AbstractJavaProjectTest;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.ui.PlatformUI;

/**
 * Abstract base class for all JUnit tests using the SWTBot. This class
 * initializes the workspace with the {@code Resource} perspective and an empty
 * test project. After each test run, all Java files are removed from the
 * project again.
 */
public abstract class AbstractSWTBotTest extends AbstractJavaProjectTest {
	protected SWTWorkbenchBot bot;

	@Override
	public void setUp() throws Exception {
		if (Display.getCurrent() != null) {
			fail("""
					SWTBot test needs to run in a non-UI thread.
					Make sure that "Run in UI thread" is unchecked in your launch configuration or that useUIThread is set to false in the pom.xml
					""");
		}
		PlatformUI.getWorkbench().getDisplay().syncCall(this::doSetUp);

		bot = new SWTWorkbenchBot();
		bot.perspectiveByLabel("Resource").activate();
	}

	protected Void doSetUp() throws Exception {
		super.setUp();
		if (m_testProject == null) {
			do_projectCreate();
			ReflectionUtils.getMethod(NewProjectWizard.class, "addRequiredLibraries", IJavaProject.class) //
					.invoke(null, m_javaProject);
		}
		return null;
	}

	@Override
	public void tearDown() throws Exception {
		PlatformUI.getWorkbench().getDisplay().syncCall(this::doTearDown);
	}

	protected Void doTearDown() throws Exception {
		if (m_testProject != null) {
			m_testProject.getProject().accept(resource -> {
				if (resource instanceof IFile file && "java".equals(file.getFileExtension())) {
					resource.delete(true, null);
					return false;
				}
				return true;
			});
		}
		return null;
	}
}
