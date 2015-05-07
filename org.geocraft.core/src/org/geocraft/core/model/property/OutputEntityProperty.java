package org.geocraft.core.model.property;


public class OutputEntityProperty extends ObjectProperty<OutputEntityObject> {

  public static final String NAME_COMMENT_SEPARATOR = ":::";

  public OutputEntityProperty(final String key) {
    super(key, OutputEntityObject.class);
    set(new OutputEntityObject());
  }

  public boolean isEmpty() {
    return getEntityName().isEmpty();
  }

  public String getEntityName() {
    return get().getEntityName();
  }

  public String getComments() {
    return get().getComments();
  }

  @Override
  public void setValueObject(final Object valueObject) {
    // Value object can be null for optional parameters such as AOI
    if (valueObject != null && getKlazz().isAssignableFrom(valueObject.getClass())) {
      set(getKlazz().cast(valueObject));
    } else if (valueObject == null) {
      OutputEntityObject value = new OutputEntityObject();
      set(value);
    }
  }

  @Override
  public void set(final OutputEntityObject value) {
    if (value == null) {
      super.set(new OutputEntityObject());
    }
    super.set(value);
  }

  @Override
  public String pickle() {
    OutputEntityObject entityObject = get();
    if (entityObject == null) {
      return "null";
    }
    return entityObject.getEntityName() + NAME_COMMENT_SEPARATOR + entityObject.getComments();
  }

  @Override
  public void unpickle(final String valueObject) {
    if (valueObject == null) {
      set(new OutputEntityObject());
    } else {
      String valueStr = valueObject.toString();
      if (valueStr.contains(NAME_COMMENT_SEPARATOR)) {
        String[] substrings = valueStr.split(NAME_COMMENT_SEPARATOR);
        set(new OutputEntityObject(substrings[0], substrings[1]));
      } else {
        set(new OutputEntityObject(valueStr, ""));
      }
    }
  }
}
