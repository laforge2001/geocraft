/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.repository;


import java.text.CollationKey;
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;

import org.geocraft.core.common.util.HashCode;


public class RepositoryViewCollator extends Collator {

  public enum SortMethod {
    BY_VAR_NAME("by variable name"),
    BY_ENTITY_NAME("by entity name");

    private String _text;

    SortMethod(String text) {
      _text = text;
    }

    @Override
    public String toString() {
      return _text;
    }
  }

  private Collator _defaultCollator;

  private SortMethod _sortMethod;

  public RepositoryViewCollator() {
    this(SortMethod.BY_ENTITY_NAME);
  }

  public RepositoryViewCollator(SortMethod sortMethod) {
    try {
      String rules = "< 0< 1< 2< 3< 4< 5< 6< 7< 8< 9" + "< a,A< b,B< c,C< d,D< e,E< f,F< g,G< h,H< i,I< j,J"
          + "< k,K< l,L< m,M< n,N< o,O< p,P< q,Q< r,R< s,S< t,T" + "< u,U< v,V< w,W< x,X< y,Y< z,Z";
      _defaultCollator = new RuleBasedCollator(rules);
    } catch (ParseException e) {
      _defaultCollator = Collator.getInstance();
    }
    _sortMethod = sortMethod;
  }

  public void setSortMethod(SortMethod sortMethod) {
    _sortMethod = sortMethod;
  }

  @Override
  public int compare(String source, String target) {
    switch (_sortMethod) {
      case BY_VAR_NAME:
        return compareByVarName(source, target);
      case BY_ENTITY_NAME:
        return compareByEntityName(source, target);
    }
    return 0;
  }

  private int compareByVarName(String source, String target) {
    String[] sourceSubs = source.split("=");
    String[] targetSubs = target.split("=");
    if (sourceSubs.length >= 2 && targetSubs.length >= 2) {
      if (sourceSubs[0].startsWith("var") && targetSubs[0].startsWith("var")) {
        String sourceIdStr = sourceSubs[0].substring(3);
        String targetIdStr = sourceSubs[0].substring(3);
        int sourceId = Integer.parseInt(sourceIdStr);
        int targetId = Integer.parseInt(targetIdStr);
        return sourceId - targetId;
      }
    }
    return 0;
  }

  private int compareByEntityName(String source, String target) {
    String[] sourceSubs = source.split("=");
    String[] targetSubs = target.split("=");
    if (sourceSubs.length >= 2 && targetSubs.length >= 2) {
      return _defaultCollator.compare(sourceSubs[1], targetSubs[1]);
    }
    return 0;
  }

  @Override
  public CollationKey getCollationKey(String source) {
    return _defaultCollator.getCollationKey(source);
  }

  @Override
  public int hashCode() {
    HashCode hash = new HashCode();
    hash.add(_defaultCollator);
    return hash.getHashCode();
  }

}
