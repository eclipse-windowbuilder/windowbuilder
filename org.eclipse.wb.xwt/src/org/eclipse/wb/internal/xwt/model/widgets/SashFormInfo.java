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
package org.eclipse.wb.internal.xwt.model.widgets;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.rcp.model.widgets.ISashFormInfo;
import org.eclipse.wb.internal.xwt.support.ControlSupport;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Model for {@link SashForm}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public class SashFormInfo extends CompositeInfo implements ISashFormInfo<ControlInfo> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SashFormInfo(EditorContext context,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(context, description, creationSupport);
		// when remove Control: remove weight
		addBroadcastListener(new ObjectEventListener() {
			@Override
			public void childRemoveBefore(ObjectInfo parent, ObjectInfo child) throws Exception {
				if (child instanceof ControlInfo && parent == SashFormInfo.this) {
					removeWeight((ControlInfo) child);
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public boolean isHorizontal() {
		return ControlSupport.hasStyle(getControl(), SWT.HORIZONTAL);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	public void command_CREATE(ControlInfo control, ControlInfo nextControl) throws Exception {
		ensureWeights();
		XmlObjectUtils.add(control, Associations.direct(), this, nextControl);
		addWeight(control);
	}

	public void command_MOVE(ControlInfo control, ControlInfo nextControl) throws Exception {
		ensureWeights();
		int oldIndex = getChildrenControls().indexOf(control);
		XmlObjectUtils.move(control, Associations.direct(), this, nextControl);
		// update weights
		if (oldIndex == -1) {
			addWeight(control);
		} else {
			moveWeight(control, oldIndex);
		}
	}

	public void command_RESIZE(ControlInfo control, int size) throws Exception {
		ensureWeights();
		List<ControlInfo> children = getChildrenControls();
		// prepare weights as current sizes
		int[] weights = new int[children.size()];
		for (int i = 0; i < children.size(); i++) {
			ControlInfo child = children.get(i);
			Rectangle bounds = child.getModelBounds();
			weights[i] = isHorizontal() ? bounds.width : bounds.height;
		}
		// update adjacent weights
		int index = children.indexOf(control);
		int sumWeight = weights[index] + weights[index + 1];
		weights[index + 1] = Math.max(0, sumWeight - size);
		weights[index] = sumWeight - weights[index + 1];
		// set weights
		setWeights(weights);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Weights operations
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds weight for {@link ControlInfo} that is already in children.
	 */
	private void addWeight(ControlInfo newControl) throws Exception {
		int[] weights = getEnsureWeights();
		// prepare weight for "newControl"
		int newWeight;
		if (weights.length == 0) {
			newWeight = 1;
		} else {
			newWeight = 0;
			for (int i = 0; i < weights.length; i++) {
				newWeight += weights[i];
			}
			newWeight /= weights.length;
		}
		// add weight
		int newControlIndex = getChildrenControls().indexOf(newControl);
		weights = ArrayUtils.add(weights, newControlIndex, newWeight);
		setWeights(weights);
	}

	/**
	 * Moves weight for {@link ControlInfo} that is already moved in children.
	 */
	private void moveWeight(ControlInfo newControl, int oldIndex) throws Exception {
		int[] weights = getEnsureWeights();
		int weight = weights[oldIndex];
		int newIndex = getChildrenControls().indexOf(newControl);
		weights = ArrayUtils.remove(weights, oldIndex);
		weights = ArrayUtils.add(weights, newIndex, weight);
		setWeights(weights);
	}

	/**
	 * Removes weight for {@link ControlInfo} that is still in children.
	 */
	private void removeWeight(ControlInfo control) throws Exception {
		String weightsString = getCreationSupport().getElement().getAttribute("weights");
		if (weightsString != null) {
			int index = getChildrenControls().indexOf(control);
			int[] weights = getWeights(weightsString);
			weights = ArrayUtils.remove(weights, index);
			setWeights(weights);
		}
	}

	/**
	 * @return the current weights, ensure them if needed.
	 */
	private int[] getEnsureWeights() throws Exception {
		String weightsString = ensureWeights();
		return getWeights(weightsString);
	}

	/**
	 * Ensures that this {@link SashFormInfo} has "weights" attribute.
	 */
	private String ensureWeights() throws Exception {
		DocumentElement element = getCreationSupport().getElement();
		String weightsString = element.getAttribute("weights");
		if (weightsString == null) {
			weightsString = StringUtils.repeat("1, ", getChildrenControls().size());
			weightsString = StringUtils.removeEnd(weightsString, ", ");
			element.setAttribute("weights", weightsString);
		}
		return weightsString;
	}

	/**
	 * @return weights if given "," separated {@link String}.
	 */
	private static int[] getWeights(String weightsString) throws Exception {
		String[] parts = StringUtils.split(weightsString, ", ");
		int[] weights = new int[parts.length];
		for (int i = 0; i < parts.length; i++) {
			String weightString = parts[i];
			weights[i] = Integer.parseInt(weightString);
		}
		return weights;
	}

	/**
	 * Sets the "weights" attribute.
	 */
	private void setWeights(int[] weights) throws Exception {
		String weightsString = StringUtils.join(ArrayUtils.toObject(weights), ", ");
		getCreationSupport().getElement().setAttribute("weights", weightsString);
	}
}
