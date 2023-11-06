/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.gef;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.GroupRequest;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.requests.LocationRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.SelectionRequest;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.swt.SWT;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lobas_av
 *
 */
public class RequestsTest extends Assert {

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_Request() throws Exception {
		Request request = new Request();
		//
		// check new Request
		assertNull(request.getType());
		//
		// check set type
		Object type = 3;
		request = new Request(type);
		assertSame(type, request.getType());
	}

	@Test
	public void test_GroupRequest() throws Exception {
		GroupRequest request = new GroupRequest();
		//
		// check new GroupRequest
		assertNull(request.getType());
		assertNull(request.getEditParts());
		//
		request = new GroupRequest("zzz");
		GraphicalEditPart editPart1 = new GraphicalEditPart() {
			@Override
			protected Figure createFigure() {
				return null;
			}
		};
		GraphicalEditPart editPart2 = new GraphicalEditPart() {
			@Override
			protected Figure createFigure() {
				return null;
			}
		};
		request.addEditPart(editPart1);
		request.addEditPart(editPart2);
		//
		// check type and add EditPart
		assertEquals("zzz", request.getType());
		assertNotNull(request.getEditParts());
		assertEquals(2, request.getEditParts().size());
		assertSame(editPart1, request.getEditParts().get(0));
		assertSame(editPart2, request.getEditParts().get(1));
		//
		// check setEditParts
		List<EditPart> editParts = new ArrayList<>();
		request.setEditParts(editParts);
		assertSame(editParts, request.getEditParts());
	}

	@Test
	public void test_ChangeBoundsRequest() throws Exception {
		ChangeBoundsRequest request = new ChangeBoundsRequest();
		// check new ChangeBoundsRequest
		assertNull(request.getType());
		assertNull(request.getEditParts());
		assertNull(request.getLocation());
		assertEquals(new Point(), request.getMoveDelta());
		assertEquals(new Dimension(), request.getSizeDelta());
		assertEquals(0, request.getResizeDirection());
		//
		GraphicalEditPart editPart = new GraphicalEditPart() {
			@Override
			protected Figure createFigure() {
				return null;
			}
		};
		Point location = new Point(30, 40);
		Point moveDelta = new Point(15, 11);
		Dimension sizeDelta = new Dimension(20, 10);
		//
		request = new ChangeBoundsRequest("sss");
		request.addEditPart(editPart);
		request.setLocation(location);
		request.setMoveDelta(moveDelta);
		request.setSizeDelta(sizeDelta);
		request.setResizeDirection(IPositionConstants.EAST);
		//
		// check set location, moveDelta, sizeDelta, direction
		assertEquals("sss", request.getType());
		assertNotNull(request.getEditParts());
		assertEquals(1, request.getEditParts().size());
		assertSame(editPart, request.getEditParts().get(0));
		assertSame(location, request.getLocation());
		assertSame(moveDelta, request.getMoveDelta());
		assertSame(sizeDelta, request.getSizeDelta());
		assertEquals(IPositionConstants.EAST, request.getResizeDirection());
		//
		// check work getTransformedRectangle()
		Rectangle rectangle = new Rectangle(1, 2, 3, 4);
		Rectangle result = request.getTransformedRectangle(rectangle);
		assertNotSame(rectangle, result);
		assertEquals(new Rectangle(1, 2, 3, 4), rectangle);
		assertEquals(new Rectangle(16, 13, 23, 14), result);
		assertSame(moveDelta, request.getMoveDelta());
		assertSame(sizeDelta, request.getSizeDelta());
	}

	@Test
	public void test_LocationRequest() throws Exception {
		LocationRequest request = new LocationRequest();
		//
		// check new LocationRequest
		assertNull(request.getType());
		assertNull(request.getLocation());
		//
		// check set type and location
		request = new LocationRequest("zzz");
		request.setLocation(new Point(1, 2));
		assertEquals("zzz", request.getType());
		assertEquals(new Point(1, 2), request.getLocation());
	}

	@Test
	public void test_SelectionRequest() throws Exception {
		SelectionRequest request = new SelectionRequest();
		//
		// check new SelectionRequest
		assertNull(request.getType());
		assertNull(request.getLocation());
		assertEquals(0, request.getModifiers());
		assertEquals(0, request.getLastButtonPressed());
		assertFalse(request.isAltKeyPressed());
		assertFalse(request.isControlKeyPressed());
		assertFalse(request.isShiftKeyPressed());
		assertFalse(request.isLeftMouseButtonPressed());
		assertFalse(request.isRightMouseButtonPressed());
		assertFalse(request.isAnyMouseButtonPressed());
		//
		// check type
		request = new SelectionRequest();
		request.setType(RequestConstants.REQ_SELECTION);
		assertSame(RequestConstants.REQ_SELECTION, request.getType());
		//
		// check location
		request.setLocation(new Point(11, 22));
		assertEquals(new Point(11, 22), request.getLocation());
		//
		// check setLastButtonPressed()
		request.setLastButtonPressed(1);
		assertEquals(1, request.getLastButtonPressed());
		//
		request.setLastButtonPressed(3);
		assertEquals(3, request.getLastButtonPressed());
		//
		// check set key state mask and help methods is...
		request.setModifiers(SWT.CONTROL | SWT.SHIFT);
		assertFalse(request.isAltKeyPressed());
		assertTrue(request.isControlKeyPressed());
		assertTrue(request.isShiftKeyPressed());
		assertFalse(request.isLeftMouseButtonPressed());
		assertFalse(request.isRightMouseButtonPressed());
		assertFalse(request.isAnyMouseButtonPressed());
	}

	@Test
	public void test_CreateRequest() throws Exception {
		ICreationFactory factory = new ICreationFactory() {
			@Override
			public void activate() {
			}

			@Override
			public Object getNewObject() {
				// Has to be larger than then integer cache from [-128, 127]
				return Integer.valueOf(273);
			}
		};
		CreateRequest request = new CreateRequest(factory);
		//
		// check new CreateRequest
		assertSame(RequestConstants.REQ_CREATE, request.getType());
		assertNull(request.getLocation());
		assertNull(request.getSize());
		//
		// check set location
		request.setLocation(new Point(12, 14));
		assertEquals(new Point(12, 14), request.getLocation());
		//
		// check set size
		request.setSize(new Dimension(15, 12));
		assertEquals(new Dimension(15, 12), request.getSize());
		//
		// check work factory
		Object newObject = request.getNewObject();
		assertEquals(273, newObject);
		assertSame(newObject, request.getNewObject());
		assertNotSame(newObject, factory.getNewObject());
		//
		// check replacing "selectObject"
		{
			Object otherObject = new Object();
			assertSame(newObject, request.getSelectObject());
			request.setSelectObject(otherObject);
			assertSame(newObject, request.getNewObject());
			assertSame(otherObject, request.getSelectObject());
		}
	}

	@Test
	public void test_PasteRequest() throws Exception {
		Object memnto = "_Test_Memento_";
		PasteRequest request = new PasteRequest(memnto);
		//
		// check new PasteRequest, type and memento
		assertSame(PasteRequest.REQ_PASTE, request.getType());
		assertSame(memnto, request.getMemento());
		assertNull(request.getLocation());
		assertNull(request.getSize());
		//
		// check set location
		request.setLocation(new Point(12, 14));
		assertEquals(new Point(12, 14), request.getLocation());
		// check set size
		request.setSize(new Dimension(15, 12));
		assertEquals(new Dimension(15, 12), request.getSize());
	}
}