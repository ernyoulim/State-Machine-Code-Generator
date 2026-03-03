public class Timeout extends Event{
    private int time;

    private Timer_Var timer;


    public Timeout(int time) {
        super("");
        this.time = time;
    }

    public void logTimer(String whitespaces){
        System.out.println(whitespaces + "declareTimer(" + timer.getId() + "," + time + "," + timer.getTimer_name() + ");");
        System.out.println(whitespaces + "startTimer(" + timer.getId() + ",tick);");
    }

    public Timer_Var getTimer() {
        return timer;
    }

    public void setTimer(Timer_Var timer) {
        setName(timer.getTimer_name());
        this.timer = timer;
    }

    @Override
    public boolean isTimeout() {
        return true;
    }

}
