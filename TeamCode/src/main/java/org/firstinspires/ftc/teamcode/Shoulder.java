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
    private int offset = 0;
    private boolean hasSetVertical = false;
    private int nullZoneRadius = 300;

    private boolean prevX = false;

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

        double targetVel = -gamepad.left_stick_y;
        double errorVel = targetVel - prevCommandVel;
        double commandVel = prevCommandVel + Range.clip(errorVel, -(elapsed / accelTime), elapsed / accelTime);

        prevCommandVel = commandVel;

        double sign = Math.copySign(1.0, commandVel);
        commandVel = sign * Math.pow(Math.abs(commandVel), exponent);

        if (shouldUsePwm(commandVel)) {
            pwmControl.setCommandVel(commandVel, offset);
        } else {
            pwmControl.setCommandVel(NaN, offset);
            shoulderMotor.setPower(commandVel);
        }
    }

/*    private void busySleep(double millis) {
        double start = runtime.milliseconds();
        while (runtime.milliseconds() - start < millis) {}
    }*/

    public void killThread() {
        pwmControl.end();
    }

    public boolean shouldUsePwm (double cmdVel) {
        int curPos = shoulderMotor.getCurrentPosition();

        if (prevX && !gamepad.x) {
            verticalEncoderCount = curPos;
            hasSetVertical = true;
        }

        prevX = gamepad.x;

        offset = curPos - verticalEncoderCount;

        if (hasSetVertical) {
            if (Math.abs(offset) < nullZoneRadius) {
                return false;
            } else if (offset * cmdVel > 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return gamepad.left_bumper;
        }
    }
}