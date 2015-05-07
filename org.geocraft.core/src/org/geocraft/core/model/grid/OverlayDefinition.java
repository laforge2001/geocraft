package org.geocraft.core.model.grid;


public class OverlayDefinition implements Comparable<OverlayDefinition> {

  private final int _id;

  private final String _name;

  public OverlayDefinition(final int id, final String name) {
    _id = id;
    _name = name;
  }

  public int getId() {
    return _id;
  }

  public String getName() {
    return _name;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _id;
    result = prime * result + ((_name == null) ? 0 : _name.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    OverlayDefinition other = (OverlayDefinition) obj;
    if (_id != other._id)
      return false;
    if (_name == null) {
      if (other._name != null)
        return false;
    } else if (!_name.equals(other._name))
      return false;
    return true;
  }

  @Override
  public int compareTo(final OverlayDefinition o) {
    if (_id < o.getId()) {
      return -1;
    }
    if (_id > o.getId()) {
      return 1;
    }
    return 0;
  }

}
