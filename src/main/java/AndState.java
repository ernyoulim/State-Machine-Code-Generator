import java.util.ArrayList;

public class AndState extends State{

    private ArrayList<State> andStates = new ArrayList<>();

    @Override
    public ArrayList<State> getChildStates() {
        return andStates;
    }

    @Override
    public void setChildStates(ArrayList<State> childStates) {
        this.andStates = childStates;
    }

    @Override
    public void logInternalState(String parent_name) {
        int i;

        System.out.println("static void doStep_" + parent_name + "(uint32_t tick) {");

        for (i = 0; i < andStates.size(); i++){
            System.out.println("    doStep_" + parent_name + "_" + andStates.get(i).getName() + "(tick);");
        }
        System.out.println("}\n");
    }

    @Override
    public void logDefineChildStates() {
        int  i;

        for (i = 0; i < getChildStates().size(); i++) {
            getChildStates().get(i).logDefineChildStates();
        }
    }

    @Override
    public void declareStates(String parent_name) {
        String state_name;
        if (andStates.isEmpty())
            return;

        for (State andState : andStates) {
            if (parent_name.isEmpty())
                state_name = getName();
            else
                state_name = parent_name + "_" + getName();

            andState.declareStates(state_name);
        }
    }

    @Override
    public int distributeTimer(int no) {
        int i;

        if (andStates.isEmpty())
            return no;

        for (i = 0; i < andStates.size(); i++) {
            no = andStates.get(i).distributeTimer(no);
        }

        return no;
    }


}
