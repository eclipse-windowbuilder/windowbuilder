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
package org.eclipse.wb.internal.swt.model.layout.form;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.core.model.broadcast.JavaInfoTreeAlmostComplete;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.support.FormLayoutSupport;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * SWT {@link FormData} model. This is related to {@link FormLayout}.
 *
 * @author mitin_aa
 * @coverage swt.model.layout.form
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
	public FormDataInfo(AstEditor editor, ComponentDescription description, CreationSupport creationSupport)
			throws Exception {
		super(editor, description, creationSupport);
		// after parsing bind this registry into hierarchy
		addBroadcastListener(new JavaInfoTreeAlmostComplete() {
			@Override
			public void invoke(JavaInfo root, List<JavaInfo> components) throws Exception {
				removeBroadcastListener(this);
				// find and assign FormAttachments
				initAttachments(components);
			}
		});
	}

	/**
	 * Initializes {@link FormAttachmentInfo} instances for all possible sides.
	 * Performs search for existing and creates virtual new if found nothing.
	 *
	 * @param components the {@link List} of all components.
	 * @throws Exception
	 */
	private void initAttachments(List<JavaInfo> components) throws Exception {
		initAttachment(FormSide.LEFT, components);
		initAttachment(FormSide.RIGHT, components);
		initAttachment(FormSide.TOP, components);
		initAttachment(FormSide.BOTTOM, components);
	}

	/**
	 * Initializes {@link FormAttachmentInfo} instance for <code>fieldName</code>.
	 * Performs search for existing and creates virtual new if found nothing.
	 *
	 * @param side       the {@link FormSide} that describes field of
	 *                   {@link FormData} in which this attachment may already
	 *                   assigned.
	 * @param components the {@link List} of all components.
	 */
	private void initAttachment(FormSide side, List<JavaInfo> components) throws Exception {
		Assignment fieldAssignment = getFieldAssignment(side.getField());
		if (fieldAssignment != null) {
			Expression attachmentExpression = fieldAssignment.getRightHandSide();
			for (JavaInfo component : components) {
				if (component instanceof FormAttachmentInfo) {
					if (component.isRepresentedBy(attachmentExpression)) {
						FormAttachmentInfo attachment = (FormAttachmentInfo) component;
						attachment.setSide(side);
						attachment.setAssociation(new EmptyAssociation());
						addChild(component);
						attachment.readPropertiesValue();
						return;
					}
				}
			}
		}
		addVirtualAttachment(side);
	}

	private FormAttachmentInfo addVirtualAttachment(FormSide side) throws Exception {
		FormAttachmentInfo attachment = (FormAttachmentInfo) JavaInfoUtils.createJavaInfo(getEditor(),
				FormLayoutSupport.getFormAttachmentClass(),
				new VirtualFormAttachmentCreationSupport(this, FormLayoutSupport.createFormAttachment()));
		// configure
		attachment.setSide(side);
		attachment.setVariableSupport(new VirtualFormAttachmentVariableSupport(attachment, side));
		attachment.setAssociation(new EmptyAssociation());
		addChild(attachment);
		return attachment;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @param side can be either of PositionConstants.LEFT,
	 *             PositionConstants.RIGHT, PositionConstants.TOP,
	 *             PositionConstants.BOTTOM
	 */
	@Override
	public FormAttachmentInfo getAttachment(int sideInt) throws Exception {
		materialize();
		FormSide side = FormSide.get(sideInt);
		for (ObjectInfo child : getChildren()) {
			if (child instanceof FormAttachmentInfo attachment) {
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
		updateAttachmentPropertyText(PositionConstants.LEFT, m_propertyLeft);
		updateAttachmentPropertyText(PositionConstants.RIGHT, m_propertyRight);
		updateAttachmentPropertyText(PositionConstants.TOP, m_propertyTop);
		updateAttachmentPropertyText(PositionConstants.BOTTOM, m_propertyBottom);
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
			m_propertyLeft = createAttachmentProperty(PositionConstants.LEFT, "left");
			m_propertyRight = createAttachmentProperty(PositionConstants.RIGHT, "right");
			m_propertyTop = createAttachmentProperty(PositionConstants.TOP, "top");
			m_propertyBottom = createAttachmentProperty(PositionConstants.BOTTOM, "bottom");
		}
	}

	@Override
	protected void sortPropertyList(List<Property> properties) {
		List<Property> sorted = new ArrayList<>();
		Property variableProperty = PropertyUtils.getByTitle(properties, "Variable");
		if (variableProperty != null) {
			sorted.add(variableProperty);
		}
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
	 * Creates the {@link ComplexProperty} for {@link FormAttachmentInfo} by
	 * attachment's property name.
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
		Collection<?> selectedProperties = CollectionUtils.select(Arrays.asList(attachment.getProperties()),
				new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				Property property = (Property) object;
				return !property.getTitle().equals("Class") && !property.getTitle().equals("Constructor");
			}
		});
		attachmentProperty.setProperties(selectedProperties.toArray(new Property[selectedProperties.size()]));
		return attachmentProperty;
	}
}