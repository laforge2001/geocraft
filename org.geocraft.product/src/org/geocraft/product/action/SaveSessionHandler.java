/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.product.action;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.MultiStatus;
import org.geocraft.Activator;
import org.geocraft.core.session.SessionManager;


public class SaveSessionHandler extends AbstractHandler {

  @SuppressWarnings("unused")
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {

    final MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, 1, "Saving Session", null);
    SessionManager.getInstance().saveSession();

    //    Session session = new Session();
    //    try {
    //      XmlUtils.writeXML(new File("/home/walucas/lucas.geocraft_session"), session);
    //    } catch (Exception e) {
    //      // TODO Auto-generated catch block
    //      e.printStackTrace();
    //    }

    return null;
  }
}
