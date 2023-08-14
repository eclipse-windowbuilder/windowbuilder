/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.wizards.jface.wizard;

import org.eclipse.wb.internal.core.utils.dialogfields.IListAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.ListDialogField;
import org.eclipse.wb.internal.core.utils.jdt.ui.JdtUiUtils;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.wizards.RcpWizardPage;
import org.eclipse.wb.internal.rcp.wizards.WizardsMessages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import java.io.InputStream;
import java.util.List;

/**
 * {@link WizardPage} that creates new JFace {@link Wizard}.
 *
 * @author scheglov_ke
 * @coverage rcp.wizards.ui
 */
public final class WizardWizardPage extends RcpWizardPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public WizardWizardPage() {
		setTitle(WizardsMessages.WizardWizardPage_title);
		setImageDescriptor(Activator.getImageDescriptor("wizard/JFace/Wizard/banner.gif"));
		setDescription(WizardsMessages.WizardWizardPage_description);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// WizardPage
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
			throws CoreException {
		{
			InputStream file = Activator.getFile("templates/jface/Wizard.jvt");
			fillTypeFromTemplate(newType, imports, monitor, file);
		}
		// add pages
		addPageInvocations(newType, imports);
	}

	private void addPageInvocations(IType newType, ImportsManager imports) throws JavaModelException {
		IBuffer buffer = newType.getCompilationUnit().getBuffer();
		// prepare addPage() invocations
		String addPagesSource = "";
		for (IType page : getSelectedPages()) {
			String simpleName = imports.addImport(page.getFullyQualifiedName());
			addPagesSource += "addPage(new " + simpleName + "());";
		}
		// prepare position for addPage() invocations
		int pagesOffset;
		{
			IMethod pagesMethod = newType.getMethod("addPages", new String[0]);
			pagesOffset = pagesMethod.getSourceRange().getOffset();
			pagesOffset = buffer.getContents().indexOf('{', pagesOffset) + 1;
		}
		// insert addPage() invocations, don't care about formatting
		buffer.replace(pagesOffset, 0, addPagesSource);
	}

	private List<IType> getSelectedPages() {
		return m_pagesField.getElements();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void initTypePage(IJavaElement elem) {
		super.initTypePage(elem);
		setSuperClass("org.eclipse.jface.wizard.Wizard", true);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// More GUI
	//
	////////////////////////////////////////////////////////////////////////////
	private ListDialogField<IType> m_pagesField;

	@Override
	protected void createLocalControls(Composite parent, int columns) {
		String[] buttonLabels =
				new String[]{
						WizardsMessages.WizardWizardPage_addButton,
						WizardsMessages.WizardWizardPage_removeButton,
						WizardsMessages.WizardWizardPage_upButton,
						WizardsMessages.WizardWizardPage_downButton};
		IListAdapter<IType> adapter = new IListAdapter<>() {
			@Override
			public void customButtonPressed(ListDialogField<IType> field, int index) {
				if (index == 0) {
					Shell shell = getShell();
					IType page =
							JdtUiUtils.selectSubType(
									shell,
									getJavaProject(),
									"org.eclipse.jface.wizard.WizardPage");
					shell.setFocus();
					if (page != null) {
						m_pagesField.addElement(page);
					}
				}
			}

			@Override
			public void doubleClicked(ListDialogField<IType> field) {
			}

			@Override
			public void selectionChanged(ListDialogField<IType> field) {
			}
		};
		LabelProvider labelProvider = new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IType) element).getFullyQualifiedName();
			}
		};
		m_pagesField = new ListDialogField<>(adapter, buttonLabels, labelProvider);
		m_pagesField.setLabelText(WizardsMessages.WizardWizardPage_pagesList);
		m_pagesField.setTableColumns(new ListDialogField.ColumnsDescription(1, false));
		m_pagesField.setRemoveButtonIndex(1);
		m_pagesField.setUpButtonIndex(2);
		m_pagesField.setDownButtonIndex(3);
		m_pagesField.doFillIntoGrid(parent, columns);
	}
}