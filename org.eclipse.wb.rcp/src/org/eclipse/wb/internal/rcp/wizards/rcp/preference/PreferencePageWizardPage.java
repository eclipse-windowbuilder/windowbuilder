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
package org.eclipse.wb.internal.rcp.wizards.rcp.preference;

import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.wizards.RcpWizardPage;
import org.eclipse.wb.internal.rcp.wizards.WizardsMessages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link WizardPage} that creates new RCP {@link PreferencePage}.
 *
 * @author lobas_av
 * @coverage rcp.wizards.ui
 */
public final class PreferencePageWizardPage extends RcpWizardPage {
	private final List<Button> m_buttons = new ArrayList<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PreferencePageWizardPage() {
		setTitle(WizardsMessages.PreferencePageWizardPage_title);
		setImageDescriptor(Activator.getImageDescriptor("wizard/PreferencePage/banner.gif"));
		setDescription(WizardsMessages.PreferencePageWizardPage_description);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// WizardPage
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
			throws CoreException {
		final String[] template = new String[1];
		getShell().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				for (Button button : m_buttons) {
					if (button.getSelection()) {
						template[0] = (String) button.getData();
						break;
					}
				}
			}
		});
		try {
			if ("FieldLayoutPreferencePage.jvt".equals(template[0])) {
				ProjectUtils.ensureResourceType(
						newType.getJavaProject(),
						Activator.getDefault().getBundle(),
						"org.eclipse.wb.swt.FieldLayoutPreferencePage");
			}
		} catch (Throwable e) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.PLUGIN_ID,
					IStatus.OK,
					"Error ensure org.eclipse.wb.swt.FieldLayoutPreferencePage",
					e));
		}
		InputStream file = Activator.getFile("templates/rcp/" + template[0]);
		fillTypeFromTemplate(newType, imports, monitor, file);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createLocalControls(Composite parent, int columns) {
		// create main container
		Composite baseComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.create(baseComposite).noMargins();
		GridDataFactory.create(baseComposite).fillH().grabH().spanH(columns);
		//
		final Button rcpInterfaceButton = new Button(baseComposite, SWT.CHECK);
		GridDataFactory.create(rcpInterfaceButton).fillH().grabH();
		rcpInterfaceButton.setText("Implement org.eclipse.ui.IWorkbenchPreferencePage interface");
		rcpInterfaceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (rcpInterfaceButton.getSelection()) {
					List<String> interfaces = new ArrayList<>();
					interfaces.add("org.eclipse.ui.IWorkbenchPreferencePage");
					setSuperInterfaces(interfaces, false);
				} else {
					setSuperInterfaces(Collections.EMPTY_LIST, false);
				}
			}
		});
		//
		new Label(baseComposite, SWT.NONE);
		// create message label
		Label label = new Label(baseComposite, SWT.NONE);
		label.setText(WizardsMessages.PreferencePageWizardPage_typeSelection);
		// create all buttons
		createButton(
				m_buttons,
				baseComposite,
				WizardsMessages.PreferencePageWizardPage_typeStandardLabel,
				WizardsMessages.PreferencePageWizardPage_typeStandardDescription,
				true,
				"org.eclipse.jface.preference.PreferencePage",
				"PreferencePage.jvt");
		createButton(
				m_buttons,
				baseComposite,
				WizardsMessages.PreferencePageWizardPage_typeFieldEditorLabel,
				WizardsMessages.PreferencePageWizardPage_typeFieldEditorDescription,
				false,
				"org.eclipse.jface.preference.FieldEditorPreferencePage",
				"FieldEditorPreferencePage.jvt");
		createButton(
				m_buttons,
				baseComposite,
				WizardsMessages.PreferencePageWizardPage_typeFieldLayoutLabel,
				WizardsMessages.PreferencePageWizardPage_typeFieldLayoutDescription,
				false,
				"org.eclipse.wb.swt.FieldLayoutPreferencePage",
				"FieldLayoutPreferencePage.jvt");
	}

	private void createButton(List<Button> buttons,
			Composite parent,
			String text,
			String tooltip,
			boolean selection,
			final String superClass,
			String template) {
		// create button
		final Button button = new Button(parent, SWT.RADIO);
		GridDataFactory.create(button).indentH(24);
		button.setText(text);
		button.setToolTipText(tooltip);
		button.setSelection(selection);
		button.setData(template);
		// create listener
		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (button.getSelection()) {
					if (superClass == null) {
						setSuperClass("java.lang.Object", true);
					} else {
						setSuperClass(superClass, true);
					}
				}
			}
		};
		button.addSelectionListener(listener);
		if (selection) {
			listener.widgetSelected(null);
		}
		// add to buttons
		buttons.add(button);
	}
}