public class Timer_Var {
    private int id;
    private String timer_name;

    public Timer_Var(int id) {
        this.id = id;
        this.timer_name = "TIMEOUT_" + Main.component_name + "_" + id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimer_name() {
        return timer_name;
    }

    public void setTimer_name(String timer_name) {
        this.timer_name = timer_name;
    }
}
