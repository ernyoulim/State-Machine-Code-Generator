import java.util.ArrayList;

public abstract class State {
    public String name;
    public ArrayList<Transition> transitions;
    private Timer_Var childTimer;

    private boolean isRoot;

    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean root) {
        isRoot = root;
    }

    public boolean aldPrintTimeout = false;

    public boolean isAldPrintTimeout() {
        return aldPrintTimeout;
    }

    public void setAldPrintTimeout(boolean aldPrintTimeout) {
        this.aldPrintTimeout = aldPrintTimeout;
    }

    public abstract ArrayList<State> getChildStates();
    public abstract void setChildStates(ArrayList<State> childStates);
    public abstract void logInternalState(String parentName);

    public abstract void logDefineChildStates();

    public abstract void declareStates(String parent_name);

    public State() {
        this.name = null;
        this.transitions = null;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Transition> getTransitions() {
        return transitions;
    }

    public void setTransitions(ArrayList<Transition> transitions) {
        this.transitions = transitions;
    }


     public Timer_Var getChildTimer() {
         return childTimer;
     }

     public void setChildTimer(Timer_Var childTimer) {
         this.childTimer = childTimer;
     }

     @Override
    public String toString() {
        return "State{" +
                "name='" + name + '\'' +
                ", transitions=" + transitions +
                '}';
    }

    public abstract int distributeTimer(int number);
}
