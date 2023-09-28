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
package org.eclipse.wb.internal.swt.model.property.editor.font;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ReusableDialog;
import org.eclipse.wb.internal.swt.model.ModelMessages;
import org.eclipse.wb.internal.swt.model.jface.resource.FontRegistryInfo;
import org.eclipse.wb.internal.swt.model.jface.resource.RegistryContainerInfo;
import org.eclipse.wb.internal.swt.support.JFaceSupport;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for {@link Font} choosing.
 *
 * @author lobas_av
 * @coverage swt.property.editor
 */
public final class FontDialog extends ReusableDialog {
	private final JavaInfo m_javaInfo;
	private FontInfo m_fontInfo;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FontDialog(Shell parentShell, JavaInfo javaInfo) {
		super(parentShell);
		m_javaInfo = javaInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public void disposeFont() {
		if (m_fontInfo != null) {
			m_fontInfo.dispose();
			m_fontInfo = null;
		}
	}

	/**
	 * @return the selected {@link FontInfo}.
	 */
	public FontInfo getFontInfo() {
		return m_fontInfo;
	}

	/**
	 * Sets the selected {@link FontInfo}.
	 */
	public void setFontInfo(FontInfo fontInfo) {
		disposeFont();
		m_fontInfo = fontInfo;
		updateGUI();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	private FontPreviewCanvas m_previewCanvas;
	private TabFolder m_tabFolder;
	private final List<AbstractFontPage> m_pages = new ArrayList<>();

	//
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		// create preview
		{
			Group previewGroup = new Group(area, SWT.NONE);
			GridDataFactory.create(previewGroup).grabH().fillH();
			GridLayoutFactory.create(previewGroup);
			previewGroup.setText(ModelMessages.FontDialog_previewGroup);
			//
			m_previewCanvas = new FontPreviewCanvas(previewGroup, SWT.NONE);
			GridDataFactory.create(m_previewCanvas).grab().fill();
		}
		// create folder for pages
		{
			m_tabFolder = new TabFolder(area, SWT.NONE);
			GridDataFactory.create(m_tabFolder).grab().fill();
			addPages(m_tabFolder);
			setPageSelection();
		}
		//
		return area;
	}

	@Override
	protected void onBeforeOpen() {
		updateGUI();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(ModelMessages.FontDialog_dialogTitle);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Updates GUI using current {@link FontInfo}.
	 */
	private void updateGUI() {
		if (getShell() != null) {
			// update preview
			m_previewCanvas.setFontInfo(m_javaInfo, m_fontInfo);
			// notify pages
			for (AbstractFontPage page : m_pages) {
				page.setFont(m_fontInfo);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Pages
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds new page with given title and {@link AbstractFontPage}.
	 */
	protected final void addPage(String title, AbstractFontPage page) {
		m_pages.add(page);
		TabItem tabItem = new TabItem(m_tabFolder, SWT.NONE);
		tabItem.setText(title);
		tabItem.setControl(page);
	}

	/**
	 * Adds pages with {@link AbstractFontPage}'s.
	 */
	protected void addPages(Composite parent) {
		addPage(ModelMessages.FontDialog_constructorPage, new ConstructionFontPage(m_javaInfo,
				parent,
				SWT.NONE,
				this));
		if (JFaceSupport.isAvialable()) {
			// FIXME removed to avoid JVM crash in eswt-converged.dll
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=201442
			//addPage(JFaceFontPage.NAME, new JFaceFontPage(parent, SWT.NONE, this, m_javaInfo));
			//
			try {
				List<FontRegistryInfo> registries =
						RegistryContainerInfo.getRegistries(m_javaInfo.getRootJava(), FontRegistryInfo.class);
				if (!registries.isEmpty()) {
					addPage(RegistryFontPage.NAME, new RegistryFontPage(parent, SWT.NONE, this, registries));
				}
			} catch (Throwable e) {
				DesignerPlugin.log(e);
			}
		}
	}

	/**
	 * Sets tab-folder selection association with current font value.
	 */
	private void setPageSelection() {
		String pageId = m_fontInfo.getPageId();
		if (pageId != null) {
			int size = m_tabFolder.getItemCount();
			for (int i = 0; i < size; i++) {
				TabItem item = m_tabFolder.getItem(i);
				if (pageId.equals(item.getText())) {
					m_tabFolder.setSelection(new TabItem[]{item});
					break;
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Closes dialog with "OK" result.
	 */
	public final void closeOk() {
		buttonPressed(IDialogConstants.OK_ID);
	}
}