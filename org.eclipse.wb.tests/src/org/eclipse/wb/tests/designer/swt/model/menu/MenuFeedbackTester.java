/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.swt.model.menu;

import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.gef.policy.menu.MenuSelectionEditPolicy;
import org.eclipse.wb.tests.gef.GraphicalRobot;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;

import java.util.function.Predicate;

/**
 * Tester for feedbacks on {@link IEditPartViewer#MENU_FEEDBACK_LAYER}.
 *
 * @author scheglov_ke
 */
public final class MenuFeedbackTester {
	private final GraphicalRobot canvas;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MenuFeedbackTester(GraphicalRobot canvas) {
		this.canvas = canvas;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Feedbacks validation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Predicate} that checks if feedback is {@link MenuSelectionEditPolicy}.
	 */
	private static Predicate<IFigure> getSelectionPredicate(GraphicalEditPart part) {
		// prepare "part" Figure bounds in absolute
		final Rectangle partBounds;
		{
			partBounds = part.getFigure().getBounds().getCopy();
			FigureUtils.translateFigureToAbsolute(part.getFigure(), partBounds);
		}
		// return predicate
		return feedback -> partBounds.equals(feedback.getBounds());
	}

	private Predicate<IFigure> getSelectionPredicate(Object object) {
		return getSelectionPredicate(canvas.getEditPart(object));
	}

	/**
	 * Asserts that there are no any "menu" feedback.
	 */
	public void assertMenuNoFeedbacks() {
		canvas.assertFigures(IEditPartViewer.MENU_FEEDBACK_LAYER);
	}

	@SuppressWarnings("unchecked")
	public void assertMenuFeedbacks(Predicate<IFigure> p) {
		assertMenuFeedbacks(new Predicate[]{p});
	}

	@SuppressWarnings("unchecked")
	public void assertMenuFeedbacks(Predicate<IFigure> p1, Predicate<IFigure> p2) {
		assertMenuFeedbacks(new Predicate[]{p1, p2});
	}

	/**
	 * Asserts that {@link IEditPartViewer#MENU_FEEDBACK_LAYER} has feedback {@link IFigure}'s that
	 * satisfy to given {@link Predicate}'s.
	 */
	public void assertMenuFeedbacks(Predicate<IFigure>... predicates) {
		canvas.assertFigures(IEditPartViewer.MENU_FEEDBACK_LAYER, predicates);
	}

	public void assertFeedback_selection(Object object) {
		assertMenuFeedbacks(getSelectionPredicate(object));
	}

	public void assertFeedback_selection_line(Object selection, Object lineObject, int location) {
		Predicate<IFigure> linePredicate = canvas.getLinePredicate(lineObject, location);
		Predicate<IFigure> selectionPredicate = getSelectionPredicate(selection);
		assertMenuFeedbacks(selectionPredicate, linePredicate);
	}

	public void assertFeedback_selection_target(Object selection, Object targetObject) {
		Predicate<IFigure> targetPredicate = canvas.getTargetPredicate(targetObject);
		Predicate<IFigure> selectionPredicate = getSelectionPredicate(selection);
		assertMenuFeedbacks(selectionPredicate, targetPredicate);
	}

	public void assertFeedback_selection_emptyFlow(Object selection,
			Object hostObject,
			boolean horizontal) {
		Predicate<IFigure> targetPredicate =
				canvas.getEmptyFlowContainerPredicate(hostObject, horizontal);
		Predicate<IFigure> flowPredicate = getSelectionPredicate(selection);
		assertMenuFeedbacks(flowPredicate, targetPredicate);
	}

	/**
	 * Asserts that there is only one feedback, that satisfies
	 * {@link #getLinePredicate(GraphicalEditPart, int)}.
	 */
	public void assertMenuLineFeedback(Object object, int location) {
		assertMenuFeedbacks(canvas.getLinePredicate(object, location));
	}

	/**
	 * Asserts that there is only one feedback, that satisfies
	 * {@link #getTargetPredicate(GraphicalEditPart)}.
	 */
	public void assertMenuTargetFeedback(Object object) {
		assertMenuFeedbacks(canvas.getTargetPredicate(object));
	}
}
