package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import static java.lang.Double.NaN;

public class Shoulder {
    private enum ControlMode {
        PWR_CONTROL, PWM_CONTROL, POS_CONTROL;
    }

    private ElapsedTime runtime = new ElapsedTime();
    private double prevTime = 0;
    private LinearOpMode opmode;
    private Gamepad gamepad;
    private DcMotorEx shoulderMotor;
    private ShoulderPWMControl pwmControl;

    private ControlMode controlMode = ControlMode.POS_CONTROL;

    private double prevCommandVel = 0;
    private double accelTime = 600;
    private double exponent = 2;
    private double maxGravityAdjustment = 0.2;
    private double holdPower = 0.2;

    private double kP = 0.045;
    private double maxPosPower = 0.65;

    private double goal = NaN;
    private boolean busy = false;

/*    private double storedAngle;
    private boolean prevX = false;
    private boolean prevY = false;*/

    private int curPos;

    private int verticalEncoderCount;
    //private int ticksToVertical = -3234;
    private int ticksToVertical = -3300;
    private int startPos;

    private int ticksPerRev = 1680 * 8;
    private boolean hasSetVertical = false;
    private int nullZoneRadius = 0;

    private boolean prevLB = false;
    private boolean prevRB = false;

    /*private boolean canAdjustPower = false;
    private boolean prevA = false;
    private boolean prevB = false;*/

    public Shoulder(LinearOpMode opmode, Gamepad gamepad, boolean shouldSetVertical) {
        this.opmode = opmode;
        this.gamepad = gamepad;

        shoulderMotor = opmode.hardwareMap.get(DcMotorEx.class, "shoulder_motor");
        //shoulderMotor.setDirection(DcMotor.Direction.FORWARD);
        shoulderMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        pwmControl = new ShoulderPWMControl(shoulderMotor);
        pwmControl.start();

        updateCurPos();
        startPos = curPos;

        if (shouldSetVertical) {
            setVertical();
        }
    }

    public void control(double stickPos) {
        updateCurPos();
        handleSetVertical();

        if (Math.abs(stickPos) > 0.01) {
            goal = NaN;
        }

        if (!Double.isNaN(goal)) {
            positionControl(goal);
        } else {
            stickControl(stickPos);
        }

/*        if (prevX && !gamepad.x) {
            storedAngle = getAngle();
        } else if (prevY && !gamepad.y) {
            goal = storedAngle;
        }

        prevX = gamepad.x;
        prevY = gamepad.y;*/

        //opmode.telemetry.addData("ShoulderPosTest", "cur angle %.3f, goal angle %.3f, vert %d", getAngle(), goal, verticalEncoderCount);
        //opmode.telemetry.addData("Shoulder", "curPos %d, startPos %d, ticksToCurPos %d", curPos, startPos, curPos - startPos);
        //opmode.telemetry.update();
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

        if (hasSetVertical) {
            commandVel = adjustForGravity(commandVel);
        }

        if (shouldHoldPos(targetVel, shoulderMotor.getVelocity(AngleUnit.DEGREES) / 8)) {
            if (controlMode != ControlMode.POS_CONTROL) {
                goal = getAngle();
                positionControl(goal);
            }
        } else if (shouldUsePwm(commandVel)) {
            if (controlMode != ControlMode.PWM_CONTROL) {
                controlMode = ControlMode.PWM_CONTROL;
                shoulderMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }

            pwmControl.setCommandVel(commandVel);
        } else {
            pwmControl.setCommandVel(NaN);

            if (controlMode != ControlMode.PWR_CONTROL) {
                controlMode = ControlMode.PWR_CONTROL;
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

        double effectiveKP = kP;

        if (controlMode == ControlMode.PWM_CONTROL) {
            effectiveKP += 0.1;
        }

        double velocity = Range.clip(errorAngle * effectiveKP, -maxPosPower, maxPosPower);

        if (Math.abs(errorAngle) > 3.0) {
            stickControl(velocity);
        } else {
            holdAngle(angle);
            busy = false;
        }
    }

    private void updateCurPos() {
        curPos = shoulderMotor.getCurrentPosition();
    }

    private void holdPos(int pos) {
        controlMode = ControlMode.POS_CONTROL;
        shoulderMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        shoulderMotor.setPower(holdPower);
        shoulderMotor.setTargetPosition(pos);
    }

    private void holdAngle(double angle) {
        int pos = (int) (((angle / 360) * ticksPerRev) + verticalEncoderCount);
        holdPos(pos);
    }

    private void handleSetVertical() {
        if (prevLB && !gamepad.left_bumper) {
            verticalEncoderCount = curPos;
            hasSetVertical = true;
        }

        prevLB = gamepad.left_bumper;
    }

    public void setVertical() {
        verticalEncoderCount = startPos + ticksToVertical;
        hasSetVertical = true;
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
        return hasSetVertical && !(Math.abs(getAngle()) < nullZoneRadius) && (getAngle()) * cmdVel > 0;
    }

    private boolean shouldHoldPos(double targetVel, double currentVel) {
        return Math.abs(targetVel) < 0.01 && Math.abs(currentVel) < 5;
    }

    /*public void setPowerAdjustments(boolean adjust) {
        canAdjustPower = adjust;
    }*/

    public double getAngle() {
        return ((double) (curPos - verticalEncoderCount) / ticksPerRev) * 360;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setGoal(double angle) {
        goal = angle;
        busy = !Double.isNaN(angle);
    }
}