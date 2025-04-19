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
package org.eclipse.wb.internal.core.editor.actions.errors;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.EditorState.BadNodeInformation;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;

/**
 * Implementation of {@link IErrorPage} for displaying {@link EditorWarning} from
 * {@link EditorState}.
 *
 * @author scheglov_ke
 * @coverage core.editor.action.error
 */
public final class WarningsErrorPage implements IErrorPage {
	private AstEditor m_editor;
	private java.util.List<EditorWarning> m_collection;

	////////////////////////////////////////////////////////////////////////////
	//
	// IErrorPage
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getTitle() {
		return Messages.WarningsErrorPage_title;
	}

	@Override
	public final void setRoot(ObjectInfo rootObject) {
		if (rootObject instanceof JavaInfo javaInfo) {
			m_editor = javaInfo.getEditor();
			EditorState editorState = EditorState.get(m_editor);
			m_collection = editorState.getWarnings();
		} else {
			m_collection = null;
		}
	}

	@Override
	public final boolean hasErrors() {
		return m_collection != null && !m_collection.isEmpty();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	private List m_nodesList;
	private Browser m_browser;

	@Override
	public final Control create(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.create(container);
		// create List with bad nodes
		{
			Group group = new Group(container, SWT.NONE);
			GridDataFactory.create(group).grabH().fill();
			GridLayoutFactory.create(group);
			group.setText(Messages.WarningsErrorPage_listLabel);
			//
			m_nodesList = new List(group, SWT.BORDER | SWT.V_SCROLL);
			GridDataFactory.create(m_nodesList).hintC(100, 10).grab().fill();
			// fill items
			if (m_collection != null) {
				for (EditorWarning warning : m_collection) {
					try {
						m_nodesList.add(warning.getMessage());
					} catch (Throwable e) {
						DesignerPlugin.log(e);
					}
				}
			}
			// add selection listener
			m_nodesList.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					showException();
				}
			});
		}
		// create Text for displaying selected bad node
		{
			Group group = new Group(container, SWT.NONE);
			GridDataFactory.create(group).grab().fill();
			GridLayoutFactory.create(group);
			group.setText(Messages.WarningsErrorPage_singleLabel);
			//
			m_browser = new Browser(group, SWT.BORDER);
			GridDataFactory.create(m_browser).hintC(100, 15).grab().fill();
		}
		// show first node
		if (m_nodesList.getItemCount() != 0) {
			m_nodesList.select(0);
			showException();
		}
		//
		return container;
	}

	/**
	 * Shows given {@link BadNodeInformation}.
	 */
	private void showException() {
		try {
			int index = m_nodesList.getSelectionIndex();
			EditorWarning warning = m_collection.get(index);
			// set html
			String html =
					DesignerExceptionUtils.getExceptionHTML0(warning.getMessage(), warning.getException());
			m_browser.setText(html);
		} catch (Throwable e) {
			DesignerPlugin.log(e);
		}
	}
}
