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
package org.eclipse.wb.tests.designer.swt.model.property;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.utils.ManagerUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.SWT;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Tests for {@link org.eclipse.wb.swt.ResourceManager}.
 *
 * @author lobas_av
 */
public class ResourceManagerTest extends RcpModelTest {
	private Class<?> DisplayClass;
	private Object m_defaultDisplay;
	private Class<?> ManagerClass;
	private Class<?> ImageDescriptorClass;
	private Class<?> ImageClass;

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		CompositeInfo shell =
				parseComposite(
						"// filler filler filler",
						"public class Test extends Shell {",
						"  public Test() {",
						"  }",
						"}");
		// add ResourceManager
		ManagerUtils.ensure_ResourceManager(shell);
		// load classes
		DisplayClass = m_lastLoader.loadClass("org.eclipse.swt.widgets.Display");
		ImageClass = m_lastLoader.loadClass("org.eclipse.swt.graphics.Image");
		ImageDescriptorClass = m_lastLoader.loadClass("org.eclipse.jface.resource.ImageDescriptor");
		ManagerClass = m_lastLoader.loadClass("org.eclipse.wb.swt.ResourceManager");
		m_defaultDisplay = ReflectionUtils.invokeMethod(DisplayClass, "getDefault()");
	}

	@Override
	protected void tearDown() throws Exception {
		if (m_testProject != null && ManagerClass != null) {
			ReflectionUtils.invokeMethod(ManagerClass, "dispose()");
		}
		super.tearDown();
		// clear fields
		DisplayClass = null;
		ManagerClass = null;
		ImageDescriptorClass = null;
		ImageClass = null;
		m_defaultDisplay = null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_getImageDescriptor() throws Exception {
		// create image descriptor
		Object imageDescriptor =
				ReflectionUtils.invokeMethod(
						ManagerClass,
						"getImageDescriptor(java.lang.Class,java.lang.String)",
						ManagerClass,
						"/javax/swing/plaf/basic/icons/JavaCup16.png");
		// check create
		assertNotNull(imageDescriptor);
	}

	public void test_getImage_null() throws Exception {
		Object imageDescriptor = null;
		Object image =
				ReflectionUtils.invokeMethod(
						ManagerClass,
						"getImage(org.eclipse.jface.resource.ImageDescriptor)",
						imageDescriptor);
		assertNull(image);
	}

	public void test_getImage() throws Exception {
		// create image descriptor
		Object imageDescriptor =
				ReflectionUtils.invokeMethod(
						ImageDescriptorClass,
						"createFromFile(java.lang.Class,java.lang.String)",
						ManagerClass,
						"/javax/swing/plaf/basic/icons/JavaCup16.png");
		// check create
		assertNotNull(imageDescriptor);
		//
		Object image =
				ReflectionUtils.invokeMethod(
						ManagerClass,
						"getImage(org.eclipse.jface.resource.ImageDescriptor)",
						imageDescriptor);
		assertNotNull(image);
		// check state
		assertFalse((Boolean) ReflectionUtils.invokeMethod(image, "isDisposed()"));
		assertSame(image, ReflectionUtils.invokeMethod(
				ManagerClass,
				"getImage(org.eclipse.jface.resource.ImageDescriptor)",
				imageDescriptor));
		// check internal state of ResourceManager
		Object descriptorImageMap =
				ReflectionUtils.getFieldObject(ManagerClass, "m_descriptorImageMap");
		assertEquals(1, ReflectionUtils.invokeMethod(descriptorImageMap, "size()"));
		// dispose image resource's
		ReflectionUtils.invokeMethod(ManagerClass, "disposeImages()");
		// check internal state of ResourceManager
		assertEquals(0, ReflectionUtils.invokeMethod(descriptorImageMap, "size()"));
		// check new image state
		assertTrue((Boolean) ReflectionUtils.invokeMethod(image, "isDisposed()"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Decorate Image
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_decorateImage_wrongCorner() throws Exception {
		// check out of range corner values to left
		try {
			ReflectionUtils.invokeMethod(
					ManagerClass,
					"decorateImage(org.eclipse.swt.graphics.Image,org.eclipse.swt.graphics.Image,int)",
					null,
					null,
					0);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Wrong decorate corner", e.getMessage());
		}
		// check out of range corner values to right
		try {
			ReflectionUtils.invokeMethod(
					ManagerClass,
					"decorateImage(org.eclipse.swt.graphics.Image,org.eclipse.swt.graphics.Image,int)",
					null,
					null,
					5);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Wrong decorate corner", e.getMessage());
		}
	}

	public void test_decorateImage() throws Exception {
		// prepare default corner for decorateImage()
		int corner = ReflectionUtils.getFieldInt(ManagerClass, "BOTTOM_RIGHT");
		// create base and decorator images
		Object[] images = createImages();
		Object base = images[0];
		Object decorator = images[1];
		try {
			// decorate image over decorateImage(Image, Image, corner)
			Object image =
					ReflectionUtils.invokeMethod(
							ManagerClass,
							"decorateImage(org.eclipse.swt.graphics.Image,org.eclipse.swt.graphics.Image,int)",
							base,
							decorator,
							corner);
			// check create
			assertNotNull(image);
			// check state
			assertFalse((Boolean) ReflectionUtils.invokeMethod(image, "isDisposed()"));
			// check equals with image created over decorateImage(Image, Image)
			assertSame(image, ReflectionUtils.invokeMethod(
					ManagerClass,
					"decorateImage(org.eclipse.swt.graphics.Image,org.eclipse.swt.graphics.Image)",
					base,
					decorator));
		} finally {
			// dispose base and decorator images
			ReflectionUtils.invokeMethod(base, "dispose()");
			ReflectionUtils.invokeMethod(decorator, "dispose()");
		}
	}

	public void test_decorateImage2() throws Exception {
		// prepare tested corner's
		int corner = ReflectionUtils.getFieldInt(ManagerClass, "BOTTOM_RIGHT");
		int corner1 = ReflectionUtils.getFieldInt(ManagerClass, "TOP_LEFT");
		// create base and decorator images
		Object[] images = createImages();
		Object base = images[0];
		Object decorator = images[1];
		try {
			// decorate BOTTOM_RIGHT image
			Object image =
					ReflectionUtils.invokeMethod(
							ManagerClass,
							"decorateImage(org.eclipse.swt.graphics.Image,org.eclipse.swt.graphics.Image,int)",
							base,
							decorator,
							corner);
			// check create
			assertNotNull(image);
			// check state
			assertSame(image, ReflectionUtils.invokeMethod(
					ManagerClass,
					"decorateImage(org.eclipse.swt.graphics.Image,org.eclipse.swt.graphics.Image,int)",
					base,
					decorator,
					corner));
			// decorate TOP_LEFT image
			Object image1 =
					ReflectionUtils.invokeMethod(
							ManagerClass,
							"decorateImage(org.eclipse.swt.graphics.Image,org.eclipse.swt.graphics.Image,int)",
							base,
							decorator,
							corner1);
			// check create
			assertNotNull(image1);
			assertSame(image1, ReflectionUtils.invokeMethod(
					ManagerClass,
					"decorateImage(org.eclipse.swt.graphics.Image,org.eclipse.swt.graphics.Image,int)",
					base,
					decorator,
					corner1));
			// check work ResourceManager.decorateImage() with multi images
			assertNotSame(image, image1);
		} finally {
			// dispose base and decorator images
			ReflectionUtils.invokeMethod(base, "dispose()");
			ReflectionUtils.invokeMethod(decorator, "dispose()");
		}
	}

	public void test_decorateImage_TOP_LEFT() throws Exception {
		int corner = ReflectionUtils.getFieldInt(ManagerClass, "TOP_LEFT");
		test_decorateImage(corner);
	}

	public void test_decorateImage_TOP_RIGHT() throws Exception {
		int corner = ReflectionUtils.getFieldInt(ManagerClass, "TOP_RIGHT");
		test_decorateImage(corner);
	}

	public void test_decorateImage_BOTTOM_LEFT() throws Exception {
		int corner = ReflectionUtils.getFieldInt(ManagerClass, "BOTTOM_LEFT");
		test_decorateImage(corner);
	}

	public void test_decorateImage_BOTTOM_RIGHT() throws Exception {
		int corner = ReflectionUtils.getFieldInt(ManagerClass, "BOTTOM_RIGHT");
		test_decorateImage(corner);
	}

	private void test_decorateImage(int corner) throws Exception {
		// create base and decorator images
		Object[] images = createImages();
		Object base = images[0];
		Object decorator = images[1];
		try {
			// decorate image with given corner
			Object image =
					ReflectionUtils.invokeMethod(
							ManagerClass,
							"decorateImage(org.eclipse.swt.graphics.Image,org.eclipse.swt.graphics.Image,int)",
							base,
							decorator,
							corner);
			// check state
			assertNotNull(image);
			assertFalse((Boolean) ReflectionUtils.invokeMethod(image, "isDisposed()"));
			assertSame(image, ReflectionUtils.invokeMethod(
					ManagerClass,
					"decorateImage(org.eclipse.swt.graphics.Image,org.eclipse.swt.graphics.Image,int)",
					base,
					decorator,
					corner));
			// prepare decorator, base and expected pixel's
			int blackPixel = getPixels(decorator)[0][0];
			int[][] result = getPixels(base);
			if (corner == 1) { // TOP_LEFT
				result[0][0] = blackPixel;
			} else if (corner == 2) { // TOP_RIGHT
				result[2][0] = blackPixel;
			} else if (corner == 3) { // BOTTOM_LEFT
				result[0][2] = blackPixel;
			} else if (corner == 4) { // BOTTOM_RIGHT
				result[2][2] = blackPixel;
			}
			// check expected and actual pixel's
			assertEquals(getText(result), getText(getPixels(image)));
		} finally {
			// dispose base and decorator images
			ReflectionUtils.invokeMethod(base, "dispose()");
			ReflectionUtils.invokeMethod(decorator, "dispose()");
		}
	}

	/**
	 * @return base(3x3, white) and decorate(1x1, black) images.
	 */
	private Object[] createImages() throws Exception {
		// prepare image constructor
		Constructor<?> constructor =
				ReflectionUtils.getConstructorBySignature(
						ImageClass,
						"<init>(org.eclipse.swt.graphics.Device,int,int)");
		// create base image
		Object baseImage = constructor.newInstance(null, 3, 3);
		fillImage(baseImage, SWT.COLOR_WHITE, 3, 3);
		// create decorate image
		Object decoratorImage = constructor.newInstance(null, 1, 1);
		fillImage(decoratorImage, SWT.COLOR_BLACK, 1, 1);
		//
		return new Object[]{baseImage, decoratorImage};
	}

	/**
	 * Fill given image for given color.
	 */
	private void fillImage(Object image, int color, int width, int height) throws Exception {
		// create GC
		Class<?> GCClass = m_lastLoader.loadClass("org.eclipse.swt.graphics.GC");
		Object gc =
				ReflectionUtils.getConstructorBySignature(
						GCClass,
						"<init>(org.eclipse.swt.graphics.Drawable)").newInstance(image);
		// prepare color
		Object colorObject =
				ReflectionUtils.invokeMethod(m_defaultDisplay, "getSystemColor(int)", color);
		// fill
		ReflectionUtils.invokeMethod(gc, "setBackground(org.eclipse.swt.graphics.Color)", colorObject);
		ReflectionUtils.invokeMethod(gc, "fillRectangle(int,int,int,int)", 0, 0, width, height);
		// release GC
		ReflectionUtils.invokeMethod(gc, "dispose()");
	}

	/**
	 * @return 2D int array of pixel's for given image.
	 */
	private static int[][] getPixels(Object image) throws Exception {
		// prepare image data
		Object data = ReflectionUtils.invokeMethod(image, "getImageData()");
		// prepare image palette
		Object palette = ReflectionUtils.getFieldObject(data, "palette");
		// prepare image size
		int width = ReflectionUtils.getFieldInt(data, "width");
		int height = ReflectionUtils.getFieldInt(data, "height");
		// create result pixel's
		int[][] pixels = new int[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				// prepare pixel value
				int pixel = (Integer) ReflectionUtils.invokeMethod(data, "getPixel(int,int)", i, j);
				// convert pixel to color value (red component)
				Object rgb = ReflectionUtils.invokeMethod(palette, "getRGB(int)", pixel);
				pixels[i][j] = ReflectionUtils.getFieldInt(rgb, "red");
			}
		}
		return pixels;
	}

	/**
	 * @return string presentation for 2D int array.
	 */
	private static String getText(int[][] data) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int[] line = data[i];
			for (int j = 0; j < line.length; j++) {
				buffer.append(line[j]);
				buffer.append(' ');
			}
			buffer.append('\n');
		}
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Plugin
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_getPluginImage() throws Exception {
		URL[] urls =
				new URL[]{m_testProject.getProject().getFile("bin").getLocation().toFile().toURL()};
		URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader());
		Class<?> local_ManagerClass = classLoader.loadClass("org.eclipse.wb.swt.ResourceManager");
		// create image
		Object image =
				ReflectionUtils.invokeMethod(
						local_ManagerClass,
						"getPluginImage(java.lang.String,java.lang.String)",
						"org.eclipse.jdt.ui",
						"/icons/full/elcl16/ch_cancel.png");
		// check create
		assertNotNull(image);
		// check state
		assertFalse((Boolean) ReflectionUtils.invokeMethod(image, "isDisposed()"));
		assertSame(image, ReflectionUtils.invokeMethod(
				local_ManagerClass,
				"getPluginImage(java.lang.String,java.lang.String)",
				"org.eclipse.jdt.ui",
				"/icons/full/elcl16/ch_cancel.png"));
		assertNull(ReflectionUtils.invokeMethod(
				local_ManagerClass,
				"getPluginImage(java.lang.String,java.lang.String)",
				"org.eclipse.jdt.ui",
				"zzz"));
		// check internal state of ResourceManager
		Object URLImageMap = ReflectionUtils.getFieldObject(local_ManagerClass, "m_URLImageMap");
		assertEquals(1, ReflectionUtils.invokeMethod(URLImageMap, "size()"));
		// dispose image resource's
		ReflectionUtils.invokeMethod(local_ManagerClass, "disposeImages()");
		// check internal state of ResourceManager
		assertEquals(0, ReflectionUtils.invokeMethod(URLImageMap, "size()"));
		// check new image state
		assertTrue((Boolean) ReflectionUtils.invokeMethod(image, "isDisposed()"));
	}

	public void test_getPluginImageDescriptor() throws Exception {
		URL[] urls =
				new URL[]{m_testProject.getProject().getFile("bin").getLocation().toFile().toURL()};
		URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader());
		Class<?> local_ManagerClass = classLoader.loadClass("org.eclipse.wb.swt.ResourceManager");
		// create image descriptor
		assertNotNull(ReflectionUtils.invokeMethod(
				local_ManagerClass,
				"getPluginImageDescriptor(java.lang.String,java.lang.String)",
				"org.eclipse.jdt.ui",
				"/icons/full/elcl16/ch_cancel.png"));
		assertNull(ReflectionUtils.invokeMethod(
				local_ManagerClass,
				"getPluginImageDescriptor(java.lang.String,java.lang.String)",
				"org.eclipse.jdt.ui",
				"zzz"));
	}
}