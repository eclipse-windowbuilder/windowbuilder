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
package org.eclipse.wb.internal.core.model.clipboard;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.ui.ImageUtils;

import org.eclipse.swt.graphics.Image;

import java.io.ByteArrayInputStream;

/**
 * Container for copy/paste information about {@link AbstractComponentInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.clipboard
 */
public final class ComponentInfoMemento extends JavaInfoMemento {
  private static final long serialVersionUID = 0L;
  private final Rectangle m_bounds;
  private final byte[] m_imageBytes;
  private transient Image m_image;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  protected ComponentInfoMemento(AbstractComponentInfo component) throws Exception {
    super(component);
    m_bounds = component.getAbsoluteBounds();
    // prepare bytes for image
    {
      Image image = component.getImage();
      if (image != null) {
        m_imageBytes = ImageUtils.getBytesPNG(image);
      } else {
        m_imageBytes = null;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the image of component, recorded from source component.
   */
  public Image getImage() {
    if (m_image == null && m_imageBytes != null) {
      m_image = new Image(null, new ByteArrayInputStream(m_imageBytes));
    }
    return m_image;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public JavaInfo create(JavaInfo existingHierarchyObject) throws Exception {
    AbstractComponentInfo component = (AbstractComponentInfo) super.create(existingHierarchyObject);
    component.setBounds(m_bounds);
    component.setModelBounds(m_bounds);
    return component;
  }

  @Override
  public void apply() throws Exception {
    super.apply();
    // now operation in finished and we can dispose image
    if (m_image != null) {
      m_image.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the associated {@link Image} of given <code>component</code> if this component has the
   *         associated {@link ComponentInfoMemento}. Otherwise returns <code>null</code>.
   */
  public static Image getImage(AbstractComponentInfo component) {
    ComponentInfoMemento memento =
        (ComponentInfoMemento) component.getArbitraryValue(JavaInfoMemento.KEY_MEMENTO);
    if (memento != null) {
      return memento.getImage();
    }
    return null;
  }
}
