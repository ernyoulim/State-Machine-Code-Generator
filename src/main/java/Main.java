import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static ArrayList<String> receivedEvents = new ArrayList<>();

    public static String component_name;

    public static void printEvent() {
        int i,j;

        for (i = 0; i < Timer_Var_Singleton.timerVarArrayList.size(); i++) {
            System.out.println("#define " +  Timer_Var_Singleton.timerVarArrayList.get(i).getTimer_name() + " " + "EVENT" + (Timer_Var_Singleton.timerVarArrayList.get(i).getId() + 1) );
        }

        for (j = 0; j < receivedEvents.size(); j++) {
            System.out.println("#define " + receivedEvents.get(j) + " EVENT" + ++i );
        }
        System.out.println(" ");

    }

    public static void printDefine(State state) {
        System.out.println("#define S_Default 0");
        if (!state.isRoot())
            System.out.println("#define S_" + state.getName() + " 1");
        state.logDefineChildStates();
        System.out.println(" ");
    }

    public static void generateCodeRecursive(State state, String parent_name) {
        // print all functions name per level
        int i;
        String name;
        ArrayList<State> childStates;

        if (state.getChildStates().isEmpty())
            return;

        state.logInternalState(parent_name);

        childStates = state.getChildStates();

        //System.out.println("static void doStep_" + parent_name + "(uint32_t tick) {");

        // print all transition of all child state
        for (i = 0; i < childStates.size(); i++) {

            if (state.isRoot())
                name = childStates.get(i).getName();
            else
                name = parent_name + "_" + childStates.get(i).getName();

            generateCodeRecursive(childStates.get(i), name);
        }
    }

    public static void declareStates(State state) {
        state.declareStates("");
        System.out.println(" ");
    }

    public static Transition createTransition(ArrayList<String> events, ArrayList<String> actions, State dstState, State srcState) {
        int i,end;
        String event;
        String action;

        ArrayList<Event> eventArrayList = new ArrayList<>();
        ArrayList<String> actionsArrayList = new ArrayList<>();

        Transition s = new Transition();

        for (i = 0; i < events.size(); i++) {
                event = events.get(i);
                if (event.startsWith("tm(")){
                    end = event.indexOf(")");
                    s.setReceived_event(new Timeout(Integer.parseInt(event.substring(3, end))));
                } else if (event.startsWith("[")) {
                    end = event.indexOf("]");
                    s.setGuard(event.substring(1,end));
                } else if (!event.equals("..")) {
                    s.setReceived_event(new ReceivedEvent(event));
                    if (!event.startsWith("[") && !receivedEvents.contains(event)) {
                        receivedEvents.add(event);
                    }
                }
        }
        //all_events.addAll(eventArrayList);

        for (i = 0; i < actions.size(); i++) {
            action = actions.get(i);
            if (action.equals(".."))
                continue;
            actionsArrayList.add(action);
        }

        s.setActions(actionsArrayList);
        s.setDstState(dstState);
        s.setSrcState(srcState);
        return s;
    }

    public static ArrayList<Transition> getTransition(JSONObject transition, ArrayList<State> stateArrayList, State srcState) throws JSONException {
        ArrayList<String> actions;
        ArrayList<String> events;
        ArrayList<String> elseActions;

        Iterator<String> keys = transition.keys();

        ArrayList<Transition> transitions = new ArrayList<>();


        while (keys.hasNext()) {
            String input = keys.next();
            Object dstStateObj = transition.get(input);
            State dstState = stateArrayList.stream().filter(state -> Objects.equals(state.getName(), dstStateObj.toString())).findFirst().orElseThrow();

            ArrayList<String> events_actions = new ArrayList<>(Arrays.asList(input.split("/")));

            events =  new ArrayList<>(Arrays.asList(events_actions.get(0).split("\\^")));

            //String elseA = events_actions.get(1).substring(events_actions.get(1).indexOf("|"));

            actions = new ArrayList<>(Arrays.asList(events_actions.get(1).split(";")));

            Transition s = createTransition(events,actions,dstState, srcState);

            transitions.add(s);
            // System.out.println(s.getActions());
        }
        return transitions;
    }


    public static State createState(JSONObject stateObj) throws JSONException {
        if (stateObj.getJSONObject("children").length() == 0){
            return new OrState();
        }
        if (stateObj.getJSONObject("children").getBoolean("and")) {
            return new AndState();
        }
        return  new OrState();
    }

    public static ArrayList<State> getChildren(JSONArray states) throws JSONException {
        int i;

        ArrayList<State> stateArrayList = new ArrayList<>();

        for (i = 0; i < states.length(); i++){
            JSONObject stateObj = states.getJSONObject(i);
            State state = createState(stateObj);
            // settle transition
            JSONObject childStatesObj = stateObj.getJSONObject("children");

            String statenName = stateObj.getString("name");
            state.setName(statenName);

            stateArrayList.add(state);
            // System.out.println(childStatesObj);

            if (childStatesObj.length() == 0) {
                // System.out.println("emtpy\n");
                continue;
            }

            ArrayList<State> childStates = getChildren(childStatesObj.getJSONArray("states"));

            state.setChildStates(childStates);

        }

        for (i = 0; i < states.length(); i++) {
            JSONObject stateObj = states.getJSONObject(i);
            JSONObject transition = stateObj.getJSONObject("transition");

            stateArrayList.get(i).setTransitions(getTransition(transition, stateArrayList, stateArrayList.get(i)));
        }
        return stateArrayList;
    }


    public static void main(String[] args) throws IOException, JSONException {
        int i;
        String filePath = "Controller.txt";
        String jsonString = new String(Files.readAllBytes(Paths.get(filePath)));
        //
        JSONObject jsonObject = new JSONObject(jsonString);
        OrState rootState = new OrState();
        rootState.setRoot(true);
        String name = jsonObject.getString("Component_Name");
        Main.component_name = name;
        rootState.setName(name);
        rootState.setChildStates(getChildren(jsonObject.getJSONArray("States")));

        rootState.distributeTimer(0);
        printDefine(rootState);
        printEvent();
        declareStates(rootState);
        generateCodeRecursive(rootState, rootState.getName());
    }
}