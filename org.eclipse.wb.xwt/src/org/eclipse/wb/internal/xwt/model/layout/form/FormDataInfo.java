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
package org.eclipse.wb.internal.xwt.model.layout.form;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.swt.model.layout.form.FormSide;
import org.eclipse.wb.internal.swt.model.layout.form.IFormDataInfo;
import org.eclipse.wb.internal.swt.support.FormLayoutSupport;
import org.eclipse.wb.internal.xwt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * SWT {@link FormData} model. This is related to {@link FormLayout}.
 *
 * @author mitin_aa
 * @coverage XWT.model.layout
 */
public final class FormDataInfo extends LayoutDataInfo implements IFormDataInfo<ControlInfo> {
	private ComplexProperty m_propertyLeft;
	private ComplexProperty m_propertyRight;
	private ComplexProperty m_propertyTop;
	private ComplexProperty m_propertyBottom;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormDataInfo(EditorContext editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		// after parsing bind this registry into hierarchy
		addBroadcastListener(new ObjectInfoTreeComplete() {
			@Override
			public void invoke() throws Exception {
				removeBroadcastListener(this);
				// find and assign FormAttachments
				ControlInfo parentControl = (ControlInfo) getParent();
				initAttachments(parentControl);
			}
		});
	}

	/**
	 * Initializes {@link FormAttachmentInfo} instances for all possible sides. Performs search for
	 * existing and creates virtual new if found nothing.
	 *
	 * @param parentControl
	 *          the control for which attachments would be searched.
	 */
	private void initAttachments(ControlInfo parentControl) throws Exception {
		initAttachment(FormSide.LEFT, parentControl);
		initAttachment(FormSide.RIGHT, parentControl);
		initAttachment(FormSide.TOP, parentControl);
		initAttachment(FormSide.BOTTOM, parentControl);
	}

	/**
	 * Initializes {@link FormAttachmentInfo} instance for <code>fieldName</code>. Performs search for
	 * existing and creates virtual new if found nothing.
	 *
	 * @param side
	 *          the {@link FormSide} that describes field of {@link FormData} in which this attachment
	 *          may already assigned.
	 * @param parentControl
	 *          the control which attachment initialized.
	 */
	private void initAttachment(FormSide side, ControlInfo parentControl) throws Exception {
		String sideFieldName = side.getField();
		DocumentElement attachmentElement = getAttachmentElement(sideFieldName);
		if (attachmentElement != null) {
			List<FormAttachmentInfo> atts = getChildren(FormAttachmentInfo.class);
			for (FormAttachmentInfo attachment : atts) {
				if (getAttachmentSideName(attachment).equals(sideFieldName)) {
					attachment.setSide(side);
					return;
				}
			}
		} else {
			// virtual
			addVirtualAttachment(side);
		}
	}

	private static String getAttachmentSideName(FormAttachmentInfo attachment) {
		String property = attachment.getElement().getParent().getTag();
		return StringUtils.removeStart(property, "FormData.");
	}

	private DocumentElement getAttachmentElement(String field) {
		DocumentElement thisElement = getCreationSupport().getElement();
		DocumentElement attachmentParentElement = thisElement.getChild("FormData." + field, false);
		if (attachmentParentElement != null) {
			return attachmentParentElement.getChildAt(0);
		}
		return null;
	}

	private FormAttachmentInfo addVirtualAttachment(FormSide side) throws Exception {
		CreationSupport creationSupport =
				new VirtualFormAttachmentCreationSupport(this,
						FormLayoutSupport.createFormAttachment(),
						side);
		FormAttachmentInfo attachment =
				(FormAttachmentInfo) XmlObjectUtils.createObject(
						getContext(),
						FormAttachment.class,
						creationSupport);
		attachment.setSide(side);
		addChild(attachment);
		return attachment;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @param side
	 *          can be either of IPositionConstants.LEFT, IPositionConstants.RIGHT,
	 *          IPositionConstants.TOP, IPositionConstants.BOTTOM
	 */
	@Override
	public FormAttachmentInfo getAttachment(int sideInt) throws Exception {
		FormSide side = FormSide.get(sideInt);
		for (ObjectInfo child : getChildren()) {
			if (child instanceof FormAttachmentInfo) {
				FormAttachmentInfo attachment = (FormAttachmentInfo) child;
				if (attachment.getSide() == side) {
					return attachment;
				}
			}
		}
		return addVirtualAttachment(side);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Width/height
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setWidth(int value) throws Exception {
		getPropertyByTitle("width").setValue(value);
	}

	@Override
	public void setHeight(int value) throws Exception {
		getPropertyByTitle("height").setValue(value);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Attachment properties
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_finish() throws Exception {
		super.refresh_finish();
		ensureAttachmentProperties();
		updateAttachmentPropertyText(IPositionConstants.LEFT, m_propertyLeft);
		updateAttachmentPropertyText(IPositionConstants.RIGHT, m_propertyRight);
		updateAttachmentPropertyText(IPositionConstants.TOP, m_propertyTop);
		updateAttachmentPropertyText(IPositionConstants.BOTTOM, m_propertyBottom);
	}

	@Override
	protected List<Property> getPropertyList() throws Exception {
		ensureAttachmentProperties();
		return super.getPropertyList();
	}

	private void updateAttachmentPropertyText(int side, ComplexProperty property) throws Exception {
		FormAttachmentInfo attachment = getAttachment(side);
		property.setText(attachment.toString());
	}

	private void ensureAttachmentProperties() throws Exception {
		if (m_propertyLeft == null) {
			m_propertyLeft = createAttachmentProperty(IPositionConstants.LEFT, "left");
			m_propertyRight = createAttachmentProperty(IPositionConstants.RIGHT, "right");
			m_propertyTop = createAttachmentProperty(IPositionConstants.TOP, "top");
			m_propertyBottom = createAttachmentProperty(IPositionConstants.BOTTOM, "bottom");
		}
	}

	@Override
	protected void sortPropertyList(List<Property> properties) {
		List<Property> sorted = Lists.newArrayList();
		sorted.add(m_propertyLeft);
		sorted.add(m_propertyRight);
		sorted.add(m_propertyTop);
		sorted.add(m_propertyBottom);
		sorted.add(PropertyUtils.getByTitle(properties, "width"));
		sorted.add(PropertyUtils.getByTitle(properties, "height"));
		properties.clear();
		properties.addAll(sorted);
	}

	/**
	 * Creates the {@link ComplexProperty} for {@link FormAttachmentInfo} by attachment's property
	 * name.
	 */
	private ComplexProperty createAttachmentProperty(int side, String title) throws Exception {
		final FormAttachmentInfo attachment = getAttachment(side);
		ComplexProperty attachmentProperty = new ComplexProperty(title, attachment.toString()) {
			@Override
			public void setValue(Object value) throws Exception {
				if (value == Property.UNKNOWN_VALUE) {
					attachment.delete();
				}
			}
		};
		Collection<?> selectedProperties =
				CollectionUtils.select(Arrays.asList(attachment.getProperties()), new Predicate() {
					@Override
					public boolean evaluate(Object object) {
						Property property = (Property) object;
						return !property.getTitle().equals("Class")
								&& !property.getTitle().equals("Constructor");
					}
				});
		attachmentProperty.setProperties(selectedProperties.toArray(new Property[selectedProperties.size()]));
		return attachmentProperty;
	}
}