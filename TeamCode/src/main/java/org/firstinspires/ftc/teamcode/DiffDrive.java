package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

public class DiffDrive {
    //private ElapsedTime runtime = new ElapsedTime();
    private Teleop1 opmode;
    private Gamepad gamepad;
    private DcMotor leftDrive;
    private DcMotor rightDrive;
    private RPSCalculator rpsCalcL = new RPSCalculator(leftDrive, 2240, 0.05);
    private RPSCalculator rpsCalcR = new RPSCalculator(rightDrive, 2240, 0.05);
    private double maxSpeed = 0.8;
    private double turnSpeed = 0.6;
    private double exponent = 3;
    //private double accelRate = 0.4;

    private boolean canAdjustSpeeds = false;
    private boolean prevX = false;
    private boolean prevY = false;
    private boolean prevA = false;
    private boolean prevB = false;

    public DiffDrive(Teleop1 opmode, Gamepad gamepad) {
        this.opmode = opmode;
        this.gamepad = gamepad;

        leftDrive  = opmode.hardwareMap.get(DcMotor.class, "left_drive");
        rightDrive = opmode.hardwareMap.get(DcMotor.class, "right_drive");

        leftDrive.setDirection(DcMotor.Direction.FORWARD);
        rightDrive.setDirection(DcMotor.Direction.REVERSE);
        //leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //rightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public void execute() {
        double targetVelL;
        double targetVelR;
        double drive = gamepad.left_stick_y;
        double turn = gamepad.right_stick_x;

        double driveSign = Math.copySign(1.0, drive);
        drive = driveSign * Math.pow(Math.abs(drive), exponent) * maxSpeed;

        turn = turn * turnSpeed;

        drive = drive * (maxSpeed - Math.abs(turn));

        targetVelL = drive + turn;
        targetVelR = drive - turn;

        /*if (rpsCalcL ) {
            double currentVelL = rpsCalcL.getRPS() * (targetVelL / rpsCalcL.getRPS());
            double currentVelR = rpsCalcR.getRPS() * (targetVelR / rpsCalcR.getRPS());
        }

        double errorVelL = targetVelL - currentVelL;
        double errorVelR = targetVelR - currentVelR;

        double commandVelL = currentVelL + Range.clip(errorVelL, -accelRate, accelRate);
        double commandVelR = currentVelR + Range.clip(errorVelR, -accelRate, accelRate);*/

        leftDrive.setPower(targetVelL);
        rightDrive.setPower(targetVelR);

        if (canAdjustSpeeds) {
            double adjustAmount = 0.05;
            if (prevX && !gamepad.x) {
                maxSpeed += adjustAmount;
                //accelRate += 0.1;
            } else if (prevY && !gamepad.y) {
                maxSpeed -= adjustAmount;
                //accelRate -= 0.1;
            }

            if (prevA && !gamepad.a) {
                turnSpeed += adjustAmount;
            } else if (prevB && !gamepad.b) {
                turnSpeed -= adjustAmount;
            }

            prevX = gamepad.x;
            prevY = gamepad.y;
            prevA = gamepad.a;
            prevB = gamepad.b;

            //opmode.telemetry.addData("Adjusting", "maxSpeed %.2f, turnSpeed %.2f", maxSpeed, turnSpeed);
            //opmode.telemetry.addData("Temp", "targetVelL %f, currentVelL %f", targetVelL, currentVelL);
            //opmode.telemetry.update();
        }

        //opmode.telemetry.addData("Status", "Run Time: " + runtime.toString());
        //opmode.telemetry.addData("Motors", "left %.2f, right %.2f", leftPower, rightPower);
        //opmode.telemetry.update();
    }

    public void setSpeedAdjustments(boolean b) {
        canAdjustSpeeds = b;
    }
}
