/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.swing.model.property.editor.models.spinner;

import org.eclipse.wb.internal.core.model.property.converter.StringArrayConverter;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.model.ModelMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang3.StringUtils;

import java.util.function.Supplier;

/**
 * Implementation of {@link AbstractSpinnerComposite} for
 * {@link javax.swing.SpinnerListModel SpinnerListModel}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
final class ListSpinnerComposite extends AbstractSpinnerComposite {
	private final Text m_textWidget;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ListSpinnerComposite(Composite parent, SpinnerModelDialog modelDialog) {
		super(parent, modelDialog);
		GridLayoutFactory.create(this);
		// Text with items
		{
			new Label(this, SWT.NONE).setText(ModelMessages.ListSpinnerComposite_itemsLabel);
			{
				m_textWidget = new Text(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
				GridDataFactory.create(m_textWidget).spanH(2).grab().fill().hintC(50, 8);
				// update preview on modify
				m_textWidget.addListener(SWT.Modify, new Listener() {
					@Override
					public void handleEvent(Event event) {
						m_modelDialog.validateAll();
					}
				});
			}
			new Label(this, SWT.NONE).setText(ModelMessages.ListSpinnerComposite_hint);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getTitle() {
		return ModelMessages.ListSpinnerComposite_title;
	}

	@Override
	public boolean setModelValue(SpinnerModelValue modelValue) {
		if (modelValue.getValue() instanceof javax.swing.SpinnerListModel listModel) {
			String text = StringUtils.join(listModel.getList().iterator(), "\n");
			getDisplay().asyncExec(() -> {
				m_textWidget.setText(text);
				m_modelDialog.validateAll();
			});
			// OK, this is our model
			return true;
		}
		return false;
	}

	@Override
	public String validate() {
		String[] items = getItems();
		if (items.length == 0) {
			return ModelMessages.ListSpinnerComposite_itemsValue;
		}
		return null;
	}

	@Override
	public Supplier<SpinnerModelValue> getModelValue() {
		String[] items = getItems();
		return () -> new SpinnerModelValue(new javax.swing.SpinnerListModel(items));
	}

	@Override
	public String getSource() throws Exception {
		String[] items = getItems();
		String itemsSource = StringArrayConverter.INSTANCE.toJavaSource(null, items);
		return "new javax.swing.SpinnerListModel(" + itemsSource + ")";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the items entered by user into {@link #m_textWidget}.
	 */
	private String[] getItems() {
		return StringUtils.split(m_textWidget.getText(), "\r\n");
	}
}
