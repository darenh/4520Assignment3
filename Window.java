// Based off page 216 in the art of multiprocessor programming
// book.
public class Window
{
	public ItemNode pred, curr;
	public Window(ItemNode pred, ItemNode curr)
	{
		this.pred = pred;
		this.curr = curr;
	}

	public static Window find(ItemNode head, int key)
	{
		ItemNode pred = null, curr = null, succ = null;
		boolean[] marked = {false};
		boolean snip;
		retry: while (true)
		{
			pred = head;
			curr = pred.next.getReference();
			while (true)
			{
				succ = curr.next.get(marked);
				while (marked[0])
				{
					snip = pred.next.compareAndSet(curr, succ, false, false);
					if (!snip)
						continue retry;
					curr = succ;
					succ = curr.next.get(marked);
				}
				if (curr.key >= key)
					return new Window(pred, curr);
				pred = curr;
				curr = succ;
			}
		}
	}
}
