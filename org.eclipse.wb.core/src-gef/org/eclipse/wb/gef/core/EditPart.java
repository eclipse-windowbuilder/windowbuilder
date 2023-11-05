/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.gef.core;

import com.google.common.collect.Iterators;

import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.gef.core.EditPartVisitor;
import org.eclipse.wb.internal.gef.core.IRootContainer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;

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
	//
	private final List<EditPolicy> m_policies = new ArrayList<>();
	private final List<Object> m_keyPolicies = new ArrayList<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Activate/Deactivate
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Activates the {@link EditPart}. EditParts that observe a dynamic model or support editing must
	 * be <i>active</i>. Called by the managing {@link EditPart}, or the Viewer in the case of the
	 * {@link IRootContainer}. This method may be called again once {@link #deactivate()} has been
	 * called.
	 * <P>
	 * During activation the receiver should:
	 * <UL>
	 * <LI>begin to observe its model if appropriate, and should continue the observation until
	 * {@link #deactivate()} is called.
	 * <LI>activate all of its EditPolicies. EditPolicies may also observe the model, although this is
	 * rare. But it is common for EditPolicies to contribute additional visuals, such as selection
	 * handles or feedback during interactions. Therefore it is necessary to tell the EditPolicies
	 * when to start doing this, and when to stop.
	 * <LI>call activate() on the EditParts it manages. This includes its children.
	 * </UL>
	 */
	public void activate() {
		setFlag(FLAG_ACTIVE, true);
		activateEditPolicies();
		for (EditPart childPart : getChildren()) {
			childPart.activate();
		}
	}

	/**
	 * Deactivates the {@link EditPart}. EditParts that observe a dynamic model or support editing
	 * must be <i>active</i>. <code>deactivate()</code> is guaranteed to be called when an EditPart
	 * will no longer be used. Called by the managing EditPart, or the Viewer in the case of the
	 * {@link IRootContainer}. This method may be called multiple times.
	 * <P>
	 * During deactivation the receiver should:
	 * <UL>
	 * <LI>remove all listeners that were added in {@link #activate}
	 * <LI>deactivate all of its EditPolicies. EditPolicies may be contributing additional visuals,
	 * such as selection handles or feedback during interactions. Therefore it is necessary to tell
	 * the EditPolicies when to start doing this, and when to stop.
	 * <LI>call deactivate() on the EditParts it manages. This includes its children.
	 * </UL>
	 */
	public void deactivate() {
		for (EditPart childPart : getChildren()) {
			childPart.deactivate();
		}
		deactivateEditPolicies();
		setFlag(FLAG_ACTIVE, false);
	}

	/**
	 * Called <em>after</em> the {@link EditPart} has been added to its parent. This is used to
	 * indicate to the {@link EditPart} that it should refresh itself for the first time.
	 */
	public void addNotify() {
		getViewer().registerEditPart(this);
		createEditPolicies();
		for (EditPart childPart : getChildren()) {
			childPart.addNotify();
		}
		refresh();
	}

	/**
	 * Called when the {@link EditPart} is being permanently removed from its {@link EditPartViewer}.
	 * This indicates that the {@link EditPart} will no longer be in the Viewer, and therefore should
	 * remove itself from the Viewer. This method is <EM>not</EM> called when a Viewer is disposed. It
	 * is only called when the EditPart is removed from its parent. This method is the inverse of
	 * {@link #addNotify()}
	 */
	public void removeNotify() {
		if (getSelected() != SELECTED_NONE) {
			getViewer().deselect(this);
		}
		for (EditPart childPart : getChildren()) {
			childPart.removeNotify();
		}
		getViewer().unregisterEditPart(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parent/Children
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the List of children <code>EditParts</code>. This method should rarely be called, and
	 * is only made public so that helper objects of this EditPart, such as EditPolicies, can obtain
	 * the children. The returned List may be by reference, and should never be modified.
	 */
	public List<EditPart> getChildren() {
		return (List<EditPart>) super.getChildren();
	}
	/**
	 * Returns the parent <code>{@link EditPart}</code>. This method should only be called internally
	 * or by helpers such as EditPolicies.
	 */
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
			for (EditPart childPart : getChildren()) {
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
	public void refresh() {
		refreshChildren();
		refreshVisuals();
	}

	/**
	 * Refreshes this EditPart's <i>visuals</i>. This method is called by {@link #refresh()}, and may
	 * also be called in response to notifications from the model.
	 */
	protected void refreshVisuals() {
	}

	protected void refreshChildren() {
		// prepare map[model, currentPart]
		Map<Object, EditPart> modelToPart = new HashMap<>();
		List<EditPart> children = getChildren();
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
				EditPart createEditPart = createEditPart(model);
				if (createEditPart != null) {
					addChild(createEditPart, index);
				} else {
					index--;
				}
			} else {
				if (model != childPart.getModel()) {
					getViewer().unregisterEditPart(childPart);
					childPart.setModel(model);
					getViewer().registerEditPart(childPart);
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
			Iterators.addAll(deselectList, children.listIterator(index));
			getViewer().deselect(deselectList);
		}
		// remove old child EditPart's
		for (int i = index; i < newPartCount; i++) {
			EditPart childPart = children.get(index);
			removeChild(childPart);
		}
		// recurse refresh()
		for (EditPart child : getChildren()) {
			child.refresh();
		}
	}

	/**
	 * Create the child <code>{@link EditPart}</code> for the given model object. This method is
	 * called from {@link #refreshChildren()}. By default, the implementation will delegate to the
	 * <code>{@link
	 * IEditPartViewer}</code> 's {@link IEditPartFactory}.
	 */
	protected EditPart createEditPart(Object model) {
		IEditPartFactory factory = getViewer().getEditPartFactory();
		return factory.createEditPart(this, model);
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
	public List<EditPolicy> getEditPolicies() {
		return m_policies;
	}

	/**
	 * Return <code>null</code> or the {@link EditPolicy} installed with the given key.
	 */
	public EditPolicy getEditPolicy(Object key) {
		int index = m_keyPolicies.indexOf(key);
		return index == -1 ? null : m_policies.get(index);
	}

	/**
	 * Installs an {@link EditPolicy}, using its class as <i>key</i>.
	 */
	public void installEditPolicy(EditPolicy policy) {
		installEditPolicy(policy.getClass(), policy);
	}

	/**
	 * Installs an {@link EditPolicy} for a specified <i>role</i>. A <i>role</i> is is simply an
	 * Object used to identify the {@link EditPolicy}.
	 */
	public void installEditPolicy(Object key, EditPolicy policy) {
		Assert.isNotNull(key, "Edit Policies must be installed with keys");
		int index = m_keyPolicies.indexOf(key);
		//
		if (index == -1) {
			if (policy != null) {
				m_keyPolicies.add(key);
				m_policies.add(policy);
			}
		} else {
			EditPolicy oldPolicy = m_policies.get(index);
			//
			if (isActive()) {
				oldPolicy.deactivate();
			}
			//
			if (policy == null) {
				m_keyPolicies.remove(index);
				m_policies.remove(index);
			} else {
				m_policies.set(index, policy);
			}
			//
			oldPolicy.dispose();
		}
		//
		if (policy != null) {
			policy.setHost(this);
			//
			if (isActive()) {
				policy.activate();
			}
		}
	}

	protected void activateEditPolicies() {
		for (EditPolicy editPolicy : m_policies) {
			editPolicy.activate();
		}
	}

	protected void deactivateEditPolicies() {
		for (EditPolicy editPolicy : m_policies) {
			editPolicy.deactivate();
		}
	}

	/**
	 * Creates the initial EditPolicies and/or reserves slots for dynamic ones. Should be implemented
	 * to install the initial EditPolicies based on the model's initial state.
	 */
	protected void createEditPolicies() {
	}

	/**
	 * @return the instances of {@link EditPolicy} which understand given {@link Request}, may be
	 *         empty {@link List}.
	 */
	private List<EditPolicy> getUnderstandingPolicies(Request request) {
		List<EditPolicy> policies = new ArrayList<>();
		for (EditPolicy editPolicy : m_policies) {
			if (editPolicy.understandsRequest(request)) {
				policies.add(editPolicy);
			}
		}
		return policies;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Request processors
	//
	////////////////////////////////////////////////////////////////////////////
	private final List<RequestProcessor> m_requestProcessors = new ArrayList<>();

	/**
	 * Adds the {@link RequestProcessor}, if not added yet.
	 */
	public final void addRequestProcessor(RequestProcessor processor) {
		if (!m_requestProcessors.contains(processor)) {
			m_requestProcessors.add(processor);
		}
	}

	/**
	 * Removes the {@link RequestProcessor}.
	 */
	public final void removeRequestProcessor(RequestProcessor processor) {
		m_requestProcessors.remove(processor);
	}

	/**
	 * @return the {@link Request} processed with registered {@link RequestProcessor}'s.
	 */
	protected final Request processRequestProcessors(Request request) {
		try {
			for (RequestProcessor processor : m_requestProcessors) {
				request = processor.process(this, request);
			}
		} catch (Throwable e) {
		}
		return request;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DragTracking
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public DragTracker getDragTracker(org.eclipse.gef.Request request) {
		// TODO
		return null;
	}
	
	/**
	 * Returns a {@link Tool} for dragging this {@link EditPart}. The SelectionTool is the only
	 * {@link Tool} by default that calls this method. The SelectionTool will use a SelectionRequest
	 * to provide information such as which mouse button is down, and what modifier keys are pressed.
	 */
	public abstract Tool getDragTrackerTool(Request request);

	////////////////////////////////////////////////////////////////////////////
	//
	// Request/Command
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the {@link Command} to perform the specified {@link Request} or <code>null</code>.
	 */
	public Command getCommand(Request request) {
		request = processRequestProcessors(request);
		for (EditPolicy editPolicy : getUnderstandingPolicies(request)) {
			Command command = editPolicy.getCommand(request);
			if (command != null) {
				return command;
			}
		}
		return null;
	}

	/**
	 * @return new instance of {@link CompoundCommand} for further wrapping commands usage.
	 */
	public CompoundCommand createCompoundCommand() {
		return new CompoundCommand();
	}

	/**
	 * Return the <code>{@link EditPart}</code> that should be used as the <i>target</i> for the
	 * specified <code>{@link Request}</code>. Tools will generally call this method with the mouse
	 * location so that the receiver can implement drop targeting. Typically, if this {@link EditPart}
	 * is not the requested target (for example, this EditPart is not a composite), it will forward
	 * the call to its parent.
	 */
	public EditPart getTargetEditPart(Request request) {
		request = processRequestProcessors(request);
		EditPart target = null;
		// update target using any understanding EditPolicy
		for (EditPolicy editPolicy : getUnderstandingPolicies(request)) {
			EditPart newTarget = editPolicy.getTargetEditPart(request);
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
		request = processRequestProcessors(request);
		for (EditPolicy editPolicy : m_policies) {
			editPolicy.performRequest(request);
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
		request = processRequestProcessors(request);
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
		request = processRequestProcessors(request);
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
		request = processRequestProcessors(request);
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
		request = processRequestProcessors(request);
		if (isActive()) {
			for (EditPolicy editPolicy : getUnderstandingPolicies(request)) {
				editPolicy.eraseTargetFeedback(request);
			}
		}
	}
}