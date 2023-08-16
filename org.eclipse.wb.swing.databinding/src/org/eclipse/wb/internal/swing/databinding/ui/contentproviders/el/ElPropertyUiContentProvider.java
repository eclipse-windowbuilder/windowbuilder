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
package org.eclipse.wb.internal.swing.databinding.ui.contentproviders.el;

import org.eclipse.wb.internal.core.databinding.ui.editor.ICompleteListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Editor for {@code EL} properties.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public abstract class ElPropertyUiContentProvider
implements
IUiContentProvider,
IBeanPropertiesSupport {
	private final ElPropertyUiConfiguration m_configuration;
	private ICompleteListener m_listener;
	private boolean m_enabled;
	private String m_errorMessage;
	private Label m_titleLabel;
	protected SourceViewer m_sourceViewer;
	private final IDocument m_document = new Document();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ElPropertyUiContentProvider(ElPropertyUiConfiguration configuration) {
		m_configuration = configuration;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	protected final String getText() {
		return m_document.get();
	}

	protected final void setText(String text) {
		m_document.set(text);
	}

	protected final void setEnabled(boolean enabled) {
		m_enabled = enabled;
		if (m_titleLabel != null) {
			m_titleLabel.setEnabled(m_enabled);
		}
		if (m_sourceViewer != null) {
			m_sourceViewer.getControl().setEnabled(m_enabled);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Complete
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setCompleteListener(ICompleteListener listener) {
		m_listener = listener;
	}

	@Override
	public String getErrorMessage() {
		return m_errorMessage;
	}

	protected final void setErrorMessage(String message) {
		m_errorMessage = message;
		if (m_listener != null) {
			m_listener.calculateFinish();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int getNumberOfControls() {
		return 2;
	}

	@Override
	public void createContent(Composite parent, int columns) {
		m_titleLabel = new Label(parent, SWT.NONE);
		GridDataFactory.create(m_titleLabel).alignHF();
		m_titleLabel.setText(m_configuration.getTitle());
		m_titleLabel.setEnabled(m_enabled);
		//
		m_sourceViewer = new SourceViewer(parent, null, SWT.BORDER);
		GridDataFactory.create(m_sourceViewer.getControl()).spanH(columns - 1).hintVC(
				m_configuration.getRows()).fill();
		new EvalutionLanguageConfiguration(m_sourceViewer, m_document, m_configuration, this);
		m_sourceViewer.getControl().setEnabled(m_enabled);
	}
}