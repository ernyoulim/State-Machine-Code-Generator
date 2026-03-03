import java.util.ArrayList;

public class OrState extends State{

    private ArrayList<State> childStates = new ArrayList<>();

    private String whitespaces = " ";


    @Override
    public ArrayList<State> getChildStates() {
        return childStates;
    }

    @Override
    public void setChildStates(ArrayList<State> childStates) {
        this.childStates = childStates;
    }

    private void tab() {
        this.whitespaces = this.whitespaces.repeat(4);
    }

    private void space() {
        this.whitespaces = this.whitespaces + " ";
    }

    private void printRoot() {
        System.out.println("static void " + getName() + "_stmDoStep(uint32_t tick) {");
    }

    @Override
    public void logInternalState(String parent_name) {
        int i,j,k;
        State childState;
        Transition childStateTransition;

        if (isRoot())
            printRoot();
        else {
            System.out.println("static void doStep_" + parent_name + "(uint32_t tick) {");
        }

        tab();
        System.out.println(this.whitespaces + "switch(state_" + parent_name + ") {" );
        space();

        for (i = 0; i < childStates.size(); i++) {
            childState = childStates.get(i);
            System.out.println(this.whitespaces+ "case S_" + childState.getName() + ":");

            if (childState.getTransitions().isEmpty() && !childState.getChildStates().isEmpty()) {
                System.out.println(this.whitespaces + "  " + "doStep_" + childState.getName() + "(tick);" );
            }

            for (j = 0; j < childState.getTransitions().size(); j++) {
                childStateTransition = childState.getTransitions().get(j);
                childStateTransition.logTransition(this.whitespaces, parent_name,j);
            }
            System.out.println(this.whitespaces + "  " + "break;");
        }
        System.out.println(this.whitespaces + "}");
        System.out.println("}\n");
    }

    @Override
    public void logDefineChildStates() {
        int i;
        State state;

        if (childStates.isEmpty())
            return;

        for (i = 0; i < childStates.size(); i++) {
            state = childStates.get(i);

            if (!state.getName().equals("Default")) {
                System.out.println("#define S_" + state.getName() + " " + i);
            }
        }

        for (i = 0; i < childStates.size(); i++) {
            state = childStates.get(i);
            state.logDefineChildStates();
        }


    }

    @Override
    public void declareStates(String parent_name) {

        String state_name;

        if (childStates.isEmpty())
            return;

        if (parent_name.isEmpty())
            state_name = getName();
        else
            state_name = parent_name + "_" + getName();

        System.out.println("static int state_" + state_name + " = " + "S_Default;");

        for (State childState : childStates) {
            if (isRoot()) {
                childState.declareStates("");
            } else {
                childState.declareStates(state_name);
            }
        }
    }

    public boolean childHasTimerTransition() {
        int i,j,k;
        ArrayList<Transition> transitions;

        for (i = 0; i < childStates.size(); i++) {
            transitions = childStates.get(i).transitions;
            for (j = 0; j < transitions.size(); j++) {
                Transition t = transitions.get(j);
                Event e = t.getReceived_event();

                if (e != null && e.isTimeout())
                    return true;
            }
        }
        return false;
    }

    public void assignTimerToChildTransition(Timer_Var timer) {
        int i, j, k;
        State childState;
        Transition transition;

        for (i = 0; i < childStates.size(); i++){
            childState = childStates.get(i);
            for (j = 0; j < childState.transitions.size(); j++) {
                transition = childState.getTransitions().get(j);
                Event e = transition.getReceived_event();

                if (e != null && e.isTimeout()) {
                    Timeout timeout = (Timeout) e;
                    timeout.setTimer(timer);
                }
            }

        }
    }

    @Override
    public int distributeTimer(int number) {
        int i, max = 0, cur_no;

        if (childStates.isEmpty())
            return number;

        if (childHasTimerTransition()) {
            Timer_Var timerVar = Timer_Var_Singleton.createOrGetTimer(number);
            this.setChildTimer(timerVar);
            assignTimerToChildTransition(timerVar);
            number =+ 1;
        }

        for (i = 0; i < childStates.size(); i++) {
            cur_no = childStates.get(i).distributeTimer(number);

            if (cur_no > max) {
                max = cur_no;
            }
        }

        return max;
    }

}
