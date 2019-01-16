package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

public class DiffDrive {

    // Declare OpMode members.
    //private ElapsedTime runtime = new ElapsedTime();
    private Teleop1 opmode;
    private Gamepad gamepad;
    private DcMotor leftDrive;
    private DcMotor rightDrive;

    public DiffDrive(Teleop1 opmode, Gamepad gamepad) {
        this.opmode = opmode;
        this.gamepad = gamepad;

        leftDrive  = opmode.hardwareMap.get(DcMotor.class, "left_drive");
        rightDrive = opmode.hardwareMap.get(DcMotor.class, "right_drive");

        leftDrive.setDirection(DcMotor.Direction.REVERSE);
        rightDrive.setDirection(DcMotor.Direction.FORWARD);
        leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public void execute() {
        double leftPower;
        double rightPower;

        double turn = gamepad.right_stick_x * 0.35;

        double drive = -gamepad.left_stick_y * 0.8;
        double driveSign = Math.copySign(1.0, drive);
        drive = driveSign * Math.pow(Math.abs(drive), 2);
        drive = drive * (1 - Math.abs(turn));

        leftPower = 0.65 * Range.clip(drive + turn, -1.0, 1.0);
        rightPower = 0.65 * Range.clip(drive - turn, -1.0, 1.0);

        leftDrive.setPower(leftPower);
        rightDrive.setPower(rightPower);

        //opmode.telemetry.addData("Status", "Run Time: " + runtime.toString());
        //opmode.telemetry.addData("Motors", "left (%.2f), right (%.2f)", leftPower, rightPower);
        //opmode.telemetry.update();
    }
}
