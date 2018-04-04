package data_structures.eseq;

public class Pair<L,R> {

  private final L left;
  private R right;

  public Pair(L left, R right) {
    this.left = left;
    this.right = right;
  }
  
  public L getLeft() { return left; }
  public R getRight() { return right; }
  public void putRight(R value){ right=value;}
  
  
  @Override
  public int hashCode() { return left.hashCode() ^ right.hashCode(); }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Pair)) return false;
    Pair pairo = (Pair) o;
    return this.left.equals(pairo.getLeft()) &&
           this.right.equals(pairo.getRight());
  }

  public String toString()
  {
	  String res = "(" + left + "," + right + ")";
	  return res;
  }
  
}