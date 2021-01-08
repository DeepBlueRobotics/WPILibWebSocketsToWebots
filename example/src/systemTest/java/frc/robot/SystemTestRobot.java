package frc.robot;

import edu.wpi.first.wpilibj.RobotBase;

import java.util.concurrent.CompletableFuture;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.hal.HALValue;
import edu.wpi.first.hal.SimDevice;
import edu.wpi.first.hal.SimValue;
import edu.wpi.first.hal.simulation.SimValueCallback;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.simulation.SimDeviceSim;
import edu.wpi.first.wpilibj.simulation.SimHooks;

public class SystemTestRobot extends Robot {

    public static void main(String... args) {
        RobotBase.startRobot(SystemTestRobot::new);
    }

    @Override
    public void startCompetition() {
        // Ensure that uncaught exceptions result in a non-zero exit status
        // so that failures can be detected by the caller (e.g. gradlew systemTest)
        try {
            super.startCompetition();
            HAL.shutdown();
            System.exit(0);
        } catch (Throwable throwable) {
            Throwable cause = throwable.getCause();
            if (cause != null) {
                throwable = cause;
            }
            DriverStation.reportError("Unhandled exception: " + throwable.toString(),
                throwable.getStackTrace());
            HAL.shutdown();
            System.exit(-1);
        }      
    }

    @Override
    public void simulationInit() {
        SimDevice webotsSupervisor = SimDevice.create("WebotsSupervisor");
        SimValue isReadySim = webotsSupervisor.createValue("ready", SimDevice.Direction.kInput, HALValue.makeBoolean(false));
        SimDeviceSim webotsSupervisorSim = new SimDeviceSim("WebotsSupervisor");
        
        // Wait for the Webots supervisor to be ready
        final var future = new CompletableFuture<Boolean>();
        try (var callback = webotsSupervisorSim.registerValueChangedCallback(isReadySim, new SimValueCallback() {
            public void callback(String name, int handle, boolean readonly, HALValue value) {
                if (value.getBoolean()) {
                    System.out.println("WebotsSupervisor is ready");
                    future.complete(true);
                }
            }
        }, true)) {
            System.out.println("Waiting for WebotsSupervisor to be ready");
            future.join();
        }
        // Reset the clock. Without this, *Periodic calls that should have 
        // occurred while we waited, will be considered behind schedule and
        // will all happen at once.
        SimHooks.restartTiming();

        // Simulate starting autonomous
        DriverStationSim.setAutonomous(true);
        DriverStationSim.setEnabled(true);
        DriverStationSim.notifyNewData();

        super.simulationInit();
    }

    private int count = 0;

    @Override
    public void simulationPeriodic() {
        super.simulationPeriodic();

        count++;
        if (count > 50*10) {
            // Simulate disabling the robot
            DriverStationSim.setEnabled(false);
            DriverStationSim.notifyNewData();

            // Call endCompetition() to end the test and report success.
            // NOTE: throwing an exception will end the test and report failure.
            endCompetition();
        }
    }
}
