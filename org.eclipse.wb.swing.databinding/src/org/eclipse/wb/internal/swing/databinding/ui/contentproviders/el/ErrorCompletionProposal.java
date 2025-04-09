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
package org.eclipse.wb.internal.swing.databinding.ui.contentproviders.el;

import org.eclipse.wb.internal.swing.databinding.Activator;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * {@link ICompletionProposal} for display errors.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public final class ErrorCompletionProposal implements ICompletionProposal {
	private final String m_message;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ErrorCompletionProposal(Throwable e) {
		m_message = e.toString();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ICompletionProposal
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void apply(IDocument document) {
	}

	@Override
	public String getDisplayString() {
		return m_message;
	}

	@Override
	public Image getImage() {
		return Activator.getImage("errors.gif");
	}

	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

	@Override
	public String getAdditionalProposalInfo() {
		return null;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}
}