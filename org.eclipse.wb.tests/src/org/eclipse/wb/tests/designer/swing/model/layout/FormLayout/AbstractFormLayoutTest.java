/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.swing.model.layout.FormLayout;

import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.swing.FormLayout.model.FormSizeConstantInfo;
import org.eclipse.wb.internal.swing.laf.LafSupport;
import org.eclipse.wb.tests.designer.swing.model.layout.AbstractLayoutTest;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.forms.util.AbstractUnitConverter;
import com.jgoodies.forms.util.DefaultUnitConverter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.awt.Component;

/**
 * Abstract test for {@link FormLayout}.
 *
 * @author scheglov_ke
 */
public abstract class AbstractFormLayoutTest extends AbstractLayoutTest {
	protected boolean m_useFormsImports = true;

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@BeforeAll
	public static void setUpUnitConverter() {
		FormSizeConstantInfo.setUnitConverter(new UnitConverterStub());
		Sizes.setUnitConverter(new UnitConverterStub());
	}

	@AfterAll
	public static void tearDownUnitConverter() {
		Sizes.setUnitConverter(DefaultUnitConverter.getInstance());
		FormSizeConstantInfo.setUnitConverter(DefaultUnitConverter.getInstance());
	}

	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		LafSupport.applySelectedLAF(LafSupport.getDefaultLAF());
	}

	@Override
	protected void configureNewProject() throws Exception {
		super.configureNewProject();
		do_configureNewProject();
	}

	static void do_configureNewProject() throws Exception {
		m_testProject.addPlugin("com.jgoodies.common");
		m_testProject.addPlugin("com.jgoodies.forms");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getTestSource(String... lines) {
		if (m_useFormsImports) {
			lines =
					CodeUtils.join(new String[]{
							"import com.jgoodies.forms.layout.*;",
					"import com.jgoodies.forms.factories.*;"}, lines);
		}
		return super.getTestSource(lines);
	}

	private static class UnitConverterStub extends AbstractUnitConverter {
		private static final long serialVersionUID = 1L;

		@Override
		protected double getDialogBaseUnitsX(Component component) {
			return 6;
		}

		@Override
		protected double getDialogBaseUnitsY(Component component) {
			return 12;
		}

		@Override
		protected int getScreenResolution(Component c) {
			return 96;
		}
	}
}
