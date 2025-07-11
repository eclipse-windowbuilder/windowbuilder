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
package org.eclipse.wb.tests.gef;

import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.Polyline;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.core.tools.PasteTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.ResizeHandle;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.gef.graphical.tools.SelectionTool;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.draw2d.FigureCanvas;
import org.eclipse.wb.internal.gef.core.EditDomain;
import org.eclipse.wb.internal.gef.graphical.GraphicalViewer;
import org.eclipse.wb.tests.utils.AbsoluteCreationTool;
import org.eclipse.wb.tests.utils.AbsolutePasteTool;
import org.eclipse.wb.tests.utils.AbsoluteSelectionTool;
import org.eclipse.wb.tests.utils.AutoScroller;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.assertj.core.api.Assertions;
import org.assertj.core.description.Description;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Fluent interface for performing create/move/resize operations on {@link GraphicalViewer}.
 *
 * @author scheglov_ke
 */
public final class GraphicalRobot {
	// viewer
	private final GraphicalViewer m_viewer;
	private final EventSender m_sender;
	private final FigureCanvas m_canvas;
	// source
	private boolean sourceSideMode = false;
	private int sourceWidth;
	private int sourceHeight;
	private int mouseInSourceX;
	private int mouseInSourceY;
	// side
	private boolean leftSide = true;
	private boolean topSide = true;
	// target
	private GraphicalEditPart target;
	private Rectangle targetBounds;
	// mouse
	private int mouseX;
	private int mouseY;
	private int mouseOldX;
	private int mouseOldY;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GraphicalRobot(GraphicalViewer viewer) {
		m_viewer = viewer;
		m_sender = new EventSender(viewer.getControl());
		m_canvas = m_viewer.getControl();
		EditDomain editDomain = m_viewer.getEditDomain();
		editDomain.addActiveToolListener(tool -> {
			if (tool == null) {
				return;
			}
			if (tool.getClass() == SelectionTool.class) {
				editDomain.setActiveTool(new AbsoluteSelectionTool());
			} else if (tool.getClass() == PasteTool.class) {
				editDomain.setActiveTool(new AbsolutePasteTool(((PasteTool) tool).getMemento()));
			} else if (tool.getClass() == CreationTool.class) {
				editDomain.setActiveTool(new AbsoluteCreationTool(((CreationTool) tool).getFactory()));
			}
		});
		editDomain.setActiveTool(new AbsoluteSelectionTool());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Emulation: state mask
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Emulates CTRL key down.
	 */
	public GraphicalRobot ctrlDown() {
		m_sender.ctrlDown();
		return this;
	}

	/**
	 * Emulates CTRL key up.
	 */
	public GraphicalRobot ctrlUp() {
		m_sender.ctrlUp();
		return this;
	}

	/**
	 * Sends {@link SWT#KeyDown} event.
	 */
	public void keyDown(int key) {
		m_sender.keyDown(key);
	}

	/**
	 * Sends {@link SWT#KeyDown} event.
	 */
	public void keyDown(int key, char c) {
		m_sender.keyDown(key, c);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Selection
	//
	////////////////////////////////////////////////////////////////////////////
	public void deselectAll() {
		m_viewer.deselectAll();
	}

	/**
	 * Selects {@link EditPart}'s of given models (and only them).
	 */
	public void select(Object... models) {
		EditPart[] editParts = getEditParts(models);
		m_viewer.setSelection(new StructuredSelection(editParts));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Operation selection
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Informs that CREATE operation is in progress. Source object has no size.
	 */
	public GraphicalRobot create() {
		create(0, 0);
		return this;
	}

	/**
	 * Informs that CREATE operation is in progress. Mouse cursor is in center of source.
	 */
	public GraphicalRobot create(int width, int height) {
		create(width, height, width / 2, height / 2);
		return this;
	}

	/**
	 * Informs that CREATE operation is in progress.
	 */
	private void create(int width, int height, int mouseInSourceX_, int mouseInSourceY_) {
		sourceWidth = width;
		sourceHeight = height;
		mouseInSourceX = mouseInSourceX_;
		mouseInSourceY = mouseInSourceY_;
	}

	/**
	 * Informs that MOVE should be performed.
	 */
	public GraphicalRobot beginMove(Object object) {
		GraphicalEditPart editPart = getEditPart(object);
		Rectangle bounds = getAbsoluteBounds(editPart);
		sourceWidth = bounds.width;
		sourceHeight = bounds.height;
		m_viewer.select(editPart);
		// find MoveHandle
		mouseX = bounds.x;
		mouseY = bounds.y;
		Rectangle rootBounds = m_canvas.getRootFigure().getBounds();
		while (rootBounds.contains(mouseX, mouseY)) {
			try (AutoScroller scroller = new AutoScroller(m_viewer, mouseX, mouseY)) {
				if (m_viewer.findHandleAt(scroller.getLocation()) instanceof MoveHandle) {
					break;
				}
			}
			mouseX++;
		}
		mouseInSourceX = mouseX - bounds.x;
		mouseInSourceY = mouseY - bounds.y;
		// begin drag
		return beginDrag();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Operation: resize and other SquareHandle-s
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Prepares mouse location on {@link ResizeHandle} with given direction.
	 */
	public GraphicalRobot toResizeHandle(Object object, final int direction) {
		Predicate<Handle> predicate = handle -> {
			if (handle instanceof ResizeHandle resizeHandle) {
				return resizeHandle.getDirection() == direction;
			}
			return false;
		};
		toHandle(object, predicate);
		// continue
		return this;
	}

	/**
	 * Prepares mouse location on resize {@link Handle} with given type and direction.
	 */
	public GraphicalRobot toResizeHandle(Object object, final Object type, final int direction) {
		Predicate<Handle> predicate = handle -> {
			if (handle.getDragTracker() instanceof ResizeTracker) {
				ResizeTracker resizeTracker = (ResizeTracker) handle.getDragTracker();
				return resizeTracker.getDirection() == direction
						&& Objects.equals(resizeTracker.getRequestType(), type);
			}
			return false;
		};
		toHandle(object, predicate);
		// continue
		return this;
	}

	/**
	 * Informs that RESIZE should be performed, using standard {@link ResizeHandle}.
	 */
	public GraphicalRobot beginResize(Object object, int direction) {
		toResizeHandle(object, direction);
		beginDrag();
		return this;
	}

	/**
	 * Prepares mouse location on side {@link Handle} that satisfies predicate.
	 */
	private GraphicalRobot toHandle(Object object, Predicate<Handle> predicate) {
		GraphicalEditPart editPart = getEditPart(object);
		Rectangle bounds = getAbsoluteBounds(editPart);
		sourceWidth = bounds.width;
		sourceHeight = bounds.height;
		m_viewer.select(editPart);
		// find Handle
		Point location = findSideHandle(bounds, predicate);
		assertNotNull(location, "Side Handle for " + predicate);
		mouseX = location.x;
		mouseY = location.y;
		mouseInSourceX = mouseX - bounds.x;
		mouseInSourceY = mouseY - bounds.y;
		// continue
		return this;
	}

	private Point findSideHandle(Rectangle bounds, Predicate<Handle> predicate) {
		Point location = null;
		{
			location = findSideHandle(predicate, bounds, 0, 0, 1, 0);
		}
		if (location == null) {
			location = findSideHandle(predicate, bounds, 0, 0, 0, 1);
		}
		if (location == null) {
			location = findSideHandle(predicate, bounds, bounds.width - 1, 0, 0, 1);
			if (location != null) {
				location.performTranslate(1, 0);
				location.performTranslate(0, 1);
			}
		}
		if (location == null) {
			location = findSideHandle(predicate, bounds, 0, bounds.height - 1, 1, 0);
			if (location != null) {
				location.performTranslate(1, 0);
				location.performTranslate(0, 1);
			}
		}
		return location;
	}

	private Point findSideHandle(Predicate<Handle> predicate,
			Rectangle bounds,
			int x,
			int y,
			int deltaX,
			int deltaY) {
		x += bounds.x;
		y += bounds.y;
		Rectangle rootBounds = m_canvas.getRootFigure().getBounds();
		while (x < bounds.right() && y < bounds.bottom() && rootBounds.contains(x, y)) {
			try (AutoScroller scroller = new AutoScroller(m_viewer, x, y)) {
				Handle handle = (Handle) m_viewer.findHandleAt(scroller.getLocation());
				if (predicate.test(handle)) {
					return handle.getBounds().getCenter();
				}
				x += deltaX;
				y += deltaY;
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Side to set position
	//
	////////////////////////////////////////////////////////////////////////////
	public GraphicalRobot leftSide() {
		leftSide = true;
		return this;
	}

	public GraphicalRobot rightSide() {
		leftSide = false;
		return this;
	}

	public GraphicalRobot topSide() {
		topSide = true;
		return this;
	}

	public GraphicalRobot bottomSide() {
		topSide = false;
		return this;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Mouse in source
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Specifies that we want to control directly mouse location, not location of source side. Use
	 * {@link #sideMode()} to use "side" mode.
	 */
	public GraphicalRobot mouseMode() {
		sourceSideMode = false;
		return this;
	}

	/**
	 * Specifies that you want to set location of sides, see {@link #leftSide()}, {@link #rightSide()}
	 * , etc.
	 */
	public GraphicalRobot sideMode() {
		sourceSideMode = true;
		return this;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Target
	//
	////////////////////////////////////////////////////////////////////////////
	public GraphicalRobot target(Object object) {
		target = getEditPart(object);
		targetBounds = getAbsoluteBounds(target);
		return this;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Position
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Location of source side in target, if positive - from left/top, if negative - from
	 * right/bottom.
	 */
	public GraphicalRobot in(int x, int y) {
		inX(x);
		inY(y);
		return this;
	}

	/**
	 * Location of source side in target, if positive - from left, if negative - from right.
	 */
	public GraphicalRobot inX(int location) {
		int sideInTarget = normalizeIn(location, targetBounds.width);
		int mouseInTarget = sideToMouseX(sideInTarget);
		mouseX = targetToAbsoluteX(mouseInTarget);
		return this;
	}

	/**
	 * Location of source side in target, if positive - from top, if negative - from bottom.
	 */
	public GraphicalRobot inY(int location) {
		int sideInTarget = normalizeIn(location, targetBounds.height);
		int mouseInTarget = sideToMouseY(sideInTarget);
		mouseY = targetToAbsoluteY(mouseInTarget);
		return this;
	}

	/**
	 * Distance of source side from target, if negative - from left/top, if positive - from
	 * right/bottom.
	 */
	public GraphicalRobot out(int x, int y) {
		outX(x);
		outY(y);
		return this;
	}

	/**
	 * Distance of source side from target, if negative - from left, if positive - from right.
	 */
	public GraphicalRobot outX(int location) {
		int sideRelTarget = normalizeOut(location, targetBounds.width);
		int mouseRelTarget = sideToMouseX(sideRelTarget);
		mouseX = targetToAbsoluteX(mouseRelTarget);
		return this;
	}

	/**
	 * Distance of source side from target, if negative - from top, if positive - from bottom.
	 */
	public GraphicalRobot outY(int location) {
		int sideRelTarget = normalizeOut(location, targetBounds.height);
		int mouseRelTarget = sideToMouseY(sideRelTarget);
		mouseY = targetToAbsoluteY(mouseRelTarget);
		return this;
	}

	/**
	 * Location in target, in fractions of its width/height.
	 */
	public GraphicalRobot in(double x, double y) {
		inX(x);
		inY(y);
		return this;
	}

	/**
	 * Location in target, in fractions of its width if <code>abs() less 1.0</code>, or in pixels as
	 * {@link #inX(int)}.
	 */
	public GraphicalRobot inX(double k) {
		if (k > -1.0 && k < 1.0) {
			int location = (int) (targetBounds.width * k);
			int sideInTarget = normalizeIn(location, targetBounds.width);
			int mouseInTarget = sideToMouseX(sideInTarget);
			mouseX = targetToAbsoluteX(mouseInTarget);
		} else {
			inX((int) k);
		}
		return this;
	}

	/**
	 * Location in target, in fractions of its width if <code>abs() less 1.0</code>, or in pixels as
	 * {@link #inY(int)}.
	 */
	public GraphicalRobot inY(double k) {
		if (k > -1.0 && k < 1.0) {
			int location = (int) (targetBounds.height * k);
			int sideInTarget = normalizeIn(location, targetBounds.height);
			int mouseInTarget = sideToMouseY(sideInTarget);
			mouseY = targetToAbsoluteY(mouseInTarget);
		} else {
			inY((int) k);
		}
		return this;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Location utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static int normalizeIn(int location, int targetSize) {
		if (location < 0) {
			location += targetSize;
		}
		return location;
	}

	private static int normalizeOut(int location, int targetSize) {
		if (location > 0) {
			location += targetSize;
		}
		return location;
	}

	private int sideToMouseX(int side) {
		if (!sourceSideMode) {
			return side;
		}
		if (leftSide) {
			return side + mouseInSourceX;
		} else {
			return side - (sourceWidth - mouseInSourceX);
		}
	}

	private int sideToMouseY(int side) {
		if (!sourceSideMode) {
			return side;
		}
		if (topSide) {
			return side + mouseInSourceY;
		} else {
			return side - (sourceHeight - mouseInSourceY);
		}
	}

	private int targetToAbsoluteX(int x) {
		return targetBounds.x + x;
	}

	private int targetToAbsoluteY(int y) {
		return targetBounds.y + y;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Operations
	//
	////////////////////////////////////////////////////////////////////////////
	public GraphicalRobot moveTo(int _mouseX, int _mouseY) {
		mouseX = _mouseX;
		mouseY = _mouseY;
		return move();
	}

	public GraphicalRobot moveTo(Object object, int _mouseX, int _mouseY) {
		target(object);
		in(_mouseX, _mouseY);
		return move();
	}

	public GraphicalRobot moveTo(Object object, double _mouseX, double _mouseY) {
		target(object);
		inX(_mouseX);
		inY(_mouseY);
		return move();
	}

	public GraphicalRobot moveTo(Object object) {
		return moveTo(object, 0, 0);
	}

	public GraphicalRobot moveOn(int deltaX, int deltaY) {
		mouseX += deltaX;
		mouseY += deltaY;
		return move();
	}

	public GraphicalRobot move() {
		m_sender.moveTo(mouseX, mouseY);
		return this;
	}

	public GraphicalRobot beginDrag(int button) {
		m_sender.startDrag(mouseX, mouseY, button);
		mouseOldX = mouseX;
		mouseOldY = mouseY;
		return this;
	}

	public GraphicalRobot beginDrag() {
		return beginDrag(1);
	}

	public GraphicalRobot beginDrag(int _mouseX, int _mouseY, int button) {
		moveTo(_mouseX, _mouseY);
		return beginDrag(button);
	}

	public GraphicalRobot beginDrag(Object object, int _mouseX, int _mouseY) {
		Point location = getLocation(object, _mouseX, _mouseY);
		return beginDrag(location.x, location.y, 1);
	}

	public GraphicalRobot beginDrag(Object object) {
		return beginDrag(object, 0, 0);
	}

	public GraphicalRobot dragOn(int deltaX, int deltaY) {
		mouseX += deltaX;
		mouseY += deltaY;
		return drag();
	}

	public GraphicalRobot dragTo(int _mouseX, int _mouseY) {
		mouseX = _mouseX;
		mouseY = _mouseY;
		return drag();
	}

	public GraphicalRobot dragTo(Object object, int _mouseX, int _mouseY) {
		target(object);
		in(_mouseX, _mouseY);
		return drag();
	}

	public GraphicalRobot dragTo(Object object, double _mouseX, double _mouseY) {
		target(object);
		in(_mouseX, _mouseY);
		return drag();
	}

	public GraphicalRobot dragTo(Object object) {
		return dragTo(object, 0, 0);
	}

	public GraphicalRobot drag() {
		int deltaX = mouseX - mouseOldX;
		int deltaY = mouseY - mouseOldY;
		int deltaX1 = deltaX / 2;
		int deltaX2 = deltaX - deltaX1;
		int deltaY1 = deltaY / 2;
		int deltaY2 = deltaY - deltaY1;
		// step 1
		mouseOldX += deltaX1;
		mouseOldY += deltaY1;
		m_sender.dragTo(mouseOldX, mouseOldY);
		// step 2
		mouseOldX += deltaX2;
		mouseOldY += deltaY2;
		m_sender.dragTo(mouseOldX, mouseOldY);
		// done
		return this;
	}

	public void endDrag() {
		m_sender.endDrag();
	}

	/**
	 * Performs left button click, for example to finish CREATE operation.
	 */
	public GraphicalRobot click(int button) {
		m_sender.click(button);
		return this;
	}

	public GraphicalRobot click() {
		click(1);
		return this;
	}

	public GraphicalRobot click(int x, int y, int button) {
		try (AutoScroller scroller = new AutoScroller(m_viewer, x, y)) {
			Point scrolledLocation = scroller.getLocation();
			m_sender.click(scrolledLocation.x, scrolledLocation.y, button);
		}
		return this;
	}

	public GraphicalRobot click(Object object, int deltaX, int deltaY) {
		Point location = getLocation(object, deltaX, deltaY);
		click(location.x, location.y, 1);
		return this;
	}

	public GraphicalRobot click(Object object) {
		click(object, 0, 0);
		return this;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// doubleClick()
	//
	////////////////////////////////////////////////////////////////////////////
	public void doubleClick() {
		m_sender.doubleClick(mouseX, mouseY, 1);
	}

	public void doubleClick(Object object) {
		doubleClick(object, 1);
	}

	public void doubleClick(Object object, int button) {
		GraphicalEditPart editPart = getEditPart(object);
		Rectangle bounds = getAbsoluteBounds(editPart);
		Point location = getLocation(editPart, bounds.width / 2, bounds.height / 2);
		m_sender.doubleClick(location.x, location.y, button);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Direct edit
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Performs direct edit and sets given text.
	 */
	public void performDirectEdit(Object object, String text) {
		select(object);
		performDirectEdit(text);
	}

	/**
	 * Performs direct edit and sets given text.
	 */
	public void performDirectEdit(String text) {
		// begin direct edit
		beginDirectEdit();
		// animate
		animateDirectEdit(text);
	}

	/**
	 * Animates already activated direct edit.
	 */
	public void animateDirectEdit(String text) {
		// prepare Text widget
		SWTBotText textWidget = new SWTBot(m_viewer.getControl()).text();
		// use Text widget
		textWidget.setText(text);
		endDirectEdit(textWidget.widget);
	}

	/**
	 * Begins direct edit for selected component.
	 */
	public void beginDirectEdit() {
		keyDown(0x20, ' ');
	}

	/**
	 * Ends direct edit for selected component.
	 */
	public void endDirectEdit(Text textWidget) {
		new EventSender(textWidget).keyDown(SWT.CR);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	public GraphicalEditPart getEditPart(final Object object) {
		GraphicalEditPart editPart = getEditPartNull(object);
		Assertions.assertThat(editPart).as(new Description() {
			@Override
			public String value() {
				return "No EditPart for " + object;
			}
		}).isNotNull();
		return editPart;
	}

	public GraphicalEditPart getEditPartNull(Object object) {
		GraphicalEditPart editPart =
				object instanceof GraphicalEditPart
				? (GraphicalEditPart) object
						: (GraphicalEditPart) m_viewer.getEditPartRegistry().get(object);
		return editPart;
	}

	public GraphicalEditPart[] getEditParts(Object[] objects) {
		GraphicalEditPart[] editParts = new GraphicalEditPart[objects.length];
		for (int i = 0; i < objects.length; i++) {
			editParts[i] = getEditPart(objects[i]);
		}
		return editParts;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Location utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return bounds of {@link GraphicalEditPart}'s figure in absolute coordinates.
	 */
	public static Rectangle getAbsoluteBounds(GraphicalEditPart editPart) {
		IFigure figure = editPart.getFigure();
		Rectangle bounds = figure.getBounds().getCopy();
		FigureUtils.translateFigureToAbsolute(figure, bounds);
		return bounds;
	}

	public Rectangle getAbsoluteBounds(Object object) {
		return getAbsoluteBounds(getEditPart(object));
	}

	/**
	 * @return location of given {@link EditPart} with offset. Negative offset means offset from
	 *         right/bottom side.
	 */
	public static Point getLocation(GraphicalEditPart editPart, int deltaX, int deltaY) {
		Rectangle bounds = getAbsoluteBounds(editPart);
		Point location = new Point(0, 0);
		if (deltaX >= 0) {
			location.x = bounds.x + deltaX;
		} else {
			location.x = bounds.right() + deltaX;
		}
		if (deltaY >= 0) {
			location.y = bounds.y + deltaY;
		} else {
			location.y = bounds.bottom() + deltaY;
		}
		return location;
	}

	public Point getLocation(Object object, int deltaX, int deltaY) {
		return getLocation(getEditPart(object), deltaX, deltaY);
	}

	public Point getLocation(Object object) {
		return getLocation(object, 0, 0);
	}

	/**
	 * @return the size of given {@link EditPart}.
	 */
	public static Dimension getSize(GraphicalEditPart editPart) {
		return editPart.getFigure().getSize();
	}

	public Dimension getSize(Object object) {
		return getSize(getEditPart(object));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Feedback utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Description of {@link IFigure}.
	 */
	public static final class FigureDescription {
		private final Class<?> m_class;
		private final Rectangle m_absoluteBounds;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public FigureDescription(Class<?> clazz, Rectangle absoluteBounds) {
			m_class = clazz;
			m_absoluteBounds = absoluteBounds;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Object
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public String toString() {
			return "(" + m_class.getName() + ", " + m_absoluteBounds + ")";
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Access
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * @return <code>true</code> if given {@link IFigure} matches this {@link FigureDescription}.
		 */
		public boolean match(IFigure figure) {
			boolean match = true;
			match &= m_class.isAssignableFrom(figure.getClass());
			if (m_absoluteBounds != null) {
				match &= m_absoluteBounds.equals(figure.getBounds());
			}
			return match;
		}
	}

	/**
	 * @return the list of {@link IFigure}'s on feedback {@link Layer}.
	 */
	public List<? extends IFigure> getFeedbackFigures() {
		return LayerManager.Helper.find(m_viewer).getLayer(IEditPartViewer.FEEDBACK_LAYER).getChildren();
	}

	/**
	 * @return the current {@link Command} from currently loaded {@link Tool}.
	 */
	public Command getCommand() throws Exception {
		Tool tool = m_viewer.getEditDomain().getActiveTool();
		// when drag is in progress, ask command from "drag tracker"
		if (tool instanceof SelectionTool) {
			Tool dragTracker = (Tool) ReflectionUtils.getFieldObject(tool, "m_dragTracker");
			if (dragTracker != null) {
				tool = dragTracker;
			}
		}
		// OK, get Command from active tool
		return (Command) ReflectionUtils.getFieldObject(tool, "m_command");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Assertions
	//
	////////////////////////////////////////////////////////////////////////////
	public void assertNullEditPart(Object object) {
		assertNull(getEditPartNull(object));
	}

	public void assertNotNullEditPart(Object object) {
		assertNotNull(getEditPartNull(object));
	}

	/**
	 * Asserts that selection is empty, i.e. no {@link EditPart} selected.
	 */
	public GraphicalRobot assertSelectedEmpty() {
		List<? extends EditPart> selectedEditParts = m_viewer.getSelectedEditParts();
		Assertions.assertThat(selectedEditParts).isEmpty();
		return this;
	}

	public void assertPrimarySelected(Object object) {
		EditPart editPart = getEditPart(object);
		assertEquals(editPart.getSelected(), EditPart.SELECTED_PRIMARY);
	}

	/**
	 * Assert that selection contains exactly given {@link EditPart}s.
	 */
	public void assertSelection(Object... objects) {
		@SuppressWarnings("unchecked")
		List<EditPart> selectedEditParts = (List<EditPart>) m_viewer.getSelectedEditParts();
		GraphicalEditPart[] editParts = getEditParts(objects);
		Assertions.assertThat(selectedEditParts).containsExactly(editParts);
	}

	public void assertChildrenCount(Object object, int count) {
		EditPart editPart = getEditPart(object);
		Assertions.assertThat(editPart.getChildren()).hasSize(count);
	}

	/**
	 * Asserts that {@link Layer} has {@link IFigure}'s that satisfy to given {@link Predicate} .
	 */
	@SuppressWarnings("unchecked")
	public void assertFigures(String layerName, Predicate<IFigure> predicate) {
		assertFigures(layerName, new Predicate[]{predicate});
	}

	/**
	 * Asserts that {@link Layer} has {@link IFigure}'s that satisfy to given {@link Predicate} 's.
	 */
	public void assertFigures(String layerName, Predicate<IFigure>... predicates) {
		// prepare feedback's
		List<? extends IFigure> feedbacks;
		{
			Layer feedbackLayer = (Layer) LayerManager.Helper.find(m_viewer).getLayer(layerName);
			feedbacks = feedbackLayer.getChildren();
			assertEquals(feedbacks.size(), predicates.length, "Wrong count of feedbacks.");
		}
		// check all feedback's
		for (int i = 0; i < predicates.length; i++) {
			Predicate<IFigure> predicate = predicates[i];
			IFigure feedback = feedbacks.get(i);
			assertTrue(predicate.test(feedback), "Predicate [" + i + "] failed.");
		}
	}

	/**
	 * Asserts that there are no any feedback on {@link IEditPartViewer#FEEDBACK_LAYER}.
	 */
	public void assertNoFeedbacks() {
		assertFigures(IEditPartViewer.FEEDBACK_LAYER);
	}

	/**
	 * Asserts that given host has "empty flow container" line feedback on
	 * {@link IEditPartViewer#FEEDBACK_LAYER}.
	 */
	public void assertEmptyFlowContainerFeedback(Object host, boolean horizontal) {
		Predicate<IFigure> predicate = getEmptyFlowContainerPredicate(host, horizontal);
		assertFeedbacks(predicate);
	}

	/**
	 * Asserts that {@link IEditPartViewer#FEEDBACK_LAYER} has feedback {@link IFigure}'s that satisfy
	 * to given {@link Predicate}'s.
	 */
	@SuppressWarnings("unchecked")
	public void assertFeedbacks(Predicate<IFigure> predicate_1) {
		assertFeedbacks0(predicate_1);
	}

	/**
	 * Asserts that {@link IEditPartViewer#FEEDBACK_LAYER} has feedback {@link IFigure}'s that satisfy
	 * to given {@link Predicate}'s.
	 */
	private void assertFeedbacks0(Predicate<IFigure>... predicates) {
		assertFigures(IEditPartViewer.FEEDBACK_LAYER, predicates);
	}

	/**
	 * Asserts that feedback layer contains exactly same {@link IFigure}'s as described.
	 */
	public void assertFeedbackFigures(FigureDescription... descriptions) {
		HashSet<IFigure> feedbackFigures = new HashSet<>(getFeedbackFigures());
		//
		for (int i = 0; i < descriptions.length; i++) {
			FigureDescription description = descriptions[i];
			// try to find figure for current description
			boolean figureFound = false;
			for (Iterator<IFigure> I = feedbackFigures.iterator(); I.hasNext();) {
				IFigure figure = I.next();
				if (description.match(figure)) {
					I.remove();
					figureFound = true;
					break;
				}
			}
			// figure should be found
			assertTrue(figureFound, "No figure found for " + description);
		}
		// all figure should be matched
		if (!feedbackFigures.isEmpty()) {
			String message = "Following figures are not matched:";
			for (IFigure figure : feedbackFigures) {
				message +=
						"\n\t"
								+ figure.getClass().getName()
								+ " "
								+ figure.getBounds()
								+ " "
								+ figure.toString();
			}
			fail(message);
		}
	}

	/**
	 * Asserts that there are no {@link IFigure}'s on feedback {@link Layer}.
	 */
	public void assertNoFeedbackFigures() {
		assertTrue(getFeedbackFigures().isEmpty(), "Feedback layer should be empty.");
	}

	/**
	 * Asserts that there are exactly given count of {@link IFigure}'s on feedback {@link Layer}.
	 */
	public void assertFeedbackFigures(int count) {
		Assertions.assertThat(getFeedbackFigures()).hasSize(count);
	}

	/**
	 * Asserts that currently loaded {@link Tool} has <code>null</code> as command.
	 */
	public GraphicalRobot assertCommandNull() throws Exception {
		final Command command = getCommand();
		Assertions.assertThat(command).describedAs(new Description() {
			@Override
			public String value() {
				return "Unexpected command " + command;
			}
		}).isNull();
		return this;
	}

	/**
	 * Asserts that currently loaded {@link Tool} has not <code>null</code> as command.
	 */
	public GraphicalRobot assertCommandNotNull() throws Exception {
		Command command = getCommand();
		assertNotNull(command, "No command.");
		return this;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure predicates
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Predicate} that checks if feedback is line used for empty flow container.
	 */
	public final Predicate<IFigure> getEmptyFlowContainerPredicate(Object hostModel, boolean horizontal) {
		GraphicalEditPart host = getEditPart(hostModel);
		// prepare "host" Figure bounds in absolute
		Rectangle bounds;
		{
			bounds = host.getFigure().getBounds().getCopy();
			FigureUtils.translateFigureToAbsolute(host.getFigure(), bounds);
		}
		// prepare expected points
		final Point p1;
		final Point p2;
		if (horizontal) {
			p1 = new Point(bounds.x, bounds.y);
			p2 = new Point(bounds.x, bounds.bottom());
		} else {
			p1 = new Point(bounds.x, bounds.y);
			p2 = new Point(bounds.right(), bounds.y);
		}
		// if host is big enough, tweak points for better look
		if (horizontal) {
			if (bounds.width > 20) {
				p1.x += 5;
				p2.x += 5;
			}
			if (bounds.height > 20) {
				p1.y += 5;
				p2.y -= 5;
			}
		} else {
			if (bounds.width > 20) {
				p1.x += 5;
				p2.x -= 5;
			}
			if (bounds.height > 20) {
				p1.y += 5;
				p2.y += 5;
			}
		}
		// return predicate
		return feedback -> {
			if (!(feedback instanceof Polyline)) {
				return false;
			}
			// prepare points of line
			Point p1_;
			Point p2_;
			{
				Polyline polyline = (Polyline) feedback;
				PointList points = polyline.getPoints();
				if (points.size() != 2) {
					return false;
				}
				p1_ = points.getPoint(0);
				p2_ = points.getPoint(1);
			}
			// compare
			return p1_.equals(p1) && p2_.equals(p2);
		};
	}

	/**
	 * @return the {@link Predicate} that checks if feedback is relative given
	 *         {@link GraphicalEditPart}.
	 *
	 * @param part
	 *          the {@link GraphicalEditPart} relative to which feedback is expected.
	 * @param location
	 *          the location of feedback, one of {@link PositionConstants#TOP},
	 *          {@link PositionConstants#BOTTOM} , {@link PositionConstants#LEFT},
	 *          {@link PositionConstants#RIGHT}.
	 */
	public static final Predicate<IFigure> getLinePredicate(GraphicalEditPart part, final int location) {
		// prepare "part" Figure bounds in absolute
		final Rectangle partBounds;
		{
			partBounds = part.getFigure().getBounds().getCopy();
			FigureUtils.translateFigureToAbsolute(part.getFigure(), partBounds);
		}
		// return predicate
		return feedback -> {
			if (!(feedback instanceof Polyline)) {
				return false;
			}
			// prepare points of line
			Point p1;
			Point p2;
			{
				Polyline polyline = (Polyline) feedback;
				PointList points = polyline.getPoints();
				if (points.size() != 2) {
					return false;
				}
				p1 = points.getPoint(0);
				p2 = points.getPoint(1);
			}
			// checks that line has expected location
			int delta = 5;
			boolean result = true;
			if (location == PositionConstants.TOP) {
				result &= p1.y == p2.y;
				result &= Math.abs(p1.y - partBounds.y) < delta;
				result &= Math.abs(p1.x - partBounds.x) < delta;
				result &= Math.abs(p2.x - partBounds.right()) < delta;
			} else if (location == PositionConstants.BOTTOM) {
				result &= p1.y == p2.y;
				result &= Math.abs(p1.y - partBounds.bottom()) < delta;
				result &= Math.abs(p1.x - partBounds.x) < delta;
				result &= Math.abs(p2.x - partBounds.right()) < delta;
			} else if (location == PositionConstants.LEFT) {
				result &= p1.x == p2.x;
				result &= Math.abs(p1.x - partBounds.x) < delta;
				result &= Math.abs(p1.y - partBounds.y) < delta;
				result &= Math.abs(p2.y - partBounds.bottom()) < delta;
			} else if (location == PositionConstants.RIGHT) {
				result &= p1.x == p2.x;
				result &= Math.abs(p1.x - partBounds.right()) < delta;
				result &= Math.abs(p1.y - partBounds.y) < delta;
				result &= Math.abs(p2.y - partBounds.bottom()) < delta;
			} else {
				fail("Unsupported location: " + location);
			}
			// OK, final result
			return result;
		};
	}

	public final Predicate<IFigure> getLinePredicate(Object object, final int location) {
		return getLinePredicate(getEditPart(object), location);
	}

	/**
	 * @return the {@link Predicate} that checks if feedback is "border target" around given
	 *         {@link GraphicalEditPart}.
	 */
	public static final Predicate<IFigure> getTargetPredicate(GraphicalEditPart part) {
		// prepare "part" Figure bounds in absolute
		final Rectangle partBounds;
		{
			partBounds = part.getFigure().getBounds().getCopy();
			FigureUtils.translateFigureToAbsolute(part.getFigure(), partBounds);
			partBounds.expand(3, 3);
		}
		// return predicate
		return feedback -> partBounds.equals(feedback.getBounds());
	}

	public final Predicate<IFigure> getTargetPredicate(Object object) {
		return getTargetPredicate(getEditPart(object));
	}
}