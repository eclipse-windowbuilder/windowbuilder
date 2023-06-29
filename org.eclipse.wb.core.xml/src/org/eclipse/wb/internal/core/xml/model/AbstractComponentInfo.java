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
package org.eclipse.wb.internal.core.xml.model;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.internal.core.laf.BaselineSupportHelper;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.NotImplementedException;

/**
 * Abstract model for any GUI component.
 *
 * @author scheglov_ke
 * @coverage XML.model
 */
public abstract class AbstractComponentInfo extends XmlObjectInfo implements IAbstractComponentInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractComponentInfo(EditorContext context,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(context, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Bounds
	//
	////////////////////////////////////////////////////////////////////////////
	private Rectangle m_bounds;
	private Rectangle m_modelBounds;

	@Override
	public final Rectangle getBounds() {
		return m_bounds;
	}

	@Override
	public final void setBounds(Rectangle bounds) {
		m_bounds = bounds;
	}

	@Override
	public final Rectangle getModelBounds() {
		return m_modelBounds;
	}

	@Override
	public final void setModelBounds(Rectangle bounds) {
		m_modelBounds = bounds;
		// set "shot" bounds
		m_bounds = m_modelBounds.getCopy();
		if (getParent() instanceof AbstractComponentInfo) {
			AbstractComponentInfo parentComponent = (AbstractComponentInfo) getParent();
			m_bounds.performTranslate(parentComponent.getClientAreaInsets());
		}
	}

	/**
	 * @return the absolute bounds, i.e. bounds relative to the top-left point of root component.
	 */
	public Rectangle getAbsoluteBounds() {
		Rectangle bounds = getBounds().getCopy();
		// make relative to screen
		{
			AbstractComponentInfo parent = (AbstractComponentInfo) getParent();
			while (parent != null) {
				bounds.performTranslate(parent.getBounds().getLocation());
				parent = (AbstractComponentInfo) parent.getParent();
			}
		}
		// make relative to root component
		{
			AbstractComponentInfo rootComponent = (AbstractComponentInfo) getRoot();
			bounds.performTranslate(rootComponent.getBounds().getLocation().getNegated());
		}
		// OK, final result
		return bounds;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if need draw dots border for this component.
	 */
	public boolean shouldDrawDotsBorder() throws Exception {
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Preferred size
	//
	////////////////////////////////////////////////////////////////////////////
	private Dimension m_preferredSize;

	@Override
	public final Dimension getPreferredSize() {
		if (m_preferredSize == null) {
			return getLivePreferredSize();
		}
		return m_preferredSize;
	}

	/**
	 * Sets preferred size of this component.
	 *
	 * This method should be used by subclasses during fetching visual information.
	 */
	public final void setPreferredSize(Dimension preferredSize) {
		m_preferredSize = preferredSize;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Baseline
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final int getBaseline() {
		Object object = getObject();
		if (object == null) {
			return getLiveBaseline();
		}
		return BaselineSupportHelper.getBaseline(object);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Insets
	//
	////////////////////////////////////////////////////////////////////////////
	private Insets m_clientAreaInsets = IFigure.NO_INSETS;

	@Override
	public final Insets getClientAreaInsets() {
		return m_clientAreaInsets;
	}

	@Override
	public final void setClientAreaInsets(Insets clientAreaInsets) {
		m_clientAreaInsets = clientAreaInsets;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Right-to-left
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if this container uses right-to-left orientation.
	 */
	@Override
	public boolean isRTL() {
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Image
	//
	////////////////////////////////////////////////////////////////////////////
	private Image m_image;

	@Override
	public final Image getImage() {
		if (m_image == null && getParent() == null) {
			return getLiveImage();
		}
		return m_image;
	}

	/**
	 * Sets new image of this component.<br>
	 * This method should be used by subclasses during fetching visual information.
	 */
	public final void setImage(Image image) {
		m_image = image;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// "Live" support
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the image of this component that should be used during creation.
	 */
	protected Image getLiveImage() {
		return null;
	}

	/**
	 * @return the preferred size of this component that should be used during creation.
	 */
	protected Dimension getLivePreferredSize() {
		Image liveImage = getLiveImage();
		if (liveImage != null) {
			return new Dimension(liveImage.getBounds().width, liveImage.getBounds().height);
		}
		return null;
	}

	/**
	 * @return the baseline of this component that should be used during creation.
	 */
	protected int getLiveBaseline() {
		return -1;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void delete() throws Exception {
		refresh_dispose();
		super.delete();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void refresh_dispose() throws Exception {
		// dispose image
		if (m_image != null) {
			m_image.dispose();
			m_image = null;
		}
		// call "super"
		super.refresh_dispose();
	}

	@Override
	protected void refresh_afterCreate() throws Exception {
		// call "super"
		super.refresh_afterCreate();
		// apply top bounds for root component
		if (isRoot()) {
			getTopBoundsSupport().apply();
		}
	}

	/**
	 * @return the visual object for this {@link AbstractComponentInfo}. In most cases this is same
	 *         object as {@link #getObject()}, but sometimes component is not visual itself, but has
	 *         visual presentation.
	 */
	public Object getComponentObject() {
		return getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TopBoundsSupport
	//
	////////////////////////////////////////////////////////////////////////////
	private TopBoundsSupport m_topBoundsSupport;

	/**
	 * @return the {@link TopBoundsSupport} for this component.
	 */
	@Override
	public final TopBoundsSupport getTopBoundsSupport() {
		if (m_topBoundsSupport == null) {
			m_topBoundsSupport = createTopBoundsSupport();
		}
		return m_topBoundsSupport;
	}

	/**
	 * @return the new {@link TopBoundsSupport} instance for this component.
	 */
	protected TopBoundsSupport createTopBoundsSupport() {
		throw new NotImplementedException();
	}
}
