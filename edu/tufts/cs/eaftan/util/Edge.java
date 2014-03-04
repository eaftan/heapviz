package edu.tufts.cs.eaftan.util;

public class Edge<V, E> {
  
  public V from;
  public V to;
  public E data;
  public boolean ownership;
  public boolean pointer;
  
  public Edge(V from, V to, E data, boolean pointer, boolean ownership) {
    this.from = from;
    this.to = to;
    this.data = data;
    this.pointer = pointer;
    this.ownership = ownership;
  }
  
  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + ((data == null) ? 0 : data.hashCode());
    result = PRIME * result + ((from == null) ? 0 : from.hashCode());
    result = PRIME * result + (ownership ? 1231 : 1237);
    result = PRIME * result + (pointer ? 1231 : 1237);
    result = PRIME * result + ((to == null) ? 0 : to.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Edge other = (Edge) obj;
    if (data == null) {
      if (other.data != null)
        return false;
    } else if (!data.equals(other.data))
      return false;
    if (from == null) {
      if (other.from != null)
        return false;
    } else if (!from.equals(other.from))
      return false;
    if (ownership != other.ownership)
      return false;
    if (pointer != other.pointer)
      return false;
    if (to == null) {
      if (other.to != null)
        return false;
    } else if (!to.equals(other.to))
      return false;
    return true;
  }

  public boolean equalsToFromData(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Edge other = (Edge) obj;
    if (data == null) {
      if (other.data != null)
        return false;
    } else if (!data.equals(other.data))
      return false;
    if (from == null) {
      if (other.from != null)
        return false;
    } else if (!from.equals(other.from))
      return false;
    if (to == null) {
      if (other.to != null)
        return false;
    } else if (!to.equals(other.to))
      return false;
    return true;
  }

  
}
