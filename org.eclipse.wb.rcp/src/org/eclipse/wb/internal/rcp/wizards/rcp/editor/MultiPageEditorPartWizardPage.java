/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.rcp.wizards.rcp.editor;

import org.eclipse.wb.internal.core.utils.dialogfields.IListAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.ListDialogField;
import org.eclipse.wb.internal.core.utils.jdt.ui.JdtUiUtils;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.wizards.WizardsMessages;
import org.eclipse.wb.internal.rcp.wizards.rcp.RcpPartWizardPage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.MultiPageEditorPart;

import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.List;

/**
 * {@link WizardPage} that creates new JFace {@link MultiPageEditorPart}.
 *
 * @author scheglov_ke
 * @coverage rcp.wizards.ui
 */
public final class MultiPageEditorPartWizardPage extends RcpPartWizardPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MultiPageEditorPartWizardPage() {
		setTitle(WizardsMessages.MultiPageEditorPartWizardPage_title);
		setImageDescriptor(Activator.getImageDescriptor("wizard/MultiPageEditorPart/banner.gif"));
		setDescription(WizardsMessages.MultiPageEditorPartWizardPage_description);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// WizardPage
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String performSubstitutions(String code, ImportsManager imports) {
		code = super.performSubstitutions(code, imports);
		code = StringUtils.replace(code, "%EDITOR_ID%", m_newTypeClassName);
		return code;
	}

	@Override
	protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
			throws CoreException {
		super.createTypeMembers(newType, imports, monitor);
		{
			InputStream file = Activator.getFile("templates/rcp/MultiPageEditorPart.jvt");
			fillTypeFromTemplate(newType, imports, monitor, file);
		}
		addPageInvocations(newType, imports);
		addEditorContribution();
	}

	private void addPageInvocations(IType newType, ImportsManager imports) throws JavaModelException {
		IBuffer buffer = newType.getCompilationUnit().getBuffer();
		// prepare addPage() invocations
		String addPagesSource = "";
		for (IType page : getSelectedPages()) {
			String simpleName = imports.addImport(page.getFullyQualifiedName());
			addPagesSource += "addPage(new " + simpleName + "(), (org.eclipse.ui.IEditorInput) null);";
		}
		// insert addPage() invocations, don't care about formatting
		String pattern = "int pages;";
		int pagesOffset = buffer.getContents().indexOf(pattern);
		buffer.replace(pagesOffset, pattern.length(), addPagesSource);
		// if no pages, remove "try" block
		if (addPagesSource.length() == 0) {
			String contents = buffer.getContents();
			int tryBegin = contents.indexOf("try {");
			int tryEnd = contents.indexOf("}", tryBegin + 1);
			tryEnd = contents.indexOf("}", tryEnd + 1);
			buffer.replace(tryBegin, tryEnd - tryBegin + 1, "");
		}
	}

	private List<IType> getSelectedPages() {
		return m_pagesField.getElements();
	}

	private void addEditorContribution() throws CoreException {
		if (m_pdeUtils != null) {
			try {
				m_pdeUtils.createEditorElement(m_newTypeClassName, getNameText(), m_newTypeClassName);
			} catch (Throwable e) {
				throw new CoreException(new Status(IStatus.ERROR,
						Activator.PLUGIN_ID,
						IStatus.OK,
						WizardsMessages.MultiPageEditorPartWizardPage_errorPluginXml,
						e));
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void initTypePage(IJavaElement elem) {
		super.initTypePage(elem);
		setSuperClass("org.eclipse.ui.part.MultiPageEditorPart", true);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// More GUI
	//
	////////////////////////////////////////////////////////////////////////////
	private ListDialogField<IType> m_pagesField;

	@Override
	protected void createLocalControls(Composite parent, int columns) {
		createLocalControls(
				parent,
				columns,
				WizardsMessages.MultiPageEditorPartWizardPage_editorName,
				WizardsMessages.MultiPageEditorPartWizardPage_newEditorPart);
		createPagesField(parent, columns);
	}

	private void createPagesField(Composite parent, int columns) {
		String[] buttonLabels =
				new String[]{
						WizardsMessages.MultiPageEditorPartWizardPage_addButton,
						WizardsMessages.MultiPageEditorPartWizardPage_removeButton,
						WizardsMessages.MultiPageEditorPartWizardPage_upButton,
						WizardsMessages.MultiPageEditorPartWizardPage_downButton};
		IListAdapter<IType> adapter = new IListAdapter<>() {
			@Override
			public void customButtonPressed(ListDialogField<IType> field, int index) {
				if (index == 0) {
					Shell shell = getShell();
					IType page =
							JdtUiUtils.selectSubType(shell, getJavaProject(), "org.eclipse.ui.IEditorPart");
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
		m_pagesField.setLabelText(WizardsMessages.MultiPageEditorPartWizardPage_pagesList);
		m_pagesField.setTableColumns(new ListDialogField.ColumnsDescription(1, false));
		m_pagesField.setRemoveButtonIndex(1);
		m_pagesField.setUpButtonIndex(2);
		m_pagesField.setDownButtonIndex(3);
		m_pagesField.doFillIntoGrid(parent, columns);
	}
}