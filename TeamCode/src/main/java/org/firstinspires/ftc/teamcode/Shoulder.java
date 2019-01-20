package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import static java.lang.Double.NaN;
import static java.lang.Double.isNaN;

public class Shoulder {
    private enum CommandMode {
        PWR_CONTROL, PWM_CONTROL, POS_CONTROL;
    }

    private ElapsedTime runtime = new ElapsedTime();
    private double prevTime = 0;
    private Teleop1 opmode;
    private Gamepad gamepad;
    private DcMotorEx shoulderMotor;
    private ShoulderPWMControl pwmControl;

    private CommandMode commandMode = CommandMode.POS_CONTROL;

    private double prevCommandVel = 0;
    private double accelTime = 600;
    private double exponent = 2;
    private double maxGravityAdjustment = 0.2;
    private double holdPower = 0.2;

    private double kP = 0.045;
    private double maxPosPower = 0.8;

    private double goalAngle = NaN;

    private double storedAngle;
    private boolean prevX = false;
    private boolean prevY = false;

    private int curPos;
    //private int holdPos;
    private int verticalEncoderCount;
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

        shoulderMotor = opmode.hardwareMap.get(DcMotorEx.class, "shoulder_motor");
        //shoulderMotor.setDirection(DcMotor.Direction.FORWARD);
        shoulderMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        pwmControl = new ShoulderPWMControl(shoulderMotor, gamepad);
        pwmControl.start();
    }

    public void control(double stickPos) {
        updateCurPos();

        if (Math.abs(stickPos) > 0.01) {
            goalAngle = NaN;
        }

        if (!Double.isNaN(goalAngle)) {
            positionControl(goalAngle);
        } else {
            stickControl(stickPos);
        }

        if (prevX && !gamepad.x) {
            storedAngle = getAngle();
        } else if (prevY && !gamepad.y) {
            goalAngle = storedAngle;
        }

        prevX = gamepad.x;
        prevY = gamepad.y;

        opmode.telemetry.addData("ShoulderPosTest", "current angle %.3f, goal angle, %.3f, stored angle %.3f", getAngle(), goalAngle, storedAngle);
        opmode.telemetry.update();
    }

    public void stickControl(double targetVel) {
        double curTime = runtime.milliseconds();
        double elapsed = curTime - prevTime;
        prevTime = curTime;

        double commandVel = targetVel;

        double sign = Math.copySign(1.0, commandVel);
        commandVel = sign * Math.pow(Math.abs(commandVel), exponent);

        double errorVel = commandVel - prevCommandVel;
        commandVel = prevCommandVel + Range.clip(errorVel, -(elapsed / accelTime), elapsed / accelTime);

        prevCommandVel = commandVel;

        handleSetVertical();

        if (hasSetVertical) {
            commandVel = adjustForGravity(commandVel);
        }

        if (shouldHoldPos(targetVel, shoulderMotor.getVelocity(AngleUnit.DEGREES) / 8)) {
            if (commandMode != CommandMode.POS_CONTROL) {
                holdPos(curPos);
            }
        } else if (shouldUsePwm(commandVel)) {
            if (commandMode != CommandMode.PWM_CONTROL) {
                commandMode = CommandMode.PWM_CONTROL;
                shoulderMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }

            pwmControl.setCommandVel(commandVel, getAngle());
        } else {
            pwmControl.setCommandVel(NaN, getAngle());

            if (commandMode != CommandMode.PWR_CONTROL) {
                commandMode = CommandMode.PWR_CONTROL;
                shoulderMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }

            shoulderMotor.setPower(commandVel);
        }

        /*if (canAdjustPower) {
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

    public void positionControl(double angle) {
        double errorAngle = angle - getAngle();

        /*if (commandMode == CommandMode.PWM_CONTROL) {
            kP += 0.1;
        }*/

        double velocity = Range.clip(errorAngle * kP, -maxPosPower, maxPosPower);

        if (Math.abs(errorAngle) > 2) {
            stickControl(velocity);
        } else {
            holdAngle(angle);
        }
    }

    private void holdPos(int pos) {
        commandMode = CommandMode.POS_CONTROL;
        shoulderMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        shoulderMotor.setPower(holdPower);
        shoulderMotor.setTargetPosition(pos);
    }

    private void holdAngle(double angle) {
        int pos = (int) (((angle / 360) * ticksPerRev) + verticalEncoderCount);
        holdPos(pos);
    }

    private void handleSetVertical() {
        if (prevRB && !gamepad.right_bumper) {
            verticalEncoderCount = curPos;
            hasSetVertical = true;
        }

        prevRB = gamepad.right_bumper;
    }

    private double adjustForGravity(double commandVel) {
        double deviation = Math.sin(Math.toRadians(getAngle()));
        //double sign = Math.copySign(1.0, deviation);
        //deviation = sign * Math.pow(Math.abs(deviation), 0.8);

        return Range.clip(commandVel - (deviation * maxGravityAdjustment), -1, 1);
    }

    public void killThread() {
        pwmControl.end();
    }

    private boolean shouldUsePwm (double cmdVel) {
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

    private boolean shouldHoldPos(double targetVel, double currentVel) {
        return Math.abs(targetVel) < 0.01 && Math.abs(currentVel) < 5;
    }

    /*public void setPowerAdjustments(boolean adjust) {
        canAdjustPower = adjust;
    }*/

    public void setGoalAngle(double angle) {
        goalAngle = angle;
    }

    public void updateCurPos() {
        curPos = shoulderMotor.getCurrentPosition();
    }

    public double getAngle() {
        return ((double) (curPos - verticalEncoderCount) / ticksPerRev) * 360;
    }
}