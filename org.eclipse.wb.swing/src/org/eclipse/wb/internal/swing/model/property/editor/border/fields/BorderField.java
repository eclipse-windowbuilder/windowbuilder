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
package org.eclipse.wb.internal.swing.model.property.editor.border.fields;

import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.swing.model.property.editor.border.BorderDialog;
import org.eclipse.wb.internal.swing.model.property.editor.border.BorderValue;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.AbstractBorderComposite;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.border.Border;

/**
 * {@link AbstractBorderField} for editing inner {@link Border}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class BorderField extends AbstractBorderField {
	private AstEditor m_editor;
	private Text m_text;
	private BorderValue m_borderValue = new BorderValue();
	private String m_source;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BorderField(AbstractBorderComposite parent, String labelText, String buttonText) {
		super(parent, 2, labelText);
		{
			m_text = new Text(this, SWT.BORDER | SWT.READ_ONLY);
			GridDataFactory.create(m_text).hintHC(35);
		}
		{
			Button button = new Button(this, SWT.NONE);
			GridDataFactory.create(button).hintHC(10);
			button.setText(buttonText);
			button.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					BorderDialog borderDialog = new BorderDialog(getShell(), m_editor);
					borderDialog.setBorderModified(m_borderValue.isPresent());
					borderDialog.setBorderValue(m_borderValue);
					if (borderDialog.open() == Window.OK) {
						m_borderValue = borderDialog.getBorderValue();
						m_source = borderDialog.getBorderSource();
						showBorder();
						notifyListeners(SWT.Selection, new Event());
					}
				}
			});
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the {@link AstEditor} to use with {@link BorderDialog}.
	 */
	public void setEditor(AstEditor editor) {
		m_editor = editor;
	}

	/**
	 * @return current {@link BorderValue}. Never {@code null}.
	 */
	public BorderValue getBorderValue() {
		return m_borderValue;
	}

	/**
	 * Sets the {@link Border} value. Must not be {@code null}.
	 */
	public void setBorderValue(BorderValue borderValue) throws Exception {
		Assert.isNotNull(borderValue, "Border value must not be null.");
		m_borderValue = borderValue;
		m_source = calculateSource();
		showBorder();
	}

	@Override
	public String getSource() {
		return m_source;
	}
	
	private String calculateSource() throws Exception{
		// try to use AbstractBorderComposite's to convert Border into source
		// TODO BorderValue
		Border border = m_borderValue.doGetValue();
		if (border != null) {
			Class<?> compositeClass = AbstractBorderComposite.getCompositeClass(border.getClass());
			if (compositeClass != null) {
				AbstractBorderComposite borderComposite = getBorderComposite(compositeClass);
				try {
					if (borderComposite.setBorder(border)) {
						return borderComposite.getSource();
					}
				} finally {
					returnBorderComposite(borderComposite);
				}
			}
		}
		// no, we don't understand this Border
		return null;
	}

	@Override
	public AbstractBorderComposite getParent() {
		return (AbstractBorderComposite) super.getParent();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Borders
	//
	////////////////////////////////////////////////////////////////////////////
	private final List<AbstractBorderComposite> m_borderComposites = new LinkedList<>();

	/**
	 * Note, that we can not reuse {@link AbstractBorderComposite}'s, so when we get some
	 * {@link AbstractBorderComposite}, we should return it back, else new instance will be created.
	 *
	 * @return the instance free of {@link AbstractBorderComposite}.
	 */
	private final AbstractBorderComposite getBorderComposite(Class<?> compositeClass)
			throws Exception {
		for (Iterator<AbstractBorderComposite> I = m_borderComposites.iterator(); I.hasNext();) {
			AbstractBorderComposite borderComposite = I.next();
			if (borderComposite.getClass() == compositeClass) {
				I.remove();
				return borderComposite;
			}
		}
		// create new instance
		Shell shell = getParent().getDialog().getTemporaryShell();
		return (AbstractBorderComposite) compositeClass.getConstructor(Composite.class).newInstance(shell);
	}

	/**
	 * Returns given {@link AbstractBorderComposite} to the list of available.
	 */
	private final void returnBorderComposite(AbstractBorderComposite borderComposite) {
		m_borderComposites.add(borderComposite);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Shows current value of {@link #m_border}.
	 */
	private void showBorder() {
		Class<?> borderType = m_borderValue.getValueType();
		if (borderType != null) {
			m_text.setText(borderType.getName());
		} else {
			m_text.setText("");
		}
	}
}
