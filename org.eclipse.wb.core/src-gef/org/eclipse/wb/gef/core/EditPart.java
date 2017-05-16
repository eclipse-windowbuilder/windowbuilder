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
package org.eclipse.wb.gef.core;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.gef.core.events.IEditPartListener;
import org.eclipse.wb.gef.core.events.IEditPartSelectionListener;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.draw2d.events.EventTable;
import org.eclipse.wb.internal.gef.core.CompoundCommand;
import org.eclipse.wb.internal.gef.core.EditPartVisitor;
import org.eclipse.wb.internal.gef.core.IRootContainer;

import org.eclipse.jface.util.Assert;

import java.util.Collections;
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
public abstract class EditPart {
  /**
   * Used to indicate no selection.
   */
  public static final int SELECTED_NONE = 0;
  /**
   * Used to indicate non-primary selection.
   */
  public static final int SELECTED = 1;
  /**
   * Used to indicate primary selection, or "Anchor" selection. Primary selection is defined as the
   * last object selected.
   */
  public static final int SELECTED_PRIMARY = 2;
  //
  private EditPart m_parent;
  private List<EditPart> m_children;
  private boolean m_isActive;
  private final List<EditPolicy> m_policies = Lists.newArrayList();
  private final List<Object> m_keyPolicies = Lists.newArrayList();
  private int m_selected;
  private Object m_model;

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
    m_isActive = true;
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
    m_isActive = false;
  }

  /**
   * Returns <code>true</code> if the {@link EditPart} is active. Editparts are active after
   * {@link #activate()} is called, and until {@link #deactivate()} is called.
   */
  public boolean isActive() {
    return m_isActive;
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
    return m_children == null ? Collections.<EditPart>emptyList() : m_children;
  }

  /**
   * Returns the parent <code>{@link EditPart}</code>. This method should only be called internally
   * or by helpers such as EditPolicies.
   */
  public EditPart getParent() {
    return m_parent;
  }

  /**
   * Sets the parent. This should only be called by the parent {@link EditPart}.
   */
  public void setParent(EditPart parent) {
    m_parent = parent;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Model
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the primary model object that this {@link EditPart} represents. EditParts may
   * correspond to more than one model object, or even no model object. In practice, the Object
   * returned is used by other EditParts to identify this EditPart. In addition, EditPolicies
   * probably rely on this method to build Commands that operate on the model.
   */
  public Object getModel() {
    return m_model;
  }

  /**
   * Sets the model. This method is made public to facilitate the use of {@link IEditPartFactory
   * EditPartFactories}.
   * <P>
   * IMPORTANT: This method should only be called once.
   */
  public void setModel(Object model) {
    m_model = model;
  }

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
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the selected state of this {@link EditPart}. This method should only be called
   * internally or by helpers such as EditPolicies.
   *
   * @return one of:
   *         <UL>
   *         <LI> {@link #SELECTED}
   *         <LI> {@link #SELECTED_NONE}
   *         <LI> {@link #SELECTED_PRIMARY}
   *         </UL>
   */
  public int getSelected() {
    return m_selected;
  }

  /**
   * Sets the selected state property to reflect the selection in the EditPartViewer. Fires
   * selectionChanged(EditPart) to any {@link IEditPartSelectionListener}s. Selection is maintained
   * by the {@link EditPartViewer}.
   * <P>
   * IMPORTANT: This method should only be called by the {@link EditPartViewer}.
   */
  public void setSelected(int selected) {
    if (m_selected != selected) {
      m_selected = selected;
      fireSelection();
    }
  }

  /**
   * Reserved for future use.
   */
  public boolean isSelectable() {
    return true;
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
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  private EventTable m_eventTable;

  /**
   * Adds a listener to the {@link EditPart}.
   */
  public void addEditPartListener(IEditPartListener listener) {
    getEnsureEventTable().addListener(IEditPartListener.class, listener);
  }

  /**
   * Removes the first occurrence of the specified listener from the list of listeners. Does nothing
   * if the listener was not present.
   */
  public void removeEditPartListener(IEditPartListener listener) {
    getEnsureEventTable().removeListener(IEditPartListener.class, listener);
  }

  /**
   * Registers the given listener as a {@link IEditPartSelectionListener} of this {@link EditPart}.
   */
  public void addSelectionListener(IEditPartSelectionListener listener) {
    getEnsureEventTable().addListener(IEditPartSelectionListener.class, listener);
  }

  /**
   * Unregisters the given listener, so that it will no longer receive notification of selection.
   */
  public void removeSelectionListener(IEditPartSelectionListener listener) {
    getEnsureEventTable().removeListener(IEditPartSelectionListener.class, listener);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Return all registers listeners for given class or <code>null</code>.
   */
  public <T extends Object> List<T> getListeners(Class<T> listenerClass) {
    return m_eventTable == null ? null : m_eventTable.getListeners(listenerClass);
  }

  /**
   * Access to <code>{@link EventTable}</code> use lazy creation mechanism.
   */
  private EventTable getEnsureEventTable() {
    if (m_eventTable == null) {
      m_eventTable = new EventTable();
    }
    return m_eventTable;
  }

  private void fireChildAdded(EditPart child, int index) {
    List<IEditPartListener> listeners = getListeners(IEditPartListener.class);
    if (listeners != null && !listeners.isEmpty()) {
      for (IEditPartListener listener : listeners) {
        listener.childAdded(child, index);
      }
    }
  }

  private void fireRemovingChild(EditPart child, int index) {
    List<IEditPartListener> listeners = getListeners(IEditPartListener.class);
    if (listeners != null && !listeners.isEmpty()) {
      for (IEditPartListener listener : listeners) {
        listener.removingChild(child, index);
      }
    }
  }

  private void fireSelection() {
    List<IEditPartSelectionListener> listeners = getListeners(IEditPartSelectionListener.class);
    if (listeners != null && !listeners.isEmpty()) {
      for (IEditPartSelectionListener listener : listeners) {
        listener.selectionChanged(this);
      }
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
    Map<Object, EditPart> modelToPart = Maps.newHashMap();
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
      List<EditPart> deselectList = Lists.newArrayList();
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
   * Adds a child <code>{@link EditPart}</code> to this {@link EditPart}. This method is called from
   * {@link #refreshChildren()}. The following events occur in the order listed:
   * <OL>
   * <LI>The child is added to the {@link #children} List, and its parent is set to
   * <code>this</code>
   * <LI> {@link EditPart#addNotify()} is called on the child.
   * <LI><code>activate()</code> is called if this part is active
   * </OL>
   * <P>
   */
  protected final void addChild(EditPart childPart, int index) {
    // check EditPart
    Assert.isNotNull(childPart);
    // check container
    if (m_children == null) {
      m_children = Lists.newArrayList();
    }
    // add to child list
    if (index == -1) {
      index = m_children.size();
      m_children.add(childPart);
    } else {
      m_children.add(index, childPart);
    }
    // set parent
    childPart.setParent(this);
    // add child figure
    addChildVisual(childPart, index);
    // notify new child
    childPart.addNotify();
    // notify listeners
    fireChildAdded(childPart, index);
    // activate if necessary
    if (isActive()) {
      childPart.activate();
    }
  }

  /**
   * Convenience method for returning the <code>{@link IEditPartViewer}</code> for this part.
   */
  public IEditPartViewer getViewer() {
    return getParent().getViewer();
  }

  /**
   * Performs the addition of the child's <i>visual</i> to this EditPart's Visual. The provided
   * subclasses {@link GraphicalEditPart} and {@link TreeEditPart} already implement this method
   * correctly, so it is unlikely that this method should be overridden.
   *
   * @param childPart
   *          The EditPart being added.
   * @param index
   *          The child's position.
   * @see #addChild(EditPart, int)
   */
  protected abstract void addChildVisual(EditPart childPart, int index);

  /**
   * Removes the childs visual from this EditPart's visual. Subclasses should implement this method
   * to support the visual type they introduce, such as Figures or TreeItems.
   *
   * @param childPart
   *          the child EditPart
   */
  protected abstract void removeChildVisual(EditPart childPart);

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

  private void activateEditPolicies() {
    for (EditPolicy editPolicy : m_policies) {
      editPolicy.activate();
    }
  }

  private void deactivateEditPolicies() {
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
    List<EditPolicy> policies = Lists.newArrayList();
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
  private final List<RequestProcessor> m_requestProcessors = Lists.newArrayList();

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