/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.gef.core;

import org.eclipse.wb.internal.gef.core.EditPartVisitor;

import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EditParts are the building blocks of GEF Viewers. As the <I>Controller</I>, an {@link EditPart}
 * ties the application's model to a visual representation. EditParts are responsible for making
 * changes to the model. EditParts typically control a single model object or a coupled set of
 * object. Model objects are often composed of other objects that the User will interact with.
 * Similarly, EditParts can be composed of or have references to other EditParts.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public abstract class EditPart extends org.eclipse.gef.editparts.AbstractEditPart {

	////////////////////////////////////////////////////////////////////////////
	//
	// Parent/Children
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the parent <code>{@link EditPart}</code>. This method should only be called internally
	 * or by helpers such as EditPolicies.
	 */
	@Override
	public EditPart getParent() {
		return (EditPart) super.getParent();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Model
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * When existing {@link EditPart} reused for new model, this method will be invoked.
	 */
	protected void updateModel() {
	}

	/**
	 * Returns a <code>List</code> containing the children model objects. If this {@link EditPart}'s
	 * model is a container, this method should be overridden to returns its children. This is what
	 * causes children EditParts to be created.
	 * <P>
	 * Called by {@link #refreshChildren()}. Must not return <code>null</code>.
	 */
	@Override
	protected List<?> getModelChildren() {
		return Collections.EMPTY_LIST;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Visiting
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Visits this {@link EditPart} and its children using given {@link EditPartVisitor}.
	 */
	public final void accept(EditPartVisitor visitor) {
		if (visitor.visit(this)) {
			for (EditPart childPart : (List<EditPart>) getChildren()) {
				childPart.accept(visitor);
			}
			visitor.endVisit(this);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EditPart
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Refreshes all properties visually displayed by this EditPart. The default implementation will
	 * call {@link #refreshChildren()} to update its structural features. It also calls
	 * {@link #refreshVisuals()} to update its own displayed properties. Subclasses should extend this
	 * method to handle additional types of structural refreshing.
	 */
	@Override
	public void refresh() {
		refreshChildren();
		refreshVisuals();
	}

	/**
	 * Refreshes this EditPart's <i>visuals</i>. This method is called by {@link #refresh()}, and may
	 * also be called in response to notifications from the model.
	 */
	@Override
	protected void refreshVisuals() {
	}

	@Override
	protected void refreshChildren() {
		// prepare map[model, currentPart]
		Map<Object, EditPart> modelToPart = new HashMap<>();
		List<EditPart> children = (List<EditPart>) getChildren();
		for (EditPart editPart : children) {
			modelToPart.put(editPart.getModel(), editPart);
		}
		// add new child EditPart's or reorder current EditPart's
		List<?> modelChildren = getModelChildren();
		int modelCount = modelChildren.size();
		int partCount = children.size();
		int index = 0;
		for (int modelIndex = 0; modelIndex < modelCount; index++, modelIndex++) {
			Object model = modelChildren.get(modelIndex);
			// do a quick check to see if editPart[i] == model[i]
			if (index < partCount) {
				EditPart part = children.get(index);
				if (part.getModel() == model) {
					updateChildVisual(part, index);
					continue;
				}
			}
			//
			EditPart childPart = modelToPart.get(model);
			if (childPart == null) {
				org.eclipse.gef.EditPart createEditPart = createChild(model);
				if (createEditPart != null) {
					addChild(createEditPart, index);
				} else {
					index--;
				}
			} else {
				if (model != childPart.getModel()) {
					childPart.unregister();
					childPart.setModel(model);
					childPart.register();
					childPart.updateModel();
				}
				// reorder child EditPart
				removeChildVisual(childPart);
				children.remove(childPart);
				children.add(index, childPart);
				addChildVisual(childPart, index);
			}
		}
		//
		int newPartCount = children.size();
		if (newPartCount - index > 1) {
			// deselect old child EditPart's
			List<EditPart> deselectList = new ArrayList<>();
			children.listIterator(index).forEachRemaining(deselectList::add);
			getViewer().deselect(deselectList);
		}
		// remove old child EditPart's
		for (int i = index; i < newPartCount; i++) {
			EditPart childPart = children.get(index);
			removeChild(childPart);
		}
		// recurse refresh()
		for (EditPart child : (List<EditPart>) getChildren()) {
			child.refresh();
		}
	}

	/**
	 * Removes a child <code>{@link EditPart}</code>. This method is called from
	 * {@link #refreshChildren()}. The following events occur in the order listed:
	 * <OL>
	 * <LI><code>deactivate()</code> is called if the child is active
	 * <LI>{@link EditPart#removeNotify()} is called on the child.
	 * <LI>The child's parent is set to <code>null</code>
	 * </OL>
	 * <P>
	 */
	protected final void removeChild(EditPart childPart) {
		int index = getChildren().indexOf(childPart);
		if (index == -1) {
			return;
		}
		fireRemovingChild(childPart, index);
		if (isActive()) {
			childPart.deactivate();
		}
		childPart.removeNotify();
		removeChildVisual(childPart);
		getChildren().remove(childPart);
		childPart.setParent(null);
	}

	/**
	 * Convenience method for returning the <code>{@link IEditPartViewer}</code> for this part.
	 */
	@Override
	public IEditPartViewer getViewer() {
		return getParent().getViewer();
	}

	/**
	 * When existing {@link EditPart} is not touched in its parent, i.e. has same model and index,
	 * this method will be invoked.
	 * <p>
	 * We need this for "tree" GEF, to re-create items after parent item dispose/re-create.
	 */
	protected void updateChildVisual(EditPart childPart, int index) {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policy
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Return all installed {@link EditPolicy EditPolicies}.
	 */
	@Deprecated
	public List<EditPolicy> getEditPolicies() {
		List<EditPolicy> policies = new ArrayList<>();
		getEditPolicyIterable().forEach(policies::add);
		return policies;
	}

	/**
	 * Installs an {@link EditPolicy}, using its class as <i>key</i>.
	 */
	public void installEditPolicy(EditPolicy policy) {
		installEditPolicy(policy.getClass(), policy);
	}

	/**
	 * Creates the initial EditPolicies and/or reserves slots for dynamic ones. Should be implemented
	 * to install the initial EditPolicies based on the model's initial state.
	 */
	@Override
	protected void createEditPolicies() {
	}

	/**
	 * @return the instances of {@link EditPolicy} which understand given {@link Request}, may be
	 *         empty {@link List}.
	 */
	private List<EditPolicy> getUnderstandingPolicies(Request request) {
		List<EditPolicy> policies = new ArrayList<>();
		for (EditPolicy editPolicy : getEditPolicyIterable()) {
			if (editPolicy.understandsRequest(request)) {
				policies.add(editPolicy);
			}
		}
		return policies;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Request/Command
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the {@link Command} to perform the specified {@link Request} or <code>null</code>.
	 */
	public Command getCommand(Request request) {
		for (EditPolicy editPolicy : getUnderstandingPolicies(request)) {
			Command command = editPolicy.getCommand(request);
			if (command != null) {
				return command;
			}
		}
		return null;
	}

	/**
	 * Return the <code>{@link EditPart}</code> that should be used as the <i>target</i> for the
	 * specified <code>{@link Request}</code>. Tools will generally call this method with the mouse
	 * location so that the receiver can implement drop targeting. Typically, if this {@link EditPart}
	 * is not the requested target (for example, this EditPart is not a composite), it will forward
	 * the call to its parent.
	 */
	public org.eclipse.gef.EditPart getTargetEditPart(Request request) {
		org.eclipse.gef.EditPart target = null;
		// update target using any understanding EditPolicy
		for (EditPolicy editPolicy : getUnderstandingPolicies(request)) {
			org.eclipse.gef.EditPart newTarget = editPolicy.getTargetEditPart(request);
			if (newTarget != null) {
				target = newTarget;
			}
		}
		// OK, we (probably) have target
		return target;
	}

	/**
	 * Performs the specified Request. This method can be used to send a generic message to an
	 * EditPart. Subclasses should extend this method to handle Requests. For now, the default
	 * implementation forward request to all EditPolicies.
	 */
	public void performRequest(Request request) {
		for (EditPolicy editPolicy : getEditPolicyIterable()) {
			((org.eclipse.wb.gef.core.policies.EditPolicy) editPolicy).performRequest(request);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Source Feedback
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Shows or updates source feedback for the given request. This method may be called multiple
	 * times so that the feedback can be updated for changes in the request, such as the mouse
	 * location changing.
	 */
	public void showSourceFeedback(Request request) {
		if (isActive()) {
			for (EditPolicy editPolicy : getUnderstandingPolicies(request)) {
				editPolicy.showSourceFeedback(request);
			}
		}
	}

	/**
	 * Erases <i>source</i> feedback for the specified {@link Request}. A {@link Request} is used to
	 * describe the type of source feedback that should be erased. This method should only be called
	 * once to erase feedback. It should only be called in conjunction with a prior call to
	 * {@link #showSourceFeedback(Request)}.
	 */
	public void eraseSourceFeedback(Request request) {
		if (isActive()) {
			for (EditPolicy editPolicy : getUnderstandingPolicies(request)) {
				editPolicy.eraseSourceFeedback(request);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Target Feedback
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Shows or updates target feedback for the given request. This method can be called multiple
	 * times so that the feedback can be updated for changes in the request, such as the mouse
	 * location changing.
	 */
	public void showTargetFeedback(Request request) {
		if (isActive()) {
			for (EditPolicy editPolicy : getUnderstandingPolicies(request)) {
				editPolicy.showTargetFeedback(request);
			}
		}
	}

	/**
	 * Erases <i>target</i> feedback for the specified {@link Request}. A {@link Request} is used to
	 * describe the type of target feedback that should be erased. This method should only be called
	 * once to erase feedback. It should only be called in conjunction with a prior call to
	 * {@link #showTargetFeedback(Request)}.
	 */
	public void eraseTargetFeedback(Request request) {
		if (isActive()) {
			for (EditPolicy editPolicy : getUnderstandingPolicies(request)) {
				editPolicy.eraseTargetFeedback(request);
			}
		}
	}
}