import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.SearchListenerAdapter;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.LocalVarInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class Diagnostics extends SearchListenerAdapter {
  public void propertyViolated(Search search) {
/*
    VM vm = search.getVM();
    ThreadInfo[] threads = vm.getLiveThreads();
//    ElementInfo[] eiCandidates = new ElementInfo[threads.length];
//    FieldInfo[] fiCandidates = new FieldInfo[threads.length];
//    ClassLoader classLoader = Class
    for (int i = 0; i < threads.length; i++) {
      ClassInfo tcl = threads[i].getClassInfo();
      if (tcl.getName().equals("TestThread")) {
	System.out.println(tcl.getName());
//	FieldInfo f1 = tcl.getInstanceField(0);
//	FieldInfo f2 = tcl.getInstanceField(1);
	ElementInfo ei = threads[i].getThisElementInfo();
//	FieldInfo f1 = ei.getFieldInfo("idx1");
//	FieldInfo f2 = ei.getFieldInfo("idx2");
//	if (f1 != null && f2 != null) {
	  int l1 = ei.getIntField("idx1");
	  int l2 = ei.getIntField("idx2");
	  System.out.println("Thread " + threads[i].getId() +
			     " uses locks " + l1 + 
			     ", " + l2);
//	}
      }
    }
*/
  }
}
