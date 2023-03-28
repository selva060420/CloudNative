package collections.stack;

import java.util.Stack;
//stack is synchronized beacause it extends vectors which is synchronized
public class StackUseCases {

	public static void main(String[] args) {
		Stack<String> stack=new Stack<String>();
		
		
		stack.push("Selva");
		stack.push("kumar");
		stack.pop();
		System.out.println(stack.peek());
		System.out.println(stack);
		System.out.println(stack.isEmpty());
	}

}
