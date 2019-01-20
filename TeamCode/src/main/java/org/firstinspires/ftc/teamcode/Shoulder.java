package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import static java.lang.Double.NaN;

public class Shoulder {
    private ElapsedTime runtime = new ElapsedTime();
    private double prevTime = 0;
    private Teleop1 opmode;
    private Gamepad gamepad;
    private DcMotor shoulderMotor;
    private ShoulderPWMControl pwmControl;

    private double prevCommandVel = 0;
    private double accelTime = 600;
    private double exponent = 2;
    private double maxGravityAdjustment = 0.2;

    private int verticalEncoderCount;
    private int curPos;
    private int ticksPerRev = 1680 * 8;
    private boolean hasSetVertical = false;
    private int nullZoneRadius = 0;

    private boolean prevRB = false;

    /*private boolean canAdjustPower = false;
    private boolean prevA = false;
    private boolean prevB = false;*/

    public Shoulder(Teleop1 opmode, Gamepad gamepad) {
        this.opmode = opmode;
        this.gamepad = gamepad;

        shoulderMotor = opmode.hardwareMap.get(DcMotor.class, "shoulder_motor");
        //shoulderMotor.setDirection(DcMotor.Direction.FORWARD);
        shoulderMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        pwmControl = new ShoulderPWMControl(shoulderMotor, gamepad);
        pwmControl.start();
    }

    public void execute() {
        double curTime = runtime.milliseconds();
        double elapsed = curTime - prevTime;
        prevTime = curTime;

        curPos = shoulderMotor.getCurrentPosition();

        double commandVel = -gamepad.left_stick_y;

        double sign = Math.copySign(1.0, commandVel);
        commandVel = sign * Math.pow(Math.abs(commandVel), exponent);

        double errorVel = commandVel - prevCommandVel;
        commandVel = prevCommandVel + Range.clip(errorVel, -(elapsed / accelTime), elapsed / accelTime);

        prevCommandVel = commandVel;

        if (hasSetVertical) {
            commandVel = adjustForGravity(commandVel);
        }

        if (shouldUsePwm(commandVel)) {
            pwmControl.setCommandVel(commandVel, getAngle());
        } else {
            pwmControl.setCommandVel(NaN, getAngle());
            shoulderMotor.setPower(commandVel);
        }

/*        if (canAdjustPower) {
            double adjustAmount = 0.01;

            if (prevA && !gamepad.a) {
                pwmControl.changePower(adjustAmount);
            } else if (prevB && !gamepad.b) {
                pwmControl.changePower(-adjustAmount);
            }

            prevA = gamepad.a;
            prevB = gamepad.b;
        }*/

        //opmode.telemetry.addData("Shoulder", "power %f, commandVel %f, angle %f", pwmControl.getPower(), commandVel, getAngle());
        //opmode.telemetry.update();
    }

    private double adjustForGravity(double commandVel) {
        double deviation = Math.sin(getAngle());
        double sign = Math.copySign(1.0, deviation);
        deviation = sign * Math.pow(Math.abs(deviation), 0.8);

        return Range.clip(commandVel - (deviation * maxGravityAdjustment), -1, 1);
    }

    public void killThread() {
        pwmControl.end();
    }

    public boolean shouldUsePwm (double cmdVel) {
        if (prevRB && !gamepad.right_bumper) {
            verticalEncoderCount = curPos;
            hasSetVertical = true;
        }

        prevRB = gamepad.right_bumper;

        if (hasSetVertical) {
            if (Math.abs(getAngle()) < nullZoneRadius) {
                return false;
            } else {
                return (getAngle()) * cmdVel > 0;
            }
        } else {
            return gamepad.left_bumper;
        }
    }

    /*public void setPowerAdjustments(boolean adjust) {
        canAdjustPower = adjust;
    }*/

    public int getCurPos() {
        return curPos;
    }

    public double getAngle() {
        return ((double) (curPos - verticalEncoderCount) / ticksPerRev) * (2 * Math.PI);
    }
}