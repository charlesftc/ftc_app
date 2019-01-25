package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;

import static java.lang.Double.NaN;

public class Slide {
    private enum ControlMode {
        PWR_CONTROL, POS_CONTROL;
    }

    private LinearOpMode opmode;
    private Gamepad gamepad;
    private DcMotor slideMotor;
    private ControlMode controlMode = ControlMode.POS_CONTROL;

    private int origPos;
    private int ticksPerLength = 800;
    private double maxPosPower = 0.8;

    private double goal = NaN;
    private boolean busy = false;

/*    private double storedExt;
    private boolean prevX = false;
    private boolean prevY = false;*/
    ///private RPSCalculator rpsCalc;

    public Slide(LinearOpMode opmode, Gamepad gamepad) {
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

        /*if (prevX && !gamepad.x) {
            storedExt = getExtension();
        } else if (prevY && !gamepad.y) {
            goal = storedExt;
        }

        prevX = gamepad.x;
        prevY = gamepad.y;*/
    }

    public void triggerControl(float power) {
        if (Math.abs(power) < 0.01) {
            if (controlMode != ControlMode.POS_CONTROL) {
                goal = getExtension();
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
        int pos = (int) (-extension * ticksPerLength);
        slideMotor.setTargetPosition(pos);

        if (Math.abs(getExtension() - extension) < 0.03) {
            busy = false;
        }
    }

    public double getExtension() {
        return -(double) slideMotor.getCurrentPosition() / ticksPerLength;
    }

    public void setGoal(double extension) {
        goal = extension;
        busy = !Double.isNaN(extension);
    }

    public boolean isBusy() {
        return busy;
    }
}
