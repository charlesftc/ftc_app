package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

/*import org.firstinspires.ftc.teamcode.Shoulder;
import org.firstinspires.ftc.teamcode.ServoElbow;
import org.firstinspires.ftc.teamcode.Slide;
import org.firstinspires.ftc.teamcode.DiffDrive;*/

@Autonomous(name="CraterAutonomous1", group="Linear")
public class CraterAutonomous1 extends LinearOpMode {
    @Override
    public void runOpMode() {
        Shoulder shoulder = new Shoulder(this, gamepad1, true);
        ServoElbow elbow = new ServoElbow(this, gamepad1);
        Slide slide = new Slide(this, gamepad1);
        Grabber grabber = new Grabber(this, gamepad1);

        ArmCoordinator armCoordinator = new ArmCoordinator(this, gamepad1, shoulder, slide, elbow);

        AutoDrive autoDrive = new AutoDrive(this, gamepad1);

        waitForStart();

        armCoordinator.setPreSamplePos();
        armCoordinator.waitTillDone(4);

        autoDrive.drive(-5, 0.25);

        armCoordinator.setSamplePos();
        armCoordinator.waitTillDone(4);

        autoDrive.drive(10, 0.2);
        autoDrive.drive(-10, 0.2);

        armCoordinator.setVerticalPos();
        armCoordinator.waitTillDone(4);

        autoDrive.drive(18, 0.25);

        autoDrive.powerDrive(24, 0.25);
    }
}
