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
package org.eclipse.wb.internal.core.gef.policy.snapping;

import org.eclipse.wb.draw2d.Figure;

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
	Figure addVerticalFeedbackLine(int x, int y, int height);

	/**
	 * Create horizontal line feedback for feedback layer.
	 */
	Figure addHorizontalFeedbackLine(int y, int x, int width);

	/**
	 * Create vertical middle line feedback for feedback layer.
	 */
	Figure addVerticalMiddleLineFeedback(int x, int y, int height);

	/**
	 * Create horizontal middle line feedback for feedback layer.
	 */
	Figure addHorizontalMiddleLineFeedback(int y, int x, int width);

	/**
	 * Create outline feedback for feedback layer.
	 */
	Figure addOutlineFeedback(Rectangle bounds);
}
