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
package org.eclipse.wb.internal.rcp.wizards.rcp;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils;
import org.eclipse.wb.internal.rcp.wizards.RcpWizardPage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Abstract {@link WizardPage} for RCP elements - views, editors, perspectives.
 *
 * @author lobas_av
 * @coverage rcp.wizards.ui
 */
public abstract class RcpPartWizardPage extends RcpWizardPage {
	private Text m_nameText;
	protected PdeUtils m_pdeUtils;
	protected String m_newTypeClassName;

	////////////////////////////////////////////////////////////////////////////
	//
	// Initialize
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void initTypePage(IJavaElement element) {
		super.initTypePage(element);
		if (element != null) {
			m_pdeUtils = PdeUtils.get(element.getJavaProject().getProject());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// WizardPage
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
			throws CoreException {
		m_newTypeClassName = newType.getFullyQualifiedName();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Additional control
	//
	////////////////////////////////////////////////////////////////////////////
	protected final void createLocalControls(Composite parent,
			int numColumns,
			String nameLabel,
			String nameValue) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.create(composite).columns(2).noMargins();
		GridDataFactory.create(composite).fillH().grabH().spanH(numColumns);
		// title
		Label label = new Label(composite, SWT.NONE);
		label.setText(nameLabel);
		// text
		m_nameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.create(m_nameText).fillH().grabH();
		m_nameText.setText(nameValue);
	}

	protected final String getNameText() {
		final String[] text = new String[1];
		getShell().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				text[0] = m_nameText.getText();
			}
		});
		return text[0];
	}
}