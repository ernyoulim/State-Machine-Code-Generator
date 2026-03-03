#define S_Default 0
#define S_Steuerung 1
#define S_LC_Unblocked 1
#define S_LC_Blocking 2
#define S_LC_Blocked 3
#define S_LC_Unblocking 4
#define S_LC_Hazard 5
#define S_WaitReq 1
#define S_WaitAck 2

#define TIMEOUT_Controller_0 EVENT1
#define TIMEOUT_Controller_1 EVENT2
#define Ack EVENT3
#define Req EVENT4

static int state_Controller = S_Default;
static int state_Steuerung_region = S_Default;
static int state_Steuerung_region1 = S_Default;

static void Controller_stmDoStep(uint32_t tick) {
    switch(state_Controller) {
     case S_Default:
       state_Controller = S_Steuerung;
       doStep_Controller_Steuerung(tick);
       break;
     case S_Steuerung:
       doStep_Steuerung(tick);
       break;
     }
}

static void doStep_Steuerung(uint32_t tick) {
    doStep_Steuerung_region(tick);
    doStep_Steuerung_region1(tick);
}

static void doStep_Steuerung_region(uint32_t tick) {
    switch(state_Steuerung_region) {
     case S_Default:
       state_Steuerung_region = S_LC_Unblocked;
       declareTimer(0,1,TIMEOUT_Controller_0);
       startTimer(0,tick);
       break;
     case S_LC_Unblocked:
       if (eventIsSet(TIMEOUT_Controller_0)) {
        if (port_read_Controller_hasTrain() == 1) {
         setEvent(Req);
         state_Steuerung_region = S_LC_Blocking;
         declareTimer(0,8,TIMEOUT_Controller_0);
         startTimer(0,tick);
         } else {
          declareTimer(0,1,TIMEOUT_Controller_0);
          startTimer(0,tick);
         }
         }
       break;
     case S_LC_Blocking:
       if (eventIsSet(Ack)) {
        state_Steuerung_region = S_LC_Blocked;
        cancelTimer(0);
        declareTimer(0,1,TIMEOUT_Controller_0);
        startTimer(0,tick);
        }
       else if (eventIsSet(TIMEOUT_Controller_0)) {
        port_write_Controller_trainStop(1);
        state_Steuerung_region = S_LC_Hazard;
        }
       break;
     case S_LC_Blocked:
       if (eventIsSet(TIMEOUT_Controller_0)) {
        if (port_read_Controller_hasVehicle()==1) {
         port_write_Controller_trainStop(1);
         state_Steuerung_region = S_LC_Hazard;
         }
        else if (port_read_Controller_hasTrain()==0) {
         setEvent(Req);
         state_Steuerung_region = S_LC_Unblocking;
         declareTimer(0,8,TIMEOUT_Controller_0);
         startTimer(0,tick);
         } else {
          declareTimer(0,1,TIMEOUT_Controller_0);
          startTimer(0,tick);
         }
         }
       break;
     case S_LC_Unblocking:
       if (eventIsSet(TIMEOUT_Controller_0)) {
        port_write_Controller_isMalfunction(1);
        state_Steuerung_region = S_LC_Hazard;
        }
       else if (eventIsSet(Ack)) {
        state_Steuerung_region = S_LC_Unblocked;
        cancelTimer(0);
        declareTimer(0,1,TIMEOUT_Controller_0);
        startTimer(0,tick);
        }
       break;
     case S_LC_Hazard:
       break;
     }
}

static void doStep_Steuerung_region1(uint32_t tick) {
    switch(state_Steuerung_region1) {
     case S_Default:
       state_Steuerung_region1 = S_WaitReq;
       break;
     case S_WaitReq:
       if (eventIsSet(Req)) {
        port_write_Controller_TLReq(1);
        port_write_Controller_BarReq(1);
        state_Steuerung_region1 = S_WaitAck;
        declareTimer(1,1,TIMEOUT_Controller_1);
        startTimer(1,tick);
        }
       break;
     case S_WaitAck:
       if (eventIsSet(TIMEOUT_Controller_1)) {
        if (port_read_Controller_TLAck()==1 && port_read_Controller_BarAck()==1) {
         setEvent(Ack);
         port_write_Controller_TLReq(0);
         port_write_Controller_BarReq(0);
         state_Steuerung_region1 = S_WaitReq;
         } else {
          declareTimer(1,1,TIMEOUT_Controller_1);
          startTimer(1,tick);
         }
         }
       break;
     }
}
