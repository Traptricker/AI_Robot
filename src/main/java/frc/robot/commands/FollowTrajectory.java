// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.SwerveControllerCommand;
import frc.robot.Constants.AutoConstants;
import frc.robot.Constants.DriveConstants;
import frc.robot.subsystems.SwerveDrive;

public class FollowTrajectory extends CommandBase {

  private final SwerveDrive drive;
  private final Trajectory trajectory;
  private boolean toReset;

  public FollowTrajectory(SwerveDrive drive, Trajectory trajectory, boolean toReset) {
    this.drive = drive;
    this.trajectory = trajectory;
    this.toReset = toReset;
    addRequirements(drive);
  }

  @Override
  public void initialize() {
    if (toReset) {
      drive.resetOdometry(trajectory.getInitialPose());
    }

    final ProfiledPIDController thetaController =
        new ProfiledPIDController(
            AutoConstants.kPThetaController, 0, 0, AutoConstants.kThetaControllerConstraints);
    thetaController.enableContinuousInput(-Math.PI, Math.PI);

    new SwerveControllerCommand(
        trajectory,
        drive::getPose, // Functional interface to feed supplier
        DriveConstants.kDriveKinematics,

        // Position controllers
        new PIDController(AutoConstants.kPXController, 0, 0),
        new PIDController(AutoConstants.kPYController, 0, 0),
        thetaController,
        drive::setModuleStates,
        drive).andThen(() -> drive.drive(0, 0, 0, false)).schedule(); // Stops the robot

    // Reset odometry to the starting pose of the trajectory.
    // drive.resetOdometry(trajectory.getInitialPose());
  }

  @Override
  public void execute() {}

  @Override
  public void end(boolean interrupted) {
    // Stops the robot when it is done with the trajectory
    drive.drive(0, 0, 0, false);
  }

  @Override
  public boolean isFinished() {
    // Returns false because when going to pick up cargo, it will be a while held
    return false;
  }

}

