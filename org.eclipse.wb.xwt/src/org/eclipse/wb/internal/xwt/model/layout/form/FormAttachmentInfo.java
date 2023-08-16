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
package org.eclipse.wb.internal.xwt.model.layout.form;

import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementUtils;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.GenericPropertySetExpression;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutUtils;
import org.eclipse.wb.internal.swt.model.layout.form.FormSide;
import org.eclipse.wb.internal.swt.model.layout.form.IFormAttachmentInfo;
import org.eclipse.wb.internal.xwt.model.util.NameSupport;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormLayout;

import java.io.Serializable;

/**
 * SWT {@link FormAttachment} model. This is related to {@link FormLayout}.
 *
 * @author mitin_aa
 * @coverage XWT.model.layout
 */
public final class FormAttachmentInfo extends XmlObjectInfo
implements
IFormAttachmentInfo<ControlInfo> {
	// constants
	private static final String PROPERTY_TITLE_ALIGNMENT = "alignment";
	private static final String PROPERTY_TITLE_CONTROL = "control";
	private static final String PROPERTY_TITLE_OFFSET = "offset";
	private static final String PROPERTY_TITLE_DENOMINATOR = "denominator";
	private static final String PROPERTY_TITLE_NUMERATOR = "numerator";
	// fields
	private final FormAttachmentInfo m_this = this;
	private FormSide m_side;
	private int m_numerator;
	private int m_denominator = 100;
	private int m_offset;
	private ControlInfo m_control;
	private int m_alignment = SWT.DEFAULT;
	private boolean m_deleting;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormAttachmentInfo(EditorContext context,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(context, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Initialize
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void initialize() throws Exception {
		super.initialize();
		addBroadcastListener(new GenericPropertySetExpression() {
			@Override
			public void invoke(GenericPropertyImpl property,
					String[] source,
					Object[] value,
					boolean[] shouldSet) throws Exception {
				if (property.getObject() == m_this) {
					FormAttachmentInfo.this.setPropertyExpression(property, value[0]);
					shouldSet[0] = false;
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_fetch() throws Exception {
		super.refresh_fetch();
		readPropertiesValue();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Side
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public FormSide getSide() {
		return m_side;
	}

	public void setSide(FormSide side) {
		m_side = side;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	private void setPropertyExpression(GenericPropertyImpl property, Object value) throws Exception {
		String title = property.getTitle();
		if (PROPERTY_TITLE_NUMERATOR.equals(title)) {
			setNumerator((Integer) value);
			setControl(null);
		}
		if (PROPERTY_TITLE_OFFSET.equals(title)) {
			setOffset((Integer) value);
		}
		if (PROPERTY_TITLE_DENOMINATOR.equals(title)) {
			setDenominator((Integer) value);
			setControl(null);
		}
		if (PROPERTY_TITLE_ALIGNMENT.equals(title)) {
			if (getControl() != null) {
				int alignment = (Integer) value;
				setAlignment(alignment);
			} else {
				return;
			}
		}
		// control
		if (PROPERTY_TITLE_CONTROL.equals(title)) {
			if (value == Property.UNKNOWN_VALUE) {
				setControl(null);
			} else {
				setControl((ControlInfo) value);
				setAlignment(getSide().getOppositeSide().getFormSide());
				setOffset(0);
			}
		}
		write();
		if (!m_deleting) {
			ExecutionUtils.refresh(this);
		}
	}

	/**
	 * Reads and stores the properties values in fields.
	 */
	public void readPropertiesValue() throws Exception {
		m_numerator = (Integer) getPropertyByTitle(PROPERTY_TITLE_NUMERATOR).getValue();
		m_denominator = (Integer) getPropertyByTitle(PROPERTY_TITLE_DENOMINATOR).getValue();
		m_offset = (Integer) getPropertyByTitle(PROPERTY_TITLE_OFFSET).getValue();
		// control value returned as Object value, not ObjectInfo
		Object control = getPropertyByTitle(PROPERTY_TITLE_CONTROL).getValue();
		m_control = (ControlInfo) getRootXML().getChildByObject(control);
		// alignment
		m_alignment = (Integer) getPropertyByTitle(PROPERTY_TITLE_ALIGNMENT).getValue();
		if (m_alignment == 0 || m_alignment == SWT.DEFAULT) {
			m_alignment = m_side.getOppositeSide().getFormSide();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void delete() throws Exception {
		try {
			// isDeleting() is not yet set, so use own
			m_deleting = true;
			setDefaults();
			super.delete();
		} finally {
			m_deleting = false;
		}
	}

	private void setDefaults() throws Exception {
		getPropertyByTitle(PROPERTY_TITLE_NUMERATOR).setValue(0);
		getPropertyByTitle(PROPERTY_TITLE_DENOMINATOR).setValue(100);
		getPropertyByTitle(PROPERTY_TITLE_OFFSET).setValue(0);
		getPropertyByTitle(PROPERTY_TITLE_ALIGNMENT).setValue(SWT.DEFAULT);
		getPropertyByTitle(PROPERTY_TITLE_CONTROL).setValue(Property.UNKNOWN_VALUE);
	}

	@Override
	public final void setNumerator(int numerator) {
		m_numerator = numerator;
	}

	@Override
	public final int getNumerator() {
		return m_numerator;
	}

	@Override
	public final void setDenominator(int denominator) {
		m_denominator = denominator;
	}

	@Override
	public final int getDenominator() {
		return m_denominator;
	}

	@Override
	public final void setOffset(int offset) {
		m_offset = offset;
	}

	@Override
	public final int getOffset() {
		return m_offset;
	}

	@Override
	public final void setControl(ControlInfo control) {
		m_control = control;
	}

	@Override
	public final ControlInfo getControl() {
		return m_control;
	}

	@Override
	public final void setAlignment(int alignment) {
		if (alignment == 0 || alignment == SWT.DEFAULT) {
			alignment = m_side.getOppositeSide().getFormSide();
		}
		if (!isValidAlignment(alignment)) {
			return;
		}
		m_alignment = alignment;
	}

	@Override
	public final int getAlignment() {
		return m_alignment;
	}

	@Override
	public boolean isVirtual() {
		return getCreationSupport() instanceof VirtualFormAttachmentCreationSupport;
	}

	@Override
	public boolean isParentTrailing() {
		return !isVirtual() && getControl() == null && getDenominator() == getNumerator();
	}

	@Override
	public boolean isParentLeading() {
		return !isVirtual() && getControl() == null && getNumerator() == 0 && getDenominator() == 100;
	}

	@Override
	public boolean isPercentaged() {
		return getNumerator() > 0 && getNumerator() < 100;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void write() throws Exception {
		ControlInfo control = getControl();
		DocumentElement element = getElement();
		if (control == null) {
			element.setAttribute(PROPERTY_TITLE_CONTROL, null);
			element.setAttribute(PROPERTY_TITLE_ALIGNMENT, null);
			if (getDenominator() == 100) {
				if (getOffset() == 0) {
					// FormAttachment (int numerator)
					element.setAttribute(PROPERTY_TITLE_NUMERATOR, "" + getNumerator());
					element.setAttribute(PROPERTY_TITLE_DENOMINATOR, null);
					element.setAttribute(PROPERTY_TITLE_OFFSET, null);
				} else {
					// FormAttachment (int numerator, int offset)
					element.setAttribute(PROPERTY_TITLE_NUMERATOR, "" + getNumerator());
					element.setAttribute(PROPERTY_TITLE_DENOMINATOR, null);
					element.setAttribute(PROPERTY_TITLE_OFFSET, "" + getOffset());
				}
			} else {
				// FormAttachment (int numerator, int denominator, int offset)
				element.setAttribute(PROPERTY_TITLE_NUMERATOR, "" + getNumerator());
				element.setAttribute(PROPERTY_TITLE_DENOMINATOR, "" + getDenominator());
				element.setAttribute(PROPERTY_TITLE_OFFSET, "" + getOffset());
			}
		} else {
			element.setAttribute(PROPERTY_TITLE_NUMERATOR, null);
			element.setAttribute(PROPERTY_TITLE_DENOMINATOR, null);
			String referenceExpression = getReferenceExpression(control);
			element.setAttribute(PROPERTY_TITLE_CONTROL, referenceExpression);
			if (getAlignment() == m_side.getOppositeSide().getFormSide()) {
				if (getOffset() == 0) {
					// FormAttachment (Control control)
					element.setAttribute(PROPERTY_TITLE_OFFSET, null);
					element.setAttribute(PROPERTY_TITLE_ALIGNMENT, null);
				} else {
					// FormAttachment (Control control, int offset)
					element.setAttribute(PROPERTY_TITLE_OFFSET, "" + getOffset());
					element.setAttribute(PROPERTY_TITLE_ALIGNMENT, null);
				}
			} else {
				// FormAttachment (Control control, int offset, int alignment)
				element.setAttribute(PROPERTY_TITLE_OFFSET, "" + getOffset());
				element.setAttribute(
						PROPERTY_TITLE_ALIGNMENT,
						"SWT." + CodeUtils.getShortClass(FormLayoutUtils.getAlignmentSource(getAlignment())));
			}
		}
	}

	private String getReferenceExpression(ControlInfo control) {
		return "{Binding ElementName=" + NameSupport.ensureName(control) + "}";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Misc
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		final String result[] = new String[]{""};
		if (isVirtual()) {
			return result[0] += "(none)";
		}
		ExecutionUtils.runIgnore(new RunnableEx() {
			@Override
			public void run() throws Exception {
				ControlInfo control = getControl();
				int offset = getOffset();
				if (control == null) {
					result[0] += "(" + getNumerator();
					int denominator = getDenominator();
					if (denominator != 100) {
						result[0] += ", " + denominator;
					}
					if (offset != 0 || denominator != 100) {
						result[0] += ", " + offset;
					}
					result[0] += ")";
				} else {
					result[0] += "(" + control.getPresentation().getText() + ", " + offset + ")";
				}
			}
		});
		return result[0];
	}

	/**
	 * @return <code>true</code> if given alignment is suitable for this attachment.
	 */
	private boolean isValidAlignment(int alignment) {
		if (alignment != SWT.CENTER) {
			boolean b1 = PlacementUtils.isHorizontalSide(getSide().getEngineSide());
			boolean b2 = PlacementUtils.isHorizontalSide(FormLayoutUtils.convertSwtAlignment(alignment));
			return b1 == b2;
		}
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Move in tree
	//
	////////////////////////////////////////////////////////////////////////////
	public void adjustAfterComponentMove() throws Exception {
		write();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Clipboard support
	//
	////////////////////////////////////////////////////////////////////////////
	public FormAttachmentClipboardInfo getClipboardInfo() {
		if (isVirtual()) {
			return null;
		}
		FormAttachmentClipboardInfo info = new FormAttachmentClipboardInfo();
		info.numerator = getNumerator();
		info.denominator = getDenominator();
		info.offset = getOffset();
		ControlInfo targetControl = getControl();
		if (targetControl != null) {
			CompositeInfo parent = (CompositeInfo) targetControl.getParent();
			info.controlID = parent.getChildrenControls().indexOf(targetControl);
		}
		info.alignment = getAlignment();
		return info;
	}

	public void applyClipboardInfo(ControlInfo thisControl, FormAttachmentClipboardInfo info)
			throws Exception {
		if (info == null) {
			return;
		}
		setNumerator(info.numerator);
		setDenominator(info.denominator);
		setOffset(info.offset);
		setAlignment(info.alignment);
		if (info.controlID != -1) {
			CompositeInfo parent = (CompositeInfo) thisControl.getParent();
			ControlInfo targetControl = parent.getChildrenControls().get(info.controlID);
			setControl(targetControl);
		}
		write();
	}

	static class FormAttachmentClipboardInfo implements Serializable {
		private static final long serialVersionUID = 0L;
		int numerator;
		int denominator;
		int offset;
		int controlID = -1;
		int alignment;
	}
}