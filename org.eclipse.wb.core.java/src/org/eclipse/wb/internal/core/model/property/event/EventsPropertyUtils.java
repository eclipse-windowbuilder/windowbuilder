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
package org.eclipse.wb.internal.core.model.property.event;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * Events property utilities.
 *
 * @author mitin_aa
 * @coverage core.model.property.events
 */
public final class EventsPropertyUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Image constants for to display in actions
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final ImageDescriptor LISTENER_METHOD_IMAGE_DESCRIPTOR =
      DesignerPlugin.getImageDescriptor("events/listener_method.gif");
  public static final ImageDescriptor LISTENER_INTERFACE_IMAGE_DESCRIPTOR =
      DesignerPlugin.getImageDescriptor("events/listener_interface.gif");
  public static final Image LISTENER_CLASS_IMAGE =
      DesignerPlugin.getImage("events/listener_class.gif");
  public static final Image LISTENER_INTERFACE_IMAGE =
      DesignerPlugin.getImage("events/listener_interface.gif");
  public static final Image EXISTING_CLASS_IMAGE =
      DesignerPlugin.getImage("events/existing_class.gif");
  public static final Image EXISTING_INTERFACE_IMAGE =
      DesignerPlugin.getImage("events/existing_interface.gif");
}
