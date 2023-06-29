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
package org.eclipse.wb.internal.rcp.model.jface;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.IJavaInfoRendering;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.IExceptionConstants;
import org.eclipse.wb.internal.swt.support.ControlSupport;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import net.bytebuddy.ByteBuddy;

/**
 * Model for {@link WizardPage}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class WizardPageInfo extends DialogPageInfo implements IJavaInfoRendering {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public WizardPageInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		JavaInfoUtils.scheduleSpecialRendering(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Rendering
	//
	////////////////////////////////////////////////////////////////////////////
	private static Shell m_parentShell;
	private WizardDialog m_wizardDialog;

	@Override
	public void render() throws Exception {
		ClassLoader editorLoader = JavaInfoUtils.getClassLoader(this);
		// prepare Wizard
		Wizard wizard;
		{
			Class<?> wizardClass = editorLoader.loadClass("org.eclipse.jface.wizard.Wizard");
			wizard = (Wizard) new ByteBuddy() //
					.subclass(wizardClass) //
					.make() //
					.load(editorLoader) //
					.getLoaded() //
					.getConstructor() //
					.newInstance();
		}
		// add this WizardPage
		wizard.addPage((WizardPage) getObject());
		// prepare parent Shell for WizardDialog
		if (m_parentShell == null) {
			m_parentShell = new Shell();
		}
		// create WizardDialog
		m_wizardDialog = new WizardDialog(m_parentShell, wizard);
		// open WizardDialog, so perform WizardPage GUI creation
		try {
			m_wizardDialog.create();
			if (System.getProperty("__wbp_WizardPage_simulateException") != null) {
				throw new RuntimeException("Simulated exception");
			}
		} catch (Throwable e) {
			m_wizardDialog.close();
			throwBetterException_forNoControl(e);
			throw ReflectionUtils.propagate(e);
		}
		m_shell = m_wizardDialog.getShell();
	}

	/**
	 * Some not-so-smart users may try to edit {@link WizardPage} without {@link Control}.
	 */
	private static void throwBetterException_forNoControl(Throwable e) {
		if (DesignerExceptionUtils.hasTraceElementsSequence(e, new String[][]{
			{"org.eclipse.core.runtime.Assert", "isNotNull"},
			{"org.eclipse.jface.wizard.Wizard", "createPageControls"}})) {
			throw new DesignerException(IExceptionConstants.NO_CONTROL_IN_WIZARD_PAGE);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void refresh_dispose() throws Exception {
		// dispose WizardDialog
		if (m_wizardDialog != null) {
			if (m_shell != null && !ControlSupport.isDisposed(m_shell)) {
				m_wizardDialog.close();
			}
			m_wizardDialog = null;
			m_shell = null;
		}
		// call "super"
		super.refresh_dispose();
	}
}
