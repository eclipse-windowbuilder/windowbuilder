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
package org.eclipse.wb.internal.core.databinding.ui.decorate;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * Color and font decorator for {@link IObserveInfo}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public interface IObserveDecorator {
	IObserveDecorator DEFAULT = new ObserveDecorator();
	IObserveDecorator ITALIC = new ItalicObserveDecorator();
	IObserveDecorator BOLD = new BoldObserveDecorator();
	IObserveDecorator BOLD_ITALIC = new BoldItalicObserveDecorator();
	IObserveDecorator HIDDEN = new GrayColorObserveDecorator();

	/**
	 * @return the foreground Color for decorate host {@link IObserveInfo} or <code>null</code>.
	 */
	Color getForeground();

	/**
	 * @return the background Color for decorate host {@link IObserveInfo} or <code>null</code>.
	 */
	Color getBackground();

	/**
	 * @return the Font for decorate host {@link IObserveInfo} or <code>null</code>.
	 */
	Font getFont(Font baseItalicFont, Font baseBoldFont, Font baseBoldItalicFont);
}