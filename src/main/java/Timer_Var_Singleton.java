import java.util.ArrayList;
import java.util.Optional;

public class Timer_Var_Singleton {

    public static final ArrayList<Timer_Var> timerVarArrayList = new ArrayList<>();

    public static Timer_Var createOrGetTimer(int id) {
        Optional<Timer_Var> timerVar = timerVarArrayList.stream().filter(timer -> timer.getId() == id).findFirst();

        if (timerVar.isPresent()) {
            return timerVar.get();
        } else {
            Timer_Var new_timer = new Timer_Var(id);
            timerVarArrayList.add(new_timer);
            return new_timer;
        }
    }

}
