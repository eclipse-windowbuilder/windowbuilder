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
package org.eclipse.wb.internal.swing.model.property.editor.border.pages;

import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.property.editor.border.BorderDialog;
import org.eclipse.wb.internal.swing.model.property.editor.border.BorderValue;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.AbstractBorderField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.BooleanField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.BorderField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.ColorField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.ComboField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.IntegerField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.RadioField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.TextField;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.BevelBorderComposite.BevelBorderValue;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.CompoundBorderComposite.CompoundBorderValue;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.EmptyBorderComposite.EmptyBorderValue;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.EtchedBorderComposite.EtchedBorderValue;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.LineBorderComposite.LineBorderValue;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.MatteBorderComposite.MatteBorderValue;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.SoftBevelBorderComposite.SoftBevelBorderValue;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.SwingBorderComposite.SwingBorderValue;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.TitledBorderComposite.TitledBorderValue;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;

/**
 * Abstract editor for some {@link Border} type.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public abstract class AbstractBorderComposite extends Composite {
	private final String m_title;
	protected BorderDialog m_borderDialog;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractBorderComposite(Composite parent, String title) {
		super(parent, SWT.NONE);
		m_title = title;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Initializes this {@link AbstractBorderComposite}.
	 */
	public void initialize(BorderDialog borderDialog, AstEditor editor) {
		m_borderDialog = borderDialog;
	}

	/**
	 * @return the title to display for user.
	 */
	public final String getTitle() {
		return m_title;
	}

	/**
	 * Sets the {@link BorderValue} to edit.
	 *
	 * @return <code>true</code> if this {@link AbstractBorderComposite} understands
	 *         given {@link BorderValue}.
	 */
	public abstract boolean setBorderValue(BorderValue border) throws Exception;

	/**
	 * @return the source for updated {@link Border}.
	 */
	public abstract String getSource() throws Exception;

	/**
	 * Constructs and returns the {@link BorderValue} for the given {@link Border}
	 * if it is supported by this composite. This method must be called from the AWT
	 * event dispatcher thread. <i>Note:</i> This method returns {@code null} if and
	 * only if {@link #setBorderValue(BorderValue)} returns {@code false} (Outside
	 * of the {@code NoBorder}).
	 * 
	 * @param border The current {@link Border}.
	 * @return The matching {@link BorderValue} or {@code null} if this border is
	 *         not supported.
	 */
	public static BorderValue getBorderValue(javax.swing.border.Border border) {
		Assert.isTrue(SwingUtilities.isEventDispatchThread(), "Must be created from AWT event dispatcher thread");
		return switch (border) {
		case null -> null;
		case SoftBevelBorder ourBorder -> new SoftBevelBorderValue(ourBorder);
		case BevelBorder ourBorder -> new BevelBorderValue(ourBorder);
		case CompoundBorder ourBorder -> new CompoundBorderValue(ourBorder);
		case MatteBorder ourBorder -> new MatteBorderValue(ourBorder);
		case EmptyBorder ourBorder -> new EmptyBorderValue(ourBorder);
		case EtchedBorder ourBorder -> new EtchedBorderValue(ourBorder);
		case LineBorder ourBorder -> new LineBorderValue(ourBorder);
		case TitledBorder ourBorder -> new TitledBorderValue(ourBorder);
		default -> new SwingBorderValue(border);
		};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Components
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Binds given {@link AbstractBorderField}, so that when {@link SWT#Selection} event issued, we
	 * notify {@link BorderDialog}.
	 */
	private void bindField(AbstractBorderField field) {
		field.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				m_borderDialog.borderUpdated();
			}
		});
	}

	/**
	 * @return the bound {@link TextField}.
	 */
	protected final TextField createTextField(String label) {
		TextField field = new TextField(this, label);
		bindField(field);
		return field;
	}

	/**
	 * @return the bound {@link IntegerField}.
	 */
	protected final IntegerField createIntegerField(String label) {
		IntegerField field = new IntegerField(this, label);
		bindField(field);
		return field;
	}

	/**
	 * @return the bound {@link RadioField}.
	 */
	protected final RadioField createRadioField(String label,
			Class<?> clazz,
			String[] fields,
			String[] titles) {
		RadioField field = new RadioField(this, label, clazz, fields, titles);
		bindField(field);
		return field;
	}

	/**
	 * @return the bound {@link ComboField}.
	 */
	protected final ComboField createComboField(String label,
			Class<?> clazz,
			String[] fields,
			String[] titles) {
		ComboField field = new ComboField(this, label, clazz, fields, titles);
		bindField(field);
		return field;
	}

	/**
	 * @return the bound {@link BooleanField}.
	 */
	protected final BooleanField createBooleanField(String label, String[] titles) {
		BooleanField field = new BooleanField(this, label, titles);
		bindField(field);
		return field;
	}

	/**
	 * @return the bound {@link ColorField}.
	 */
	protected final ColorField createColorField(String label) {
		ColorField field = new ColorField(this, label);
		bindField(field);
		return field;
	}

	/**
	 * @return the bound {@link BorderField}.
	 */
	protected final BorderField createBorderField(String label, String buttonText) {
		BorderField field = new BorderField(this, label, buttonText);
		bindField(field);
		return field;
	}
}
