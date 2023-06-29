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
package org.eclipse.wb.internal.swt.model.widgets;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

import java.util.List;

/**
 * Interface model of {@link Composite}.
 *
 * @author scheglov_ke
 * @coverage swt.model.widgets
 */
public interface ICompositeInfo extends IScrollableInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the collection of {@link IControlInfo} children.
	 */
	List<? extends IControlInfo> getChildrenControls();

	/**
	 * Returns the {@link Insets} that can be used to crop bounds of this {@link Composite} to produce
	 * a rectangle which describes the area of this {@link Composite} which is capable of displaying
	 * data (that is, not covered by the "trimmings").
	 * <p>
	 * Note, that this method is different from {@link #getClientAreaInsets()}. For example in
	 * {@link Group} point <code>(0,0)</code> is point on group border, but
	 * {@link Group#getClientArea()} returns size of border on sides. But still, if we <b>want</b> to
	 * place child {@link Control} exactly in top-left point of {@link Group}, we should use
	 * <code>(0,0)</code>. However if we want to place {@link Control} in <b>top-left of preferred
	 * location</b>, then {@link #getClientAreaInsets2()} should be used.
	 *
	 * @return the {@link Insets} for "displaying data" part of this {@link Composite}.
	 */
	Insets getClientAreaInsets2();
}