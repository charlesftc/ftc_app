package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.ArrayList;

@TeleOp(name="Teleop1", group="Linear")
public class Teleop1 extends LinearOpMode {
    private ElapsedTime runtime = new ElapsedTime();

    private boolean prevX = false;
    private boolean prevY = false;

    @Override
    public void runOpMode() {
        DiffDrive diffDrive = new DiffDrive(this, gamepad2);
        diffDrive.setSpeedAdjustments( true);
        Shoulder shoulder = new Shoulder(this, gamepad1);
        //shoulder.setPowerAdjustments(true);
        ServoElbow elbow = new ServoElbow(this, gamepad1);
        Slide slide = new Slide(this, gamepad1);
        Grabber grabber = new Grabber(this, gamepad2);

        ArmCoordinator armCoordinator = new ArmCoordinator(this, gamepad1, shoulder, slide, elbow);

        AutoDrive autoDrive = new AutoDrive(this, gamepad2);

        //telemetry.addData("Status", "Initialized");
        //telemetry.update();

        waitForStart();
        runtime.reset();

        while (opModeIsActive()) {
            armCoordinator.update();
            diffDrive.control(gamepad2.left_stick_y, gamepad2.right_stick_x);
            shoulder.control(-gamepad1.left_stick_y);
            elbow.control(-gamepad1.right_stick_y);
            slide.control(gamepad1.right_trigger - gamepad1.left_trigger);
            grabber.control();

            if (prevX && !gamepad2.x) {
                autoDrive.drive(36);
            } else if (prevY && !gamepad2.y) {
                autoDrive.turn(15);
            }

            prevX = gamepad2.x;
            prevY = gamepad2.y;

            telemetry.update();
        }

        shoulder.killThread();
        elbow.killThread();
    }
}
