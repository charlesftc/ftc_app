package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;

import static java.lang.Double.NaN;

public class Slide {
    private enum ControlMode {
        PWR_CONTROL, POS_CONTROL;
    }

    private Teleop1 opmode;
    private Gamepad gamepad;
    private DcMotor slideMotor;
    private ControlMode controlMode = ControlMode.POS_CONTROL;

    private int origPos;
    private int ticksPerLength = 800;
    private double maxPosPower = 0.8;

    private double goal = NaN;

    private double storedExt;
    private boolean prevA = false;
    private boolean prevB = false;
    ///private RPSCalculator rpsCalc;

    public Slide(Teleop1 opmode, Gamepad gamepad) {
        this.opmode = opmode;
        this.gamepad = gamepad;

        slideMotor = opmode.hardwareMap.get(DcMotor.class, "slide_motor");
        slideMotor.setDirection(DcMotor.Direction.FORWARD);
        slideMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        origPos = slideMotor.getCurrentPosition();
        //slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //rpsCalc = new RPSCalculator(shoulderMotor, ticksPerRev, 0.2);
        //rpsCalc.start();
    }

    public void control(float power) {
        if (Math.abs(power) > 0.01) {
            goal = NaN;
        }

        if (!Double.isNaN(goal)) {
            positionControl(goal);
        } else {
            triggerControl(power);
        }

        if (prevA && !gamepad.a) {
            storedExt = getExtension();
        } else if (prevB && !gamepad.b) {
            goal = storedExt;
        }

        prevA = gamepad.a;
        prevB = gamepad.b;
    }

    public void triggerControl(float power) {
        if (Math.abs(power) < 0.01) {
            if (controlMode != ControlMode.POS_CONTROL) {
                setGoal(getExtension());
                positionControl(goal);
            }
        } else {
            if (controlMode != ControlMode.PWR_CONTROL) {
                controlMode = ControlMode.PWR_CONTROL;
                slideMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }

            slideMotor.setPower(power);
        }

        /*opmode.telemetry.addData("Slide", "curPos %d, orig pos %d, diff %d", (int) (getExtension() * ticksPerLength), origPos, (int) (getExtension() * ticksPerLength) - origPos);
        opmode.telemetry.update();*/
    }

    private void positionControl(double extension) {
        controlMode = ControlMode.POS_CONTROL;
        slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slideMotor.setPower(maxPosPower);
        int pos = (int) (extension * ticksPerLength);
        slideMotor.setTargetPosition(pos);
    }

    public double getExtension() {
        return (double) slideMotor.getCurrentPosition() / ticksPerLength;
    }

    public void setGoal(double extension) {
        goal = extension;
    }
}
