package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name="Teleop1", group="Linear")
public class Teleop1 extends LinearOpMode {
    private ElapsedTime runtime = new ElapsedTime();

    @Override
    public void runOpMode() {
        DiffDrive diffDrive = new DiffDrive(this, gamepad2);
        diffDrive.setSpeedAdjustments(true);
        Shoulder shoulder = new Shoulder(this, gamepad1);
        //shoulder.setPowerAdjustments(true);
        Elbow elbow = new Elbow(this, gamepad1);
        Slide slide = new Slide(this, gamepad1);
        Grabber grabber = new Grabber(this, gamepad2);

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();
        runtime.reset();

        while (opModeIsActive()) {
            diffDrive.control();
            shoulder.control(-gamepad1.left_stick_y);
            elbow.control();
            slide.control(gamepad1.right_trigger - gamepad1.left_trigger);
            grabber.control();
        }

        shoulder.killThread();
        elbow.killThread();
    }
}
