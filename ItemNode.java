// AtomicMarkableReference Used in Multiprocessor book page 214 and more
import java.util.concurrent.atomic.AtomicMarkableReference;

public class ItemNode
{
  int key;
  AtomicMarkableReference<ItemNode> next;

  public ItemNode(int currentKey)
  {
  	this.key = currentKey;
  }
}
