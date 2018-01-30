package cstar.yongfeng.collect;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cc.kave.commons.model.events.CommandEvent;
import cc.kave.commons.model.events.IDEEvent;
import cc.kave.commons.model.events.visualstudio.DebuggerEvent;
import cc.kave.commons.model.events.visualstudio.DebuggerMode;
import cc.kave.commons.model.events.visualstudio.WindowAction;
import cc.kave.commons.model.events.visualstudio.WindowEvent;

/***
 * <p>This class <b>Analyzer</b> provides the function to calculate the debugging operations
 * of debugging in Visual Studio. Including the <b>Debugging Common Sences, Debugging Foundations, Debugging Tricks</b>.</p>
 * <p>To instantiate an object of Analyzer initialized with the parameter, lslsEvent, list of event stream. 
 * </p>
 */
public class Analyzer {

	/** list of event stream */
	private List<ArrayList<IDEEvent>> lslsEvents;
	
	public Analyzer(List<ArrayList<IDEEvent>> ls){
		if(ls == null){
			System.out.println("[ERROR]: Developers' list of event stream can not be NULL!");
			return;
		}
		this.lslsEvents = new ArrayList<ArrayList<IDEEvent>>(ls);
	}
	
	/////////////////////////////////
	// Common Sences
	
	/** To get the stream times of whole event streams.*/
	public int getStreamTimes(){
		return this.lslsEvents.size();
	}
	
	/** To get time duration between two event.
	 * @return duration in Long.
	 * */
	public long getDurationBy(IDEEvent eventStart, IDEEvent eventEnd){
		long duration = 0l;
		
		Date dateStart = Date.from(eventStart.getTriggeredAt().toInstant());
		Date dateEnd = Date.from(eventEnd.getTriggeredAt().toInstant());
		long longStart = dateStart.getTime();
		long longEnd = dateEnd.getTime();
		duration = longEnd - longStart;
		
//		System.out.println("[start]:" + eventStart.getTriggeredAt().toString() + "[end]:" + eventEnd.getTriggeredAt().toString());
//		System.out.println("[start]:" + dateStart.getTime() + "[end]:" + dateEnd.getTime());
//		System.out.println("[duration]:" + duration + "\n");
		
		return duration;
	}
	
	/**
	 * <p>To get the debugging duration time of whole event streams.</p>
	 * @return duration time
	 */
	public long getStreamDuration(){
		long tim = 0l;
		
		for(int i=0; i<lslsEvents.size(); i++){ // for each stream
			ArrayList<IDEEvent> lsStream = lslsEvents.get(i);
			int lenStream = lsStream.size();
			IDEEvent eventStart = lsStream.get(0);
			IDEEvent eventEnd = lsStream.get(lenStream-1);
			long deltTime = getDurationBy(eventStart, eventEnd);
			tim += deltTime;
		}
		return tim;
	}
	
	/////////////////////////////////
	// Debugging Foundations
	
	/**
	 * <p>Feature 1: To calculate the number of using breakpoint.</p>
	 * @return breakpoint usage time
	 * @BUG If the developer sets breakpoint by clicking and the program did not stop at the breakpoint,
	 *      the operation <b>CAN NOT</b> be captured.
	 */
	public int getBreakpoint(){
		int count = 0;
		
		for(int i=0; i<lslsEvents.size(); i++){ // for each stream
			ArrayList<IDEEvent> lsStream = lslsEvents.get(i);
			for(int j=0; j<lsStream.size(); j++){ // for each event
				IDEEvent event = lsStream.get(j);
				if(event instanceof CommandEvent){
					/** insert breakpoint by using right click menu */
					boolean flag1 = ((CommandEvent)event).getCommandId().contains(":375:EditorContextMenus.CodeWindow.Breakpoint.InsertBreakpoint");
					/** insert breakpoint by pressing shortcut F9 */
					boolean flag2 = ((CommandEvent)event).getCommandId().contains(":255:Debug.ToggleBreakpoint");
					
					if( flag1 || flag2){
						count++;
//						System.out.println("[time]:" + event.TriggeredAt.toString());
						break;
					}
				}else if(event instanceof DebuggerEvent){
					/** program stops at the breakpoint */
					boolean flag3 = (((DebuggerEvent)event).Mode==DebuggerMode.Break) && (((DebuggerEvent)event).Reason.equals("dbgEventReasonBreakpoint"));
					
					if( flag3 ){
						count++;
//						System.out.println("[time]:" + event.TriggeredAt.toString());
						break;
					}
				}else{
					continue;
				}
			}
		}
		
		return count;
	}
	
	/**
	 * <p>Feature 2: To calculate the number of using restart.</p>
	 * @return restart usage time
	 */
	public int getRestart(){
		int count = 0;
		
		for(int i=0; i<lslsEvents.size(); i++){ // for each stream
			ArrayList<IDEEvent> lsStream = lslsEvents.get(i);
			for(int j=0; j<lsStream.size(); j++){ // for each event
				IDEEvent event = lsStream.get(j);
				if(event instanceof CommandEvent){
					/** restart debugging in the original debugging process */
					boolean flag1 = ((CommandEvent)event).getCommandId().contains(":296:Debug.Restart");
					
					if(flag1){
						count++;
						break;
					}
				}else{
					continue;
				}
			}
			
		}
		
		return count;
	}
	
	/**
	 * <p>Feature 3: To calculate the number of using StepInto and StepOver.</p>
	 * @return StepInto, StepOver usage time
	 */
	public int getStepIntoOver(){
		int count = 0;
		
		for(int i=0; i<lslsEvents.size(); i++){ // for each stream
			ArrayList<IDEEvent> lsStream = lslsEvents.get(i);

			for(int j=0; j<lsStream.size(); j++){ // for each event
				IDEEvent event = lsStream.get(j);
				if(event instanceof CommandEvent){
					/** stepInto operation in debugging process */
					boolean flag1 = ((CommandEvent)event).getCommandId().contains(":248:Debug.StepInto");
					/** stepOver operation in debugging process */
					boolean flag2 = ((CommandEvent)event).getCommandId().contains(":249:Debug.StepOver");
					
					if( flag1 || flag2){
						count++;
						break;
					}
				}else{
					continue;
				}
			}
			
			
		}
		
		return count;
	}
	
	/**
	 * <p>Feature 4: To calculate the number of using StepIntoSpecific.</p>
	 * @return StepIntoSpecific usage time
	 */
	public int getStepIntoSpecific(){
		int count = 0;
		
		for(int i=0; i<lslsEvents.size(); i++){ // for each stream
			ArrayList<IDEEvent> lsStream = lslsEvents.get(i);
			int flag1 = 0;
			int flag2 = 0;
			for(int j=0; j<lsStream.size(); j++){ // for each event
				IDEEvent event = lsStream.get(j);

				if(event instanceof DebuggerEvent){
					/** stepIntoSpecific operation in debugging process */
					if(((DebuggerEvent)event).Mode == DebuggerMode.Run && ((DebuggerEvent)event).Reason.equals("dbgEventReasonGo")==true){
						flag1 ++;
					}

				}else if(event instanceof CommandEvent){
					/** click unknown button */
					if(((CommandEvent)event).getCommandId().equals("unknown button")==true){
						flag2++;
					}	
				}else{
					continue;
				}	
				
			}
			if( flag1>=1 && flag2>=1 ){
				count++;
			}
		}
		
		return count;
	}
	
	/**
	 * <p>Feature 5: To calculate the number of using Monitor Windows to check the value of variables.</p>
	 * <p>Monitor Windows includes <b>Auto, Locals, Watch, Call Stack, Breakpoints, Exception Settings, Command Window, Immediate Window, Diagnostic Tools</b></p>
	 * @return Monitor Windows usage time 
	 */
	public int getMonitors(){
		int count=0;
		
		for(int i=0; i<lslsEvents.size(); i++){ // for each stream
			ArrayList<IDEEvent> lsStream = lslsEvents.get(i);
			for(int j=0; j<lsStream.size(); j++){ // for each event
				IDEEvent event = lsStream.get(j);

				if(event instanceof WindowEvent){
					WindowEvent we = (WindowEvent)event;
					
					/** activate the following 9 monitoring windows*/
					boolean flag1 = (we.Action == WindowAction.Activate) && we.Window.getCaption().contains("Auto");
					boolean flag2 = (we.Action == WindowAction.Activate) && we.Window.getCaption().contains("Locals");
					boolean flag3 = (we.Action == WindowAction.Activate) && we.Window.getCaption().contains("Watch 1");
					boolean flag4 = (we.Action == WindowAction.Activate) && we.Window.getCaption().contains("Call Stack");
					boolean flag5 = (we.Action == WindowAction.Activate) && we.Window.getCaption().contains("Breakpoints");
					boolean flag6 = (we.Action == WindowAction.Activate) && we.Window.getCaption().contains("Exception Settings");
					boolean flag7 = (we.Action == WindowAction.Activate) && we.Window.getCaption().contains("Command Window");
					boolean flag8 = (we.Action == WindowAction.Activate) && we.Window.getCaption().contains("Immediate Window");
					boolean flag9 = (we.Action == WindowAction.Activate) && we.Window.getCaption().contains("Diagnostic Tools");
	
					if(flag1 || flag2 || flag3 || flag4 || flag5 || flag6 || flag7 || flag8 || flag9){ // activate one of them
						count++;
//						System.out.println("[window]: " + we.Window.getCaption());
						break;
					}

				}else{
					continue;
				}	
				
			}
		}
		
		return count;
	}
	
	/**
	 * <p>Feature 6: To calculate the number of using operation <b>StepOut</b> (Shift + F11).</p>
	 * @return StepOut usage time 
	 */
	public int getStepOut(){
		int count=0;
		
		for(int i=0; i<lslsEvents.size(); i++){ // for each stream
			ArrayList<IDEEvent> lsStream = lslsEvents.get(i);
			for(int j=0; j<lsStream.size(); j++){ // for each event
				IDEEvent event = lsStream.get(j);

				if(event instanceof CommandEvent){
					CommandEvent ce = (CommandEvent)event;
					/** stepOut operation*/
					boolean flag1 = ce.getCommandId().contains(":250:Debug.StepOut");
	
					if( flag1 ){
						count++;
//						System.out.println("[command]: " + ce.getCommandId());
						break;
					}

				}else{
					continue;
				}	
				
			}
		}
		
		return count;
	}
	
	/**
	 * <p>Feature 7: To calculate the number of using operation <b>Run To Cursor</b>.</p>
	 * <p>Note that the operation can be conducted <b>BEFORE</b> or <b>DURING</b> the debugging process, the triggered events might be a little different.</p>
	 * @return Run to cursor operations usage time
	 */
	public int getRuntoCursor(){
		int count = 0;
		
		for(int i=0; i<lslsEvents.size(); i++){ // for each stream
			ArrayList<IDEEvent> lsStream = lslsEvents.get(i);
			for(int j=0; j<lsStream.size(); j++){ // for each event
				IDEEvent event = lsStream.get(j);

				if(event instanceof CommandEvent){
					CommandEvent ce = (CommandEvent)event;
					/** run to cursor operation*/
					boolean flag1 = ce.getCommandId().contains(":251:Debug.RunToCursor");
	
					if( flag1 ){
						count++;
//						System.out.println("[command]: " + ce.getCommandId());
						break;
					}

				}else{
					continue;
				}	
				
			}
		}
		
		return count;
	}
	
	/**
	 * <p>Feature 8: To calculate the number of using operation <b>Add to Watch</b>.</p>
	 * @return Run to cursor operations usage time
	 */
	public int getAddWatch(){
		int count=0;
		
		for(int i=0; i<lslsEvents.size(); i++){ // for each stream
			ArrayList<IDEEvent> lsStream = lslsEvents.get(i);
			for(int j=0; j<lsStream.size(); j++){ // for each event
				IDEEvent event = lsStream.get(j);

				if(event instanceof CommandEvent){
					CommandEvent ce = (CommandEvent)event;
					/** run to cursor operation*/
					boolean flag1 = ce.getCommandId().contains(":252:Debug.AddWatch");
	
					if( flag1 ){
						count++;
//						System.out.println("[command]: " + ce.getCommandId());
						break;
					}

				}else{
					continue;
				}	
				
			}
		}
		
		return count;
	}
	
	/////////////////////////////////
	// Debugging Tricks
	
	/**
	 * <p>Feature 9: To calculate the number of using operation <b>Editing</b>.</p>
	 * @return Editing usage time
	 */
	public int getEditing(){
		int count = 0;
		
		for(int i=0; i<lslsEvents.size(); i++){ // for each stream
			ArrayList<IDEEvent> lsStream = lslsEvents.get(i);
			for(int j=0; j<lsStream.size(); j++){ // for each event
				IDEEvent event = lsStream.get(j);

				if(event instanceof CommandEvent){
					CommandEvent ce = (CommandEvent)event;
					/** editing during the debugging */
					boolean flag1 = ce.getCommandId().contains("VsAction:1:Edit");
	
					if( flag1 ){
						count++;
//						System.out.println("[command]: " + ce.getCommandId());
						break;
					}

				}else{
					continue;
				}	
				
			}
		}
		
		return count;
	}
	
	/**
	 * <p>Feature 10: To calculate the number of using operation <b>Breakpoint Condition Setting</b>.</p>
	 * @return Breakpoint Condition Setting usage time
	 */
	public int getBreakCondition(){
		int count = 0;
		
		for(int i=0; i<lslsEvents.size(); i++){ // for each stream
			ArrayList<IDEEvent> lsStream = lslsEvents.get(i);
			for(int j=0; j<lsStream.size(); j++){ // for each event
				IDEEvent event = lsStream.get(j);

				if(event instanceof CommandEvent){
					CommandEvent ce = (CommandEvent)event;
					/** setting Breakpoint Condition  */
					boolean flag1 = ce.getCommandId().contains("Conditions...");
					boolean flag2 = ce.getCommandId().contains(":320:EditorContextMenus.CodeWindow.Breakpoint.BreakpointConditions");
	
					if( flag1 || flag2){
						count++;
						System.out.println("[command]: " + ce.getCommandId());
						break;
					}

				}else{
					continue;
				}	
				
			}
		}
		
		return count;
	}
	
	/**
	 * <p>Feature 11: To calculate the number of using operation <b>Changing Execution Stream</b>.</p>
	 * @return Execution changing usage time
	 * @BUG Not finished yet
	 */
	public int getExecutionChanged(){
		int count = 0;
		
		for(int i=0; i<lslsEvents.size(); i++){ // for each stream
			ArrayList<IDEEvent> lsStream = lslsEvents.get(i);
			for(int j=0; j<lsStream.size(); j++){ // for each event
				IDEEvent event = lsStream.get(j);

				if(event instanceof CommandEvent){
					CommandEvent ce = (CommandEvent)event;
					//TODO: Changing execution stream

				}else{
					continue;
				}	
				
			}
		}
		
		return count;
	}
	
	/**
	 * <p>Feature 12: To calculate the number of using operation <b>Tracking out-scope objects</b>.</p>
	 * @return tracking usage time
	 */
	public int getOutScope(){
		int count = 0;
		
		for(int i=0; i<lslsEvents.size(); i++){ // for each stream
			ArrayList<IDEEvent> lsStream = lslsEvents.get(i);
			int flag1 = 0;
			int flag2 = 0;
			for(int j=0; j<lsStream.size(); j++){ // for each event
				IDEEvent event = lsStream.get(j);			

				if(event instanceof CommandEvent){
					CommandEvent ce = (CommandEvent)event;
					if( ce.getCommandId().contains("Make Object ID") || ce.getCommandId().contains(":327:DebuggerContextMenus.AutosWindow.MakeObjectID")){
						flag1++;
					}else if( ce.getCommandId().contains("Add Watch") ||  ce.getCommandId().contains(":252:Debug.AddWatch")){
						flag2++;
					}

				}else{
					continue;
				}	
			}
			if(flag1 >= 1 && flag2 >= 1){
				count++;
			}
		}	
		return count;
	}
	
	/**
	 * <p>Feature 13: To calculate the number of using operation <b>Breaking at an Handled Exception</b>.</p>
	 * @return break at an handled exception usage time
	 */
	public int getBreakException(){
		int count = 0;
		
		for(int i=0; i<lslsEvents.size(); i++){ // for each stream
			ArrayList<IDEEvent> lsStream = lslsEvents.get(i);
			int flag1 = 0;
			int flag2 = 0;
			for(int j=0; j<lsStream.size(); j++){ // for each event
				IDEEvent event = lsStream.get(j);			

				if(event instanceof CommandEvent){
					CommandEvent ce = (CommandEvent)event;
					if(ce.getCommandId().contains("Exception Settings") || ce.getCommandId().contains(":339:Debug.ExceptionSettings")){
						flag1++;
					}
				}else if(event instanceof DebuggerEvent){
					DebuggerEvent de = (DebuggerEvent)event;
					if(de.Mode == DebuggerMode.Run && de.Reason.equals("dbgEventReasonExceptionThrown")){
						flag2++;
					}
				}else{
					continue;
				}	
			}
			if(flag1 >= 1 && flag2 >= 1){
				count++;
			}
		}	
		return count;
	}
	
	/**
	 * <p>Feature 14: To calculate the number of using operation <b>Show Threads in Source</b>.</p>
	 * @return Show Threads in Source usage time
	 */
	public int getMultiThread(){
		int count = 0;
		
		for(int i=0; i<lslsEvents.size(); i++){ // for each stream
			ArrayList<IDEEvent> lsStream = lslsEvents.get(i);
			for(int j=0; j<lsStream.size(); j++){ // for each event
				IDEEvent event = lsStream.get(j);			

				if(event instanceof CommandEvent){
					CommandEvent ce = (CommandEvent)event;
					/** press the button of Show Threads in Source*/
					boolean flag1 = ce.getCommandId().contains("Show Threads in Source") || 
							ce.getCommandId().contains(":346:DebuggerContextMenus.GPUThreadsWindowShortcutMenu.Debug.LocationToolbar.ShowThreadIpIndicators");
					if( flag1 ){
						count++;
						break;
					}
				}else{
					continue;
				}	
			}
		}	
		return count;
	}
	
	/**
	 * <p>Feature 15: To calculate the number of using operation <b>Performance Observation</b>.</p>
	 * <p></p>
	 * @return observe performance time
	 */
	public int getPerformance(){
		int count = 0;
		
		for(int i=0; i<lslsEvents.size(); i++){ // for each stream
			ArrayList<IDEEvent> lsStream = lslsEvents.get(i);
			for(int j=0; j<lsStream.size(); j++){ // for each event
				IDEEvent event = lsStream.get(j);			

				if(event instanceof CommandEvent){
					CommandEvent ce = (CommandEvent)event;
					/** press the button of Show Threads in Source*/
					boolean flag1 = ce.getCommandId().contains("Performance Profiler...") || 
							ce.getCommandId().contains(":775:Debug.DiagnosticsHub.Launch");
					if( flag1 ){
						count++;
						break;
					}
				}else{
					continue;
				}	
			}
		}	
		return count;
	}
}
