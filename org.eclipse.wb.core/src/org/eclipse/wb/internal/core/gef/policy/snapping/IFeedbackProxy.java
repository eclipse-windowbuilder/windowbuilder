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
package org.eclipse.wb.internal.core.gef.policy.snapping;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Feedback drawing proxy for snapping engine.
 *
 * @author mitin_aa
 * @coverage core.gef.policy.snapping
 */
public interface IFeedbackProxy {
	/**
	 * Create vertical line feedback for feedback layer.
	 */
	IFigure addVerticalFeedbackLine(int x, int y, int height);

	/**
	 * Create horizontal line feedback for feedback layer.
	 */
	IFigure addHorizontalFeedbackLine(int y, int x, int width);

	/**
	 * Create vertical middle line feedback for feedback layer.
	 */
	IFigure addVerticalMiddleLineFeedback(int x, int y, int height);

	/**
	 * Create horizontal middle line feedback for feedback layer.
	 */
	IFigure addHorizontalMiddleLineFeedback(int y, int x, int width);

	/**
	 * Create outline feedback for feedback layer.
	 */
	IFigure addOutlineFeedback(Rectangle bounds);
}
