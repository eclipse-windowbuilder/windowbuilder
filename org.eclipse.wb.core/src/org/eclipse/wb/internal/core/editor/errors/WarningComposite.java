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
 *    Marcel du Preez - - Hiding/showing of buttons depending on WB Basic preference
 *******************************************************************************/
package org.eclipse.wb.internal.core.editor.errors;

import org.eclipse.wb.core.controls.BrowserComposite;
import org.eclipse.wb.core.editor.constants.IEditorPreferenceConstants;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * {@link Composite} for displaying non-error {@link Exception} on design pane.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage core.editor.errors
 */
public abstract class WarningComposite extends Composite {
	private Button m_switchButton;
	private final BrowserComposite m_browser;
	private final Label m_titleLabel;
	private int m_sourcePosition;
	private final boolean wbBasic;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public WarningComposite(Composite parent, int style) {
		super(parent, style);
		wbBasic = InstanceScope.INSTANCE.getNode(
				IEditorPreferenceConstants.WB_BASIC_UI_PREFERENCE_NODE).getBoolean(
						IEditorPreferenceConstants.WB_BASIC_UI,
						true);
		GridLayoutFactory.create(this);
		{
			Composite titleComposite = new Composite(this, SWT.NONE);
			GridDataFactory.create(titleComposite).alignHL();
			GridLayoutFactory.create(titleComposite).columns(2).margins(10);
			{
				Label label = new Label(titleComposite, SWT.NONE);
				label.setImage(parent.getDisplay().getSystemImage(SWT.ICON_WARNING));
			}
			{
				Font boldFont = FontDescriptor.createFrom(getFont()) //
						.setHeight(14) //
						.setStyle(SWT.BOLD) //
						.createFont(null);
				m_titleLabel = new Label(titleComposite, SWT.NONE);
				m_titleLabel.setFont(boldFont);
				m_titleLabel.addDisposeListener(event -> boldFont.dispose());
			}
		}
		{
			m_browser = new BrowserComposite(this, SWT.NONE);
			GridDataFactory.create(m_browser).grab().fill();
		}
		createButtons();
	}

	protected void createButtons() {
		Composite buttonsComposite = new Composite(this, SWT.NONE);
		GridDataFactory.create(buttonsComposite).alignHR();
		int numButtons = getNumButtons();
		GridLayoutFactory.create(buttonsComposite).columns(numButtons).equalColumns().marginsH(0);
		createButtons(buttonsComposite);
	}

	protected void createButtons(Composite buttonsComposite) {
		{
			Button refreshButton = new Button(buttonsComposite, SWT.NONE);
			GridDataFactory.create(refreshButton).fillH();
			refreshButton.setText(Messages.WarningComposite_refreshButton);
			refreshButton.setImage(
					EnvironmentUtils.IS_MAC ? null : DesignerPlugin.getImage("actions/errors/refresh32.png"));
			refreshButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					doRefresh();
				}
			});
			refreshButton.setVisible(!wbBasic);
		}
		{
			m_switchButton = new Button(buttonsComposite, SWT.NONE);
			GridDataFactory.create(m_switchButton).fillH();
			m_switchButton.setText(Messages.WarningComposite_switchButton);
			m_switchButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					doShowSource(m_sourcePosition);
				}
			});
			m_switchButton.setVisible(!wbBasic);
		}
	}

	protected int getNumButtons() {
		return 2;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Operations
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Handles the 'refresh' action.
	 */
	protected abstract void doRefresh();

	/**
	 * Handles the 'switch to source' action.
	 *
	 * @param sourcePosition
	 *          the position in the source code to switch to.
	 */
	protected abstract void doShowSource(int sourcePosition);

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the {@link Throwable} to display.
	 */
	public void setException(Throwable e) {
		ErrorEntryInfo entry = DesignerExceptionUtils.getErrorEntry(e);
		m_titleLabel.setText(entry.getTitle());
		m_browser.setText(DesignerExceptionUtils.getWarningHTML(entry));
		updateForSourcePosition(e);
	}

	private void updateForSourcePosition(Throwable e) {
		m_sourcePosition = DesignerExceptionUtils.getSourcePosition(e);
		boolean hasSourcePosition = m_sourcePosition != -1;
		// text
		if (hasSourcePosition) {
			m_switchButton.setText(Messages.WarningComposite_goProblemButton);
		} else {
			m_switchButton.setText(Messages.WarningComposite_switchButton);
		}
		// image
		if (!EnvironmentUtils.IS_MAC) {
			Image image;
			if (hasSourcePosition) {
				image = DesignerPlugin.getImage("actions/errors/switch32locate.png");
			} else {
				image = DesignerPlugin.getImage("actions/errors/switch32.png");
			}
			m_switchButton.setImage(image);
		}
	}
}
