package frc.robot.subsystems.swerve.modules;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.Timer;
import frc.robot.subsystems.swerve.SwerveState;
import frc.robot.subsystems.swerve.modules.moduleinterface.IModule;
import frc.robot.subsystems.swerve.modules.moduleinterface.ModuleFactory;
import frc.robot.subsystems.swerve.modules.moduleinterface.ModuleInputsAutoLogged;
import org.littletonrobotics.junction.Logger;

public class Module {

    private final ModuleInputsAutoLogged moduleInputs;
    private final ModuleUtils.ModuleName moduleName;
    private final IModule module;

    private boolean driveMotorClosedLoop;
    private SwerveModuleState targetState;

    public Module(ModuleUtils.ModuleName moduleName) {
        this.moduleName = moduleName;
        this.module = ModuleFactory.createModule(moduleName);
        this.moduleInputs = new ModuleInputsAutoLogged();
        this.targetState = new SwerveModuleState();
        this.driveMotorClosedLoop = SwerveState.DEFAULT_DRIVE.getLoopMode().isClosedLoop;

        resetByEncoder();
    }

    public void logStatus() {
        updateInputs();
        reportAlerts();
    }

    private void updateInputs() {
        module.updateInputs(moduleInputs);
        moduleInputs.driveMotorDistanceMeters = getDriveDistanceMeters();
        moduleInputs.driveMotorVelocityMeters = getDriveVelocityMetersPerSecond();
        moduleInputs.isAtTargetState = isAtTargetState();
        Logger.processInputs(ModuleUtils.getLoggingPath(moduleName), moduleInputs);
    }

    private void reportAlerts() {
        if (!moduleInputs.allComponentsConnected) {
            Logger.recordOutput(ModuleUtils.getAlertLoggingPath(moduleName) + "componentDisconnectedAt", Timer.getFPGATimestamp());
        }
    }


    public void setDriveMotorClosedLoop(boolean closedLoop) {
        driveMotorClosedLoop = closedLoop;
    }

    public void stop() {
        module.stop();
    }

    public void setBrake(boolean isBrake) {
        module.setBrake(isBrake);
    }

    public void resetByEncoder() {
        module.resetByEncoder();
    }


    /**
     * The odometry thread can update itself faster than the main code loop (which is 50 hertz).
     * Instead of using the latest odometry update, the accumulated odometry positions since the last loop to get a more
     * accurate position.
     *
     * @param odometryUpdateIndex the index of the odometry update
     * @return the position of the module at the given odometry update index
     */
    public SwerveModulePosition getOdometryPosition(int odometryUpdateIndex) {
        return new SwerveModulePosition(
                ModuleUtils.toDriveMeters(moduleInputs.odometryUpdatesDriveDistance[odometryUpdateIndex]),
                moduleInputs.odometryUpdatesSteerAngle[odometryUpdateIndex]
        );
    }

    public SwerveModuleState getTargetState() {
        return targetState;
    }

    public SwerveModuleState getCurrentState() {
        return new SwerveModuleState(getDriveVelocityMetersPerSecond(), getCurrentAngle());
    }

    public double getDriveDistanceMeters() {
        return ModuleUtils.toDriveMeters(getDriveDistanceAngle());
    }

    public Rotation2d getDriveDistanceAngle() {
        return moduleInputs.driveMotorAngleWithoutCoupling;
    }

    private double getDriveVelocityMetersPerSecond() {
        return ModuleUtils.toDriveMeters(moduleInputs.driveMotorVelocityWithoutCoupling);
    }

    private Rotation2d getCurrentAngle() {
        return moduleInputs.steerMotorAngle;
    }



    public boolean isAtTargetState() {
        return isAtAngle(getTargetState().angle) && isAtVelocity(getTargetState().speedMetersPerSecond);
    }

    public boolean isAtVelocity(double targetSpeedMetersPerSecond) {
        return MathUtil.isNear(
                targetSpeedMetersPerSecond,
                getDriveVelocityMetersPerSecond(),
                ModuleConstants.SPEED_TOLERANCE_METERS_PER_SECOND
        );
    }

    public boolean isAtAngle(Rotation2d targetAngle) {
        boolean isStopping = moduleInputs.steerMotorVelocity.getRadians() <= ModuleConstants.ANGLE_VELOCITY_DEADBAND.getRadians();
        if (!isStopping){
            return false;
        }
        boolean isAtAngle = MathUtil.isNear(
                MathUtil.angleModulus(targetAngle.getRadians()),
                MathUtil.angleModulus(getCurrentAngle().getRadians()),
                ModuleConstants.ANGLE_TOLERANCE.getRadians()
        );
        return isAtAngle;
    }


    public void pointToAngle(Rotation2d angle, boolean optimize) {
        SwerveModuleState moduleState = new SwerveModuleState(0, angle);
        if (optimize) {
            this.targetState = SwerveModuleState.optimize(moduleState, getCurrentAngle());
        }
        else {
            this.targetState = moduleState;
        }
        module.setTargetAngle(targetState.angle);
    }

    public void runDriveMotorByVoltage(double voltage) {
        module.runDriveMotorByVoltage(voltage);
    }

    public void runSteerMotorByVoltage(double voltage) {
        module.runSteerMotorByVoltage(voltage);
    }

    public void setTargetState(SwerveModuleState targetState) {
        this.targetState = SwerveModuleState.optimize(targetState, getCurrentAngle());
        module.setTargetAngle(this.targetState.angle);
        setTargetVelocity(this.targetState.speedMetersPerSecond, this.targetState.angle);
    }

    private void setTargetVelocity(double targetVelocityMetersPerSecond, Rotation2d targetSteerAngle) {
        targetVelocityMetersPerSecond = ModuleUtils.reduceSkew(targetVelocityMetersPerSecond, targetSteerAngle, getCurrentAngle());

        if (driveMotorClosedLoop) {
            setTargetClosedLoopVelocity(targetVelocityMetersPerSecond);
        }
        else {
            setTargetOpenLoopVelocity(targetVelocityMetersPerSecond);
        }
    }

    public void setTargetClosedLoopVelocity(double targetVelocityMetersPerSecond) {
        module.setTargetClosedLoopVelocity(targetVelocityMetersPerSecond);
    }

    public void setTargetOpenLoopVelocity(double targetVelocityMetersPerSecond) {
        module.setTargetOpenLoopVelocity(targetVelocityMetersPerSecond);
    }

}