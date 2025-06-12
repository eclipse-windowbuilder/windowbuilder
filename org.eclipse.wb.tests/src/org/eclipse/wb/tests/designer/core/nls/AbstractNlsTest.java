/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.core.nls;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.nls.NlsSupport;
import org.eclipse.wb.internal.core.nls.SourceDescription;
import org.eclipse.wb.internal.core.nls.bundle.eclipse.old.EclipseSource;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.jupiter.api.AfterEach;

/**
 * Abstract test for NLS.
 *
 * @author scheglov_ke
 */
public abstract class AbstractNlsTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@AfterEach
	public void tearDown() throws Exception {
		// process UI messages (without this we have exception from Java UI)
		waitEventLoop(1);
		//
		super.tearDown();
		if (m_testProject != null) {
			deleteFiles(m_testProject.getJavaProject().getProject().getFolder("src"));
			waitForAutoBuild();
		}
	}

	/**
	 * Returns the {@link EclipseSource} source description for the given component.
	 */
	protected static SourceDescription getSourceDescription(JavaInfo component) throws Exception {
		for (SourceDescription sourceDescription : NlsSupport.getSourceDescriptions(component)) {
			if (sourceDescription.getSourceClass() == EclipseSource.class) {
				return sourceDescription;
			}
		}
		fail();
		return null;
	}
}
