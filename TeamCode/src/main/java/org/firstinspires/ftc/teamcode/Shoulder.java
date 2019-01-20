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

    private int verticalEncoderCount;
    private int curPos;
    private boolean hasSetVertical = false;
    private int nullZoneRadius = 0;

    private boolean prevX = false;

    private boolean canAdjustPower = false;
    private boolean prevA = false;
    private boolean prevB = false;

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

        double targetVel = -gamepad.left_stick_y;
        double errorVel = targetVel - prevCommandVel;
        double commandVel = prevCommandVel + Range.clip(errorVel, -(elapsed / accelTime), elapsed / accelTime);

        prevCommandVel = commandVel;

        double sign = Math.copySign(1.0, commandVel);
        commandVel = sign * Math.pow(Math.abs(commandVel), exponent);

        if (canAdjustPower) {
            double adjustAmount = 0.01;

            if (prevA && !gamepad.a) {
                pwmControl.changePower(adjustAmount);
            } else if (prevB && !gamepad.b) {
                pwmControl.changePower(-adjustAmount);
            }

            prevA = gamepad.a;
            prevB = gamepad.b;
        }



        if (shouldUsePwm(commandVel)) {
            pwmControl.setCommandVel(commandVel, getOffset());
        } else {
            pwmControl.setCommandVel(NaN, getOffset());
            shoulderMotor.setPower(commandVel);
        }

        opmode.telemetry.addData("Shoulder", "power %f, commandVel %f, offset %d", pwmControl.getPower(), commandVel, getOffset());
        opmode.telemetry.update();
    }

/*    private void busySleep(double millis) {
        double start = runtime.milliseconds();
        while (runtime.milliseconds() - start < millis) {}
    }*/

    public void killThread() {
        pwmControl.end();
    }

    public boolean shouldUsePwm (double cmdVel) {
        if (prevX && !gamepad.x) {
            verticalEncoderCount = curPos;
            hasSetVertical = true;
        }

        prevX = gamepad.x;

        if (hasSetVertical) {
            if (Math.abs(getOffset()) < nullZoneRadius) {
                return false;
            } else {
                return (getOffset()) * cmdVel > 0;
            }
        } else {
            return gamepad.left_bumper;
        }
    }

    public void setPowerAdjustments(boolean adjust) {
        canAdjustPower = adjust;
    }

    public int getCurPos() {
        return curPos;
    }

    public int getOffset () {
        return curPos - verticalEncoderCount;
    }
}