import java.util.ArrayList;

public class Transition {

    public Transition() {
        this.received_event = null;
        this.guard = null;
        this.actions = null;
        this.dstState = null;
    }

    public Event received_event;

    public String guard;

    private State srcState;

    public Event getReceived_event() {
        return received_event;
    }

    public void setReceived_event(Event received_event) {
        this.received_event = received_event;
    }

    public String getGuard() {
        return guard;
    }

    public void setGuard(String guard) {
        this.guard = guard;
    }

    public State getSrcState() {
        return srcState;
    }

    public void setSrcState(State srcState) {
        this.srcState = srcState;
    }

    public State getDstState() {
        return dstState;
    }

    public void setDstState(State dstState) {
        this.dstState = dstState;
    }

    public ArrayList<String> actions;

    public ArrayList<String> elseActions;

    public ArrayList<String> getElseActions() {
        return elseActions;
    }

    public void setElseActions(ArrayList<String> elseActions) {
        this.elseActions = elseActions;
    }

    public State dstState;

    public ArrayList<String> getActions() {
        return actions;
    }

    public void setActions(ArrayList<String> actions) {
        this.actions = actions;
    }

    public void logTimer(String whitespaces, Event event){
        event.logTimer(whitespaces);
    }

    public void logActions(String whitespaces){
        int i;
        for (i = 0; i < actions.size(); i++) {
            if (actions.get(i).startsWith("<"))
                continue;
            if (actions.get(i).startsWith("("))
                System.out.println(whitespaces + "setEvent" + actions.get(i) + ";");
            else
                System.out.println(whitespaces + actions.get(i) + ";");
        }
    }

    public void logElseActions(String whitespaces) {
        int i, endidx;
        for (i = 0; i < actions.size(); i++) {
            if (actions.get(i).startsWith("<")) {
                endidx = actions.get(i).indexOf(">");
                System.out.println(whitespaces + actions.get(i).substring(1,endidx) + ";");
            }
        }
    }

    public void logUseSocket(String whitespaces, String parent_name, String dstName) {
        int i;
        System.out.println(whitespaces + "#ifdef USESOCKET");
        if (parent_name.equals(Main.component_name)) {
            System.out.println(whitespaces + "sendActivation(\"" + parent_name + "/" + dstName +"\")");
            System.out.println(whitespaces + "sendDeactivation(\"" + parent_name + "/" + srcState.getName() +"\")");
        }
        else {
            System.out.println(whitespaces + "sendActivation(\"" + Main.component_name + "/" + parent_name.replace("_", "/") + "/" + dstName+"\")");
            System.out.println(whitespaces + "sendDeactivation(\"" + Main.component_name + "/" + parent_name.replace("_", "/") + "/" + srcState.getName()+"\")");
        }

        if (!srcState.getChildStates().isEmpty()) {
            System.out.println(whitespaces + "switch(state_" + parent_name + ") {");
            for (i = 0; i < srcState.getChildStates().size(); i++) {
                String childStateName = srcState.getChildStates().get(i).getName();
                System.out.println(whitespaces + "case S_" + parent_name + "_" + childStateName);
                System.out.println(whitespaces + "sendDeactivation(\"" + Main.component_name + "/" + parent_name.replace("_", "/") + "/" + srcState.getName()+ "/" +  childStateName + "\")");
                System.out.println(whitespaces + "break;");
            }
        }
        System.out.println(whitespaces + "#endif");
    }

    public void logNxtState(String whitespaces, String parent_name) {
        System.out.println(whitespaces + "state_" + parent_name + " = " + "S_" + dstState.getName() + ";");
        logUseSocket(whitespaces, parent_name, dstState.name);
    }

    public void cancelTimer(String whitespaces, int id) {
        System.out.println(whitespaces + "cancelTimer(" + id  + ");");
    }

    public void restart_timer(String whitespaces) {
        received_event.logTimer(whitespaces);
    }

    public void printCodeIdx(String whitespaces, String parent_name, int idx) {
        int i;
        StringBuilder cur_whitespaces = new StringBuilder(whitespaces);
        Transition transition;
        Event event;

        String ifString;

        if (idx == 0)
            ifString = "if (";
        else
            ifString = "else if (";

        if (received_event != null) {
            cur_whitespaces.append(" ".repeat(1));
            if (!srcState.aldPrintTimeout) {
                System.out.println(cur_whitespaces + ifString + "eventIsSet(" + received_event.getName() + ")) {");
            }
        }

        if (this.guard != null) {
            cur_whitespaces.append(" ".repeat(1));
            System.out.println(cur_whitespaces+ ifString + guard + ") {");
            srcState.setAldPrintTimeout(true);
        }

        cur_whitespaces.append(" ");
        logActions(cur_whitespaces.toString());
        logNxtState(cur_whitespaces.toString(), parent_name);

        if (!srcState.getChildStates().isEmpty()) {
            cancelTimer(cur_whitespaces.toString(), srcState.getChildTimer().getId());
            System.out.println(cur_whitespaces + "state_" + parent_name + "_" + srcState.getName() + " = " + "S_Default;" );
        }

        if (received_event != null) {
            if (!received_event.isTimeout()) {
                for (i = 0; i < srcState.getTransitions().size(); i++) {
                    transition = srcState.getTransitions().get(i);

                    event = transition.getReceived_event();

                    if (event == null)
                        continue;

                    if (event.isTimeout()) {
                        Timeout t = (Timeout) event;
                        cancelTimer(cur_whitespaces.toString(), t.getTimer().getId());
                        break;
                    }
                }
            }
        }

        for (i = 0; i < dstState.getTransitions().size(); i++) {
            transition = dstState.getTransitions().get(i);

            event = transition.getReceived_event();

            if (event == null)
                continue;

            if (event.isTimeout()) {
                logTimer(cur_whitespaces.toString(), event);
                break;
            }
        }

        if (!dstState.getChildStates().isEmpty()) {
            System.out.println(cur_whitespaces + "doStep_" + parent_name + "_" + dstState.getName() + "(tick)" + ";");
        }

        if (idx == srcState.getTransitions().size() - 1 && srcState.aldPrintTimeout) {
                System.out.println(cur_whitespaces + "} else {");
                restart_timer(cur_whitespaces + " ");
                logElseActions(cur_whitespaces.toString());
                System.out.println(cur_whitespaces + "}");
        }

        if (this.received_event != null)
            System.out.println(cur_whitespaces + "}");

    }

    public void logTransition(String whitespaces, String parent_name, int idx) {
        whitespaces = whitespaces + " ";
        printCodeIdx(whitespaces,parent_name,idx);
    }

    @Override
    public String toString() {
        return "Transition{" +
                "event='" + received_event + '\'' +
                ", actions=" + actions +
                ", dstState=" + dstState +
                '}';
    }
}
