package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;

import static java.lang.Double.NaN;

public class Elbow {
    private enum ControlMode {
        PWR_CONTROL, POS_CONTROL;
    }

    private Teleop1 opmode;
    private Gamepad gamepad;
    private DcMotor elbowMotor;
    private ControlMode controlMode = ControlMode.POS_CONTROL;

    //private int origPos;
    //private int ticksPerLength = 800;

    private int curPos;
    private int verticalEncoderCount;
    private int ticksPerRev = 288;

    private double holdPower = 0.1;
    private double posPower = 0.7;

    private double goal = NaN;

    private boolean hasSetVertical = false;
    private boolean prevRB = false;

    private double storedAngle;
    private boolean prevX = false;
    private boolean prevY = false;

    public Elbow(Teleop1 opmode, Gamepad gamepad) {
        this.opmode = opmode;
        this.gamepad = gamepad;

        elbowMotor = opmode.hardwareMap.get(DcMotor.class, "elbow_motor");
        elbowMotor.setDirection(DcMotor.Direction.FORWARD);
        elbowMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        //origPos = elbowMotor.getCurrentPosition();
    }

    public void control(float power) {
        curPos = elbowMotor.getCurrentPosition();
        handleSetVertical();

        if (Math.abs(power) > 0.01) {
            goal = NaN;
        }

        if (!Double.isNaN(goal)) {
            positionControl(goal, posPower);
        } else {
            stickControl(power);
        }

        if (prevX && !gamepad.x) {
            storedAngle = getAngle();
        } else if (prevY && !gamepad.y) {
            goal = storedAngle;
        }

        prevX = gamepad.x;
        prevY = gamepad.y;
    }

    private void stickControl(float power) {
        if (Math.abs(power) < 0.01) {
            if (controlMode != ControlMode.POS_CONTROL) {
                positionControl(getAngle(), holdPower);
            }
        } else {
            if (controlMode != ControlMode.PWR_CONTROL) {
                controlMode = ControlMode.PWR_CONTROL;
                elbowMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }

            elbowMotor.setPower(power);
        }

        /*opmode.telemetry.addData("Slide", "curPos %d, orig pos %d, diff %d", (int) (getPosition() * ticksPerLength), origPos, (int) (getPosition() * ticksPerLength) - origPos);
        opmode.telemetry.update();*/
    }

    private void positionControl(double angle, double power) {
        controlMode = ControlMode.POS_CONTROL;
        elbowMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        elbowMotor.setPower(power);
        int pos = (int) (((angle / 360) * ticksPerRev) + verticalEncoderCount);
        elbowMotor.setTargetPosition(pos);
    }

    private void handleSetVertical() {
        if (prevRB && !gamepad.right_bumper) {
            verticalEncoderCount = curPos;
            hasSetVertical = true;
        }

        prevRB = gamepad.right_bumper;
    }

    private double getAngle() {
        return ((double) (elbowMotor.getCurrentPosition() - verticalEncoderCount) / ticksPerRev) * 360;
    }

    public void setGoal(double angle) {
        goal = angle;
    }
}
