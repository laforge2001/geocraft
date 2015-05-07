/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.volumeviewer.input;


import java.util.EnumMap;
import java.util.concurrent.Callable;

import org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasImplementor;

import com.ardor3d.framework.Canvas;
import com.ardor3d.input.ButtonState;
import com.ardor3d.input.InputState;
import com.ardor3d.input.Key;
import com.ardor3d.input.KeyboardState;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.MouseState;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.input.logical.MouseButtonPressedCondition;
import com.ardor3d.input.logical.MouseWheelMovedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TriggerConditions;
import com.ardor3d.input.logical.TwoInputStates;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;


/**
 * Main class for initializing and handling mouse interaction.
 * 
 * TODO: More documentation.
 * 
 * @author Joshua Slack
 */
public class VolumeMouseLook {

  private final ViewCanvasImplementor _impl;

  private boolean _resetMousePosition;

  private boolean _focusTrigger = false;

  private Callable<?> _focusTriggerCallback;

  public VolumeMouseLook(final ViewCanvasImplementor impl) {
    _impl = impl;
  }

  /**
   * @param left
   */
  public void doClick(final MouseButton pressed, final MouseState mouse) {

    final int absMouseX = mouse.getX();
    final int absMouseY = mouse.getY();

    // SET FOCUS ANCHOR
    if (_focusTrigger) {
      _focusTrigger = false;
      _impl.setViewFocus(absMouseX, absMouseY);
      if (_focusTriggerCallback != null) {
        try {
          _focusTriggerCallback.call();
        } catch (final Exception e) {
          // ignore
        }
      }
    } else if (pressed == MouseButton.RIGHT) {
      _impl.doPick(absMouseX, absMouseY, true, null);
    } else if (pressed == MouseButton.LEFT) {
      _impl.doPick(absMouseX, absMouseY, false, null);
    }

  }

  public void doDrag(final KeyboardState key, final MouseState mouse) {
    final float scale = 0.01f;

    // left
    final boolean btn1 = mouse.getButtonState(MouseButton.LEFT) == ButtonState.DOWN;
    // middle
    final boolean btn2 = mouse.getButtonState(MouseButton.MIDDLE) == ButtonState.DOWN;
    // right
    final boolean btn3 = mouse.getButtonState(MouseButton.RIGHT) == ButtonState.DOWN;

    int deltaX = mouse.getDx();
    int deltaY = mouse.getDy();

    if (_resetMousePosition) {
      deltaX = deltaY = 0;
      _resetMousePosition = false;
    }

    if (btn1 && btn3) {
      // Zoom
      if (deltaY != 0) {
        final float scalar = (isShiftDown(key) ? 0.1f : 1.0f) * (deltaY > 0 ? -1 : 1);
        _impl.zoomCamera(scalar);
      }
    } else if (btn2 || btn1 && isControlDown(key)) {
      // Pan - Shift focal point along camera plane
      if (deltaX != 0 || deltaY != 0) {
        _impl.panCamera(deltaX, -deltaY);
      }
    } else if (btn1) {
      // rotate camera horizontally/vertically using scale and x/y mouse shift
      if (deltaX != 0 || deltaY != 0) {
        _impl.rotateCamera(scale * -deltaX, scale * deltaY);
      }
    }
  }

  public void doWheel(final KeyboardState key, final MouseState mouse) {
    final int wheelDelta = mouse.getDwheel();
    if (wheelDelta != 0) {
      final float scalar = (isShiftDown(key) ? 0.1f : 1.0f) * (wheelDelta > 0 ? -1 : 1);
      if (isControlDown(key)) {
        _impl.zoomCamera(scalar);
      } else {
        _impl.zoomCamera(scalar, mouse.getX(), mouse.getY());
      }
    }
  }

  private boolean isControlDown(final KeyboardState key) {
    return key.getKeysDown().contains(Key.LCONTROL) || key.getKeysDown().contains(Key.RCONTROL);
  }

  private boolean isShiftDown(final KeyboardState key) {
    return key.getKeysDown().contains(Key.LSHIFT) || key.getKeysDown().contains(Key.RSHIFT);
  }

  /**
   * Next mouse update will reset the delta tracking to 0.
   */
  public void resetMouse() {
    _resetMousePosition = true;
  }

  /**
   * Next button click will reset the focus center using a pick.
   * @param callback When a reset occurs, this callback will be notified.
   */
  public void armFocusTrigger(final Callable<?> callback) {
    _focusTrigger = true;
    _focusTriggerCallback = callback;
  }

  /**
   * @param layer
   * @param impl 
   * @return 
   */
  public static VolumeMouseLook setupTriggers(final LogicalLayer layer, final ViewCanvasImplementor impl) {

    final Predicate<TwoInputStates> someMouseDown = Predicates.or(TriggerConditions.leftButtonDown(),
        Predicates.or(TriggerConditions.rightButtonDown(), TriggerConditions.middleButtonDown()));
    final Predicate<TwoInputStates> dragged = Predicates.and(TriggerConditions.mouseMoved(), someMouseDown);

    final VolumeMouseLook mouseLook = new VolumeMouseLook(impl);
    final TriggerAction dragAction = new TriggerAction() {

      public void perform(@SuppressWarnings("unused") final Canvas source, final InputState inputState,
          @SuppressWarnings("unused") final double tpf) {
        mouseLook.doDrag(inputState.getKeyboardState(), inputState.getMouseState());
      }
    };
    final TriggerAction wheelAction = new TriggerAction() {

      public void perform(@SuppressWarnings("unused") final Canvas source, final InputState inputState,
          @SuppressWarnings("unused") final double tpf) {
        mouseLook.doWheel(inputState.getKeyboardState(), inputState.getMouseState());
      }
    };
    final TriggerAction leftClickAction = new TriggerAction() {

      public void perform(@SuppressWarnings("unused") final Canvas source, final InputState inputState,
          @SuppressWarnings("unused") final double tpf) {
        mouseLook.doClick(MouseButton.LEFT, inputState.getMouseState());
      }
    };
    final TriggerAction rightClickAction = new TriggerAction() {

      public void perform(@SuppressWarnings("unused") final Canvas source, final InputState inputState,
          @SuppressWarnings("unused") final double tpf) {
        mouseLook.doClick(MouseButton.RIGHT, inputState.getMouseState());
      }
    };

    layer.registerTrigger(new InputTrigger(new MouseButtonPressedCondition(MouseButton.LEFT), leftClickAction));
    layer.registerTrigger(new InputTrigger(new MouseButtonPressedCondition(MouseButton.RIGHT), rightClickAction));
    layer.registerTrigger(new InputTrigger(dragged, dragAction));
    layer.registerTrigger(new InputTrigger(new MouseWheelMovedCondition(), wheelAction));

    layer.registerTrigger(new InputTrigger(someMouseDown, new TriggerAction() {

      @Override
      public void perform(@SuppressWarnings("unused") final Canvas source, final InputState inputState,
          @SuppressWarnings("unused") final double tpf) {
        final EnumMap<MouseButton, ButtonState> states = inputState.getMouseState().getButtonStates();
        boolean down = false;
        if (states != null) {
          for (final ButtonState state : states.values()) {
            down = down || state == ButtonState.DOWN;
          }
        }
        if (impl.getFocusRods().isInteracting() != down) {
          impl.getFocusRods().setInteracting(down);
          impl.makeDirty();
        }
      }
    }));
    return mouseLook;
  }

}
