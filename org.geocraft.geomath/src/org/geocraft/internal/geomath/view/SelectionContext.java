/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.geomath.view;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.help.IContext;
import org.eclipse.help.IContext2;
import org.eclipse.help.IHelpResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.geocraft.algorithm.IStandaloneAlgorithmDescription;


public class SelectionContext implements IContext2 {

  private final IHelpResource[] _helpResources;

  private String _text;

  private String _title;

  public SelectionContext(final IContext context, final IStructuredSelection selection) {
    Assert.isNotNull(selection);
    if (context instanceof IContext2) {
      _title = ((IContext2) context).getTitle();
    }
    List<IHelpResource> helpResources = new ArrayList<IHelpResource>();

    // Create a link to the wush wiki
    String element = selection.getFirstElement().toString();
    StringBuffer location = new StringBuffer("http://wush.net/trac/geocraft/wiki/");
    helpResources.add(new SelectionHelpResource(element, location.append(element).toString()));

    // Create a link to the online help system content
    TreeObject object = (TreeObject) selection.getFirstElement();

    IStandaloneAlgorithmDescription description = object.getStandaloneAlgorithm();
    helpResources.add(new SelectionHelpResource(description.getName(), description.getHelpId()));

    // Add static help topics
    if (context != null) {
      IHelpResource[] resources = context.getRelatedTopics();
      if (resources != null) {
        for (IHelpResource resource : resources) {
          helpResources.add(resource);
        }
      }
    }
    _helpResources = helpResources.toArray(new IHelpResource[helpResources.size()]);
    if (context != null) {
      _text = context.getText();
    }
    if (_text == null) {
      _text = "Algorithm Information";
    }
  }

  public String getCategory(final IHelpResource topic) {
    if (topic instanceof SelectionHelpResource) {
      return "AlgorithmCategory";
    }
    return null;
  }

  public String getStyledText() {
    return _text;
  }

  public String getTitle() {
    return _title;
  }

  public IHelpResource[] getRelatedTopics() {
    return _helpResources;
  }

  public String getText() {
    return _text;
  }
}
