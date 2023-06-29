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
package org.eclipse.wb.internal.core.xml.editor;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * Manages the installation/deinstallation of global actions for multi-page editors. Responsible for
 * the redirection of global actions to the active editor. Multi-page contributor replaces the
 * contributors for the individual editors in the multi-page editor.
 *
 * @author scheglov_ke
 * @coverage XML.editor
 */
public class AbstractXmlEditorContributor extends MultiPageEditorActionBarContributor {
	private IEditorPart m_activeEditor;
	private AbstractXmlEditor m_designerEditor;

	////////////////////////////////////////////////////////////////////////////
	//
	// MultiPageEditorActionBarContributor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setActiveEditor(IEditorPart part) {
		if (part instanceof MultiPageEditorPart) {
			m_designerEditor = (AbstractXmlEditor) part;
		}
		super.setActiveEditor(part);
	}

	@Override
	public void setActivePage(IEditorPart part) {
		if (m_activeEditor == part) {
			return;
		}
		m_activeEditor = part;
		// update IActionBars
		IActionBars actionBars = getActionBars();
		if (actionBars != null) {
			redirectTextActions(actionBars);
		}
	}

	/**
	 * Configures {@link IActionBars} to use global action handlers from {@link ITextEditor}.
	 */
	private void redirectTextActions(IActionBars actionBars) {
		ITextEditor editor =
				m_activeEditor instanceof ITextEditor ? (ITextEditor) m_activeEditor : null;
		redirectTextAction(actionBars, editor, ITextEditorActionConstants.DELETE);
		redirectTextAction(actionBars, editor, ITextEditorActionConstants.CUT);
		redirectTextAction(actionBars, editor, ITextEditorActionConstants.COPY);
		redirectTextAction(actionBars, editor, ITextEditorActionConstants.PASTE);
		redirectTextAction(actionBars, editor, ITextEditorActionConstants.SELECT_ALL);
		redirectTextAction(actionBars, editor, ITextEditorActionConstants.FIND);
		redirectTextAction(actionBars, editor, ITextEditorActionConstants.DELETE_LINE);
		//redirectTextAction(actionBars, editor, ITextEditorActionConstants.DELETE_LINE_TO_BEGINNING);
		//redirectTextAction(actionBars, editor, ITextEditorActionConstants.DELETE_LINE_TO_END);
		redirectTextAction(actionBars, editor, IDEActionFactory.BOOKMARK.getId());
		// UNDO and REDO should be handled by XML editor
		if (m_activeEditor == null && m_designerEditor != null) {
			ITextEditor xmlEditor = m_designerEditor.getSourcePage().getXmlEditor();
			redirectTextAction(actionBars, xmlEditor, ITextEditorActionConstants.UNDO);
			redirectTextAction(actionBars, xmlEditor, ITextEditorActionConstants.REDO);
		}
		// update
		actionBars.updateActionBars();
	}

	/**
	 * Configures {@link IActionBars} to use global action handler from {@link ITextEditor}.
	 */
	private static void redirectTextAction(IActionBars actionBars, ITextEditor editor, String id) {
		actionBars.setGlobalActionHandler(id, getTextAction(editor, id));
	}

	/**
	 * @return the {@link IAction} registered with the given text {@link ITextEditor}, may be
	 *         <code>null</code>.
	 */
	private static IAction getTextAction(ITextEditor editor, String actionID) {
		return editor == null ? null : editor.getAction(actionID);
	}
}
