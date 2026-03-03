public class ReceivedEvent extends Event{

    public ReceivedEvent(String name) {
        super(name);
    }

    @Override
    public void logTimer(String whitespaces) {

    }

    @Override
    public boolean isTimeout() {
        return false;
    }

}
