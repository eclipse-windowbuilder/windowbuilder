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
package org.eclipse.wb.core.model;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;

import org.eclipse.swt.graphics.Image;

/**
 * Interface model for any GUI component.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface IAbstractComponentInfo extends IObjectInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Insets
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the insets of client area of this component. {@link #getBounds()} returns bounds
   *         relative to the client area of parent, but often we need bounds relative to the
   *         top-left point of parent.
   */
  Insets getClientAreaInsets();

  /**
   * Sets the insets of client area of this component.
   */
  void setClientAreaInsets(Insets clientAreaInsets);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this container uses right-to-left orientation.
   */
  boolean isRTL();

  /**
   * @return the preferred size of this component. If this {@link IAbstractComponentInfo} is in
   *         process of creation, {@link #getLivePreferredSize()} will be used to get "live"
   *         preferred size.
   */
  Dimension getPreferredSize();

  /**
   * @return the toolkit {@link Object} instance created for this {@link IAbstractComponentInfo}.
   */
  Object getObject();

  /**
   * @return the {@link Image} of this component, can be <code>null</code> if GUI toolkit does not
   *         support getting separate images for components. If this {@link IAbstractComponentInfo}
   *         is in process of creation, "live" image will be returned.
   */
  Image getImage();

  /**
   * @return the baseline of this component. If this {@link IAbstractComponentInfo} is in process of
   *         creation, {@link #getLiveBaseline()} will be used to get "live" baseline.
   */
  int getBaseline();

  /**
   * @return the {@link ITopBoundsSupport} for this component.
   */
  ITopBoundsSupport getTopBoundsSupport();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the "shot" bounds.
   *
   * The "shot" bounds is the bounds relative top-left corner of the parent including top and left
   * border size, i.e. parent insets left and insets top. The transformation between "model" bounds
   * and "shot" bounds is:
   *
   * <pre><code>
	 * shotBounds.x = modelBounds.x + parentInsets.left;
	 * shotBounds.y = modelBounds.y + parentInsets.top;
	 * </code></pre>
   *
   * If this {@link AbstractComponentInfo} is in process of "paste", absolute bounds of source
   * component will be returned (i.e. bounds relative top-left corner of source root component).
   *
   * @return the "shot" bounds.
   */
  Rectangle getBounds();

  /**
   * Sets new "shot" bounds of this component.
   *
   * The "shot" bounds is the bounds relative to top-left corner of the parent including top and
   * left border size, i.e. parent insets left and insets top.
   *
   * The transformation between "model" bounds and "shot" bounds is:
   *
   * <pre><code>
	 * shotBounds.x = modelBounds.x + parentInsets.left;
	 * shotBounds.y = modelBounds.y + parentInsets.top;
	 * </code></pre>
   *
   * This method should be used by subclasses during fetching visual information.
   */
  void setBounds(Rectangle bounds);

  /**
   * Return the bounds in terms of GUI toolkit ("model" bounds).
   *
   * The "model" bounds is the bounds relative to top-left corner of parent's client area. This
   * bounds value is usually the bounds of component object, placed on parent container.
   *
   * The transformation between "shot" bounds and "model" bounds is:
   *
   * <pre><code>
	 * modelBounds.x = shotBounds.x - parentInsets.left;
	 * modelBounds.y = shotBounds.y - parentInsets.top;
	 * </code></pre>
   *
   * During paste process this bounds is not actually a "model" bounds, but same as "shot" bounds
   * during paste. See {@link #getBounds(Rectangle)}.
   *
   * @return the bounds in terms of GUI toolkit ("model" bounds).
   */
  Rectangle getModelBounds();

  /**
   * Sets new "model" bounds of this component.
   *
   * The "model" bounds is the bounds relative to top-left corner of parent's client area. This
   * bounds value is usually the bounds of component object, placed on parent container.
   *
   * The transformation between "shot" bounds and "model" bounds is:
   *
   * <pre><code>
	 * modelBounds.x = shotBounds.x - parentInsets.left;
	 * modelBounds.y = shotBounds.y - parentInsets.top;
	 * </code></pre>
   *
   * This method should be used by subclasses during fetching visual information.
   *
   * During paste process this bounds is not actually a "model" bounds, but same as "shot" bounds
   * during paste. See {@link #getBounds(Rectangle)}.
   */
  void setModelBounds(Rectangle bounds);
}