package frc.utils;

import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.littletonrobotics.junction.Logger;

public class GBSubsystem extends SubsystemBase {
    @Override
    public void periodic() {
        super.periodic();
        if (getCurrentCommand() != null) {
            Logger.recordOutput("subsystems/" + getClass().getName(), getCurrentCommand().getName());
        }else{
            Logger.recordOutput("subsystems/" + getClass().getName(), "no command is currently running on the subsystem");
        }
    }
}
