package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;

public class DiffDrive {
    //private ElapsedTime runtime = new ElapsedTime();
    private Teleop1 opmode;
    private Gamepad gamepad;
    private DcMotor leftDrive;
    private DcMotor rightDrive;
    //private RPSCalculator rpsCalcL = new RPSCalculator(leftDrive, 2240, 0.05);
    //private RPSCalculator rpsCalcR = new RPSCalculator(rightDrive, 2240, 0.05);
    private double maxSpeeds[] = {0.5, 0.8, 1.0};
    private double turnSpeeds[] = {0.3, 0.5, 0.7};

    private double exponent = 1.5;
    //private double accelRate = 0.4;

    private boolean canAdjustSpeeds = false;
    private boolean prevUp = false;
    private boolean prevDown = false;
    private boolean prevRight = false;
    private boolean prevLeft = false;

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

    public void control(double leftY, double rightX) {
        int index = getSpeedIndex();

        double targetVelL;
        double targetVelR;
        double drive = leftY;
        double turn = rightX;

        double driveSign = Math.copySign(1.0, drive);
        drive = driveSign * Math.pow(Math.abs(drive), exponent);

        drive = drive * maxSpeeds[index];

        turn = turn * turnSpeeds[index];

        drive = drive * (maxSpeeds[index] - Math.abs(turn));

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
            if (prevUp && !gamepad.dpad_up) {
                maxSpeeds[index] += adjustAmount;
                //accelRate += 0.1;
            } else if (prevDown && !gamepad.dpad_down) {
                maxSpeeds[index] -= adjustAmount;
                //accelRate -= 0.1;
            }

            if (prevRight && !gamepad.dpad_right) {
                turnSpeeds[index] += adjustAmount;
            } else if (prevLeft && !gamepad.dpad_left) {
                turnSpeeds[index] -= adjustAmount;
            }

            prevUp = gamepad.dpad_up;
            prevDown = gamepad.dpad_down;
            prevRight = gamepad.dpad_right;
            prevLeft = gamepad.dpad_left;

            //opmode.telemetry.addData("Adjusting", "maxSpeed %.2f, turnSpeed %.2f", maxSpeeds[index], turnSpeeds[index]);
            //opmode.telemetry.addData("Velocity", "drive %.2f, turn %.2f", drive, turn);
            //opmode.telemetry.addData("Temp", "targetVelL %f, currentVelL %f", targetVelL, currentVelL);
            //opmode.telemetry.update();
        }

        //opmode.telemetry.addData("Status", "Run Time: " + runtime.toString());
        //opmode.telemetry.addData("Motors", "left %.2f, right %.2f", leftPower, rightPower);
        //opmode.telemetry.update();
    }

    private int getSpeedIndex() {
        if (gamepad.left_trigger > 0.5) {
            return 0;
        } else if (gamepad.right_trigger > 0.5) {
            return 2;
        } else {
            return 1;
        }
    }

    public void setSpeedAdjustments(boolean b) {
        canAdjustSpeeds = b;
    }
}
