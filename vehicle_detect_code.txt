#define S_Default 0
#define S_VehicleDetect 1
#define S_Input1 1
#define S_Input2 2

#define TIMEOUT_0 EVENT1

static int state = S_Default;
static int state_VehicleDetect = S_Default

static void VehicleDetect_stmDoStep(uint32_t tick) {
    switch(state) {
    case S_Default:
        state = S_VehicleDetect;
        doStep_VehicleDetect(tick);
        break;
    case S_VehicleDetect:
        doStep_VehicleDetect(tick);
        break;
    }
}

static void doStep_VehicleDetect(uint32_t tick) {
    switch(state_VehicleDetect) {
     case S_Default:
       state_VehicleDetect = S_Input1;
       declareTimer(0,20,TIMEOUT_0);
       startTimer(0,tick);
       break;
     case S_Input1:
      if (eventIsSet(TIMEOUT_0)) {
       port_write_VehicleDetect_hasVehicle(1);
       state_VehicleDetect = S_Input2;
       declareTimer(0,5,TIMEOUT_0);
       startTimer(0,tick);
      }
      break;
     case S_Input2:
      if (eventIsSet(TIMEOUT_0)) {
       port_write_VehicleDetect_hasVehicle(0);
       state_VehicleDetect = S_Input1;
       declareTimer(0,20,TIMEOUT_0);
       startTimer(0,tick);
      }
      break;
     }
}