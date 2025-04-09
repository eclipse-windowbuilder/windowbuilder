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
package org.eclipse.wb.tests.draw2d;

import org.eclipse.wb.tests.gef.TestLogger;

import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

import java.util.Arrays;

/**
 * @author lobas_av
 *
 */
public class DebugGraphics extends SWTGraphics {
	private final TestLogger m_logger;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DebugGraphics(GC gc, TestLogger logger) {
		super(gc);
		m_logger = logger;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Graphics
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void clipRect(Rectangle rectangle) {
		m_logger.log("clipRect(" + rectangle + ")");
		super.clipRect(rectangle);
	}

	@Override
	public void drawArc(int x, int y, int width, int height, int offset, int length) {
		m_logger.log("drawArc("
				+ x
				+ ", "
				+ y
				+ ", "
				+ width
				+ ", "
				+ height
				+ ", "
				+ offset
				+ ", "
				+ length
				+ ")");
		super.drawArc(x, y, width, height, offset, length);
	}

	@Override
	public void drawFocus(int x, int y, int w, int h) {
		m_logger.log("drawFocus(" + x + ", " + y + ", " + w + ", " + h + ")");
		super.drawFocus(x, y, w, h);
	}

	@Override
	public void drawImage(Image srcImage,
			int x1,
			int y1,
			int w1,
			int h1,
			int x2,
			int y2,
			int w2,
			int h2) {
		ImageData data = srcImage.getImageData();
		m_logger.log("drawImage("
				+ data.width
				+ "x"
				+ data.height
				+ "x"
				+ data.depth
				+ ", "
				+ x1
				+ ", "
				+ y1
				+ ", "
				+ w1
				+ ", "
				+ h1
				+ ", "
				+ x2
				+ ", "
				+ y2
				+ ", "
				+ w2
				+ ", "
				+ h2
				+ ")");
		super.drawImage(srcImage, x1, y1, w1, h1, x2, y2, w2, h2);
	}

	@Override
	public void drawImage(Image srcImage, int x, int y) {
		ImageData data = srcImage.getImageData();
		m_logger.log("drawImage("
				+ data.width
				+ "x"
				+ data.height
				+ "x"
				+ data.depth
				+ ", "
				+ x
				+ ", "
				+ y
				+ ")");
		super.drawImage(srcImage, x, y);
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		m_logger.log("drawLine(" + x1 + ", " + y1 + ", " + x2 + ", " + y2);
		super.drawLine(x1, y1, x2, y2);
	}

	@Override
	public void drawOval(int x, int y, int width, int height) {
		m_logger.log("drawOval(" + x + ", " + y + ", " + width + ", " + height);
		super.drawOval(x, y, width, height);
	}

	@Override
	public void drawPolygon(PointList points) {
		m_logger.log("drawPolygon(" + Arrays.toString(points.getCopy().toIntArray()) + ")");
		super.drawPolygon(points);
	}

	@Override
	public void drawPolyline(PointList points) {
		m_logger.log("drawPolyline(" + Arrays.toString(points.getCopy().toIntArray()) + ")");
		super.drawPolyline(points);
	}

	@Override
	public void drawRectangle(int x, int y, int width, int height) {
		m_logger.log("drawRectangle(" + x + ", " + y + ", " + width + ", " + height + ")");
		super.drawRectangle(x, y, width, height);
	}

	@Override
	public void drawRoundRectangle(Rectangle r, int arcWidth, int arcHeight) {
		m_logger.log("drawRoundRectangle(" + r + ", " + arcWidth + ", " + arcHeight + ")");
		super.drawRoundRectangle(r, arcWidth, arcHeight);
	}

	@Override
	public void drawString(String s, int x, int y) {
		m_logger.log("drawString(|" + s + "|, " + x + ", " + y + ")");
		super.drawString(s, x, y);
	}

	@Override
	public void drawText(String s, int x, int y) {
		m_logger.log("drawText(|" + s + "|, " + x + ", " + y + ")");
		super.drawText(s, x, y);
	}

	@Override
	public void fillArc(int x, int y, int width, int height, int offset, int length) {
		m_logger.log("fillArc("
				+ x
				+ ", "
				+ y
				+ ", "
				+ width
				+ ", "
				+ height
				+ ", "
				+ offset
				+ ", "
				+ length
				+ ")");
		super.fillArc(x, y, width, height, offset, length);
	}

	@Override
	public void fillGradient(int x, int y, int width, int height, boolean vertical) {
		m_logger.log("fillGradient("
				+ x
				+ ", "
				+ y
				+ ", "
				+ width
				+ ", "
				+ height
				+ ", "
				+ vertical
				+ ")");
		super.fillGradient(x, y, width, height, vertical);
	}

	@Override
	public void fillOval(int x, int y, int w, int h) {
		m_logger.log("fillOval(" + x + ", " + y + ", " + w + ", " + h + ")");
		super.fillOval(x, y, w, h);
	}

	@Override
	public void fillPolygon(PointList points) {
		m_logger.log("fillPolygon(" + Arrays.toString(points.getCopy().toIntArray()) + ")");
		super.fillPolygon(points);
	}

	@Override
	public void fillRectangle(int x, int y, int width, int height) {
		m_logger.log("fillRectangle(" + x + ", " + y + ", " + width + ", " + height + ")");
		super.fillRectangle(x, y, width, height);
	}

	@Override
	public void fillRoundRectangle(Rectangle r, int arcWidth, int arcHeight) {
		m_logger.log("fillRoundRectangle(" + r + ", " + arcWidth + ", " + arcHeight + ")");
		super.fillRoundRectangle(r, arcWidth, arcHeight);
	}

	@Override
	public void fillString(String s, int x, int y) {
		m_logger.log("fillString(|" + s + "|, " + x + ", " + y + ")");
		super.fillString(s, x, y);
	}

	@Override
	public void fillText(String s, int x, int y) {
		m_logger.log("fillText(|" + s + "|, " + x + ", " + y + ")");
		super.fillText(s, x, y);
	}

	@Override
	public void popState() {
		m_logger.log("popState()");
		super.popState();
	}

	@Override
	public void pushState() {
		m_logger.log("pushState()");
		super.pushState();
	}

	@Override
	public void restoreState() {
		m_logger.log("restoreState()");
		super.restoreState();
	}

	@Override
	public void setBackgroundColor(Color color) {
		m_logger.log("setBackgroundColor(" + color + ")");
		super.setBackgroundColor(color);
	}

	@Override
	public void setFont(Font f) {
		m_logger.log("setFont(" + Arrays.toString(f.getFontData()) + ")");
		super.setFont(f);
	}

	@Override
	public void setForegroundColor(Color color) {
		m_logger.log("setForegroundColor(" + color + ")");
		super.setForegroundColor(color);
	}

	@Override
	public void setLineStyle(int style) {
		m_logger.log("setLineStyle(" + style + ")");
		super.setLineStyle(style);
	}

	@Override
	public void setLineWidth(int width) {
		m_logger.log("setLineWidth(" + width + ")");
		super.setLineWidth(width);
	}

	@Override
	public void setXORMode(boolean b) {
		m_logger.log("setXORMode(" + b + ")");
		super.setXORMode(b);
	}

	@Override
	public void translate(int dx, int dy) {
		m_logger.log("translate(" + dx + ", " + dy + ")");
		super.translate(dx, dy);
	}
}