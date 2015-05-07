package org.geocraft.core.model.property;


public class OutputEntityObject {

  private String _entityName;

  private String _comments;

  public OutputEntityObject() {
    this("", "");
  }

  public OutputEntityObject(final String entityName, final String comments) {
    _entityName = entityName;
    _comments = comments;
  }

  public String getEntityName() {
    return _entityName;
  }

  public String getComments() {
    return _comments;
  }

  public void setEntityName(final String entityName) {
    _entityName = entityName;
  }

  public void setComments(final String comments) {
    _comments = comments;
  }

  @Override
  public String toString() {
    return _entityName + OutputEntityProperty.NAME_COMMENT_SEPARATOR + _comments;
  }
}
