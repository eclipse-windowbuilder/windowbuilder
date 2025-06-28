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
package org.eclipse.wb.internal.layout.group.gef;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Translatable;

/**
 * Helper for drawing feedbacks by {@link FeedbacksDrawer}.
 *
 * @author mitin_aa
 */
public interface IFeedbacksHelper {
	/**
	 * Translates the {@link Translatable} from model to feedback coordinate systems.
	 */
	void translateModelToFeedback(Translatable t);

	/**
	 * Adds a figure to appropriate layer.
	 */
	void addFeedback2(IFigure figure);
}
