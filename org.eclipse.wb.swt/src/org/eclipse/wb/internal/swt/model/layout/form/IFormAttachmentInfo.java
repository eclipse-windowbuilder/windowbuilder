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
package org.eclipse.wb.internal.swt.model.layout.form;

import org.eclipse.wb.core.model.IObjectInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormLayout;

/**
 * Interface for SWT {@link FormAttachment} model. This is related to {@link FormLayout}.
 *
 * @author mitin_aa
 * @coverage swt.model.layout.form
 */
public interface IFormAttachmentInfo<C extends IControlInfo> extends IObjectInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	//	Access
	//
	////////////////////////////////////////////////////////////////////////////
	int getAlignment();

	int getNumerator();

	int getDenominator();

	int getOffset();

	C getControl();

	void setControl(C object);

	void setNumerator(int percent);

	void setDenominator(int value);

	void setOffset(int offset);

	void setAlignment(int value);

	/**
	 * @return the FormSide reflecting which side this attachment belongs to.
	 */
	FormSide getSide();

	////////////////////////////////////////////////////////////////////////////
	//
	//	Operations
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Does code changes according to FormAttachment's properties set.
	 */
	void write() throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	//	Misc
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if this attachment doesn't yet exist in code.
	 */
	boolean isVirtual();

	/**
	 * @return <code>true</code> if this attachment bound to parent in leading direction (control ==
	 *         null && denominator == 100 && numerator == 0);
	 */
	boolean isParentLeading();

	/**
	 * @return <code>true</code> if this attachment bound to parent in trailing direction (control ==
	 *         null && denominator == numerator);
	 */
	boolean isParentTrailing();

	/**
	 * @return <code>true</code> if this attachment bound to parent by some percentage value (control
	 *         == null && 0 < numerator < 100);
	 */
	boolean isPercentaged();
}
